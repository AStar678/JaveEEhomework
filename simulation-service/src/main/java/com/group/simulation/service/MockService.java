package com.group.simulation.service;

import com.group.common.dto.Result;
import com.group.simulation.client.ViewerFeignClient;
import com.group.simulation.dto.DonateRequest;
import com.group.simulation.dto.StartRoomRequest;
import org.springframework.stereotype.Service;

/**
 * Mock服务实现
 */
@Service("mockService")
public class MockService implements ViewerFeignClient {

    @Override
    public Result<Boolean> startRoom(StartRoomRequest request) {
        // 模拟启动直播间成功
        System.out.println("Mock: 直播间" + request.getRoomName() + "(ID:" + request.getRoomId() + ")启动成功");
        return Result.success(true);
    }

    @Override
    public Result<Boolean> stopRoom(Long roomId) {
        // 模拟关闭直播间成功
        System.out.println("Mock: 直播间(ID:" + roomId + ")关闭成功");
        return Result.success(true);
    }

    @Override
    public Result<Boolean> donate(DonateRequest request) {
        // 模拟打赏成功
        System.out.println("Mock: 观众(ID:" + request.getViewerId() + ")在直播间(ID:" + request.getRoomId() + ")打赏了" + request.getAmount() + "元");
        return Result.success(true);
    }

    @Override
    public Result<Boolean> enterRoom(Long roomId, Long viewerId) {
        // 模拟观众进入直播间成功
        System.out.println("Mock: 观众(ID:" + viewerId + ")进入了直播间(ID:" + roomId + ")");
        return Result.success(true);
    }
}