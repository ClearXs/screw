package com.jw.screw.admin.sys.datasource.service;


import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.sys.datasource.dto.DatasourceAddDTO;
import com.jw.screw.admin.sys.datasource.dto.DatasourceUpdateDTO;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;

import java.util.List;

/**
 * 数据源操作
 * @author jiangw
 * @date 2020/11/13 11:00
 * @since 1.0
 */
public interface DatasourceService {

    /**
     * 添加数据源
     */
    Integer addDatasource(DatasourceAddDTO datasourceAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 更新数据源
     */
    Integer updateDatasource(DatasourceUpdateDTO datasourceUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException;

    /**
     * 删除数据源
     */
    Integer deleteDatasource(String id) throws BasicOperationException;

    /**
     * 查找所有数据源
     */
    List<DatasourceVO> queryAllDatasource() throws InstantiationException, IllegalAccessException;

    /**
     * 测试数据库连接
     */
    boolean testConnection(String dataSourceId);
}
