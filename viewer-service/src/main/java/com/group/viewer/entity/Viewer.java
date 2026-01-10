package com.group.viewer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("viewer")
public class Viewer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer gender;
}
