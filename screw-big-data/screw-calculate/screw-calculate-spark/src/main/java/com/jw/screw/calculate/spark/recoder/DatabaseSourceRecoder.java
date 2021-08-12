package com.jw.screw.calculate.spark.recoder;

import com.jw.screw.calculate.spark.model.SourceStatistics;
import com.jw.screw.calculate.spark.recoder.mapper.SourceStatisticsMapper;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractDatabaseRecoder;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class DatabaseSourceRecoder extends AbstractDatabaseRecoder<SourceStatistics> {

    public DatabaseSourceRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected DatabaseConfig getInitConfig() {
        return new DatabaseConfig(SourceStatistics.class, SourceStatisticsMapper.class);
    }

    @Override
    public void record(SourceStatistics message) throws Exception {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        try {
            SourceStatisticsMapper mapper = sqlSession.getMapper(SourceStatisticsMapper.class);
            int result = mapper.insert(message);
            if (result < 1) {
                throw new Exception();
            }
        } catch (Exception e) {
            sqlSession.rollback(true);
            throw new Exception(e);
        }
        sqlSession.commit(true);
        sqlSession.close();
    }

    @Override
    public SourceStatistics getMessage(String id) {
        return null;
    }

    @Override
    public List<SourceStatistics> getAll() {
        return null;
    }

    @Override
    public List<SourceStatistics> query(QueryFilter<SourceStatistics> queryFilter) {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        SourceStatisticsMapper mapper = sqlSession.getMapper(SourceStatisticsMapper.class);
        try {
            return mapper.query(queryFilter);
        } finally {
            sqlSession.close();
        }
    }
}
