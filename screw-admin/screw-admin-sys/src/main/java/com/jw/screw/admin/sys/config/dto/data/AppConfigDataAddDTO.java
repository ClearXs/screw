package com.jw.screw.admin.sys.config.dto.data;

import com.jw.screw.admin.common.validate.Exist;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppConfigDataAddDTO {

    @ApiModelProperty(value = "配置KEY")
    @NotEmpty(message = "配置KEY不能为空")
    @Exist(unique = "CONFIG_DATA_KEY", errorMessage = "${value}配置KEY重复")
    private String configDataKey;

    @ApiModelProperty(value = "配置数据的类型")
    @NotEmpty(message = "配置数据的类型不能为空")
    private String configDataType;

    @ApiModelProperty(value = "配置数据")
    @NotEmpty(message = "配置数据不能为空")
    private String configDataValue;

    @ApiModelProperty(value = "配置数据的存储状态")
    @NotEmpty(message = "配置数据的存储状态不能为空")
    private String configDataStoreState;

    @ApiModelProperty(value = "配置的版本ID")
    @NotEmpty(message = "配置的版本ID不能为空")
    @Exist(extension = true)
    private String configVersionId;

    @ApiModelProperty(value = "配置备注")
    private String remark;

}
