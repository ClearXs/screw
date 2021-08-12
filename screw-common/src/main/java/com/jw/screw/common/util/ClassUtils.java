package com.jw.screw.common.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtils {

    private final static String CGLIB = "EnhancerByCGLIB";
    /**
     * 获取class对象只有一个的泛型全限定类名
     * @param clazz class对象
     * @return 返回一个泛型对象
     */
    public static String getSingleGenericClassName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Class<?> genericClass = clazz;
        if (clazz.getName().contains(CGLIB)) {
            genericClass = clazz.getSuperclass();
        }
        Type type = genericClass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArguments.length == 0) {
                throw new NullPointerException(String.format("target %s can't find generic", genericClass.getName()));
            }
            return typeArguments[0].getTypeName();
        }
        return null;
    }

    public static String[] getAllGenericClassNames(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArguments.length == 0) {
                throw new NullPointerException(String.format("target %s can't find generic", clazz.getName()));
            }
            return Arrays.stream(typeArguments).map(Type::getTypeName).collect(Collectors.toList()).toArray(new String[]{ });
        }
        return null;
    }

    /**
     * {@code Object}类型的参数转为Class对象
     * @param args 参数类型
     * @return class[] 对象
     */
    public static Class<?>[] objectToClass(Object... args) {
        return Arrays.stream(args)
                .map(Object::getClass)
                .collect(Collectors.toList())
                .toArray(new Class<?>[]{ });
    }

    public static Method getMethod(Class<?> clazz, String targetMethodName, Object... args) {
        Class<?>[] argsClass = objectToClass(args);
        return getMethod(clazz, targetMethodName, argsClass);
    }

    public static Method getMethod(Class<?> clazz, String targetMethodName, Class<?>[] argsClass) {
        Method method = null;
        try {
            method = clazz.getMethod(targetMethodName, argsClass);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getDeclaredMethod(targetMethodName, argsClass);
            } catch (NoSuchMethodException e2) {
            }
        }
        if (method == null) {
            Set<Method> allMethods = new HashSet<>();
            allMethods.addAll(Arrays.asList(clazz.getMethods()));
            allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            List<Method> targetMethods = allMethods.stream()
                    .filter(m -> targetMethodName.equals(m.getName()) && argsClass.length == m.getParameterCount())
                    .collect(Collectors.toList());
            if (Collections.isNotEmpty(targetMethods)) {
                method = targetMethods.get(0);
            }
        }
        return method;
    }
}
