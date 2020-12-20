package com.jw.screw.admin.api.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jw.screw.admin.api.dto.ConfigDTO;
import com.jw.screw.admin.api.model.ConfigModel;
import com.jw.screw.admin.api.model.DatasourceModel;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.sys.config.dao.AppConfigDao;
import com.jw.screw.admin.sys.config.dao.AppConfigVersionDao;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import com.jw.screw.admin.sys.server.dao.AppServerDao;
import com.jw.screw.admin.sys.server.entity.AppServer;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import com.jw.screw.admin.sys.server.service.AppServerService;
import com.jw.screw.provider.annotations.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * 配置中心提供的查询配置相关操作的service
 * @author jiangw
 * @date 2020/12/10 14:25
 * @since 1.0
 */
@Service
@ProviderService(publishService = RemoteConfigService.class)
public class RemoteConfigService {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppServerService appServerService;

    @Autowired
    private AppConfigVersionDao appVersionDao;

    @Autowired
    private AppConfigDao appConfigDao;

    @Autowired
    private AppServerDao appServerDao;

    /**
     * 根据服务的code查找该服务所有的配置相关的信息
     * @param serverCode
     * @return 返回json串 包括服务信息
     * @throws Exception
     */
    public String queryConfigByServerCodeToJson(String serverCode) throws Exception {
        ConfigDTO configDTO = new ConfigDTO();
        AppServerVO serverVO = appServerService.queryAppServersByServerCode(serverCode);
        // 1.服务信息
        configDTO.setServerCode(serverVO.getServerCode());
        configDTO.setServerIp(serverVO.getServerIp());
        configDTO.setServerName(serverVO.getServerName());
        configDTO.setServerPort(serverVO.getServerPort());
        configDTO.setServerVersion(serverVO.getServerVersion());
        // 2.数据源信息
        DatasourceVO datasourceVO = serverVO.getDatasourceVO();
        if (!ObjectUtils.isEmpty(datasourceVO)) {
            DatasourceModel datasourceModel = new EntityFactoryBuilder<DatasourceModel>()
                    .setEntityClass(DatasourceModel.class)
                    .setVo(datasourceVO)
                    .build();
            configDTO.setDatasourceModel(datasourceModel);
        }
        List<AppConfigVO> appConfigVOS = appConfigService.queryConfigByServerId(serverVO.getId());
        if (!CollectionUtils.isEmpty(appConfigVOS)) {
            List<ConfigModel> configModels = new EntityFactoryBuilder<ConfigModel>()
                    .setEntityClass(ConfigModel.class)
                    .build(appConfigVOS.toArray());;
            configDTO.setConfigModel(configModels);
        }
        return JSONObject.toJSONString(configDTO);
    }

    /**
     * 根据配置id 查找配置
     * @param configId
     * @return 返回json串
     * @throws Exception
     */
    public String queryConfigByConfigId(String configId) throws Exception {
        AppConfig config = appConfigDao.selectOne(new QueryWrapper<AppConfig>().eq("ID", configId));
        if (ObjectUtils.isEmpty(config)) {
            return null;
        }
        AppServer server = appServerDao.selectOne(new QueryWrapper<AppServer>().eq("ID", config.getServerId()));
        if (ObjectUtils.isEmpty(server)) {
            return null;
        }
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setServerCode(server.getServerCode());
        configDTO.setServerIp(server.getServerIp());
        configDTO.setServerPort(server.getServerPort());
        configDTO.setServerCode(server.getServerCode());
        List<ConfigModel> configModels = new EntityFactoryBuilder<ConfigModel>()
                .setEntityClass(ConfigModel.class)
                .build(Collections.singletonList(config).toArray());
        configDTO.setConfigModel(configModels);
        return JSONObject.toJSONString(configDTO);
    }

    /**
     *
     * @param configId
     * @return 返回json串
     * @see #queryConfig(String)
     * @throws Exception
     */
    public String queryConfig(String configId) throws Exception {
        AppConfig config = appConfigDao.selectOne(new QueryWrapper<AppConfig>().eq("ID", configId));
        if (ObjectUtils.isEmpty(config)) {
            return null;
        }
        // 判断当前配置下是否是发布的版本
        String versionId = config.getConfigVersionId();
        AppConfigVersion version = queryVersion(versionId);
        if (ObjectUtils.isEmpty(version)) {
            return null;
        }
        return queryConfigByConfigId(version.getConfigId());
    }

    /**
     * 根据versionId 查找配置的json
     * @param versionId
     * @return 返回json串
     * @see #queryConfig(String)
     * @throws Exception
     */
    public String queryConfigByVersionId(String versionId) throws Exception {
        AppConfigVersion version = appVersionDao.selectOne(new QueryWrapper<AppConfigVersion>().eq("ID", versionId));
        if (ObjectUtils.isEmpty(version)) {
            return null;
        }
        return queryConfig(version.getConfigId());
    }

    /**
     * 根据versionId查找对于的version实体
     * @param versionId
     * @return {@link AppConfigVersion}
     */
    public AppConfigVersion queryVersion(String versionId) {
        return appVersionDao.selectOne(new QueryWrapper<AppConfigVersion>().eq("ID", versionId));
    }
}
