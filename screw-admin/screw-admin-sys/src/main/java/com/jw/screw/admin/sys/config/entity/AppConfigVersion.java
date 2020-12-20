package com.jw.screw.admin.sys.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jw.screw.admin.common.model.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("T_APP_CONFIG_VERSION")
public class AppConfigVersion extends CommonEntity implements Serializable {

    private String configVersion;

    private String configVersionStatus;

    private String configId;

}
