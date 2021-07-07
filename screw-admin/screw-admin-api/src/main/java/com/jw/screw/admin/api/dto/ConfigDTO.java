package com.jw.screw.admin.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jw.screw.admin.api.model.ConfigModel;
import com.jw.screw.admin.api.model.DatasourceModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConfigDTO {

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务code")
    private String serverCode;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务版本")
    private String serverVersion;

    @ApiModelProperty(value = "数据源")
    @JsonProperty("dataSource")
    private List<DatasourceModel> datasourceModel;

    @ApiModelProperty(value = "配置")
    @JsonProperty("configs")
    private List<ConfigModel> configModel;

}
