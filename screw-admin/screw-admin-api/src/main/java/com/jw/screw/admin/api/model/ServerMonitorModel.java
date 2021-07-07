package com.jw.screw.admin.api.model;

import com.zzht.patrol.monitor.core.mircometer.Metrics;
import com.zzht.patrol.screw.common.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 服务监控数据
 * @author jiangw
 * @date 2020/12/28 11:19
 * @since 1.0
 */
@Data
public class ServerMonitorModel {

    @ApiModelProperty(value = "提供者服务key")
    private String providerKey;

    @ApiModelProperty(value = "提供者角色")
    private String providerRole;

    @ApiModelProperty(value = "消费者服务key")
    private String consumerKey;

    @ApiModelProperty(value = "消费者角色")
    private String consumerRole;

    @ApiModelProperty(value = "服务ip")
    private String host;

    @ApiModelProperty(value = "服务端口")
    private Integer port;

    @ApiModelProperty(value = "服务健康状况")
    private String health;

    @ApiModelProperty(value = "最后一次更新时间")
    private String lastUpdateTime;

    @ApiModelProperty(value = "服务性能指标")
    private Map<String, List<Metrics>> metrics;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerMonitorModel that = (ServerMonitorModel) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port);
    }

    public boolean equals(String host, Integer port) {
        if (StringUtils.isEmpty(host)) {
            return false;
        }
        if (port == null) {
            return false;
        }
        return Objects.equals(this.host, host) && Objects.equals(this.port,port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
