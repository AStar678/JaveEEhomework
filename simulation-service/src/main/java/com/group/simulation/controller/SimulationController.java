package com.group.simulation.controller;

import com.group.common.dto.Result;
import com.group.simulation.dto.DonateRequest;
import com.group.simulation.dto.StartRoomRequest;
import com.group.simulation.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    /**
     * 启动直播间
     */
    @PostMapping("/start-room")
    public Result<Boolean> startRoom(@RequestBody StartRoomRequest request) {
        boolean success = simulationService.startRoom(request);
        return Result.success("直播间启动成功", success);
    }

    /**
     * 关闭直播间
     */
    @PostMapping("/stop-room")
    public Result<Boolean> stopRoom(@RequestParam Long roomId) {
        boolean success = simulationService.stopRoom(roomId);
        return Result.success("直播间关闭成功", success);
    }

    /**
     * 模拟打赏
     */
    @PostMapping("/donate")
    public Result<Boolean> simulateDonate(@RequestBody DonateRequest request) {
        boolean success = simulationService.simulateDonate(request);
        return Result.success("打赏模拟成功", success);
    }

    /**
     * 批量模拟打赏
     */
    @PostMapping("/batch-donate")
    public Result<Map<String, Object>> batchSimulateDonate(@RequestBody Map<String, Object> request) {
        Long roomId = Long.parseLong(request.get("roomId").toString());
        Integer count = Integer.parseInt(request.get("count").toString());
        Map<String, Object> result = simulationService.batchSimulateDonate(roomId, count);
        return Result.success("批量打赏模拟成功", result);
    }

    /**
     * 观众进入直播间
     */
    @PostMapping("/enter-room")
    public Result<Boolean> simulateEnterRoom(@RequestParam Long roomId, @RequestParam Integer count) {
        boolean success = simulationService.simulateEnterRoom(roomId, count);
        return Result.success("观众进入直播间模拟成功", success);
    }

    /**
     * 查询当前配置
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = simulationService.getConfig();
        return Result.success("配置查询成功", config);
    }

    /**
     * 更新配置
     */
    @PostMapping("/config")
    public Result<Boolean> updateConfig(@RequestBody Map<String, Object> config) {
        boolean success = simulationService.updateConfig(config);
        return Result.success("配置更新成功", success);
    }

    /**
     * 查询当前模拟状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = simulationService.getStatus();
        return Result.success("状态查询成功", status);
    }
}