package com.jw.screw.admin.sys.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.validate.ExistMapper;
import com.jw.screw.admin.sys.config.dto.AppConfigQueryDTO;
import com.jw.screw.admin.sys.config.entity.AppConfig;
import com.jw.screw.admin.sys.config.model.AppConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppConfigDao extends BaseMapper<AppConfig>, ExistMapper {

    /**
     * 查询分页
     * @param page {@link Page}
     * @param appConfigQueryDTO {@link AppConfigQueryDTO}
     * @return {@link AppConfigVO}
     */
    List<AppConfigVO> queryByPage(Page<AppConfigVO> page, @Param("queryDTO")AppConfigQueryDTO appConfigQueryDTO);

    @Override
    int isExist(@Param("whereSql") String whereSql);
}
