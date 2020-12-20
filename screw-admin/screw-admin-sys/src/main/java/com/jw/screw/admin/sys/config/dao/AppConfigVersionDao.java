package com.jw.screw.admin.sys.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.sys.config.dto.version.AppConfigVersionQueryDTO;
import com.jw.screw.admin.sys.config.entity.AppConfigVersion;
import com.jw.screw.admin.sys.config.model.AppConfigVersionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppConfigVersionDao extends BaseMapper<AppConfigVersion> {

    List<AppConfigVersionVO> queryByPage(Page<AppConfigVersionVO> page, @Param("queryDTO") AppConfigVersionQueryDTO appConfigQueryDTO);

    /**
     * 查询最新的版本
     */
    AppConfigVersion queryLatestVersion(String configId);
}
