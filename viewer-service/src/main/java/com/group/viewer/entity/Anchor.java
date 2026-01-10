package com.group.viewer.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anchor")
public class Anchor {
    @TableId
    private Long id;
    private String name;
    private Integer gender;
}
