package com.jw.screw.admin.sys.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.constant.StringPool;
import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.id.UniqueIdUtils;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.config.constant.ConfigDataConstant;
import com.jw.screw.admin.sys.config.constant.ConfigVersionConstant;
import com.jw.screw.admin.sys.config.dao.AppConfigDao;
import com.jw.screw.admin.sys.config.dao.AppConfigDataDao;
import com.jw.screw.admin.sys.config.dao.AppConfigVersionDao;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.entity.AppConfigData;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import com.jw.screw.admin.sys.datasource.dao.DatasourceDao;
import com.jw.screw.admin.sys.datasource.entity.Datasource;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import com.jw.screw.admin.sys.server.constant.AppServerConstant;
import com.jw.screw.admin.sys.server.dao.AppServerDao;
import com.jw.screw.admin.sys.server.dto.AppServerAddDTO;
import com.jw.screw.admin.sys.server.dto.AppServerUpdateDTO;
import com.jw.screw.admin.sys.server.entity.AppServer;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import com.jw.screw.admin.sys.server.service.AppServerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppServerServiceImpl implements AppServerService {

    @Resource
    private AppServerDao appServerDao;

    @Resource
    private AppConfigService appConfigService;

    @Resource
    private AppConfigDao appConfigDao;

    @Resource
    private AppConfigVersionDao versionDao;

    @Resource
    private AppConfigDataDao configDataDao;

    @Resource
    private DatasourceDao datasourceDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addAppServer(AppServerAddDTO appServerAddDto) throws InstantiationException, IllegalAccessException,
            BasicOperationException, UnknowOperationException {
        // 数据存在校验
        Validators.doExist(appServerDao, appServerAddDto);
        AppServer appServer = new EntityFactoryBuilder<AppServer>()
                .setVo(appServerAddDto)
                .setEntityClass(AppServer.class)
                .build();
        appServer.setServerCode(DigestUtils.md5DigestAsHex(appServer.getServerName().getBytes()));
        if (StringUtils.isEmpty(appServer.getSystemId())) {
            appServer.setSystemId(UniqueIdUtils.getId());
        }
        if (StringUtils.isEmpty(appServer.getSystemName())) {
            appServer.setSystemName("应用");
        }
        Validators.doResult(appServerDao.insert(appServer));
        // 查看添加是否有配置，如果有，则取它已经发布的版本的数据（或许存在未发布但已经编辑的）
        // 1.检查是否有配置
        // 2.检查当前配置的最新版本，以这个最新版本构建一个当前服务未发布版本的配置
        // 3.如果当前配置没有存在开启的版本（那么就只有可能是未发布的版本，不会存在关闭状态的版本），那么复制它所有所有版本及其配置.
        List<AppConfigVO> appConfig = appServerAddDto.getAppConfig();
        if (CollectionUtils.isEmpty(appConfig)) {
            return DataOperationState.SUCCESSFUL;
        }
        for (AppConfigVO configVO : appConfig) {
            // 1.配置复制
            AppConfig config = configCopy(appServer, configVO);
            // 2.查找该配置下的所有版本
            List<AppConfigVersion> versions = versionDao.selectList(new QueryWrapper<AppConfigVersion>().eq("CONFIG_ID", configVO.getId()));
            if (CollectionUtils.isEmpty(versions)) {
                throw new UnknowOperationException("配置：" + config.getConfigName() + "，没有版本，请检查相关配置");
            }
            // 3.找到该版本已发布的版本
            List<AppConfigVersion> openVersions = versions
                    .stream()
                    .filter(version -> ConfigVersionConstant.OPEN.equals(version.getConfigVersionStatus()))
                    .collect(Collectors.toList());
            if (openVersions.size() > 1) {
                throw new UnknowOperationException("配置：" + config.getConfigName() + "，存在两个开启版本，无法继续创建服务");
            }
            // 4.开始复制发布版本的数据
            if (!CollectionUtils.isEmpty(openVersions)) {
                for (AppConfigVersion openVersion : openVersions) {
                    versionAndDataCopy(config, openVersion);
                }
            } else {
                // 或者复制未发布版本的数据
                List<AppConfigVersion> nonDeployVersions = versions
                        .stream()
                        .filter(version -> ConfigVersionConstant.NON_DEPLOY.equals(version.getConfigVersionStatus()))
                        .collect(Collectors.toList());
                for (AppConfigVersion nonDeployVersion : nonDeployVersions) {
                    versionAndDataCopy(config, nonDeployVersion);
                }
            }
        }
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public Integer isExistServer(AppServerAddDTO addDTO) throws BasicOperationException {
        Validators.doExist(appServerDao, addDTO);
        return DataOperationState.SUCCESSFUL;
    }

    private AppConfig configCopy(AppServer appServer, AppConfigVO configVO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        AppConfig config = new EntityFactoryBuilder<AppConfig>()
                .setVo(configVO)
                .setEntityClass(AppConfig.class)
                .build();
        config.setId("");
        config.setConfigJson("");
        config.setConfigVersionId("");
        config.setCreateTime(null);
        config.setCreateBy("");
        config.setUpdateTime(null);
        config.setUpdateBy("");
        config.setDeleted(0);
        config.setVersion(0);
        config.setServerId(appServer.getId());
        Validators.doResult(appConfigDao.insert(config));
        return config;
    }

    private void versionAndDataCopy(AppConfig config, AppConfigVersion configVersion) throws BasicOperationException {
        // 找到该版本的所有数据
        List<AppConfigData> appConfigData = configDataDao.selectList(new QueryWrapper<AppConfigData>()
                .eq("CONFIG_VERSION_ID", configVersion.getId()));
        configVersion.setId("");
        configVersion.setConfigId(config.getId());
        configVersion.setConfigVersionStatus(ConfigVersionConstant.NON_DEPLOY);
        configVersion.setConfigVersion(null);
        configVersion.setCreateBy("");
        configVersion.setCreateTime(null);
        configVersion.setUpdateBy("");
        configVersion.setUpdateTime(null);
        configVersion.setDeleted(0);
        configVersion.setVersion(0);
        Validators.doResult(versionDao.insert(configVersion));
        // 复制配置数据
        for (AppConfigData configData : appConfigData) {
            configData.setId("");
            configData.setConfigDataStoreState(ConfigDataConstant.DataStoreState.SAVE_TEMP);
            configData.setConfigVersionId(configVersion.getId());
            configData.setCreateBy("");
            configData.setCreateTime(null);
            configData.setUpdateBy("");
            configData.setUpdateTime(null);
            configData.setDeleted(0);
            configData.setVersion(0);
            Validators.doResult(configDataDao.insert(configData));
        }
    }

    @Override
    public Integer updateAppServer(AppServerUpdateDTO appServerUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        Validators.doExist(appServerDao, appServerUpdateDTO);
        AppServer appServer = new EntityFactoryBuilder<AppServer>()
                .setVo(appServerUpdateDTO)
                .setEntityClass(AppServer.class)
                .build();
        Validators.doResult(appServerDao.updateById(appServer));
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public List<AppServerVO> queryAppServers() throws IllegalAccessException, InstantiationException {
        List<AppServerVO> appServers = appServerDao.listAll();
        for (AppServerVO appServerVO : appServers) {
            String dataSourceId = appServerVO.getDataSourceId();
            if (!StringUtils.isEmpty(dataSourceId)) {
                List<Datasource> datasourceList = datasourceDao.selectList(new QueryWrapper<Datasource>().in("ID", dataSourceId.split(StringPool.COMMA)));
                List<DatasourceVO> datasourceVOList = new EntityFactoryBuilder<DatasourceVO>().setEntityClass(DatasourceVO.class).build(datasourceList.toArray());
                appServerVO.setDatasourceVO(datasourceVOList);
            }
        }
        return appServers;
    }

    @Override
    public AppServerVO queryAppServersByServerCode(String serverCode) throws Exception {
        AppServer server = appServerDao.selectOne(new QueryWrapper<AppServer>()
                .eq("SERVER_CODE", serverCode));
        if (ObjectUtils.isEmpty(server)) {
            throw new Exception("没有找到对应服务");
        }
        AppServerVO serverVO = new EntityFactoryBuilder<AppServerVO>()
                .setEntityClass(AppServerVO.class)
                .setVo(server)
                .build();
        // 查找数据源
        String dataSourceId = serverVO.getDataSourceId();
        if (!StringUtils.isEmpty(dataSourceId)) {
            List<Datasource> datasource = datasourceDao.selectList(new QueryWrapper<Datasource>().in("ID", dataSourceId));
            if (!ObjectUtils.isEmpty(datasource)) {
                List<DatasourceVO> datasourceVO = new EntityFactoryBuilder<DatasourceVO>()
                        .setEntityClass(DatasourceVO.class)
                        .build(datasource.toArray());
                serverVO.setDatasourceVO(datasourceVO);
            }
        }
        return serverVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer deleteAppServerById(String serverIds) throws BasicOperationException {
        String[] serverIdArray = serverIds.split(StringPool.COMMA);
        for (String serverId : serverIdArray) {
            List<AppConfig> appConfigs = appConfigDao.selectList(new QueryWrapper<AppConfig>().eq("SERVER_ID", serverId));
            if (!CollectionUtils.isEmpty(appConfigs)) {
                for (AppConfig appConfig : appConfigs) {
                    appConfigService.deleteAppConfig(appConfig.getId());
                }
            }
            Validators.doResult(appServerDao.deleteById(serverId));
        }
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public Map<String, List<AppServerVO>> getServerDirectory(String operate) throws InstantiationException, IllegalAccessException, UnknowOperationException {
        List<String> menus;
        if (AppServerConstant.DataOperate.ALL.equals(operate)) {
            menus = appServerDao.queryServerDirectory(null);
        } else if (AppServerConstant.DataOperate.DEFAULT.equals(operate)) {
            menus = appServerDao.queryServerDirectory(AppServerConstant.DataOperate.DEFAULT);
        } else {
            throw new UnknowOperationException("未知的操作：" + operate + "，无法获取服务目录");
        }
        Map<String, List<AppServerVO>> directories = new HashMap<>(menus.size());
        for (String menu : menus) {
            // 查找目录下对应服务，保证服务名唯一
            List<AppServer> servers = appServerDao.selectList(new QueryWrapper<AppServer>().eq("SYSTEM_NAME", menu));
            List<AppServerVO> serverVOList = new ArrayList<>(servers.size());
            for (AppServer server : servers) {
                AppServerVO serverVO = new EntityFactoryBuilder<AppServerVO>()
                        .setVo(server)
                        .setEntityClass(AppServerVO.class)
                        .build();
                List<AppConfig> appConfigs = appConfigDao.selectList(new QueryWrapper<AppConfig>().eq("SERVER_ID", serverVO.getId()));
                List<AppConfigVO> appConfigVOList = new ArrayList<>();
                for (AppConfig appConfig : appConfigs) {
                    AppConfigVO appConfigVO = new EntityFactoryBuilder<AppConfigVO>()
                            .setVo(appConfig)
                            .setEntityClass(AppConfigVO.class)
                            .build();
                    appConfigVOList.add(appConfigVO);
                }
                serverVO.setAppConfigVO(appConfigVOList);
                serverVOList.add(serverVO);
            }
            directories.put(menu, serverVOList);
        }
        return directories;
    }
}