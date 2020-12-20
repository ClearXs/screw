package com.jw.screw.admin.sys.server.dto;

import com.jw.screw.admin.common.validate.Exist;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AppServerAddDTO {

    @ApiModelProperty(value = "服务名称")
    @NotEmpty(message = "名称不能位空")
    @Exist(unique = "SERVER_NAME", errorMessage = "名称重复")
    private String serverName;

    @ApiModelProperty(value = "服务ip")
    @NotEmpty(message = "ip不能位空")
    private String serverIp;

    @ApiModelProperty(value = "服务端口")
    @NotNull(message = "端口不能位空")
    private Integer serverPort;

    @ApiModelProperty(value = "服务版本")
    @NotEmpty(message = "服务版本不能为空")
    private String serverVersion;

    @ApiModelProperty(value = "数据源ID")
    private String dataSourceId;

    @ApiModelProperty(value = "服务状态")
    private Integer serverState = 0;

    @ApiModelProperty(value = "系统/项目id")
    private String systemId;

    @ApiModelProperty(value = "系统/项目名称")
    private String systemName;

    @ApiModelProperty(value = "配置")
    private List<AppConfigVO> appConfig;

}
