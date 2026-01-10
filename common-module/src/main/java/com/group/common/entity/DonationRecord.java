package com.group.common.entity;

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
    private Integer anchorGender; // 1-男, 2-女
    private Long viewerId;
    private String viewerName;
    private Integer viewerGender; // 1-男, 2-女
    private BigDecimal amount;
    private LocalDateTime donateTime;
    private String traceId;    // 核心：幂等性凭证
    private Integer syncStatus; // 0-未同步
}
