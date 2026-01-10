package com.group.analysis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_progress")
public class EtlProgress {
    @TableId
    private String taskName;
    private Long lastProcessedId;
    private LocalDateTime lastProcessedTime;
    private LocalDateTime updateTime;
}
