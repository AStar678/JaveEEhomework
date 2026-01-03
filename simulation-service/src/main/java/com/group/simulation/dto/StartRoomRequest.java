package com.group.simulation.dto;

import lombok.Data;

/**
 * 启动直播间请求DTO
 */
@Data
public class StartRoomRequest {
    private Long roomId;        // 直播间ID
    private Long hostId;        // 主播ID
    private String roomName;    // 直播间名称
    private String category;    // 直播间分类
    private String coverUrl;    // 直播间封面URL
}