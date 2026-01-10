package com.group.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.group.analysis.entity.HourlyStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HourlyStatsMapper extends BaseMapper<HourlyStats> {
}
