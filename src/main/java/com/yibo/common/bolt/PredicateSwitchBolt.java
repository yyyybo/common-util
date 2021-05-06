package com.yibo.common.bolt;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * .
 */
class PredicateSwitchBolt<O> extends Bolt<O> {

    private Bolt<O> trueBolt;
    private Bolt<O> falseBolt;

    PredicateSwitchBolt(Bolt<Boolean> predicate, Bolt<O> t, Bolt<O> f) {
        new Builder<>(this).dependsOn(D.Predicate, predicate).build();
        this.trueBolt = Preconditions.checkNotNull(t, "true Bolt must not be null.");
        this.falseBolt = Preconditions.checkNotNull(f, "false Bolt must not be null.");
    }

    @Override
    public String getName() {
        return super.getName() + "_" + hashCode();
    }

    @Override
    protected ListenableFuture<O> evaluate() throws Exception {
        boolean predicate = getDep(D.Predicate);
        return predicate ? trueBolt.register(graph).execute() : falseBolt.register(graph).execute();
    }

    public enum D {
        Predicate
    }
}
