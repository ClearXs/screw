package com.jw.screw.admin.sys.server.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AppServerVO {

    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务code")
    private String serverCode;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务版本")
    private String serverVersion;

    @ApiModelProperty(value = "数据源ID")
    private String dataSourceId;

    @ApiModelProperty(value = "服务状态")
    private Integer serverState;

    @ApiModelProperty(value = "系统ID")
    private String systemId;

    @ApiModelProperty(value = "系统名称")
    private String systemName;

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

    @ApiModelProperty(value = "数据源")
    @JsonProperty("datasource")
    private List<DatasourceVO> datasourceVO;

    @ApiModelProperty(value = "配置")
    @JsonProperty("appConfigs")
    private List<AppConfigVO> appConfigVO;

}
