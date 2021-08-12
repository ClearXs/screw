package com.jw.screw.logging.core.recoder.mapper;

import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.QueryFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 插入日志表
     * @param message
     * @return
     */
    int insert(Message message);

    List<Message> selectAll();

    /**
     *
     * @return
     */
    Message selectOneById(String id);

    List<Message> query(QueryFilter<Message> queryFilter);
}
