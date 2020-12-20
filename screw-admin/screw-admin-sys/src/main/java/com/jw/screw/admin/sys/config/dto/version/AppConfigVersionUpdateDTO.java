package com.jw.screw.admin.sys.config.dto.version;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppConfigVersionUpdateDTO {

    @ApiModelProperty(value = "ID")
    @NotEmpty(message = "id不能为空")
    private String id;

    @ApiModelProperty(value = "配置版本号")
    private String configVersion;

    @ApiModelProperty(value = "配置版本状态")
    @NotEmpty(message = "配置版本状态不能为空")
    private String configVersionStatus;

    @ApiModelProperty(value = "配置id")
    @NotEmpty(message = "配置的id不能为空")
    private String configId;

    @ApiModelProperty(value = "乐观锁")
    private int version;
}
