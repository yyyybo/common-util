package com.yibo.common.bolt;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;

/**
 * Bolt的概念来源于Storm， 一个Bolt类似一个插槽，在整个执行过程中，代表一段业务处理逻辑。
 * Bolt执行时通过递归来发现其依赖的Bolt，当其依赖的Bolt完成时，才会真正执行 {@link Bolt#evaluate()}
 * 业务代码需要实现{@link Bolt#evaluate()}并提供异步返回结果。
 * <p>
 * {@link Bolt#sinkBolts} 表示当前Bolt执行完后，发送通知即可。不需要等待{@link Bolt#sinkBolts}执行结果。
 * 比如用来发送非可靠的消息通知
 * <p>
 * {@link Bolt#emit()} 方法的调用可能阻塞，并且直到{@link Bolt#evaluate()}返回的Future就绪或者失败，才会继续执行
 * <p>
 * 实现 {@link Bolt#evaluate()} 时，可以通过 {@link Bolt#getDep(Enum)} 来获取依赖值。这个方法会抛业务出异常，跟
 * 同步调用类似。需要自行处理。在{@link Bolt#evaluate()} 中调用{@link Bolt#getDep(Enum)} 不会阻塞。
 * <p>
 * <p>
 * 如果 {@link Bolt#evaluate()} 是一个耗时的操作，可以单独设置 {@link Bolt#executor} 一个线程池
 * .
 */
public abstract class Bolt<O> {
    public static final Bolt NULL_NODE = Bolt.value(null);
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean evaluated = new AtomicBoolean();
    private final SettableFuture<O> future = SettableFuture.create();
    BoltGraph graph = null;
    private String name;
    private List<Bolt> sinkBolts;
    /**
     * 依赖的Bolt
     */
    private ImmutableMap<Enum, Bolt> dependencies;
    /**
     * 一些失败可以继续执行的依赖Key
     */
    private ImmutableSet<Enum> optionals;
    private Executor executor;
    private long startTimeMs;
    private long evaluateStartTimeMs;
    private long evaluateStopTimeMs;
    private long stopTimeMs;

    protected Bolt() {
        this.name = getClass().getSimpleName();
        this.sinkBolts = ImmutableList.of();
        this.dependencies = ImmutableMap.of();
        this.optionals = ImmutableSet.of();
        this.executor = MoreExecutors.sameThreadExecutor();
    }

    public static <T> Bolt<T> build(Class<? extends Bolt<T>> boltClass, Pair<Enum, Bolt>... dependencies) {
        Builder<T> builder = new Builder(boltClass);
        if (dependencies.length > 0) {
            builder.withDependencies(false, dependencies);
        }
        return builder.build();
    }

    public static <T> Bolt<T> value(T value) {
        return value(value, null);
    }

    public static <T> Bolt<T> value(T value, String name) {

        return value(() -> value, name);
    }

    public static <T> Bolt<T> value(Supplier<T> supplier) {
        return value(supplier, null);
    }

    public static <T> Bolt<T> value(Supplier<T> supplier, String name) {

        ValueBolt<T> valueBolt = new ValueBolt<>(supplier);
        if (StringUtils.isNotEmpty(name)) {
            ((Bolt) valueBolt).name = name;
        }
        return valueBolt;
    }

    public static <T> Bolt<T> noValue() {
        return NULL_NODE;
    }

    public static <T> Bolt<T> ifThenElse(Bolt<Boolean> predicate, Bolt<T> trueBolt, Bolt<T> faseBolt) {
        return new PredicateSwitchBolt<>(predicate, trueBolt, faseBolt);
    }

    public static <T> Bolt<T> ifThen(Bolt<Boolean> predicate, Bolt<T> trueBolt) {
        return new PredicateSwitchBolt<>(predicate, trueBolt, NULL_NODE);
    }

    public static <F, T> Bolt<T> transform(Bolt<F> from, Function<F, T> function) {
        return new FunctionBolt(from, function);
    }

    public String getName() {
        return name;
    }

    public String getHumanReadableName() {
        return getName();
    }

    public final ListenableFuture<O> execute() {
        if (!evaluated.compareAndSet(false, true)) {
            return future;
        }

        Pair<ListenableFuture, ListenableFuture> dependencies = futureFromDependencies();
        final ListenableFuture required = dependencies.getLeft();
        // optional的不要影响主流程
        final ListenableFuture optional = dependencies.getRight();

        startTimeMs = System.currentTimeMillis();

        try {
            final Class<? extends Bolt> clazz = this.getClass();
            Futures.addCallback(required, new FutureCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (graph != null && graph.isCompleted()) {
                        // 流程结束，由于依赖关系，可以保证后续节点不会继续执行，设置null只是触发下完成状态。
                        future.set(null);
                        aborted();
                        return;
                    }

                    evaluateStartTimeMs = System.currentTimeMillis();
                    try {
                        preEvaluate();

                        final ListenableFuture<O> val = evaluate();

                        if (val == null) {
                            throw new NullPointerException(
                                    String.format("Bolt %s evaluate() returned null Future object!", getName()));
                        }

                        evaluateStopTimeMs = System.currentTimeMillis();
                        postEvaluate(val);


                        Futures.addCallback(val, new FutureCallback<O>() {
                            @Override
                            public void onSuccess(O result) {
                                ensueComplete(result, null, true);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                ensueComplete(null, t, true);
                            }
                        });
                        if (log.isDebugEnabled()) {
                            log.debug("{} evaluate cost {} ms", getName(), evaluateStopTimeMs - evaluateStartTimeMs);
                        }
                        // valuebolt的名字跟hashcode有关，记录监控的话指标太多
                        if (ServiceBolt.class.isAssignableFrom(clazz)) {
                            // 监控记录 name 和时间
                            log.info("bolt{} 消耗时间{}",getName(), evaluateStopTimeMs - evaluateStartTimeMs);
                        }
                    } catch (Throwable e) {
                        evaluateStopTimeMs = System.currentTimeMillis();
                        ensueComplete(null, e, false);
                    }
                }

                /**
                 * 这里只传递异常，不负责具体处理
                 *
                 * @param t
                 */
                @Override
                public void onFailure(Throwable t) {
                    future.setException(t);
                }
            }, this.executor);
        } catch (Throwable e) {
            future.setException(e);
        } finally {
            stopTimeMs = System.currentTimeMillis();
        }


        return future;
    }

    public final O emit() throws Exception {
        try {
            return getUninterruptibly(future);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw new RuntimeException(cause);
            }
            throw (Exception) e.getCause();
        }
    }

    public final O emit(long timeout, TimeUnit unit) throws Exception {
        try {
            return getUninterruptibly(future, timeout, unit);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw new RuntimeException(cause);
            }
            throw (Exception) e.getCause();
        }
    }

    public final void setSinkBolts(List<Bolt> sinkBolts) {
        Preconditions.checkArgument(!evaluated.get(), "Bolt [%s] has been executed.", getName());
        Preconditions.checkNotNull(sinkBolts);
        this.sinkBolts = sinkBolts;
    }

    public final Bolt<O> addSinkBolts(List<Bolt> sinkNodes) {
        setSinkBolts(
                ImmutableList.<Bolt>builder()
                        .addAll(this.sinkBolts)
                        .addAll(sinkNodes)
                        .build());
        return this;
    }

    public final Bolt addSinkBolts(Bolt... nodes) {
        return addSinkBolts(ImmutableList.copyOf(nodes));
    }

    protected void preEvaluate() throws Exception {
    }

    protected abstract ListenableFuture<O> evaluate() throws Exception;

    protected void postEvaluate(ListenableFuture<O> future) throws Exception {
    }

    protected void aborted() {
    }

    protected void completeEvaluate(Object result, Throwable e, boolean completedInCallback) throws Exception {
        if (e != null) {
            errorEvaluate(e);
        } else {
            successEvaluate((O) result);
        }
    }

    protected void successEvaluate(O result) throws Exception {

    }

    protected void errorEvaluate(Throwable e) throws Exception {

    }

    /**
     * 获取依赖值，并当有任何异常时，返回默认值
     *
     * @param name
     * @param defaultValue
     * @param <T>
     * @return
     */
    protected final <T> T getDep(Enum name, T defaultValue) {
        checkArgument(name != null, "name must not be null.");
        Bolt<T> bolt = getDepBolt(name);
        try {
            T val = bolt.emit();
            return val == null ? defaultValue : val;
        } catch (Throwable e) {
            log.warn("getDep {} ignore this error:", name, e);
            return defaultValue;
        }
    }

    /**
     * 获取依赖值，并当有任何异常时，返回默认值
     *
     * @param name
     * @param defaultValue
     * @param <T>
     * @return
     */
    protected final <T> T getDep(Enum name, long timeout, TimeUnit unit, T defaultValue) {
        checkArgument(name != null, "name must not be null.");
        checkArgument(timeout > 0, "timeout must be positive");
        checkNotNull(unit, "time unit cannot be null");
        Bolt<T> bolt = getDepBolt(name);
        try {
            T val = bolt.emit(timeout, unit);
            return val == null ? defaultValue : val;
        } catch (Throwable e) {
            log.warn("getDep {} ignore this error:", name, e);
            return defaultValue;
        }
    }

    /**
     * 获取以来只
     *
     * @param name
     * @param <T>
     * @return
     * @throws Exception
     */
    protected final <T> T getDep(Enum name) throws Exception {
        checkArgument(name != null, "name must not be null.");
        return (T) getDepBolt(name).emit();
    }

    /**
     * 整个Bolt执行的开始时间
     *
     * @return
     */
    protected final long getStartTimeMs() {
        return startTimeMs;
    }

    /**
     * 执行 {@link #evaluate()} 的开始时间
     *
     * @return
     */
    protected final long getEvaluateStartTimeMs() {
        return evaluateStartTimeMs;
    }

    /**
     * 执行 {@link #evaluate()} 的结束时间，如果是异步，则可能非常短暂。
     *
     * @return
     */
    protected final long getEvaluateStopTimeMs() {
        return evaluateStopTimeMs;
    }

    /**
     * 整个Bolt 执行的结束时间
     *
     * @return
     */
    protected final long getStopTimeMs() {
        return stopTimeMs;
    }

    Bolt<O> register(BoltGraph graph) {
        if (graph != null && this.graph == null) {
            synchronized (this) {
                if (this.graph == null) {
                    this.graph = graph;
                } else {
                    if (this.graph != graph) {
                        throw new IllegalStateException(
                                "register a difference Graph to this Bolt " + getName() + "in " + this.graph.getClass().getName() + ", new graph class " + graph == null ?
                                        "null" : graph.getClass().getName());
                    }
                }
            }
        }
        return this;
    }

    private Pair<ListenableFuture, ListenableFuture> futureFromDependencies() {
        final List<ListenableFuture<Object>> requiredFutures = newArrayListWithExpectedSize(dependencies.size());
        final List<ListenableFuture<Object>> optionalFutures = newArrayListWithExpectedSize(dependencies.size());

        for (Map.Entry<Enum, Bolt> item : dependencies.entrySet()) {
            if (optionals.contains(item.getKey())) {
                optionalFutures.add(item.getValue().register(graph).execute());
            } else {
                requiredFutures.add(item.getValue().register(graph).execute());
            }
        }

        final ListenableFuture<List<Object>> combinedOptionalFutures = Futures.successfulAsList(optionalFutures);
        final ListenableFuture<List<Object>> combinedRequiredFutures = Futures.allAsList(requiredFutures);
//        ListenableFuture combinedFuture = Futures.allAsList(combinedOptionalFutures, combinedRequiredFutures);
        return Pair.of(combinedRequiredFutures, combinedOptionalFutures);
    }

    private void applySinkBolts() {
        for (Bolt node : sinkBolts) {
            try {
                Futures.addCallback(node.register(graph).execute(), new FutureCallback() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error("sinkBolt evaluated failed.", t);
                    }
                });
            } catch (Throwable e) {
                //ignore
                log.warn("sink bolt {} ignore this error: ", node.getName(), e);
            }
        }
    }

    private <T> Bolt<T> getDepBolt(Enum name) {
        checkNotNull(name, "name must not be null.");
        Bolt<T> bolt = dependencies.get(name);
        checkNotNull(bolt, "dependency '%s' not initiated.", name);
        return bolt;
    }

    private void ensueComplete(O result, Throwable e, boolean completedInCallback) {
        try {
            completeEvaluate(null, e, completedInCallback);
            if (e != null) {
                future.setException(e);
            } else {
                future.set(result);
            }
        } catch (Throwable completeException) {
            log.error("###Bolt error happens at completeEvaluate,", e);
            e = completeException;
            future.setException(completeException);
        }

        if (e == null) {
            try {
                applySinkBolts();
            } catch (Throwable t) {
                log.warn("###Bolt error happens at applySinkBolts()", t);
            }
        }
    }

    public static class Builder<T> {
        protected Bolt<T> instance;

        protected String name;
        protected Map<Enum, Bolt> dependencies;
        protected Set<Enum> optionals;
        protected Executor executor;
        protected List<Bolt> sinkBolts = ImmutableList.of();

        public Builder() {

        }

        public Builder(Bolt<T> instance) {
            checkNotNull(instance, "'Bolt' must not be null");
            this.instance = instance;
        }

        public Builder(Class<? extends Bolt<T>> boltClass) {
            checkNotNull(boltClass, "'Bolt' must not be null");
            this.instance = createInstance(boltClass);
        }

        public Builder<T> withBolt(Bolt<T> instance) {
            checkNotNull(instance, "'Bolt' must not be null");
            this.instance = instance;
            return this;
        }

        public Builder<T> withExecutor(Executor executor) {
            checkNotNull(executor, "'executor' must not be null");
            this.executor = executor;
            return this;
        }

        public Builder<T> withBolt(Class<? extends Bolt<T>> boltClass) {
            return withBolt(createInstance(boltClass));
        }

        public Builder<T> withName(String name) {
            checkArgument(name != null && !name.isEmpty(),
                    "'name' must be specified.");
            this.name = name;
            return this;
        }

        public Builder<T> dependsOn(Enum name, Bolt dependency) {
            return dependsOn(name, dependency, false);
        }

        public Builder<T> dependsOn(Enum name, Bolt dependency, boolean optional) {
            checkArgument(name != null, "'name' must not be null.");
            checkArgument(dependency != null, "'dependency' must not be null.");

            if (optional) {
                if (optionals == null) {
                    optionals = Sets.newLinkedHashSet();
                }
                checkArgument(optionals.add(name), "You have already added a dependent bolt named " + name);
            }
            if (dependencies == null) {
                dependencies = Maps.newLinkedHashMap();
            }
            checkArgument(dependencies.put(name, dependency) == null,
                    "You have already added a dependent bolt named " + name);
            return this;
        }

        public Builder<T> withRequiredDependencies(Pair<Enum, Bolt>... deps) {
            return withDependencies(false, deps);
        }

        public Builder<T> withOptianalDependencies(Pair<Enum, Bolt>... deps) {
            return withDependencies(true, deps);
        }

        public Builder<T> withDependencies(boolean optional, Pair<Enum, Bolt>... deps) {
            checkArgument(deps.length > 0, "Args must not gt 0");
            try {
                for (int i = 0; i < deps.length; i++) {
                    dependsOn(deps[i].getLeft(), deps[i].getRight(), optional);
                }
            } catch (ClassCastException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder<T> withSinks(List<Bolt> sinkBolts) {
            this.sinkBolts = sinkBolts == null ? ImmutableList.<Bolt>of() : sinkBolts;
            return this;
        }

        public Builder<T> withSinks(Bolt... sinkNodes) {
            return withSinks(ImmutableList.copyOf(sinkNodes));
        }


        protected <T> Bolt<T> createInstance(Class<? extends Bolt<T>> boltClass) {
            try {
                return boltClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                        String.format(
                                "Cannot create instance for Node [%s], make sure it has a default constructor",
                                boltClass.getSimpleName()), e);
            }
        }


        public Bolt<T> build() {
            checkNotNull(instance, "'Bolt type' must not be null.");
            instance.dependencies = dependencies == null ? ImmutableMap.<Enum, Bolt>of() : ImmutableMap.copyOf(dependencies);
            instance.optionals = optionals == null ? ImmutableSet.<Enum>of() : ImmutableSet.copyOf(optionals);
            instance.name = StringUtils.isNotBlank(name) ? name : instance.getClass().getSimpleName();
            instance.sinkBolts = sinkBolts == null ? ImmutableList.<Bolt>of() : ImmutableList.copyOf(sinkBolts);
            instance.executor = executor == null ? MoreExecutors.sameThreadExecutor() : executor;
            return instance;
        }
    }
}
