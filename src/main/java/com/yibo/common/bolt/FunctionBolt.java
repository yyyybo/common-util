package com.yibo.common.bolt;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * .
 */
class FunctionBolt<F, T> extends Bolt<T> {

    private Function<F, T> function;

    public FunctionBolt(Bolt<F> from, Function<F, T> function) {
        new Builder<>(this).dependsOn(D.From, from).build();
        this.function = function;
    }

    @Override
    public String getName() {
        return super.getName() + "_" + hashCode();
    }

    @Override
    protected ListenableFuture<T> evaluate() throws Exception {
        return Futures.immediateFuture(function.apply((F) getDep(D.From)));
    }

    enum D {
        From
    }
}
