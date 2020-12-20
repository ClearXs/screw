package com.jw.screw.admin.sys.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.JSONHelper;
import com.jw.screw.admin.common.PageFactoryBuilder;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.config.constant.ConfigVersionConstant;
import com.jw.screw.admin.sys.config.dao.AppConfigDao;
import com.jw.screw.admin.sys.config.dao.AppConfigDataDao;
import com.jw.screw.admin.sys.config.dao.AppConfigVersionDao;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionAddDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionQueryDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionUpdateDTO;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.entity.AppConfigData;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import com.jw.screw.admin.sys.config.service.AppConfigService;
import com.jw.screw.admin.sys.config.service.AppConfigVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppConfigVersionServiceImpl implements AppConfigVersionService {

    @Resource
    private AppConfigVersionDao appConfigVersionDao;

    @Resource
    private AppConfigDataDao appConfigDataDao;

    @Resource
    private AppConfigDao appConfigDao;

    @Resource
    private AppConfigService appConfigService;

    @Override
    public Integer addAppConfigVersion(AppConfigVersionAddDTO appConfigVersionAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        AppConfigVersion configVersion = new EntityFactoryBuilder<AppConfigVersion>()
                .setVo(appConfigVersionAddDTO)
                .setEntityClass(AppConfigVersion.class)
                .build();
        Validators.doResult(appConfigVersionDao.insert(configVersion));
        return DataOperationState.SUCCESSFUL;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer openedVersion(AppConfigVersionUpdateDTO appConfigVersionUpdateDTO) throws InstantiationException, IllegalAccessException, UnknowOperationException, BasicOperationException {
        String configId = appConfigVersionUpdateDTO.getConfigId();
        if (StringUtils.isEmpty(configId)) {
            throw new UnknowOperationException("当前配置id为空，无法继续执行操作");
        }
        // 1.把已经开放的版本更新为关闭
        AppConfigVersion appConfigVersion = appConfigVersionDao.selectOne(new QueryWrapper<AppConfigVersion>()
                .eq("CONFIG_VERSION_STATUS", ConfigVersionConstant.OPEN)
                .eq("CONFIG_ID", configId));
        if (appConfigVersion != null) {
            appConfigVersion.setConfigVersionStatus(ConfigVersionConstant.CLOSED);
            Validators.doResult(appConfigVersionDao.update(appConfigVersion,
                    new UpdateWrapper<AppConfigVersion>()
                            .eq("CONFIG_VERSION_STATUS", ConfigVersionConstant.OPEN)));
        }
        // 2.更新当前版本
        AppConfigVersion configVersion = new EntityFactoryBuilder<AppConfigVersion>()
                .setVo(appConfigVersionUpdateDTO)
                .setEntityClass(AppConfigVersion.class)
                .build();
        // 没有版本号的版本由保存得到
        // 1.找到版本中最大的版本号使其自增0.1
        // 2.如果没有设置初始版本号
        if (StringUtils.isEmpty(configVersion.getConfigVersion())) {
            AppConfigVersion latestConfigVersion = appConfigVersionDao.queryLatestVersion(configId);
            if (latestConfigVersion == null) {
                configVersion.setConfigVersion(new BigDecimal("1.0").toString());
            } else {
                String latestVersion = latestConfigVersion.getConfigVersion();
                // 可能存在找到空的版本号
                if (StringUtils.isEmpty(latestVersion)) {
                    configVersion.setConfigVersion(new BigDecimal("1.0").toString());
                } else {
                    configVersion.setConfigVersion(new BigDecimal(latestConfigVersion.getConfigVersion())
                            .add(new BigDecimal("0.1"))
                            .toString());
                }
            }
        }
        configVersion.setConfigVersionStatus(ConfigVersionConstant.OPEN);
        Validators.doResult(appConfigVersionDao.updateById(configVersion));
        // 3.更新配置
        List<AppConfigData> configDataList = appConfigDataDao.selectList(new QueryWrapper<AppConfigData>()
                .eq("CONFIG_VERSION_ID", configVersion.getId()));
        Map<String, Object> keyValues = new HashMap<>();
        for (AppConfigData configData : configDataList) {
            keyValues.put(configData.getConfigDataKey(), configData.getConfigDataValue());
        }
        AppConfig appConfig = appConfigDao.selectOne(new QueryWrapper<AppConfig>()
                .eq("ID", configId));
        appConfig.setConfigVersionId(configVersion.getId());
        appConfig.setConfigJson(JSONHelper.assemble(keyValues));
        Validators.doResult(appConfigDao.updateById(appConfig));
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public Integer removeAppConfigVersion(String id) {
        return appConfigVersionDao.deleteById(id);
    }

    @Override
    public PageResult<AppConfigVersionVO> queryAppConfigVersion(AppConfigVersionQueryDTO appConfigVersionQueryDTO) {
        PageFactoryBuilder<AppConfigVersionVO> builder = new PageFactoryBuilder<>();
        builder.setPageParams(appConfigVersionQueryDTO);
        Page<AppConfigVersionVO> page = builder.buildPage();
        List<AppConfigVersionVO> appConfigVOS = appConfigVersionDao.queryByPage(page, appConfigVersionQueryDTO);
        for (AppConfigVersionVO appConfigVO : appConfigVOS) {
            appConfigVO.setConfigId(appConfigVersionQueryDTO.getConfigId());
        }
        page.setRecords(appConfigVOS);
        return builder.buildPageResult(page);
    }

}
