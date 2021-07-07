package com.jw.screw.admin.api.controller;

import com.jw.screw.admin.api.model.ServerMonitorModel;
import com.jw.screw.admin.api.model.ServerMonitorQueryDTO;
import com.jw.screw.admin.api.service.RemoteMonitorService;
import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.zzht.patrol.screw.monitor.remote.TracingModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 监控中心接口
 * @author jiangw
 * @date 2020/12/28 13:34
 * @since 1.0
 */
@RestController
@RequestMapping("/api/webapi/monitor")
@Api(value = "/api/webapi/monitor", tags = "监控中心接口")
public class MonitorController extends BaseController {

    @Autowired
    private RemoteMonitorService monitorService;

    @ApiOperation(value = "获取监控中心列表")
    @GetMapping("/getMonitorList")
    public MsgResponse<Set<ServerMonitorModel>> getMonitorList(ServerMonitorQueryDTO serverQuery) {
        MsgResponse<Set<ServerMonitorModel>> response;
        try {
            Set<ServerMonitorModel> serverMonitorList = monitorService.getServerMonitorList(serverQuery.getServerKey());
            response = getSuccessResponse("", serverMonitorList);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "获取激活服务性能指标")
    @GetMapping("/getActiveServerMetrics")
    public MsgResponse<ServerMonitorModel> getActiveServerMetrics(ServerMonitorQueryDTO serverQuery) {
        MsgResponse<ServerMonitorModel> response;
        try {
            ServerMonitorModel serverMonitorMetrics = monitorService.getServerMonitorMetrics(serverQuery);
            response = getSuccessResponse("", serverMonitorMetrics);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "获取服务链路追踪")
    @GetMapping("getServerTracing")
    public MsgResponse<TracingModel> getServerTracing(ServerMonitorQueryDTO serverQuery) {
        MsgResponse<TracingModel> response;
        try {
            TracingModel serverTracing = monitorService.getServerTracing(serverQuery);
            response = getSuccessResponse("", serverTracing);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }
}
