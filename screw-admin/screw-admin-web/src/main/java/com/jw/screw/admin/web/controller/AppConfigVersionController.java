package com.jw.screw.admin.web.controller;

import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionAddDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionQueryDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import com.jw.screw.admin.sys.config.service.AppConfigVersionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/appConfig/version")
@Api(value = "/appConfig/version", tags = "应用配置版本管理")
public class AppConfigVersionController extends BaseController {

    @Resource
    private AppConfigVersionService appConfigVersionService;

    @ApiOperation(value = "新增配置版本")
    @PostMapping(value = "/addConfigVersion")
    public MsgResponse<Integer> addConfigVersion(@RequestBody @Valid AppConfigVersionAddDTO appConfigVersionAddDTO) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("新增配置版本成功", appConfigVersionService.addAppConfigVersion(appConfigVersionAddDTO));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "查询配置版本")
    @GetMapping(value = "/queryConfigVersions")
    public MsgResponse<PageResult<AppConfigVersionVO>> queryConfigVersions(AppConfigVersionQueryDTO appConfigVersionQueryDTO) {
        MsgResponse<PageResult<AppConfigVersionVO>> response;
        try {
            PageResult<AppConfigVersionVO> result = appConfigVersionService.queryAppConfigVersion(appConfigVersionQueryDTO);
            response = getSuccessResponse("查询配置版本成功", result);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "更新配置版本")
    @PostMapping(value = "/openedVersion")
    public MsgResponse<Integer> updateConfigVersion(@RequestBody @Valid AppConfigVersionUpdateDTO appConfigVersionUpdateDTO) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("更新配置版本成功", appConfigVersionService.openedVersion(appConfigVersionUpdateDTO));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "删除配置版本")
    @DeleteMapping(value = "/deleteConfigVersion")
    public MsgResponse<Integer> deleteConfigVersion(String id) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("删除配置版本成功", appConfigVersionService.removeAppConfigVersion(id));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }
}
