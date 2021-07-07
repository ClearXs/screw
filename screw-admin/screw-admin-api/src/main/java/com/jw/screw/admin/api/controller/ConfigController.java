package com.jw.screw.admin.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.jw.screw.admin.api.dto.ConfigDTO;
import com.jw.screw.admin.api.model.ConfigModel;
import com.jw.screw.admin.api.model.DatasourceModel;
import com.jw.screw.admin.common.BaseController;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.MsgResponse;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import com.jw.screw.admin.sys.server.service.AppServerService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 配置管理对外接口
 * @author jiangw
 * @date 2020/11/19 15:58
 * @since 1.0
 */
@RestController
@RequestMapping("/api/webapi/config")
@Api(value = "/api/webapi/config", tags = "配置接口")
public class ConfigController extends BaseController {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppServerService appServerService;

    @GetMapping("/queryConfigJson")
    public MsgResponse<ConfigDTO> queryConfigJson(@RequestParam String appCode, String configKey) {
        // 查询服务
        MsgResponse<ConfigDTO> response;
        try {
            String[] configKeys = null;
            if (!StringUtils.isEmpty(configKey)) {
                configKeys = configKey.split(",");
            }
            ConfigDTO configDTO = new ConfigDTO();
            AppServerVO serverVO = appServerService.queryAppServersByServerCode(appCode);
            // 1.服务信息
            configDTO.setServerCode(serverVO.getServerCode());
            configDTO.setServerIp(serverVO.getServerIp());
            configDTO.setServerName(serverVO.getServerName());
            configDTO.setServerPort(serverVO.getServerPort());
            configDTO.setServerVersion(serverVO.getServerVersion());
            // 2.数据源信息
            List<DatasourceVO> datasourceVO = serverVO.getDatasourceVO();
            if (!ObjectUtils.isEmpty(datasourceVO)) {
                List<DatasourceModel> datasourceModel = new EntityFactoryBuilder<DatasourceModel>()
                        .setEntityClass(DatasourceModel.class)
                        .build(datasourceVO.toArray());
                configDTO.setDatasourceModel(datasourceModel);
            }
            List<AppConfigVO> appConfigVOS;
            // 3.配置信息
            if (ObjectUtils.isEmpty(configKeys)) {
                appConfigVOS = appConfigService.queryConfigByServerId(serverVO.getId());
            } else {
                appConfigVOS = appConfigService.queryConfigsByConfigKeys(configKeys);
            }
            if (!CollectionUtils.isEmpty(appConfigVOS)) {
                List<ConfigModel> configModels = new EntityFactoryBuilder<ConfigModel>()
                        .setEntityClass(ConfigModel.class)
                        .build(appConfigVOS.toArray());;
                for (ConfigModel config : configModels) {
                    String configJson = config.getConfigJson();
                    if (!StringUtils.isEmpty(configJson)) {
                        JSONObject jsonObj = JSONObject.parseObject(configJson);
                        config.setJsonObject(jsonObj);
                    }
                }
                configDTO.setConfigModel(configModels);
            }
            response = getSuccessResponse("查询成功", configDTO);
        } catch (Exception e) {
            response = getExceptionResponse(e);
        }
        return response;
    }
}
