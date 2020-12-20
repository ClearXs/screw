package com.jw.screw.admin.sys.datasource.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jw.screw.admin.common.validate.ExistMapper;
import com.jw.screw.admin.sys.datasource.entity.Datasource;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface DatasourceDao extends BaseMapper<Datasource>, ExistMapper {

    List<DatasourceVO> listAll();

    @Override
    int isExist(String whereSql);
}
