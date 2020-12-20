package com.jw.screw.admin.sys.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jw.screw.admin.common.model.CommonEntity;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("T_APP_SERVER")
public class AppServer extends CommonEntity implements Serializable {

    private String serverName;

    private String serverCode;

    private String serverIp;

    private Integer serverPort;

    private String serverVersion;

    private String dataSourceId;

    private String systemId;

    private String systemName;

    private Integer serverState;
}
