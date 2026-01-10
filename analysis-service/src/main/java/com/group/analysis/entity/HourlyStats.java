package com.group.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hourly_stats")
public class HourlyStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime statHour;
    private Long anchorId;
    private String anchorName;
    private Integer anchorGender; // 新增字段
    private Integer viewerGender;
    private BigDecimal totalAmount;
}
