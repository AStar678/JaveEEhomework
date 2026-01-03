package com.group.simulation.dto;

import lombok.Data;

/**
 * 打赏请求DTO
 */
@Data
public class DonateRequest {
    private Long roomId;        // 直播间ID
    private Long viewerId;      // 观众ID
    private Integer amount;     // 打赏金额
    private Long giftId;        // 礼物ID
}