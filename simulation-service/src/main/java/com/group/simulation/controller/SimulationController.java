package com.group.simulation.controller;

import com.group.common.dto.Result;
import com.group.simulation.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*") // 允许跨域
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping("/start")
    public Result<String> startSimulation(@RequestParam(defaultValue = "20") int threads) {
        simulationService.startSimulation(threads);
        return Result.success("模拟服务已启动，线程数: " + threads);
    }

    @GetMapping("/stop")
    public Result<String> stopSimulation() {
        simulationService.stopSimulation();
        return Result.success("模拟服务已停止");
    }
}
