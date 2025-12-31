package com.group.viewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.group.viewer.entity.DonationRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DonationMapper extends BaseMapper<DonationRecord> {
    // 如果有复杂SQL (比如TOP10)，可以在这里用 @Select 注解编写，或者写在 xml 中
}