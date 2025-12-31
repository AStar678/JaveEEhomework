package com.group.viewer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("donation_record")
public class DonationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long anchorId;
    private String anchorName;
    private Long viewerId;
    private String viewerName;
    private BigDecimal amount;
    private LocalDateTime donateTime;
    private String traceId;    // 核心：幂等性凭证
    private Integer syncStatus; // 0-未同步
}