/**
 * @FileName: SimulationScheduledTasks.java
 * @Author: 陈子聪
 * @Date: 2026-01-03
 * @Description: 模拟服务定时任务类，用于自动执行各种模拟任务
 * @History:
 * 2026-01-03 陈子聪 创建文件并实现定时任务功能
 */
package com.group.simulation.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SimulationScheduledTasks {

    @Autowired
    private SimulationService simulationService;

    private final Random random = new Random();

    // 每30分钟自动启动一个新的直播间
    @Scheduled(cron = "0 0/30 * * * ?")
    public void autoStartRoom() {
        long roomId = System.currentTimeMillis();
        long hostId = 10000 + random.nextInt(1000);
        String roomName = "自动测试直播间" + roomId;
        String category = "游戏";
        String coverUrl = "http://example.com/cover.jpg";
        
        com.group.simulation.dto.StartRoomRequest request = new com.group.simulation.dto.StartRoomRequest();
        request.setRoomId(roomId);
        request.setHostId(hostId);
        request.setRoomName(roomName);
        request.setCategory(category);
        request.setCoverUrl(coverUrl);
        
        simulationService.startRoom(request);
    }

    // 每5分钟自动模拟一次批量捐赠
    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoBatchDonate() {
        // 随机选择一个直播间ID进行模拟
        long roomId = System.currentTimeMillis() / 1000000;
        int batchSize = 10 + random.nextInt(50);
        
        simulationService.batchSimulateDonate(roomId, batchSize);
    }

    // 每2分钟自动模拟观众进入直播间
    @Scheduled(cron = "0 0/2 * * * ?")
    public void autoEnterRoom() {
        // 随机选择一个直播间ID进行模拟
        long roomId = System.currentTimeMillis() / 1000000;
        int viewerCount = 50 + random.nextInt(200);
        
        simulationService.simulateEnterRoom(roomId, viewerCount);
    }
}