package com.jw.screw.admin.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServerMonitorQueryDTO {

    @ApiModelProperty(value = "服务key")
    private String serverKey;

    @ApiModelProperty(value = "服务host")
    private String serverHost;

    @ApiModelProperty(value = "服务port")
    private Integer serverPort;
}
