package com.jw.screw.storage.recoder;

import com.jw.screw.storage.AliasFor;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.initialize.RecordInitializer;

import java.lang.annotation.*;
import java.util.List;

/**
 * <b>记录器，通用于记录到任何存储模板中，如ES、Database、Redis、Hive等</b>
 * <p>
 *     <b>Recoder生命周期：</b>
 *     <p>
 *         初始化{@link AbstractRecoder#init(Object)}：准备进行实例化一些session、如sqlSession、hqlSession、
 *         或者其他存储层需要进行的一些操作。如es进行判断索引是否存储
 *         它的过程是{@link RecordInitializer}进行定义
 *     </p>
 *     <p>
 *         执行过程：每一个recoder执行的，采取线程池的方法进行执行，考虑IO较为密集，所以核心池大小选取用{@code Runtime.getRuntime().availableProcessors()}，最大线程
 *         为{@code (2 * Runtime.getRuntime().availableProcessors()) + 1}
 *         当执行失败的任务采用Dead线程池执行任务回调{@link AbstractRecoder#failureMessageHandle}进行错误处理，
 *         这部分思想参考自rabbitMQ死信队列，它针对那些过期、没有返回ACK的消息放入死信队列进行处理，当前我只考虑发生异常的数据进行回调
 *     </p>
 *     <p>
 *         死亡：当程序关闭时，通过{@code Runtime.getRuntime().addShutdownHook()}JVM提供的关闭钩子，回调{@link #shutdownCallback}，进行一些正确释放资源的操作。
 *     </p>
 * </p>
 * <p>
 *      <b>独立性</b>
 *      每一个recoder针对一个实体，如果不同实体需要创建不同recoder进行相应的CURD处理
 * </p>
 * <p>
 *     <b>多样性</b>
 *     <p>针对每一个存储类型都做了相应的抽象实现，如：Database的{@link AbstractDatabaseRecoder}，es的{@link AbstractESRecoder}</p>
 *     <p>每一个存储类型都需要继承对应Recoder</p>
 *     <p>如：{@code public class DatabaseSourceRecoder extends AbstractDatabaseRecoder<SourceStatistics>}</p>
 * </p>
 * <p>
 *     <b>灵活配置</b>
 *     通过{@link com.jw.screw.storage.properties.StorageProperties}进行配置
 * </p>
 * <p>
 *      <b>自动扫包</b>
 *      <p>screw storage支持 spring.factories相应的机制，通过在META-INF下创建screw.factories文件，就可以自动引入screw-storage容器中</p>
 *      <p>
 *          示例：
 *          com.jw.screw.storage.recoder.Recoder=\
 *          com.jw.screw.logging.core.recoder.DatabaseMessageRecoder,\
 *      </p>
 * </p>
 * @author jiangw
 * @date 2021/7/2 9:59
 * @since 1.1
 */
public interface Recoder<T> {

    /**
     * 内存
     */
    String MEMORY = "memory";

    /**
     * 数据库
     */
    String DATABASE = "db";

    /**
     * redis
     */
    String REDIS = "redis";

    /**
     * es
     */
    String ELASTICSEARCH = "es";

    /**
     * 文件
     */
    String FILE = "file";

    /**
     * 数据仓库
     */
    String HIVE = "hive";

    Object execute(java.util.concurrent.Callable<Object> callable);

    /**
     * 具体记录方法，记录是一个异步的操作
     * @param message
     */
    @AliasFor(value = "execute", parameterTypes = java.util.concurrent.Callable.class)
    void record(T message) throws Exception;

    /**
     * 获取日志
     * @param id
     * @return
     */
    @AliasFor(value = "execute", parameterTypes = java.util.concurrent.Callable.class)
    T getMessage(String id);

    /**
     * 获取所有日志
     * @return
     */
    @AliasFor(value = "execute", parameterTypes = java.util.concurrent.Callable.class)
    List<T> getAll();

    /**
     * 查询过滤器，包含分页。还未写全
     * @param queryFilter 默认采用{@link com.jw.screw.storage.DefaultQueryFilter}
     */
    @AliasFor(value = "execute", parameterTypes = java.util.concurrent.Callable.class)
    List<T> query(QueryFilter<T> queryFilter);

    /**
     * <b>提供程序关闭时记录回调</b>
     * <p>避免每个记录器操作时，比如累计数据量达到阈值才进行记录等优化性能操作导致数据不一致的现象</p>
     */
    default void shutdownCallback() {

    }

    /**
     * 表示每个recoder类型
     */
    @Inherited
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Callable {

        String name() default "";
    }
}
