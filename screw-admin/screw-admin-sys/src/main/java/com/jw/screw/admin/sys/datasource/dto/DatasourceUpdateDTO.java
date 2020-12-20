package com.jw.screw.admin.sys.datasource.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DatasourceUpdateDTO {

    @ApiModelProperty(value = "ID")
    @NotEmpty(message = "id不能为空")
    private String id;

    @ApiModelProperty(value = "数据源名称")
    @NotEmpty(message = "数据源名称不能为空")
    private String datasourceName;

    @ApiModelProperty(value = "数据源类型")
    @NotEmpty(message = "数据源类型不能为空")
    private String datasourceType;

    @ApiModelProperty(value = "数据源连接名称")
    @NotEmpty(message = "数据源连接名称不能为空")
    private String datasourceConnectName;

    @ApiModelProperty(value = "数据源IP")
    @NotEmpty(message = "数据源IP不能为空")
    private String datasourceIp;

    @ApiModelProperty(value = "数据源端口")
    @NotEmpty(message = "数据源端口不能为空")
    private String datasourcePort;

    @ApiModelProperty(value = "数据源用户名")
    @NotEmpty(message = "数据源用户名不能为空")
    private String datasourceUsername;

    @ApiModelProperty(value = "数据源密码")
    @NotEmpty(message = "数据源密码不能为空")
    private String datasourcePassword;

    @ApiModelProperty(value = "乐观锁")
    private int version;

}
