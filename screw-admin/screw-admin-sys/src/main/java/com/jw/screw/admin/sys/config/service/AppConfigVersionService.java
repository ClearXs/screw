package com.jw.screw.admin.sys.config.service;


import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionAddDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionQueryDTO;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;

/**
 * 配置版本操作
 * @author jiangw
 * @date 2020/11/13 14:05
 * @since 1.0
 */
public interface AppConfigVersionService {

    /**
     * 添加
     */
    Integer addAppConfigVersion(AppConfigVersionAddDTO appConfigVersionAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 对于一个版本的状态，有且仅有一个开启状态，并且开启状态不能转换为其他状态。它的操作应该存在几种情况。
     * 1.一个关闭状态更新为开启
     * 2.一个未发布状态跟新为开启
     */
    Integer openedVersion(AppConfigVersionUpdateDTO appConfigVersionUpdateDTO) throws InstantiationException, IllegalAccessException, UnknowOperationException, BasicOperationException;

    /**
     * 删除
     */
    Integer removeAppConfigVersion(String id);

    /**
     * 根据分页查询
     */
    PageResult<AppConfigVersionVO> queryAppConfigVersion(AppConfigVersionQueryDTO appConfigVersionQueryDTO);


}
