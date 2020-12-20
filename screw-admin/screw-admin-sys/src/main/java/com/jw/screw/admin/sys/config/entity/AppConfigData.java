package com.jw.screw.admin.sys.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jw.screw.admin.common.model.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("T_APP_CONFIG_DATA")
public class AppConfigData extends CommonEntity implements Serializable {

    private String configDataKey;

    private String configDataType;

    private String configDataValue;

    private String configDataStoreState;

    private String configVersionId;

    private String remark;
}
