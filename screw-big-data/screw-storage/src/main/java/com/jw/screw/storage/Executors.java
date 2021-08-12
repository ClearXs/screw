package com.jw.screw.storage;

import com.jw.screw.common.util.ClassUtils;
import com.jw.screw.common.util.Collections;
import com.jw.screw.storage.annotation.ReadWriteExecutor;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executor 代理对象创建类
 * @author jiangw
 * @date 2021/7/21 18:13
 * @since 1.1
 */
public class Executors {

    public static Executor create(Class<?> superClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setCallback(new MessageIntercept());
        return (Executor) enhancer.create();
    }

    public static Executor create(Class<?> superClass, Object... args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setCallback(new MessageIntercept());
        return (Executor) enhancer.create(ClassUtils.objectToClass(args), args);
    }

    /**
     * 拦截被{@link ReadWriteExecutor}标识的注解方法。不被拦截方法正常调用。
     * 当方法被拦截后，判断当前配置是否开启执行，如果是那么：
     * 1.从{@link Recoder}中选取最合适中的一个
     * 2.进行对应的方法调用
     */
    static class MessageIntercept implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            method.setAccessible(true);
            ReadWriteExecutor readWriteExecutor = method.getAnnotation(ReadWriteExecutor.class);
            if (readWriteExecutor == null) {
                return proxy.invokeSuper(obj, args);
            }
            if (!(obj instanceof Executor)) {
                return null;
            }
            StorageProperties storageProperties = ((Executor) obj).getStorageProperties();
            boolean enable = storageProperties.isEnable();
            if (!enable) {
                return null;
            }
            boolean read = readWriteExecutor.read();
            boolean write = readWriteExecutor.write();
            Recoder<?> recoder = null;
            if (read) {
                recoder = chooser(((Executor) obj).getReadRecords(), args);
            } else if (write) {
                recoder = chooser(((Executor) obj).getWriteRecords(), args);
            }
            if (recoder != null) {
                Class<?>[] argsClazz = Arrays.stream(args)
                        .filter(o -> !(o instanceof Class<?>))
                        .map(Object::getClass)
                        .collect(Collectors.toList())
                        .toArray(new Class<?>[]{ });
                // 去除class对象
                Method recordMethod = getRecordMethod(recoder.getClass(), method.getName(), argsClazz);
                if (recordMethod != null) {
                    recordMethod.setAccessible(true);
                    // 去除class对象
                    Object[] objects = Arrays.stream(args)
                            .filter(o -> !(o instanceof Class<?>))
                            .collect(Collectors.toList())
                            .toArray(new Object[]{});
                    return recordMethod.invoke(recoder, objects);
                }
            }
            return null;
        }

        /**
         * 获取record调用的方法
         * @param recordClazz record class对象
         * @param targetMethodName 需要调用目标方法的名称
         * @param targetArgs 目标方法的参数
         * @return method实例对象
         */
        Method getRecordMethod(Class<?> recordClazz, String targetMethodName, Class<?>[] targetArgs) {
            Method method = null;
            try {
                method = recordClazz.getMethod(targetMethodName, targetArgs);
            } catch (NoSuchMethodException e) {
                Method[] methods = recordClazz.getMethods();
                List<Method> targetMethods = Arrays.stream(methods)
                        .filter(m -> targetMethodName.equals(m.getName()) && targetArgs.length == m.getParameterCount())
                        .collect(Collectors.toList());
                if (Collections.isNotEmpty(targetMethods)) {
                    method = targetMethods.get(0);
                }
            }
            return method;
        }

        /**
         * 根据实体类型选择一个recoder
         * @param list recorder的list列表
         */
        <T> Recoder<T> chooser(Map<String, Recoder<T>> list, Object... args) {
            if (Collections.isNotEmpty(list)) {
                Class<?> clazz = null;
                for (Object arg : args) {
                    if (arg instanceof Class<?>) {
                        clazz = (Class<?>) arg;
                    }
                }
                if (clazz == null) {
                    return null;
                }
                return list.get(clazz.getName());
            } else {
                return null;
            }
        }
    }
}
