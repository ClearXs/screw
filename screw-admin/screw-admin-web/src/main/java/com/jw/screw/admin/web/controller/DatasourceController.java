package com.jw.screw.admin.web.controller;

import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.sys.datasource.dto.DatasourceAddDTO;
import com.jw.screw.admin.sys.datasource.dto.DatasourceDTO;
import com.jw.screw.admin.sys.datasource.dto.DatasourceUpdateDTO;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import com.jw.screw.admin.sys.datasource.service.DatasourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/datasource")
@Api(value = "/database", tags = "数据库管理")
public class DatasourceController extends BaseController {

    @Resource
    private DatasourceService databaseService;

    @ApiOperation(value = "查询数据源")
    @GetMapping(value = "/queryDatasource")
    public MsgResponse<List<DatasourceVO>> queryDatasource() {
        MsgResponse<List<DatasourceVO>> result;
        try {
            List<DatasourceVO> datasources = databaseService.queryAllDatasource();
            result = getSuccessResponse("查询数据源成功", datasources);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "新增数据源")
    @PostMapping(value = "/addDatasource")
    public MsgResponse<Integer> addDatasource(@RequestBody @Valid DatasourceAddDTO resources) {
        MsgResponse<Integer> result;
        try {
            Integer success = databaseService.addDatasource(resources);
            result = handleBasicOperationResponse("新增数据源成功", success);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "修改数据源")
    @PutMapping(value = "/editDatasource")
    public MsgResponse<Integer> editDatasource(@RequestBody @Valid DatasourceUpdateDTO resources) {
        MsgResponse<Integer> result;
        try {
            Integer success = databaseService.updateDatasource(resources);
            result = handleBasicOperationResponse("修改数据源成功", success);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "删除数据源")
    @DeleteMapping(value = "/deleteDatasource")
    public MsgResponse<Object> deleteDatasource(String ids) {
        MsgResponse<Object> result;
        try {
            Integer success = databaseService.deleteDatasource(ids);
            result = handleBasicOperationResponse("删除数据源成功", success);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "测试数据库链接")
    @PostMapping(value = "/testConnect")
    public MsgResponse<Boolean> testConnect(@RequestBody @Valid String dataSourceIds) {
        MsgResponse<Boolean> result;
        try {
            boolean isConnection = databaseService.testConnection(dataSourceIds);
            result = getSuccessResponse("测试数据库完成", isConnection);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }

    @ApiOperation(value = "测试数据库链接")
    @PostMapping(value = "/testConnectByEntity")
    public MsgResponse<Boolean> testConnectByEntity(@RequestBody @Valid DatasourceDTO datasource) {
        MsgResponse<Boolean> result;
        try {
            boolean isConnection = databaseService.testConnection(datasource);
            result = getSuccessResponse("测试数据库完成", isConnection);
        } catch (Exception e) {
            result = getExceptionResponse(e);
        }
        return result;
    }
}
