package com.jw.screw.calculate.spark.recoder.mapper;

import com.jw.screw.calculate.spark.model.SourceStatistics;
import com.jw.screw.storage.QueryFilter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SourceStatisticsMapper {

    int insert(SourceStatistics statistics);

    List<SourceStatistics> query(QueryFilter<SourceStatistics> queryFilter);
}
