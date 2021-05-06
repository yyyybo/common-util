package com.yibo.common.bolt;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author .
 */
public class ServiceBoltFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;


    public <T> Bolt.Builder<T> builder() {
        return new ServiceBoltBuilder<>();
    }

    public <T> Bolt.Builder builder(Class<? extends Bolt<T>> boltClass) {
        return new ServiceBoltBuilder<T>().withBolt(boltClass);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    class ServiceBoltBuilder<T> extends Bolt.Builder<T> {

        @Override
        protected <T> Bolt<T> createInstance(Class<? extends Bolt<T>> boltClass) {
            Bolt<T> bolt = super.createInstance(boltClass);
            if (bolt instanceof ServiceBolt) {
                ((ServiceBolt) bolt).applicationContext = applicationContext;
            }
            return bolt;
        }

        @Override
        public Bolt.Builder<T> withBolt(Bolt<T> instance) {
            if (instance instanceof ServiceBolt && ((ServiceBolt<T>) instance).applicationContext == null) {
                ((ServiceBolt) instance).applicationContext = applicationContext;
            }
            return super.withBolt(instance);
        }
    }
}
