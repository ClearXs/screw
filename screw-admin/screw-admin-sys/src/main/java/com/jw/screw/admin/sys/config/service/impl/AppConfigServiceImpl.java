package com.jw.screw.admin.sys.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.JSONHelper;
import com.jw.screw.admin.common.PageFactoryBuilder;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.constant.StringPool;
import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.config.constant.ConfigVersionConstant;
import com.jw.screw.admin.sys.config.dao.AppConfigDao;
import com.jw.screw.admin.sys.config.dao.AppConfigDataDao;
import com.jw.screw.admin.sys.config.dao.AppConfigVersionDao;
import com.jw.screw.admin.sys.config.dto.AppConfigAddDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigQueryDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigUpdateDTO;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.entity.AppConfigData;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import com.jw.screw.admin.sys.server.dao.AppServerDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    @Resource
    private AppConfigDao appConfigDao;

    @Resource
    private AppConfigVersionDao appConfigVersionDao;

    @Resource
    private AppConfigDataDao appConfigDataDao;

    @Override
    public PageResult<AppConfigVO> queryAppConfigs(AppConfigQueryDTO queryDTO) throws InstantiationException, IllegalAccessException {
        PageFactoryBuilder<AppConfigVO> builder = new PageFactoryBuilder<>();
        builder.setPageParams(queryDTO);
        Page<AppConfigVO> page = builder.buildPage();
        List<AppConfigVO> vos = appConfigDao.queryByPage(page, queryDTO);
        // 查询配置
        if (!CollectionUtils.isEmpty(vos)) {
            for (AppConfigVO configVO : vos) {
                AppConfigVersion configVersion = appConfigVersionDao.selectOne(new QueryWrapper<AppConfigVersion>()
                        .eq("CONFIG_ID", configVO.getId())
                        .eq("CONFIG_VERSION_STATUS", ConfigVersionConstant.OPEN));
                if (!ObjectUtils.isEmpty(configVersion)) {
                    AppConfigVersionVO versionVO = new EntityFactoryBuilder<AppConfigVersionVO>()
                            .setEntityClass(AppConfigVersionVO.class)
                            .setVo(configVersion)
                            .build();
                    configVO.setAppConfigVersionVO(versionVO);
                }
            }
        }
        page.setRecords(vos);
        return builder.buildPageResult(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addAppConfig(AppConfigAddDTO appConfigAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        Validators.doExist(appConfigDao, appConfigAddDTO);
        // 1.保存配置
        AppConfig appConfig = new EntityFactoryBuilder<AppConfig>()
                .setVo(appConfigAddDTO)
                .setEntityClass(AppConfig.class)
                .build();
        Validators.doResult(appConfigDao.insert(appConfig));
        // 2.关联保存配置版本
        AppConfigVersion configVersion = new AppConfigVersion();
        configVersion.setConfigId(appConfig.getId());
        configVersion.setConfigVersionStatus(ConfigVersionConstant.NON_DEPLOY);
        Validators.doResult(appConfigVersionDao.insert(configVersion));
        return DataOperationState.SUCCESSFUL;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer deleteAppConfig(String configIds) throws BasicOperationException {
        // 1.逻辑删除版本信息
        // 找到所有的与此配置有关的版本
        String[] configIdArray = configIds.split(StringPool.COMMA);
        for (String configId : configIdArray) {
            List<AppConfigVersion> versions = appConfigVersionDao.selectList(new QueryWrapper<AppConfigVersion>().eq("CONFIG_ID", configId));
            if (!CollectionUtils.isEmpty(versions)) {
                for (AppConfigVersion version : versions) {
                    // 找到与此版本关联的配置数据
                    List<AppConfigData> configDataList = appConfigDataDao.selectList(new QueryWrapper<AppConfigData>().eq("CONFIG_VERSION_ID", version.getId()));
                    if (!CollectionUtils.isEmpty(configDataList)) {
                        for (AppConfigData configData : configDataList) {
                            Validators.doResult(appConfigDataDao.deleteById(configData.getId()));
                        }
                    }
                    Validators.doResult(appConfigVersionDao.deleteById(version.getId()));
                }
            }
            Validators.doResult(appConfigDao.deleteById(configId));
        }
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public Integer deleteRealAppConfig(String configId) {
        return null;
    }

    @Override
    public Integer updateAppConfig(AppConfigUpdateDTO appConfigUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        Validators.doExist(appConfigDao, appConfigUpdateDTO);
        // 查找最新的配置乐观锁的版本，可能被其他功能改过
        AppConfig latestConfig = appConfigDao.selectOne(new QueryWrapper<AppConfig>().eq("ID", appConfigUpdateDTO.getId()));
        if (appConfigUpdateDTO.getVersion() != latestConfig.getVersion()) {
            // 取最新的版本号与json数据
            appConfigUpdateDTO.setVersion(latestConfig.getVersion());
            appConfigUpdateDTO.setConfigJson(latestConfig.getConfigJson());
        }
        AppConfig appConfig = new EntityFactoryBuilder<AppConfig>()
                .setVo(appConfigUpdateDTO)
                .setEntityClass(AppConfig.class)
                .build();
        Validators.doResult(appConfigDao.updateById(appConfig));
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public List<AppConfigVO> queryConfigsByConfigKeys(String[] configKeys) throws IllegalAccessException, InstantiationException {
        if (ObjectUtils.isEmpty(configKeys)) {
            return null;
        }
        List<AppConfig> appConfigs = appConfigDao.selectList(new QueryWrapper<AppConfig>()
                .in("CONFIG_KEY", configKeys));
        return new EntityFactoryBuilder<AppConfigVO>()
                .setEntityClass(AppConfigVO.class)
                .build(appConfigs.toArray());
    }

    @Override
    public List<AppConfigVO> queryConfigByServerId(String serverId) throws IllegalAccessException, InstantiationException {
        List<AppConfig> appConfigs = appConfigDao.selectList(new QueryWrapper<AppConfig>()
                .eq("SERVER_ID", serverId));
        if (CollectionUtils.isEmpty(appConfigs)) {
            return null;
        }
        return new EntityFactoryBuilder<AppConfigVO>()
                .setEntityClass(AppConfigVO.class)
                .build(appConfigs.toArray());
    }

    @Override
    public void rebuildConfigJson(String configVersionId) throws BasicOperationException {
        // 1.找到当前版本是否是开启的版本
        AppConfigVersion configVersion = appConfigVersionDao.selectOne(new QueryWrapper<AppConfigVersion>()
                .eq("ID", configVersionId));
        String configJson = "";
        if (ConfigVersionConstant.OPEN.equals(configVersion.getConfigVersionStatus())) {
            // 找当当前版本的配置数据
            List<AppConfigData> appConfigData = appConfigDataDao.selectList(new QueryWrapper<AppConfigData>()
                    .eq("CONFIG_VERSION_ID", configVersionId));
            if (!CollectionUtils.isEmpty(appConfigData)) {
                Map<String, Object> keyValues = new HashMap<>();
                for (AppConfigData configData : appConfigData) {
                    keyValues.put(configData.getConfigDataKey(), configData.getConfigDataValue());
                }
                configJson = JSONHelper.assemble(keyValues);
            }
        }
        AppConfig appConfig = appConfigDao.selectOne(new QueryWrapper<AppConfig>()
                .eq("CONFIG_VERSION_ID", configVersionId));
        if (!ObjectUtils.isEmpty(appConfig)) {
            appConfig.setConfigJson(configJson);
            Validators.doResult(appConfigDao.updateById(appConfig));
        }
    }
}
