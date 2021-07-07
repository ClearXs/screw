package com.jw.screw.admin.sys.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jw.screw.admin.common.validate.ExistMapper;
import com.jw.screw.admin.sys.server.entity.AppServer;
import com.jw.screw.admin.sys.server.model.AppServerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用服务
 * @author jiangw
 * @date 2020/11/11 15:49
 * @since 1.0
 */
@Mapper
public interface AppServerDao extends BaseMapper<AppServer>, ExistMapper {

    /**
     * 查找所有服务
     * @return {@link AppServer}
     */
    List<AppServerVO> listAll();

    /**
     * 查询服务目录
     * @param defaultServer 默认服务名称
     * @return 服务
     */
    List<String> queryServerDirectory(String defaultServer);

    /**
     * 查找默认服务
     * @return {@link AppServer}
     */
    List<AppServer> queryDefaultServer();

    /**
     * 删除数据源后更新服务
     * @param dataSourceId 数据源id
     * @return 删除是否成功
     */
    int deleteDataSourceUpdateServer(String dataSourceId);

    @Override
    int isExist(@Param("whereSql") String whereSql);
}
