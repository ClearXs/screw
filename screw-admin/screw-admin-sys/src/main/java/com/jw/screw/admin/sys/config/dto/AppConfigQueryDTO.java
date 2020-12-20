package com.jw.screw.admin.sys.config.dto;

import com.jw.screw.admin.common.model.PageParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置查询
 * @author jiangw
 * @date 2020/11/13 14:44
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppConfigQueryDTO extends PageParams {

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "服务id")
    private String serverId;

}
