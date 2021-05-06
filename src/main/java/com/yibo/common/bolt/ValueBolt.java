package com.yibo.common.bolt;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.Supplier;

/**
 * .
 */
final class ValueBolt<O> extends Bolt<O> {

    private final Supplier<O> supplier;

    protected ValueBolt(O value) {
        super();
        this.supplier = () -> value;
    }

    public ValueBolt(Supplier<O> supplier) {
        super();
        this.supplier = supplier == null ? () -> null : supplier;
    }

    @Override
    protected ListenableFuture<O> evaluate() throws Exception {
        return Futures.immediateFuture(supplier.get());
    }

    @Override
    public String getName() {
        return super.getName() + "_" + hashCode();
    }

    @Override
    public String getHumanReadableName() {

        return super.getName();
    }
}
