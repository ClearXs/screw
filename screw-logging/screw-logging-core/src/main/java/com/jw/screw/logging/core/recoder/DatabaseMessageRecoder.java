package com.jw.screw.logging.core.recoder;

import com.jw.screw.logging.core.constant.TransferType;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.logging.core.recoder.mapper.MessageMapper;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractDatabaseRecoder;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * @author jiangw
 * @date 2021/7/22 10:26
 * @since 1.1
 */
public class DatabaseMessageRecoder extends AbstractDatabaseRecoder<Message> {

    public DatabaseMessageRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void failureMessageHandle(Message message) throws Exception {
        super.failureMessageHandle(message);
    }

    @Override
    protected DatabaseConfig getInitConfig() {
        return new DatabaseConfig(Message.class, MessageMapper.class);
    }

    @Override
    public void record(Message message) throws Exception {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        try {
            MessageMapper mapper = sqlSession.getMapper(MessageMapper.class);
            message.setTransferType(TransferType.DB);
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
    public Message getMessage(String id) {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        MessageMapper mapper = sqlSession.getMapper(MessageMapper.class);
        try {
            return mapper.selectOneById(id);
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<Message> getAll() {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        MessageMapper mapper = sqlSession.getMapper(MessageMapper.class);
        try {
            return mapper.selectAll();
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<Message> query(QueryFilter<Message> queryFilter) {
        SqlSession sqlSession = getSqlSessionFactory().openSession();
        MessageMapper mapper = sqlSession.getMapper(MessageMapper.class);
        try {
            return mapper.query(queryFilter);
        } finally {
            sqlSession.close();
        }
    }
}
