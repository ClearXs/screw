package com.jw.screw.admin.sys.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jw.screw.admin.common.model.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 配置表
 * @author jiangw
 * @date 2020/11/13 13:54
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("T_APP_CONFIG")
public class AppConfig extends CommonEntity implements Serializable {

    private String configName;

    private String configKey;

    private String configJson;

    private String roleId;

    private String serverId;

    private String configVersionId;
}
