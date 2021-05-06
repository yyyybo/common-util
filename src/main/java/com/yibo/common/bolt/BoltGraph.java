package com.yibo.common.bolt;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * .
 */
public abstract class BoltGraph<I, O> {

    volatile boolean completed = false;

    /**
     * 这个返回的future，get的时候记得设置超时
     * Timeout的时候，记得主动调用cancel接口，触发interrupt和图的完成
     * 图完成之后，后续没有指定的bolt会直接跳过逻辑的执行，节省资源
     * @return 代表整个图的运算结果的future
     */
    public ListenableFuture<O> execute() {
        ListenableFuture<O> future = createGraph().register(this).execute();
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onFailure(Throwable t) {
                //设置终止状态，节点执行前检查此状态
                complete();
            }
        });
        return future;
    }

    public String printGraph() {
        Bolt bolt = createGraph();
        if (bolt == null) {
            return "null";
        }
        try {
            StringBuilder flowchart = new StringBuilder();
            flowchart.append("digraph G { \r\n");
            flowchart.append(bolt.getName() + " [style=filled,fillcolor = red]\r\n");
            flowchart.append(printGraph(bolt, new HashSet<Bolt>()));
            flowchart.append("}");
            return flowchart.toString();
        } catch (NoSuchFieldException e) {
            return "create graph Error." + e.getMessage();
        }
    }

    boolean isCompleted() {
        return completed;
    }

    void complete() {
        this.completed = true;
    }

    private String printGraph(Bolt bolt, Set<Bolt> printed) throws NoSuchFieldException {
        if (bolt instanceof PredicateSwitchBolt) {
            return printPredicateSwitchBolt(null, (PredicateSwitchBolt) bolt, printed);
        } else {
            return printNormalBolt(bolt, printed);
        }
    }

    private String printPredicateSwitchBolt(Bolt bolt, PredicateSwitchBolt predicateSwitchBolt, Set<Bolt> printed) throws NoSuchFieldException {
        if (printed.contains(predicateSwitchBolt)) {
            return "";
        }


        final Field trueBoltField = PredicateSwitchBolt.class.getDeclaredField("trueBolt");
        final Field falseBoltField = PredicateSwitchBolt.class.getDeclaredField("falseBolt");
        ReflectionUtils.makeAccessible(trueBoltField);
        ReflectionUtils.makeAccessible(falseBoltField);
        Bolt trueBolt = (Bolt) ReflectionUtils.getField(trueBoltField, predicateSwitchBolt);
        Bolt falseBolt = (Bolt) ReflectionUtils.getField(falseBoltField, predicateSwitchBolt);

        StringBuilder sb = new StringBuilder();
        sb.append(printNormalBolt(predicateSwitchBolt, printed));

        printed.add(predicateSwitchBolt);

        sb.append(predicateSwitchBolt.getName()).append(" -> ").append(trueBolt.getName()).append(" [headlabel=T,color=\"green\"]\r\n");
        sb.append(predicateSwitchBolt.getName()).append(" -> ").append(falseBolt.getName()).append(" [headlabel=F,color=\"red\"]\r\n");

        if (trueBolt instanceof PredicateSwitchBolt) {
            sb.append(printPredicateSwitchBolt(bolt, (PredicateSwitchBolt) trueBolt, printed));
        } else {
            sb.append(printNormalBolt(trueBolt, printed));
        }
        if (falseBolt instanceof PredicateSwitchBolt) {
            sb.append(printPredicateSwitchBolt(bolt, (PredicateSwitchBolt) falseBolt, printed));
        } else {
            sb.append(printNormalBolt(falseBolt, printed));
        }

        return sb.toString();
    }

    private String printNormalBolt(final Bolt bolt, final Set<Bolt> printed) throws NoSuchFieldException {
        if (bolt == null) {
            return "";
        }
        if (bolt == Bolt.NULL_NODE) {
            return "";
        }
        if (printed.contains(bolt)) {
            return "";
        }

        printed.add(bolt);

        final Field dependenciesField = Bolt.class.getDeclaredField("dependencies");
        final Field optionalField = Bolt.class.getDeclaredField("optionals");
        final Field sinkBoltsField = Bolt.class.getDeclaredField("sinkBolts");
        ReflectionUtils.makeAccessible(dependenciesField);
        ReflectionUtils.makeAccessible(optionalField);
        ReflectionUtils.makeAccessible(sinkBoltsField);
        Map<Enum, Bolt> dependencies = (Map<Enum, Bolt>) ReflectionUtils.getField(dependenciesField, bolt);
        final Set<Enum> optionals = (Set<Enum>) ReflectionUtils.getField(optionalField, bolt);

        StringBuilder sb = new StringBuilder();

        if (dependencies.isEmpty()) {
            return "";
        }

        Set<Enum> leafNode = Sets.newHashSet();
        for (Map.Entry<Enum, Bolt> item : dependencies.entrySet()) {
            if (printed.contains(item.getValue())) {
                continue;
            }
            if (item.getValue() instanceof PredicateSwitchBolt) {
                final String subGraph = printPredicateSwitchBolt(bolt, (PredicateSwitchBolt)item.getValue(), printed);
                if (StringUtils.isEmpty(subGraph)) {
                    leafNode.add(item.getKey());
                }
                sb.append(subGraph);
            } else {
                final String subGraph = printGraph(item.getValue(), printed);
                if (StringUtils.isEmpty(subGraph)) {
                    leafNode.add(item.getKey());
                }
                sb.append(subGraph);
            }
        }

        if (CollectionUtils.isNotEmpty(leafNode)) {
            dependencies.entrySet()
                .stream()
                .filter(e -> leafNode.contains(e.getKey()))
                .forEach(e -> sb.append(e.getValue().getName() + " [style=filled,fillcolor = green]\n"));
        }

        final Map<Boolean, List<Map.Entry<Enum, Bolt>>> grouped = dependencies.entrySet()
            .stream()
            .collect(Collectors.groupingBy(b -> optionals.contains(b.getKey())));
        // 必须依赖
        final List<Map.Entry<Enum, Bolt>> required = grouped.get(Boolean.FALSE);
        if (CollectionUtils.isEmpty(required)) {
            sb.append(bolt.getName() + " [style=filled,fillcolor = green]\n");
        } else {
            sb.append(" {");
            sb.append(Joiner.on(" ").join(Iterables.transform(required, input -> {
                if (input.getValue() instanceof PredicateSwitchBolt) {
                    return rendorPredicateSwitchBolt(input);
            } else {
                    return input.getValue().getName();
            }
            })));
            sb.append("}");
            sb.append(" ->");
            sb.append(bolt.getName());
            sb.append("\r\n");
        }
        // 非必须依赖
        final List<Map.Entry<Enum, Bolt>> optional = grouped.get(Boolean.TRUE);
        if(CollectionUtils.isNotEmpty(optional)) {
        sb.append("{");
            sb.append(Joiner.on(" ").join(Iterables.transform(optional, input -> {
                if (input.getValue() instanceof PredicateSwitchBolt) {
                    return rendorPredicateSwitchBolt(input);
                } else {
                    return input.getValue().getName();
                }
            })));
            sb.append("}");
            sb.append(" ->");
            sb.append(bolt.getName());
            sb.append(" [style = dashed] \r\n");
        }

        return sb.toString();
    }

    private String rendorPredicateSwitchBolt(Map.Entry<Enum, Bolt> input) {
                    try {
                        final Field trueBoltField = PredicateSwitchBolt.class.getDeclaredField("trueBolt");
                        final Field falseBoltField = PredicateSwitchBolt.class.getDeclaredField("falseBolt");
                        ReflectionUtils.makeAccessible(trueBoltField);
                        ReflectionUtils.makeAccessible(falseBoltField);
                        Bolt trueBolt = (Bolt) ReflectionUtils.getField(trueBoltField, input.getValue());
                        Bolt falseBolt = (Bolt) ReflectionUtils.getField(falseBoltField, input.getValue());
                        StringBuilder sb = new StringBuilder();
                        if (trueBolt != Bolt.NULL_NODE) {
                sb.append(trueBolt.getName());
                        }
                        if (falseBolt != Bolt.NULL_NODE) {
                sb.append(falseBolt.getName());
                        }

                        return sb.toString();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
    }

    protected abstract Bolt<O> createGraph();
}
