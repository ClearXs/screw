package com.jw.screw.admin.sys.config.dto.data;

import com.jw.screw.admin.common.model.PageParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppConfigDataQueryDTO extends PageParams {

    @ApiModelProperty(value = "配置数据的类型")
    private String configDataType;

    @ApiModelProperty(value = "配置数据的存储状态")
    private String configDataStoreState;

    @ApiModelProperty(value = "版本ID")
    @NotNull(message = "版本ID不能为空")
    private String configVersionId;
}
