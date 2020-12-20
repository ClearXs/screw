package com.jw.screw.admin.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 排序列
 * @author jiangw
 * @date 2020/11/13 14:38
 * @since 1.0
 */
@Data
public class OrderColumn {

    @ApiModelProperty(name = "排序的列")
    String orderColumn;

    @ApiModelProperty(name = "排序规则，默认是asc", value = "true")
    Boolean asc = true;
}
