package com.yibo.common.bolt;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracing;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

public abstract class ServiceBolt<O> extends Bolt<O> {

    private static final String BOLT_EXCEPTION = "BOLT_EXCEPTION";

    private Span parentSpan;
    private ScopedSpan child;

    protected ServiceBolt() {
        super();
        this.parentSpan = Tracing.currentTracer().currentSpan();
    }

    ApplicationContext applicationContext;

    final protected <T> T getService(String name) {
        return (T) applicationContext.getBean(name);
    }

    final protected <T> T getService(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    final protected <T> T getService(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    @Override
    final protected void preEvaluate() throws Exception {
        final String name = getName();
        if (StringUtils.isNotBlank(name)) {
            child = Tracing.currentTracer().startScopedSpan(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
        }
        doPreEvaluate();
    }

    protected void doPreEvaluate() throws Exception {
    }

    @Override
    final protected void postEvaluate(ListenableFuture<O> future) throws Exception {
        doPostEvaluate(future);
    }

    protected void doPostEvaluate(ListenableFuture<O> future) throws Exception {
    }

    @Override
    final protected void completeEvaluate(Object result, Throwable e, boolean completedInCallback) throws Exception {
        Throwable completeException = e;
        try {
            doCompleteEvaluate(result, e, completedInCallback);
        } catch (Throwable ce) {
            completeException = ce;
            throw ce;
        } finally {
            if (child != null) {
                if (completeException != null) {
                    child.error(completeException);
                } else {
                    child.finish();
                }
            }
        }
    }

    @Override
    protected void aborted() {
        if (child != null) {
            child.annotate(getName() + " is aborted");
            child.finish();
        }
    }

    protected void doCompleteEvaluate(Object result, Throwable e, boolean completedInCallback) throws Exception {
        if (e != null) {
            errorEvaluate(e);
        } else {
            successEvaluate((O) result);
        }
    }
}
