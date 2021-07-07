package com.jw.screw.admin.sys.server.service;


import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.sys.server.dto.AppServerAddDTO;
import com.jw.screw.admin.sys.server.dto.AppServerUpdateDTO;
import com.jw.screw.admin.sys.server.model.AppServerVO;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * 应用服务
 * @author jiangw
 * @date 2020/11/13 10:01
 * @since 1.0
 */
public interface AppServerService {

    /**
     * 增加应用服务
     */
    Integer addAppServer(AppServerAddDTO appServerAddDto) throws InstantiationException, IllegalAccessException, BasicOperationException, UnknowOperationException, NoSuchMethodException, InvocationTargetException;

    /**
     * 判断服务是否可以创建
     */
    Integer isExistServer(AppServerAddDTO addDTO) throws BasicOperationException;

    /**
     * 更新应用服务
     */
    Integer updateAppServer(AppServerUpdateDTO appServerUpdateDTO) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, BasicOperationException;

    /**
     * 查询所有的应用服务
     */
    List<AppServerVO> queryAppServers() throws IllegalAccessException, InstantiationException;

    /**
     * 查询server根据code
     */
    AppServerVO queryAppServersByServerCode(String serverCode) throws Exception;

    /**
     * 根据id删除应用服务，需级联删除配置，配置版本、配置数据
     * @return
     */
    Integer deleteAppServerById(String serverIds) throws BasicOperationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    /**
     * 获取服务的目录
     */
    Map<String, List<AppServerVO>> getServerDirectory(String operate) throws InstantiationException, IllegalAccessException, UnknowOperationException;

}
