package com.jw.screw.admin.sys.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jw.screw.admin.common.model.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("T_APP_DATASOURCE")
public class Datasource extends CommonEntity implements Serializable {

    private String datasourceName;

    private String datasourceType;

    private String datasourceConnectName;

    private String datasourceIp;

    private String datasourcePort;

    private String datasourceUsername;

    private String datasourcePassword;
}
