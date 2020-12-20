package com.jw.screw.admin.common.id;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 使用snowflakeld算法生成id
 * @author jiangw
 * @date 2020/8/2 0:04
 * @since 1.0
 */
@Component
public class DefaultIdGenerator implements IdGenerator, Serializable {

    private static final long serialVersionUID = 3405824291434808429L;

    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public String getId() {
        return String.valueOf(getUid());
    }

    @Override
    public long getUid() {
        if (ObjectUtils.isEmpty(snowflakeIdWorker)) {
            init();
        }
        return snowflakeIdWorker.nextId();
    }

    private void init() {
        // 暂时默认为1
        snowflakeIdWorker = new SnowflakeIdWorker(1, 1);
    }
}
