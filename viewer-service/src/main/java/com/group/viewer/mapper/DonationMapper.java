package com.group.viewer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.group.viewer.dto.TopViewerDTO;
import com.group.common.entity.DonationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DonationMapper extends BaseMapper<DonationRecord> {

    @Select("SELECT viewer_id, viewer_name, SUM(amount) as total_amount " +
            "FROM donation_record " +
            "WHERE anchor_id = #{anchorId} " +
            "GROUP BY viewer_id, viewer_name " +
            "ORDER BY total_amount DESC " +
            "LIMIT 10")
    List<TopViewerDTO> getTopViewersByAnchorId(@Param("anchorId") Long anchorId);
}
