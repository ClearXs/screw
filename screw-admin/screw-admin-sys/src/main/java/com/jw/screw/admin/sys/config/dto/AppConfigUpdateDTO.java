package com.jw.screw.admin.sys.config.dto;

import com.jw.screw.admin.common.validate.Exist;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppConfigUpdateDTO {

    @ApiModelProperty(value = "ID")
    @NotEmpty(message = "id不能为空")
    private String id;

    @ApiModelProperty(value = "配置名称")
    @NotEmpty(message = "配置名称不能为空")
    private String configName;

    @ApiModelProperty(value = "配置分类key")
    @NotEmpty(message = "配置分类key不能为空")
    private String configKey;

    @ApiModelProperty(value = "配置JSON数据")
    private String configJson;

    @ApiModelProperty(value = "服务ID")
    @NotEmpty(message = "服务id不能为空")
    @Exist(extension = true)
    private String serverId;

    @ApiModelProperty(value = "配置版本信息")
    private String configVersionId;

    @ApiModelProperty(value = "乐观锁")
    private int version;

}
