package com.jw.screw.admin.sys.config.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AppConfigVersionVO {

    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "配置版本号")
    private String configVersion;

    @ApiModelProperty(value = "配置版本状态")
    private String configVersionStatus;

    @ApiModelProperty(value = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "配置数据")
    @JsonProperty("appConfigData")
    private List<AppConfigDataVO> appConfigDataVO;

    @ApiModelProperty(value = "删除标识")
    private int deleted;

    @ApiModelProperty(value = "乐观锁标识")
    private int version;

    @ApiModelProperty(value = "配置id")
    private String configId;
}
