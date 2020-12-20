package com.jw.screw.admin.sys.server.dto;

import com.jw.screw.admin.common.validate.Exist;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AppServerUpdateDTO {

    @ApiModelProperty(value = "ID")
    @NotEmpty(message = "id不能为空")
    @Exist(id = true)
    private String id;

    @ApiModelProperty(value = "服务名称")
    @NotEmpty(message = "名称不能位空")
    @Exist(unique = "SERVER_NAME", errorMessage = "名称重复")
    private String serverName;

    @ApiModelProperty(value = "服务code")
    private String serverCode;

    @ApiModelProperty(value = "服务ip")
    @NotEmpty(message = "服务ip不能为空")
    private String serverIp;

    @ApiModelProperty(value = "服务端口")
    @NotNull(message = "服务端口不能为空")
    private Integer serverPort;

    @ApiModelProperty(value = "服务版本")
    private String serverVersion;

    @ApiModelProperty(value = "数据源ID")
    private String dataSourceId;

    @ApiModelProperty(value = "服务状态")
    private Integer serverState = 0;

    @ApiModelProperty(value = "乐观锁标识")
    private int version;
}
