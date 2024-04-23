package com.zst.registrycenter.utils;

import org.springframework.aop.framework.AopProxyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrivateAccessor {
    public static<T> T invoke(Object ref, Class<?> clz, String name, Object... args) {
        try {
            if (clz.getName().contains("$$")) {
                ref = AopProxyUtils.getSingletonTarget(ref);
                clz = ref.getClass();
            }

            Class<?>[] argClasses = TypeUtils.getClassesFromObj(args);
            Method targetMethod = TypeUtils.getMethodByNameAndArgs(clz, name, argClasses);

            if (targetMethod != null) {
                targetMethod.setAccessible(true);
                return (T) targetMethod.invoke(ref, args);
            }
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ite.getTargetException();
            } else {
                throw new RuntimeException(ite);
            }
        }
        throw new RuntimeException("Method not found");
    }

    /**
     * 访问对象中的指定名称的方法
     * @param ref
     * @param name
     * @param args
     * @return
     * @param <T>
     */
    public static <T> T invoke(Object ref, String name, Object... args) {
        Class<?> clz = ref.getClass();
        return invoke(ref, clz, name, args);
    }

    /**
     * 访问指定类的静态方法
     * @param clazz
     * @param name
     * @param args
     * @return
     * @param <T>
     */
    public static <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
        return invoke(null, clazz, name, args);
    }

    public static void set(Object ref, Class<?> clz, String fieldName, Object value) {
        try {
            Field field = TypeUtils.getFieldByName(clz, fieldName);
            if (field == null) {
                throw new RuntimeException("Field not found");
            }

            field.setAccessible(true);
            field.set(ref, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field " + fieldName, e);
        }
    }

    public static void set(Object ref, String fieldName, Object value) {
        set(ref, ref.getClass(), fieldName, value);
    }

    public static void setStatic(Class<?> clz, String fieldName, Object value) {
        set(null, clz, fieldName, value);
    }

    public static <T> T get(Object ref, Class<?> clz, String fieldName) {
        try {
            Field field = TypeUtils.getFieldByName(clz, fieldName);
            if (field == null) {
                throw new RuntimeException("Field not found");
            }

            field.setAccessible(true);
            return (T) field.get(ref);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field " + fieldName, e);
        }
    }

    public static <T> T get(Object ref, String fieldName) {
        return get(ref, ref.getClass(), fieldName);
    }

    public static <T> T getStatic(Class<?> clz, String fieldName) {
        return get(null, clz, fieldName);
    }
}
