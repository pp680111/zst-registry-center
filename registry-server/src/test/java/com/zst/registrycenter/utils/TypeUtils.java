package com.zst.registrycenter.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TypeUtils {
    /**
     * 获取对象的Class对象
     *
     * 当参数值为null时对应的Class对象也为null
     * @param args
     * @return
     */
    public static Class<?>[] getClassesFromObj(Object[] args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }

        Class<?>[] ret = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            ret[i] = args[i] == null ? null : args[i].getClass();
        }
        return ret;
    }

    /**
     * 从一个Class中获取指定方法
     * @param target
     * @param methodName
     * @param argClasses
     * @return
     */
    public static Method getMethodByNameAndArgs(Class<?> target, String methodName, Class<?>[] argClasses) {
        Method[] methods = target.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName) && typeEquals(m.getParameterTypes(), argClasses)) {
                return m;
            }
        }

        // 如果target类中没有的话，可能是在父类中的方法，递归查找
        if (target.getSuperclass() != null) {
            return getMethodByNameAndArgs(target.getSuperclass(), methodName, argClasses);
        }

        return null;
    }

    public static boolean typeEquals(Class<?>[] left, Class<?>[] right) {
        if (left.length != right.length) {
            return false;
        }

        for (int i = 0; i < left.length; i++) {
            /*
                如果参数值的类型为null,那么参数值就是一个null值。此时如果待匹配的方法中的参数值类型为基础类型的话，
                参数值就不可能是null值，因此判定为方法不匹配
             */

            if (right[i] == null) {
                return !left[i].isPrimitive();
            }
            if (!left[i].isAssignableFrom(right[i]) && !primitiveTypeFuzzyEquals(left[i], right[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据名称查找指定字段
     * @param clz
     * @param fieldName
     * @return
     */
    public static Field getFieldByName(Class<?> clz, String fieldName) {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clz.getSuperclass() != null) {
                return getFieldByName(clz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    /**
     * 对于原生类型与封箱类型之间的对比，则使用模糊对比的方式来进行匹配
     * @return
     */
    private static boolean primitiveTypeFuzzyEquals(Class<?> left, Class<?> right) {
        return (left.equals(int.class) && right.equals(Integer.class) || left.equals(Integer.class) && right.equals(int.class))
                || (left.equals(long.class) && right.equals(Long.class) || left.equals(Long.class) && right.equals(long.class))
                || (left.equals(short.class) && right.equals(Short.class) || left.equals(Short.class) && right.equals(short.class))
                || (left.equals(boolean.class) && right.equals(Boolean.class) || left.equals(Boolean.class) && right.equals(boolean.class))
                || (left.equals(char.class) && right.equals(Character.class) || left.equals(Character.class) && right.equals(char.class))
                || (left.equals(byte.class) && right.equals(Byte.class) || left.equals(Byte.class) && right.equals(byte.class))
                || (left.equals(float.class) && right.equals(Float.class) || left.equals(Float.class) && right.equals(float.class))
                || (left.equals(double.class) && right.equals(Double.class) || left.equals(Double.class) && right.equals(double.class));
    }
}
