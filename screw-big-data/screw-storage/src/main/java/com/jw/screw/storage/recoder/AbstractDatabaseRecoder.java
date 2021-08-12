package com.jw.screw.storage.recoder;

import com.jw.screw.storage.properties.StorageProperties;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;

@Recoder.Callable(name = Recoder.DATABASE)
public abstract class AbstractDatabaseRecoder<T> extends AbstractRecoder<T> {

    private SqlSessionFactory sqlSessionFactory;

    protected AbstractDatabaseRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void init(Object obj) throws IOException {
        if (obj instanceof SqlSessionFactory) {
            this.sqlSessionFactory = (SqlSessionFactory) obj;
        }
    }

    protected SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    @Override
    protected abstract DatabaseConfig getInitConfig();

    public static class DatabaseConfig {

        private final Class<?> entity;

        private final Class<?> mapper;

        public DatabaseConfig(Class<?> entity, Class<?> mapper) {
            this.entity = entity;
            this.mapper = mapper;
        }

        public Class<?> getEntity() {
            return entity;
        }

        public Class<?> getMapper() {
            return mapper;
        }
    }
}
