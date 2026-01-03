package com.group.simulation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.group.common.dto.Result;
import com.group.simulation.dto.DonateRequest;
import com.group.simulation.dto.StartRoomRequest;

/**
 * 观众服务Feign客户端
 */
@FeignClient(name = "viewer-service", path = "/api/viewer")
public interface ViewerFeignClient {

    /**
     * 启动直播间
     */
    @PostMapping("/start-room")
    Result<Boolean> startRoom(@RequestBody StartRoomRequest request);

    /**
     * 关闭直播间
     */
    @PostMapping("/stop-room")
    Result<Boolean> stopRoom(@RequestParam("roomId") Long roomId);

    /**
     * 模拟打赏
     */
    @PostMapping("/donate")
    Result<Boolean> donate(@RequestBody DonateRequest request);

    /**
     * 观众进入直播间
     */
    @PostMapping("/enter-room")
    Result<Boolean> enterRoom(@RequestParam("roomId") Long roomId, @RequestParam("viewerId") Long viewerId);
}