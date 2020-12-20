package com.jw.screw.admin.sys.config.dto;

import com.jw.screw.admin.common.validate.Exist;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppConfigAddDTO {

    @ApiModelProperty(value = "配置名称")
    @NotEmpty(message = "配置名称不能为空")
    @Exist(unique = "CONFIG_NAME", errorMessage = "配置名称重复")
    private String configName;

    @ApiModelProperty(value = "配置分类key")
    @NotEmpty(message = "配置分类key不能为空")
    @Exist(unique = "CONFIG_KEY", errorMessage = "配置key重复")
    private String configKey;

    @ApiModelProperty(value = "服务ID")
    @NotEmpty(message = "服务id不能为空")
    @Exist(extension = true)
    private String serverId;

}
