package com.jw.screw.admin.api;

import com.jw.screw.admin.api.service.RemoteConfigService;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.config.constant.ConfigDataConstant;
import com.jw.screw.admin.sys.config.constant.ConfigVersionConstant;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataUpdateDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionUpdateDTO;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigDataVO;
import com.zzht.patrol.screw.common.exception.ConnectionException;
import com.zzht.patrol.screw.provider.Notifier;
import com.zzht.patrol.screw.provider.annotations.ProviderService;
import com.zzht.patrol.screw.spring.ScrewSpringProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 配置中心的通知器
 * @author jiangw
 * @date 2020/12/10 11:14
 * @since 1.0
 */
@Component
@Aspect
@ProviderService(publishService = ConfigNotifier.class)
public class ConfigNotifier extends Notifier implements InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(ConfigNotifier.class);

    @Autowired
    private ScrewSpringProvider screwProvider;

    @Autowired
    private RemoteConfigService remoteService;

    public ConfigNotifier() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setNettyProvider(screwProvider.getProvider());
    }

    // --------------------------- 单播添加配置数据 ---------------------------

    @Pointcut("execution(* com.jw.screw.admin.sys.config.service.impl.AppConfigVersionServiceImpl.openedVersion*(..))")
    public void openedVersionPoint() {
    }

    /**
     * 当版本进行开启的时候，对变更的配置进行通知
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("openedVersionPoint()")
    public Object openedVersion(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        Integer proceed = (Integer) point.proceed();
        AppConfigVersionUpdateDTO arg = (AppConfigVersionUpdateDTO) args[0];
        Validators.doResult(proceed);
        String configId = arg.getConfigId();
        // 找到新增的配置
        String result = remoteService.queryConfigByConfigId(configId);
        try {
            if (!StringUtils.isEmpty(result)) {
                onAdd(result);
            }
        } catch (NoSuchMethodException | ConnectionException e) {
            logger.warn("notify onAdd event error: {}", e.getMessage());
        }
        return proceed;
    }

    @Pointcut("execution(* com.jw.screw.admin.sys.config.service.impl.AppConfigDataServiceImpl.saveAppConfigData*(..))")
    public void saveConfigDataPoint() {
    }

    @Around("saveConfigDataPoint()")
    public Object saveConfigData(ProceedingJoinPoint point) throws Throwable {
        Integer proceed = (Integer) point.proceed();
        Validators.doResult(proceed);
        Object[] args = point.getArgs();
        String logicOperate = (String) args[1];
        // 判断是不是发布新的版本数据
        if (ConfigDataConstant.DataOperation.EDIT.equals(logicOperate)) {
            return proceed;
        }
        List<AppConfigDataUpdateDTO> configData = (List<AppConfigDataUpdateDTO>) args[0];
        if (CollectionUtils.isEmpty(configData)) {
            return proceed;
        }
        AppConfigDataUpdateDTO data = configData.get(0);
        // 找到发布版本的配置
        String result = remoteService.queryConfigByVersionId(data.getConfigVersionId());
        try {
            if (!StringUtils.isEmpty(result)) {
                onAdd(result);
            }
        } catch (NoSuchMethodException | ConnectionException e) {
            logger.warn("notify onAdd event error: {}", e.getMessage());
        }
        return proceed;
    }

    /**
     * 配置添加，进行通知
     * @param result 相应服务的配置添加的结果
     */
    private void onAdd(String result) throws NoSuchMethodException, ConnectionException {
        if (logger.isInfoEnabled()) {
            logger.info("execute notify onAdd event, result: [{}]", result);
        }
        unicast(result, ConfigNotifier.class, "onAdd", String.class);
    }

    /**
     * 配置产生变化，进行通知
     * @param result 相应服务的配置变化的结果
     */
    @Deprecated
    public void onChange(String result) {
    }

    // --------------------------- 单播删除配置数据 ---------------------------

    /**
     * 配置文件的删除
     */
    @Pointcut("execution(* com.jw.screw.admin.sys.config.service.impl.AppConfigServiceImpl.deleteAppConfig*(..))")
    public void deleteConfigPoint() {
    }

    /**
     * 配置项的删除
     */
    @Pointcut("execution(* com.jw.screw.admin.sys.config.service.impl.AppConfigDataServiceImpl.removeAppConfigData*(..))")
    public void deleteConfigData() {

    }

    @Around("deleteConfigPoint()")
    public Object deleteConfig(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        String configId = (String) args[0];
        String result = remoteService.queryConfig(configId);
        Integer proceed = (Integer) point.proceed();
        Validators.doResult(proceed);
        try {
            // 字符串不为空，说明是发布的版本
            if (!StringUtils.isEmpty(result)) {
                onDelete(result);
            }
        } catch (NoSuchMethodException | ConnectionException e) {
            logger.warn("notify onDelete event error: {}", e.getMessage());
        }
        return proceed;
    }

    @Around("deleteConfigData()")
    public Object deleteConfigData(ProceedingJoinPoint point) throws Throwable {
        Integer proceed = (Integer) point.proceed();
        Validators.doResult(proceed);
        Object[] args = point.getArgs();
        try {
            List<AppConfigDataVO> configDataList = (List<AppConfigDataVO>) args[0];
            if (!CollectionUtils.isEmpty(configDataList)) {
                AppConfigDataVO appConfigDataVO = configDataList.get(0);
                String versionId = appConfigDataVO.getConfigVersionId();
                String result = remoteService.queryConfigByVersionId(versionId);
                if (!StringUtils.isEmpty(result)) {
                    onDelete(result);
                }
            }
        } catch (Exception e) {
            logger.warn("notify onDelete event error: {}", e.getMessage());
        }
        return proceed;
    }

    /**
     * 配置移除，进行通知
     * @param result 相应服务的配置删除的结果
     */
    private void onDelete(String result) throws NoSuchMethodException, ConnectionException {
        if (logger.isInfoEnabled()) {
            logger.info("execute notify onDelete event, result: [{}]", result);
        }
        unicast(result, ConfigNotifier.class, "onDelete", String.class);
    }

    // --------------------------- 单播更新配置数据 ---------------------------

    @Around("saveConfigDataPoint()")
    public Object updateConfig(ProceedingJoinPoint point) throws Throwable {
        Integer proceed = (Integer) point.proceed();
        Validators.doResult(proceed);
        Object[] args = point.getArgs();
        String logicOperate = (String) args[1];
        // 判断是不是发布新的版本数据
        if (!ConfigDataConstant.DataOperation.EDIT.equals(logicOperate)) {
            return proceed;
        }
        List<AppConfigDataUpdateDTO> configData = (List<AppConfigDataUpdateDTO>) args[0];
        if (CollectionUtils.isEmpty(configData)) {
            return proceed;
        }
        AppConfigDataUpdateDTO data = configData.get(0);
        AppConfigVersion version = remoteService.queryVersion(data.getConfigVersionId());
        if (ObjectUtils.isEmpty(version)) {
            return proceed;
        }
        // 判断当前版本是否是已发布的
        if (!ConfigVersionConstant.OPEN.equals(version.getConfigVersionStatus())) {
            return proceed;
        }
        String result = remoteService.queryConfigByVersionId(version.getId());
        try {
            if (!StringUtils.isEmpty(result)) {
                onUpdate(result);
            }
        } catch (NoSuchMethodException | ConnectionException e) {
            logger.warn("notify onDelete event error: {}", e.getMessage());
        }
        return proceed;
    }

    /**
     * 配置更新，进行通知
     * @param result 相应服务配置更新的结果
     */
    public void onUpdate(String result) throws NoSuchMethodException, ConnectionException {
        if (logger.isInfoEnabled()) {
            logger.info("execute notify onUpdate event, result: [{}]", result);
        }
        unicast(result, ConfigNotifier.class, "onUpdate", String.class);
    }

    @Override
    public void destroy() throws Exception {
        shutdown.set(true);
        notifier.shutdown();
    }
}
