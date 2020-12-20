package com.jw.screw.admin.sys.config.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AppConfigDataVO {

    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "配置JSON KEY")
    private String configDataKey;

    @ApiModelProperty(value = "配置数据的类型")
    private String configDataType;

    @ApiModelProperty(value = "配置的JSON数据")
    private String configDataValue;

    @ApiModelProperty(value = "配置数据的存储状态")
    private String configDataStoreState;

    @ApiModelProperty(value = "配置的版本ID")
    private String configVersionId;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "删除标识")
    private int deleted;

    @ApiModelProperty(value = "乐观锁标识")
    private int version;

    @ApiModelProperty(value = "配置备注")
    private String remark;
}
