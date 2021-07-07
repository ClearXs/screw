package com.jw.screw.admin.sys.config.service;



import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.AppConfigAddDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigQueryDTO;
import com.jw.screw.admin.sys.config.dto.AppConfigUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigVO;

import java.util.List;

/**
 * 配置操作
 * @author jiangw
 * @date 2020/11/13 14:05
 * @since 1.0
 */
public interface AppConfigService {

    /**
     * 查找所有
     */
    PageResult<AppConfigVO> queryAppConfigs(AppConfigQueryDTO queryDTO) throws InstantiationException, IllegalAccessException;

    /**
     * 添加配置
     * 1.添加当前的配追
     * 2.绑定追加一个未知版本的configVersion
     */
    Integer addAppConfig(AppConfigAddDTO appConfigAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 逻辑删除配置
     * 1.级联删除版本信息
     * 2.级联删除版本数据
     * @param configIds
     */
    Integer deleteAppConfig(String configIds) throws BasicOperationException;

    /**
     * 物理删除配置
     */
    Integer deleteRealAppConfig(String configId);

    /**
     * 更新配置
     */
    Integer updateAppConfig(AppConfigUpdateDTO appConfigUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 根据配置key，查找配置
     */
    List<AppConfigVO> queryConfigsByConfigKeys(String[] configKeys) throws IllegalAccessException, InstantiationException;

    /**
     * 根据服务，查找配置
     */
    List<AppConfigVO> queryConfigByServerId(String serverId) throws IllegalAccessException, InstantiationException;

    /**
     * 根据版本id，重新构建config json
     * @param configVersionId
     */
    void rebuildConfigJson(String configVersionId) throws BasicOperationException;
}
