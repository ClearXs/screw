package com.jw.screw.admin.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 分页参数
 * @author jiangw
 * @date 2020/11/13 14:40
 * @since 1.0
 */
@Data
public class PageParams {

    @ApiModelProperty(value = "页码(不能为空)", example = "1")
    @NotNull(message = "分页参数不能为空")
    protected Integer pageNum;

    @ApiModelProperty(value = "每页数量(不能为空)", example = "10")
    @NotNull(message = "每页数量不能为空")
    @Max(value = 500, message = "每页最大为500")
    protected Integer pageSize;

    @ApiModelProperty("是否查询总条数")
    protected Boolean searchCount;

    @ApiModelProperty("创建时间")
    protected String[] createTime;

    @ApiModelProperty("更新时间")
    protected String[] updateTime;

    @ApiModelProperty("排序")
    protected List<OrderColumn> orders;
}
