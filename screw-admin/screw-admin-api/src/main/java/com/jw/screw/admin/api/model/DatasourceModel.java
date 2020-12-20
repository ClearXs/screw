package com.jw.screw.admin.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasourceModel {

    @ApiModelProperty(value = "数据源名称")
    private String datasourceName;

    @ApiModelProperty(value = "数据源类型")
    private String datasourceType;

    @ApiModelProperty(value = "数据源连接名称")
    private String datasourceConnectName;

    @ApiModelProperty(value = "数据源IP")
    private String datasourceIp;

    @ApiModelProperty(value = "数据源端口")
    private String datasourcePort;

    @ApiModelProperty(value = "数据源用户名")
    private String datasourceUsername;

    @ApiModelProperty(value = "数据源密码")
    private String datasourcePassword;
}
