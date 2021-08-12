package com.jw.screw.storage.initialize;

import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

/**
 * 不同数据源初始化接口
 * @author jiangw
 * @date 2021/7/22 17:33
 * @since 1.1
 */
public interface RecordInitializer<T> {

    /**
     * {@link Recoder}初始化时进行调用
     * @param properties 数据源的配置信息
     * @param config 不同数据源的配置属性
     * @return 需要的数据原对象，比如 datasource需要{@link org.apache.ibatis.session.SqlSessionFactory} hive需要{@link com.jw.screw.storage.hive.session.HqlSessionFactory}
     */
    T init(StorageProperties properties, Object config);
}
