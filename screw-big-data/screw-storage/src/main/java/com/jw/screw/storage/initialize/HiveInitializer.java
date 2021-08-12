package com.jw.screw.storage.initialize;

import com.jw.screw.storage.hive.session.HqlSession;
import com.jw.screw.storage.hive.session.HqlSessionFactory;
import com.jw.screw.storage.properties.HiveProperties;
import com.jw.screw.storage.properties.StorageProperties;

import java.sql.SQLException;

/**
 * hive初始化
 * @author jiangw
 * @date 2021/7/22 17:32
 * @since 1.1
 */
class HiveInitializer implements RecordInitializer<HqlSession> {

    @Override
    public HqlSession init(StorageProperties properties, Object config) {
        HiveProperties hiveProperties = properties.getHive();
        try {
            return HqlSessionFactory.hqlSession(hiveProperties);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
