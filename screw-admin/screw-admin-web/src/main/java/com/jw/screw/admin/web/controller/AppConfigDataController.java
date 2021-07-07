package com.jw.screw.admin.web.controller;

import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataAddDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataQueryDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigDataVO;
import com.jw.screw.admin.sys.config.service.AppConfigDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/appConfig/data")
@Api(value = "/appConfig/data", tags = "应用配置数据管理")
public class AppConfigDataController extends BaseController {

    @Resource
    private AppConfigDataService appConfigDataService;

    @ApiOperation(value = "新增配置数据")
    @PostMapping(value = "/addConfigData")
    public MsgResponse<Integer> addConfigData(@RequestBody @Valid List<AppConfigDataAddDTO> appConfigDataAddDTO) {
        MsgResponse<Integer> response;
        try {
            if (CollectionUtils.isEmpty(appConfigDataAddDTO)) {
                return getSuccessResponse("", DataOperationState.SUCCESSFUL);
            }
            response = handleBasicOperationResponse("新增配置数据成功", appConfigDataService.addAppConfigData(appConfigDataAddDTO));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "查询配置数据")
    @GetMapping(value = "/queryConfigData")
    public MsgResponse<PageResult<AppConfigDataVO>> queryConfigData(AppConfigDataQueryDTO appConfigDataQueryDTO) {
        MsgResponse<PageResult<AppConfigDataVO>> response;
        try {
            PageResult<AppConfigDataVO> result = appConfigDataService.queryAppConfigData(appConfigDataQueryDTO);
            response = getSuccessResponse("查询配置数据成功", result);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "更新配置数据")
    @PutMapping(value = "/updateConfigData")
    public MsgResponse<Integer> updateConfigData(@RequestBody @Valid List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("更新配置数据成功", appConfigDataService.updateAppConfigData(appConfigDataUpdateDTO));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "保存配置文件")
    @PostMapping(value = "/saveConfigData")
    public MsgResponse<Integer> saveConfigData(@RequestBody List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO,
                                               @RequestParam String logicOperate) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("更新配置数据成功", appConfigDataService.saveAppConfigData(appConfigDataUpdateDTO, logicOperate));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }

    @ApiOperation(value = "删除配置数据")
    @DeleteMapping(value = "/deleteConfigData")
    public MsgResponse<Integer> deleteConfigData(@RequestBody List<AppConfigDataVO> appConfigData) {
        MsgResponse<Integer> response;
        try {
            response = handleBasicOperationResponse("删除配置数据成功", appConfigDataService.removeAppConfigData(appConfigData));
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }
}
