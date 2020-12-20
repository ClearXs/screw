package com.jw.screw.admin.sys.config.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jw.screw.admin.common.validate.Exist;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppConfigDataUpdateDTO {

    @ApiModelProperty(value = "ID")
    @Exist(id = true)
    private String id;

    @ApiModelProperty(value = "配置KEY")
    @NotEmpty(message = "配置KEY不能为空")
    @Exist(unique = "CONFIG_DATA_KEY", errorMessage = "配置KEY重复")
    private String configDataKey;

    @ApiModelProperty(value = "配置数据的类型")
    @NotEmpty(message = "配置数据的类型不能为空")
    private String configDataType;

    @ApiModelProperty(value = "配置的数据")
    @NotEmpty(message = "配置的数据不能为空")
    private String configDataValue;

    @ApiModelProperty(value = "配置数据的存储状态")
    @NotEmpty(message = "配置数据的存储状态不能为空")
    private String configDataStoreState;

    @ApiModelProperty(value = "配置的版本ID")
    @NotEmpty(message = "配置的版本ID不能为空")
    @Exist(extension = true)
    private String configVersionId;

    @ApiModelProperty(value = "配置数据的版本")
    @NotEmpty(message = "配置数据的版本不能为空")
    @JsonProperty("configVersion")
    private AppConfigVersionVO appConfigVersionVO;

    @ApiModelProperty(value = "配置备注")
    private String remark;

    @ApiModelProperty(value = "乐观锁")
    private int version;

}
