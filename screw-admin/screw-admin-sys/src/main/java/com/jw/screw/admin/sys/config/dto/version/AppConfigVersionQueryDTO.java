package com.jw.screw.admin.sys.config.dto.version;

import com.jw.screw.admin.common.model.PageParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppConfigVersionQueryDTO extends PageParams {

    @ApiModelProperty(value = "配置版本号")
    private String configVersion;

    @ApiModelProperty(value = "配置版本状态")
    private String configVersionStatus;

    @ApiModelProperty(value = "配置id")
    @NotNull(message = "配置的id不能为空")
    private String configId;
}