package com.jw.screw.storage.hive.session;

import com.jw.screw.storage.properties.HiveProperties;

import java.sql.SQLException;

public class HqlSessionFactory {

    public static HqlSession hqlSession(HiveProperties hiveProperties) throws SQLException, ClassNotFoundException {
        return new DefaultHqlSession(hiveProperties);
    }
}
