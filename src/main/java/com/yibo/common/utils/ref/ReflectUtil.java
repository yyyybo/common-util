package com.yibo.common.utils.ref;

import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射综合类
 *
 * @author 莫问
 * @date 2018/9/6
 *  
 */
public class ReflectUtil {

    /**
     * 功能：执行对象中的某个函数。
     *
     * @param instance   对象
     * @param methodName 函数名
     * @param args       参数
     * @return Object 执行后函数的返回值
     */
    public static Object invokeMethod(Object instance, String methodName, Object... args)
        throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
        InvocationTargetException, InstantiationException {
        //根据参数得到对应的类型数组
        Class[] argArr = null;
        if (!ObjectUtils.isEmpty(args)) {
            argArr = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argArr[i] = args[i].getClass();
            }
        }
        Method m = instance.getClass()
            .getDeclaredMethod(methodName, argArr);
        //执行该类的中该方法
        return m.invoke(instance, args);
    }

    /**
     * 功能：得到某个类的一个实例。
     *
     * @param className 全类名(含包名)
     * @return <T> T
     */
    public static <T> T getInstance(String className)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class testClass = Class.forName(className);
        // 调用缺省构造函数，直接用testClass调用就可
        Object obj = testClass.newInstance();
        return (T)obj;
    }

    /**
     * 功能：得到某个类的所有方法。
     *
     * @param classType 类
     * @return Method[]
     */
    public static Method[] getAllMethod(Class<?> classType) {
        return classType.getDeclaredMethods();
    }

    /**
     * 功能：执行一个静态函数。
     *
     * @param cls        类
     * @param methodName 静态函数名称
     * @param args       参数
     * @return Object 执行后的返回值
     */
    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... args)
        throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        int arguments = args.length;
        Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        Method method = cls.getMethod(methodName, parameterTypes);
        return method.invoke(null, args);
    }
}
