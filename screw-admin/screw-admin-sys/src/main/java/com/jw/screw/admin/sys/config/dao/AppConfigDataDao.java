package com.jw.screw.admin.sys.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.validate.ExistMapper;
import com.jw.screw.admin.sys.config.dto.data.AppConfigDataQueryDTO;
import com.jw.screw.admin.sys.config.entity.AppConfigData;
import com.jw.screw.admin.sys.config.model.AppConfigDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppConfigDataDao extends BaseMapper<AppConfigData>, ExistMapper {

    List<AppConfigDataVO> queryByPage(Page<AppConfigDataVO> page, @Param("queryDTO") AppConfigDataQueryDTO appConfigQueryDTO);

    @Override
    int isExist(@Param("whereSql") String whereSql);
}
