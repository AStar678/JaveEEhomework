/**
 * @FileName: DonateRequest.java
 * @Author: 陈子聪
 * @Date: 2026-01-03
 * @Description: 打赏请求数据传输对象，用于封装打赏相关参数
 * @History:
 * 2026-01-03 陈子聪 创建文件并定义打赏请求参数
 */
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