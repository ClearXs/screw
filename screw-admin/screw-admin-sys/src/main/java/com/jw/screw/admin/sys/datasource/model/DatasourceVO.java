package com.jw.screw.admin.sys.datasource.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class DatasourceVO {

    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "数据源名称")
    private String datasourceName;

    @ApiModelProperty(value = "数据源类型")
    private String datasourceType;

    @ApiModelProperty(value = "数据源连接名称")
    private String datasourceConnectName;

    @ApiModelProperty(value = "数据源IP")
    private String datasourceIp;

    @ApiModelProperty(value = "数据源端口")
    private String datasourcePort;

    @ApiModelProperty(value = "数据源用户名")
    private String datasourceUsername;

    @ApiModelProperty(value = "数据源密码")
    private String datasourcePassword;

    @ApiModelProperty(value = "数据源连接类型", example = "default, druid")
    @NotEmpty(message = "数据源连接类型不能为空")
    private String datasourceConnectType;

    @ApiModelProperty(value = "数据源连接变量", example = "json")
    private String datasourceConnectVariables;

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

    @ApiModelProperty(value = "乐观锁")
    private int version;
}
