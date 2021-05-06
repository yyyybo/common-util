package com.yibo.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * 避免一些异常情况的工具类
 *
 * @author 莫问
 */
public class Safes {

    public static <K, V> Map<K, V> of(Map<K, V> source) {
        return Optional.ofNullable(source)
            .orElse(Maps.newHashMapWithExpectedSize(0));
    }

    public static <T> Iterator<T> of(Iterator<T> source) {
        return Optional.ofNullable(source)
            .orElse(Lists.<T>newArrayListWithCapacity(0).iterator());
    }

    public static <T> Collection<T> of(Collection<T> source) {
        return Optional.ofNullable(source)
            .orElse(Lists.newArrayListWithCapacity(0));
    }

    public static <T> Iterable<T> of(Iterable<T> source) {
        return Optional.ofNullable(source)
            .orElse(Lists.newArrayListWithCapacity(0));
    }

    public static <T> List<T> of(List<T> source) {
        return Optional.ofNullable(source)
            .orElse(Lists.newArrayListWithCapacity(0));
    }

    public static <T> Set<T> of(Set<T> source) {
        return Optional.ofNullable(source)
            .orElse(Sets.newHashSetWithExpectedSize(0));
    }

    public static BigDecimal of(BigDecimal source) {
        return Optional.ofNullable(source)
            .orElse(BigDecimal.ZERO);
    }

    public static String of(String source) {
        return Optional.ofNullable(source)
            .orElse(StringUtils.EMPTY);
    }

    public static String of(String source, String defaultStr) {
        return Optional.ofNullable(source)
            .orElse(defaultStr);
    }

    /**
     * 对象为空, 返回默认值
     *
     * @param source       原对象
     * @param defaultValue 默认值对象
     * @param <T>          泛型
     * @return 不为空:原对象 为空:默认值对象
     */
    public static <T> T of(T source, T defaultValue) {

        return Optional.ofNullable(source)
            .orElse(defaultValue);
    }

    public static <T> T first(Collection<T> source) {

        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        T t = null;
        Iterator<T> iterator = source.iterator();
        if (iterator.hasNext()) {
            t = iterator.next();
        }
        return t;
    }

    public static void run(Runnable runnable, Consumer<Throwable> error) {
        try {
            runnable.run();
        } catch (Throwable t) {
            error.accept(t);
        }
    }

}
