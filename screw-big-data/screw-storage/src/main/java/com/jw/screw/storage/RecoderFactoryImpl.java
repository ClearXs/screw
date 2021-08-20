package com.jw.screw.storage;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.proxy.CglibInvocationInterceptor;
import com.jw.screw.common.proxy.ProxyFactory;
import com.jw.screw.common.util.ClassUtils;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;
import net.sf.cglib.proxy.MethodProxy;

import javax.el.MethodNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建日志工程
 * @author jiangw
 * @date 2021/6/28 15:12
 * @since 1.1
 */
public class RecoderFactoryImpl implements RecoderFactory {

    /**
     * 存在这同一个类型，多个实例的情况，所以需要采取二维map进行存储
     * key: {@link Recoder}的名称
     * value 多个recoder实例，其中key采取实体类的权限类名。
     */
    private final static Map<String,
            Map<String, Recoder<Object>>> CACHE = new ConcurrentHashMap<>(16);

    private ScrewFactoriesLoader factoriesLoader = new ScrewFactoriesLoader();

    @Override
    public Map<String, Recoder<Object>> readRecords(StorageProperties properties) throws SQLException, ClassNotFoundException, IOException {
        String readPersistence = properties.getReadPersistence();
        if (StringUtils.isEmpty(readPersistence)) {
            throw new NullPointerException("logging properties `persistence` is empty, please check in properties");
        }
        return load(properties, readPersistence.split(StringPool.COMMA));
    }

    @Override
    public Map<String, Recoder<Object>> writeRecords(StorageProperties properties) throws SQLException, ClassNotFoundException, IOException {
        String writePersistence = properties.getWritePersistence();
        if (StringUtils.isEmpty(writePersistence)) {
            writePersistence = properties.getReadPersistence();
            if (StringUtils.isEmpty(writePersistence)) {
                throw new NullPointerException("logging properties `persistence` is empty, please check in properties");
            }
        }
        return load(properties, writePersistence.split(StringPool.COMMA));
    }

    @Override
    public Map<String, Map<String, Recoder<Object>>> getCache() {
        return CACHE;
    }

    /**
     * 从类路径下加载META-INF/screw.factories文件
     * @param properties 配置文件
     * @param persistenceArray recoder 实体的类型
     * @return Map的对象，其中key/value可以参考自{@link #getCache()}
     * @throws IOException 加载失败
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected Map<String, Recoder<Object>> load(StorageProperties properties, String[] persistenceArray) throws IOException, SQLException, ClassNotFoundException {
        // 如果cache为空，可以知道还没有进行扫描包
        if (Collections.isEmpty(getCache())) {
            // 扫描所有包下面的screw.factories文件
            // 根据screw.factories创建recoder实例
            Set<String> recoderClassNames = factoriesLoader.loadFactoriesAndGet(Recoder.class.getName(), Thread.currentThread().getContextClassLoader());
            for (String recoderClassName : recoderClassNames) {
                newRecoder(recoderClassName, properties);
            }
        }
        if (Collections.isEmpty(getCache())) {
            throw new NullPointerException("create recoder failed, please check in screw.factories is empty");
        }
        Map<String, Recoder<Object>> recoderList = null;
        for (String persistenceType : persistenceArray) {
            Map<String, Map<String, Recoder<Object>>> cache = getCache();
            recoderList = cache.get(persistenceType);
        }
        if (Collections.isEmpty(recoderList)) {
            throw new NullPointerException("create logger failed, can't find specific logger");
        }
        return recoderList;
    }

    @Override
    public <T> Recoder<T> newRecord0(Class<Recoder<T>> clazz, Object... args) {
        return ProxyFactory.proxy().newProxyInstance(clazz, new RecoderInterceptor(), args);
    }

    private static class RecoderInterceptor implements CglibInvocationInterceptor {

        @Override
        public Object invoke(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            // 除去cglib method
            Method peelOffMethod = ClassUtils.getMethod(Recoder.class, method.getName(), method.getParameterTypes());
            // 因为直接采取Recoder接口作为查找的Class对象，所以就会存在Method不存在的情况，针对不存在的不做任何处理，直接进行调用
            if (peelOffMethod == null) {
                return proxy.invokeSuper(object, args);
            }
            com.jw.screw.storage.AliasFor aliasFor = peelOffMethod.getAnnotation(com.jw.screw.storage.AliasFor.class);
            if (aliasFor == null) {
                return proxy.invokeSuper(object, args);
            }
            if (!(object instanceof Recoder)) {
                return null;
            }
            Class<? extends Recoder> recoderClass = ((Recoder) object).getClass();
            String alias = aliasFor.value();
            Class<?>[] classes = aliasFor.parameterTypes();
            try {
                Method aliasMethod = recoderClass.getMethod(alias, classes);
                aliasMethod.setAccessible(true);
                return aliasMethod.invoke(object, (Callable<Object>) () -> {
                    try {
                        return proxy.invokeSuper(object, args);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return null;
                });
            } catch (MethodNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * <b>通过类加载器加载各个包下面的screw.factories文件</b>
     * <p>参考自{@link org.springframework.core.io.support.SpringFactoriesLoader}</p>
     */
    private static class ScrewFactoriesLoader {

        private final static String SCREW_FACTORIES = "META-INF/screw.factories";

        private final Map<String, LinkedHashSet<String>> cache = new HashMap<>();

        /**
         * 加载factories并且获取
         * @param name {@link Recoder}名称，或者其他全限定类基类名称
         * @param classLoader 加载当前路径下的类，可能是自定义的类加载器
         * @return 加载结果，包含所有的recoder
         * @throws IOException
         */
        public Set<String> loadFactoriesAndGet(String name, ClassLoader classLoader) throws IOException {
            LinkedHashSet<String> results = cache.get(name);
            if (results == null) {
                results = new LinkedHashSet<>();
                cache.put(name, results);
            } else {
                return results;
            }
            Enumeration<URL> resources = classLoader.getResources(SCREW_FACTORIES);
            while (resources.hasMoreElements()) {
                Properties properties = new Properties();
                URL url = resources.nextElement();
                properties.load(getInputStream(url));
                String recoderClassNames = (String) properties.get(name);
                results.addAll(Arrays.asList(recoderClassNames.split(StringPool.COMMA)));
            }
            return results;
        }

        /**
         * 参考自{@link org.springframework.core.io.UrlResource#getInputStream()}
         */
        private InputStream getInputStream(URL url) throws IOException {
            URLConnection urlConnection = url.openConnection();
            try {
                return urlConnection.getInputStream();
            } catch (IOException e) {
                if (urlConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) urlConnection).disconnect();
                }
                throw new IOException(e);
            }
        }

    }
}
