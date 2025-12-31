package com.group.common.util;

import cn.hutool.core.util.IdUtil;

public class IdGenerator {

    /**
     * 生成不带中划线的 UUID (32位)
     */
    public static String uuid() {
        return IdUtil.simpleUUID();
    }

    /**
     * 生成雪花算法 ID (Long类型，有序，适合做数据库主键)
     */
    public static long snowflakeId() {
        return IdUtil.getSnowflake(1, 1).nextId();
    }
}