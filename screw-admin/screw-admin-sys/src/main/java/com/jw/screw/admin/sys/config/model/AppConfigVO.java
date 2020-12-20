package com.jw.screw.admin.sys.config.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AppConfigVO {

    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "配置key")
    private String configKey;

    @ApiModelProperty(value = "配置JSON数据")
    private String configJson;

    @ApiModelProperty(value = "角色ID")
    private String roleId;

    @ApiModelProperty(value = "服务ID")
    private String serverId;

    @ApiModelProperty(value = "配置版本信息")
    private String configVersionId;

    @ApiModelProperty(value = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "删除标识")
    private int deleted;

    @ApiModelProperty(value = "乐观锁标识")
    private int version;

    @ApiModelProperty(value = "启用的配置版本")
    @JsonProperty("appConfigVersion")
    private AppConfigVersionVO appConfigVersionVO;

    @ApiModelProperty(value = "配置服务")
    @JsonProperty("appServer")
    private AppServerVO appServerVO;

}
