package com.jw.screw.admin.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ConfigModel {

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "配置key")
    private String configKey;

    @ApiModelProperty(value = "配置JSON数据")
    @JsonProperty("json")
    private String configJson;

    @ApiModelProperty(value = "配置JSON数据")
    private Map<String, Object> jsonObject;
}
