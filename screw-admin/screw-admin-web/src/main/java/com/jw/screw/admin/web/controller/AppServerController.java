package com.jw.screw.admin.web.controller;

import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.sys.server.dto.AppServerAddDTO;
import com.jw.screw.admin.sys.server.dto.AppServerUpdateDTO;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import com.jw.screw.admin.sys.server.service.AppServerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appServer")
@Api(value = "/appServer", tags = "应用服务管理")
public class AppServerController extends BaseController {

    @Resource
    private AppServerService appServerService;

    @ApiOperation(value = "判断服务数据是否可以创建")
    @PostMapping(value = "/isExist")
    public MsgResponse<Integer> isExist(@RequestBody @Valid AppServerAddDTO addDTO){
        MsgResponse<Integer> result;
        try {
            result = handleBasicOperationResponse("服务数据可以创建", appServerService.isExistServer(addDTO));
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "新增应用服务")
    @PostMapping(value = "/addAppServer")
    public MsgResponse<Integer> addAppServer(@RequestBody @Valid AppServerAddDTO addDTO){
        MsgResponse<Integer> result;
        try {
            result = handleBasicOperationResponse("新增应用服务成功", appServerService.addAppServer(addDTO));
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "查询应用服务")
    @GetMapping(value = "/queryAppServer")
    public MsgResponse<List<AppServerVO>> queryAppServer(){
        MsgResponse<List<AppServerVO>> result;
        try {
            List<AppServerVO> list = appServerService.queryAppServers();
            result = getSuccessResponse("查询应用服务成功", list);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "修改应用服务")
    @PutMapping(value = "/editAppServer")
    public MsgResponse<Integer> editAppServer(@RequestBody @Valid AppServerUpdateDTO updateDTO){
        MsgResponse<Integer> result;
        try {
            result = handleBasicOperationResponse("更新应用服务成功", appServerService.updateAppServer(updateDTO));
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "删除应用服务")
    @DeleteMapping(value = "/deleteAppServer")
    public MsgResponse<Integer> deleteAppServer(String serverId){
        MsgResponse<Integer> result;
        try {
            result = handleBasicOperationResponse("删除应用服务成功", appServerService.deleteAppServerById(serverId));
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "查询服务目录结构")
    @GetMapping(value = "/queryServerDirectory")
    public MsgResponse<Map<String, List<AppServerVO>>> queryServerDirectory(String operate){
        MsgResponse<Map<String, List<AppServerVO>>> result;
        try {
            Map<String, List<AppServerVO>> directory = appServerService.getServerDirectory(operate);
            result = getSuccessResponse("查询成功", directory);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

}
