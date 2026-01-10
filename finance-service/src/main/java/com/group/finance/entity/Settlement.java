package com.group.finance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("settlement")
public class Settlement {
    @TableId
    private Long anchorId;
    private String anchorName;
    private BigDecimal totalRevenue; // 新增：打赏总流水
    private BigDecimal totalSettledAmount;
    private BigDecimal totalWithdrawnAmount;
    private LocalDateTime lastUpdateTime;
}
