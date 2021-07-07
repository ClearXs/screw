package com.jw.screw.admin.web.controller;

import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.AppConfigAddDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigQueryDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/appConfig")
@Api(value = "/appConfig", tags = "应用配置管理")
public class AppConfigController extends BaseController {

    @Resource
    private AppConfigService appConfigService;

    @ApiOperation(value = "新增配置")
    @PostMapping(value = "/addConfig")
    public MsgResponse<Integer> addConfig(@RequestBody @Valid AppConfigAddDTO appConfig) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("新增配置成功", appConfigService.addAppConfig(appConfig));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "查询配置根据分页")
    @GetMapping(value = "/queryConfig")
    public MsgResponse<PageResult<AppConfigVO>> queryAllConfig(AppConfigQueryDTO queryDTO) {
        MsgResponse<PageResult<AppConfigVO>> response;
        try {
            PageResult<AppConfigVO> result = appConfigService.queryAppConfigs(queryDTO);
            response = getSuccessResponse("查询配置成功", result);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "更新配置")
    @PutMapping(value = "/updateConfig")
    public MsgResponse<Integer> updateConfig(@RequestBody @Valid AppConfigUpdateDTO appConfig) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("更新配置成功", appConfigService.updateAppConfig(appConfig));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "删除配置")
    @DeleteMapping(value = "/deleteConfig")
    public MsgResponse<Integer> deleteConfig(String configIds) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("删除配置成功", appConfigService.deleteAppConfig(configIds));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

}
