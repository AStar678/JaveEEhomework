package com.group.analysis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("viewer_profile")
public class ViewerProfile {
    @TableId
    private Long viewerId;
    private String viewerName;
    private BigDecimal totalAmount;
    private BigDecimal percentile;
    private String tagLabel;
    private LocalDateTime updateTime;
}
