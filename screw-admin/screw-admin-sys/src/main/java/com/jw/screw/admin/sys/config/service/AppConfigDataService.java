package com.jw.screw.admin.sys.config.service;


import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.model.PageResult;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataAddDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataQueryDTO;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataUpdateDTO;
import com.jw.screw.admin.sys.config.model.AppConfigDataVO;

import java.util.List;

/**
 * TDD
 * @author jiangw
 * @date 2020/11/13 14:06
 * @since 1.0
 */
public interface AppConfigDataService {

    /**
     * 分页查询配置数据
     */
    PageResult<AppConfigDataVO> queryAppConfigData(AppConfigDataQueryDTO appConfigDataQueryDTO);

    /**
     * 添加配置数据
     */
    Integer addAppConfigData(List<AppConfigDataAddDTO> appConfigDataAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 更新配置数据
     */
    Integer updateAppConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 保存配置数据
     * 1.配置保存，创建一个新的配置版本，此时版本号为空，并且状态是未开启状态。更新或增加配置数据，更新对应配置文件的值
     * 2.配置发布，创建一个新的配置版本，此时版本号为最新版本号的递增，并且状态是开启状态，更新或增加配置数据
     * 3.配置修改，更新或增加配置数据
     * @param appConfigDataUpdateDTO 配置的数据
     * @param logicOperate 进行上述三个参数的标识符
     */
    Integer saveAppConfigData(List<AppConfigDataUpdateDTO> appConfigDataUpdateDTO, String logicOperate) throws UnknowOperationException, InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 移除配置数据
     */
    Integer removeAppConfigData(List<AppConfigDataVO> ids) throws BasicOperationException;
}
