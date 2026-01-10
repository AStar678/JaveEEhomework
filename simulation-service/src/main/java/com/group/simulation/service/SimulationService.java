package com.group.simulation.service;

import com.group.common.entity.DonationRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class SimulationService {

    @Autowired
    private RestTemplate restTemplate;

    private ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong requestCount = new AtomicLong(0);
    private final Random random = new Random();

    // 模拟数据范围
    private static final int ANCHOR_COUNT = 100;
    private static final int VIEWER_COUNT = 300000;

    public void startSimulation(int threadCount) {
        if (isRunning.compareAndSet(false, true)) {
            executorService = Executors.newFixedThreadPool(threadCount);
            log.info("开始模拟打赏，启动线程数: {}", threadCount);

            for (int i = 0; i < threadCount; i++) {
                executorService.submit(this::simulationTask);
            }

            // 启动一个监控线程打印QPS
            new Thread(this::monitorTask).start();
        } else {
            log.warn("模拟服务已经在运行中");
        }
    }

    public void stopSimulation() {
        if (isRunning.compareAndSet(true, false)) {
            if (executorService != null) {
                executorService.shutdownNow();
            }
            log.info("模拟服务停止中...");
        }
    }

    private void simulationTask() {
        while (isRunning.get()) {
            try {
                DonationRecord record = generateRandomRecord();
                String url = "http://localhost:8081/viewer/donate";
                
                // 发送请求
                restTemplate.postForObject(url, record, Object.class);
                
                requestCount.incrementAndGet();
                
                // 简单的限流，防止把本机打死，实际压测可以去掉
                // TimeUnit.MILLISECONDS.sleep(1); 
            } catch (Exception e) {
                // 忽略异常，只打印少量日志防止刷屏
                if (requestCount.get() % 1000 == 0) {
                    log.error("模拟请求失败: {}", e.getMessage());
                }
            }
        }
    }

    private DonationRecord generateRandomRecord() {
        DonationRecord record = new DonationRecord();
        
        // 随机主播 1-100
        long anchorId = random.nextInt(ANCHOR_COUNT) + 1;
        record.setAnchorId(anchorId);
        // anchorName 和 anchorGender 由后端补全，这里不需要传
        
        // 随机观众 1-300000 (模拟名字)
        long viewerId = random.nextInt(VIEWER_COUNT) + 1;
        // record.setViewerId(viewerId); // ID 由后端生成或查询
        record.setViewerName("观众" + viewerId);
        record.setViewerGender(random.nextBoolean() ? 1 : 2); // 随机观众性别
        
        // 随机金额 10-500
        record.setAmount(new BigDecimal(random.nextInt(491) + 10));
        
        record.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        return record;
    }

    private void monitorTask() {
        long lastCount = 0;
        while (isRunning.get()) {
            try {
                TimeUnit.SECONDS.sleep(1);
                long currentCount = requestCount.get();
                long qps = currentCount - lastCount;
                lastCount = currentCount;
                log.info("当前总请求数: {}, QPS: {}", currentCount, qps);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
