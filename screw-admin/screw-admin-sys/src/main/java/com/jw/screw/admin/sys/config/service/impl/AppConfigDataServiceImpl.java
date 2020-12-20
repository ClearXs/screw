package com.jw.screw.admin.sys.config.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.JSONHelper;
import com.jw.screw.admin.common.PageFactoryBuilder;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.constant.StringPool;
import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.config.constant.ConfigDataConstant;
import com.jw.screw.admin.sys.config.constant.ConfigVersionConstant;
import com.jw.screw.admin.sys.config.dao.AppConfigDao;
import com.jw.screw.admin.sys.config.dao.AppConfigDataDao;
import com.jw.screw.admin.sys.config.dao.AppConfigVersionDao;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataAddDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataQueryDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataUpdateDTO;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.entity.AppConfigData;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigDataVO;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import com.jw.screw.admin.sys.config.service.AppConfigDataService;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppConfigDataServiceImpl implements AppConfigDataService {

    @Resource
    private AppConfigDataDao appConfigDataDao;

    @Resource
    private AppConfigVersionDao appConfigVersionDao;

    @Resource
    private AppConfigDao appConfigDao;

    @Resource
    private AppConfigService appConfigService;

    @Override
    public PageResult<AppConfigDataVO> queryAppConfigData(AppConfigDataQueryDTO appConfigDataQueryDTO) {
        PageFactoryBuilder<AppConfigDataVO> builder = new PageFactoryBuilder<>();
        builder.setPageParams(appConfigDataQueryDTO);
        Page<AppConfigDataVO> page = builder.buildPage();
        List<AppConfigDataVO> appConfigVOS = appConfigDataDao.queryByPage(page, appConfigDataQueryDTO);
        page.setRecords(appConfigVOS);
        return builder.buildPageResult(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addAppConfigData(List<AppConfigDataAddDTO> appConfigDataAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        List<AppConfigData> appConfigData = new ArrayList<>();
        for (AppConfigDataAddDTO configDataAddDTO : appConfigDataAddDTO) {
            Validators.doExist(appConfigDataDao, configDataAddDTO);
            appConfigData.add(new EntityFactoryBuilder<AppConfigData>()
                    .setVo(configDataAddDTO)
                    .setEntityClass(AppConfigData.class)
                    .build());
        }
        for (AppConfigData configData : appConfigData) {
            Validators.doResult(appConfigDataDao.insert(configData));
        }
        return DataOperationState.SUCCESSFUL;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer updateAppConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        List<AppConfigData> appConfigData = new ArrayList<>();
        for (AppConfigDataUpdateDTO configDataUpdateDTO : appConfigDataUpdateDTO) {
            Validators.doExist(appConfigDataDao, configDataUpdateDTO);
            appConfigData.add(new EntityFactoryBuilder<AppConfigData>()
                    .setVo(configDataUpdateDTO)
                    .setEntityClass(AppConfigData.class)
                    .build());
        }
        for (AppConfigData configData : appConfigData) {
            Validators.doResult(appConfigDataDao.updateById(configData));
        }
        return DataOperationState.SUCCESSFUL;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer saveAppConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO, String logicOperate) throws UnknowOperationException, InstantiationException, IllegalAccessException, BasicOperationException {
        if (CollectionUtils.isEmpty(appConfigDataUpdateDTO)) {
            // 数据为空不提示错误
            return DataOperationState.SUCCESSFUL;
        }
        switch (logicOperate) {
            case ConfigDataConstant
                    .DataOperation
                    .DEPLOY:
                        deployConfigData(appConfigDataUpdateDTO);
                        break;
            case ConfigDataConstant
                    .DataOperation
                    .SAVE:
                        saveConfigData(appConfigDataUpdateDTO);
                        break;
            case ConfigDataConstant
                    .DataOperation
                    .EDIT:
                        editConfigData(appConfigDataUpdateDTO);
                        break;
            default:
                throw new UnknowOperationException("未知的操作" + logicOperate);
        }
        return DataOperationState.SUCCESSFUL;
    }

    /**
     * 发布配置操作
     * @param appConfigDataUpdateDTO
     */
    private void deployConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) throws InstantiationException, IllegalAccessException, UnknowOperationException, BasicOperationException {
        String configId = appConfigDataUpdateDTO.get(0).getAppConfigVersionVO().getConfigId();
        if (StringUtils.isEmpty(configId)) {
            throw new UnknownError("当前配置id为空，无法继续执行操作");
        }
        // 1.更新已经发布配置版本为关闭，除了配置版本是未发布的（存在没有发布的版本）
        AppConfigVersion configVersion = appConfigVersionDao.selectOne(new QueryWrapper<AppConfigVersion>()
                .eq("CONFIG_VERSION_STATUS", ConfigVersionConstant.OPEN).eq("CONFIG_ID", configId));
        if (!ObjectUtils.isEmpty(configVersion)) {
            configVersion.setConfigVersionStatus(ConfigVersionConstant.CLOSED);
            Validators.doResult(appConfigVersionDao.updateById(configVersion));
        }
        // 2.新增配置版本，以目前最大版本自增作为最新的版本号
        AppConfigVersion latestVersion = appConfigVersionDao.queryLatestVersion(configId);
        // 如果没有找到，那么设置默认开始版本为1.0
        configVersion = new AppConfigVersion();
        if (latestVersion == null) {
            configVersion.setConfigVersion("1.0");
        } else {
            configVersion.setConfigVersion(new BigDecimal(latestVersion.getConfigVersion())
                    .add(new BigDecimal("0.1")).toString());
        }
        configVersion.setConfigId(configId);
        configVersion.setConfigVersionStatus(ConfigVersionConstant.OPEN);
        Validators.doResult(appConfigVersionDao.insert(configVersion));
        // 3.更新配置的versionId
        latestVersion = configVersion;
        AppConfig appConfig = appConfigDao.selectOne(new QueryWrapper<AppConfig>().eq("ID", configId));
        appConfig.setConfigVersionId(latestVersion.getId());
        appConfig.setConfigJson(JSONHelper.assemble(buildKeyValues(appConfigDataUpdateDTO)));
        Validators.doResult(appConfigDao.updateById(appConfig));
        for (AppConfigDataUpdateDTO updateDTO : appConfigDataUpdateDTO) {
            updateDTO.setId("");
            AppConfigVersionVO versionVO = new EntityFactoryBuilder<AppConfigVersionVO>()
                    .setVo(latestVersion)
                    .setEntityClass(AppConfigVersionVO.class)
                    .build();
            updateDTO.setConfigVersionId(latestVersion.getId());
            updateDTO.setConfigDataStoreState(ConfigDataConstant.DataStoreState.DEPLOY);
            updateDTO.setAppConfigVersionVO(versionVO);
        }
        // 4.保存配置的数据
        editConfigData(appConfigDataUpdateDTO);
    }

    /**
     * 保存配置的操作
     * @param appConfigDataUpdateDTOS
     */
    private void saveConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTOS) throws InstantiationException, IllegalAccessException, UnknowOperationException, BasicOperationException {
        // 1.生成一个新的未发布的版本
        AppConfigVersion configVersion = new AppConfigVersion();
        configVersion.setConfigVersionStatus(ConfigVersionConstant.NON_DEPLOY);
        String configId = appConfigDataUpdateDTOS.get(0).getAppConfigVersionVO().getConfigId();
        configVersion.setConfigId(configId);
        Validators.doResult(appConfigVersionDao.insert(configVersion));
        for (AppConfigDataUpdateDTO updateDTO : appConfigDataUpdateDTOS) {
            // 创建新的configData
            updateDTO.setId("");
            updateDTO.setConfigVersionId(configVersion.getId());
            AppConfigVersionVO versionVO = new EntityFactoryBuilder<AppConfigVersionVO>()
                    .setVo(configVersion)
                    .setEntityClass(AppConfigVersionVO.class)
                    .build();
            updateDTO.setAppConfigVersionVO(versionVO);
            updateDTO.setConfigDataStoreState(ConfigDataConstant.DataStoreState.SAVE);
        }
        // 2.保存配置数据
        editConfigData(appConfigDataUpdateDTOS);
    }

    private void editConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) throws IllegalAccessException, InstantiationException, UnknowOperationException, BasicOperationException {
        // 1.前端保证每个配置数据都有CONFIG_VERSION_ID
        // 2.前端保证每个配置数据的暂存的存储状态，后端负责发布与存储状态。

        // 数据的前处理
        preHandler(appConfigDataUpdateDTO);
        // --- 数据库未存在的数据
        List<AppConfigDataUpdateDTO> nonExistingConfigs = appConfigDataUpdateDTO.stream().filter((updateDTO -> updateDTO.getId() == null || "".equals(updateDTO.getId()))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(nonExistingConfigs)) {
            List<AppConfigDataAddDTO> configDataAddDTO = new ArrayList<>();
            for (AppConfigDataUpdateDTO nonExistingConfig : nonExistingConfigs) {
                configDataAddDTO.add(new EntityFactoryBuilder<AppConfigDataAddDTO>()
                        .setVo(nonExistingConfig)
                        .setEntityClass(AppConfigDataAddDTO.class)
                        .build());
            }
            if (!CollectionUtils.isEmpty(configDataAddDTO)) {
                addAppConfigData(configDataAddDTO);
            }
        }
        // --- 数据库存在的数据
        List<AppConfigDataUpdateDTO> existingConfigs = appConfigDataUpdateDTO.stream().filter((updateDTO -> updateDTO.getId() != null && !"".equals(updateDTO.getId()))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(existingConfigs)) {
            updateAppConfigData(existingConfigs);
        }
        // --- 如果当前配置数据的版本是发布状态，那么同时更新配置
        if (CollectionUtils.isEmpty(appConfigDataUpdateDTO)) {
            return;
        }
        AppConfigVersionVO configVersion = appConfigDataUpdateDTO.get(0).getAppConfigVersionVO();
        if (!ConfigVersionConstant.OPEN.equals(configVersion.getConfigVersionStatus())) {
            return;
        }
        String configId = configVersion.getConfigId();
        if (StringUtils.isEmpty(configId)) {
            throw new UnknowOperationException("配置id为空，无法更新配置");
        }
        AppConfig appConfig = appConfigDao.selectOne(new QueryWrapper<AppConfig>().eq("ID", configId));
        appConfig.setConfigJson(JSONHelper.assemble(buildKeyValues(appConfigDataUpdateDTO)));
        Validators.doResult(appConfigDao.updateById(appConfig));
    }

    /**
     * 插入/更新数据库的处理
     * 1.把暂存的状态更新为存储的状态
     * 2.格式化话json数据
     * @param appConfigDataUpdateDTO
     */
    private void preHandler(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) {
        for (AppConfigDataUpdateDTO configDataUpdateDTO : appConfigDataUpdateDTO) {
            if (ConfigDataConstant.DataStoreState.SAVE_TEMP.equals(configDataUpdateDTO.getConfigDataStoreState())) {
                configDataUpdateDTO.setConfigDataStoreState(ConfigDataConstant.DataStoreState.SAVE);
            }
            String dataValue = configDataUpdateDTO.getConfigDataValue();
            try {
                // 若当前不是json数据，或者说格式很奇怪，那么使用默认过滤方式
                dataValue = JSONObject.parse(dataValue).toString();
            } catch (Exception e) {
                log.error(e.getMessage());
                // 过滤json数据 /n /t
                dataValue = dataValue.replace(StringPool.NEWLINE, "");
                dataValue = dataValue.replace(StringPool.TAB, "");
            }
            configDataUpdateDTO.setConfigDataValue(dataValue);
        }
    }

    private Map<String, Object> buildKeyValues(List<AppConfigDataUpdateDTO> configData) {
        Map<String, Object> keyValues = new HashMap<>();
        for (AppConfigDataUpdateDTO data : configData) {
            keyValues.put(data.getConfigDataKey(), data.getConfigDataValue());
        }
        return keyValues;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer removeAppConfigData(List<AppConfigDataVO> configDataList) throws BasicOperationException {
        if (CollectionUtils.isEmpty(configDataList)) {
            throw new RuntimeException("数据为空，无法删除");
        }
        String configVersionId = configDataList.get(0).getConfigVersionId();
        if (StringUtils.isEmpty(configVersionId)) {
            throw new BasicOperationException("配置版本id为空，无法继续执行删除操作");
        }
        for (AppConfigDataVO configData : configDataList) {
            Validators.doResult(appConfigDataDao.deleteById(configData.getId()));
        }
        appConfigService.rebuildConfigJson(configVersionId);
        return DataOperationState.SUCCESSFUL;
    }

}
