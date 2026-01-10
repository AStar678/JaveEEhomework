package com.group.finance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sharing_ratio")
public class SharingRatio {
    @TableId
    private Long anchorId;
    private String anchorName;
    private BigDecimal ratio;
}
