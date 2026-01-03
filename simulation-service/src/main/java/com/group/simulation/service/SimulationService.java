/**
 * @FileName: SimulationService.java
 * @Author: 陈子聪
 * @Date: 2026-01-03
 * @Description: 模拟服务核心实现类，提供直播间管理、打赏模拟、观众进入等功能
 * @History:
 * 2026-01-03 陈子聪 创建文件并实现所有模拟服务核心功能
 */
package com.group.simulation.service;

import com.group.common.dto.Result;
import com.group.simulation.client.ViewerFeignClient;
import com.group.simulation.dto.DonateRequest;
import com.group.simulation.dto.StartRoomRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import javax.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟服务实现
 */
@Service
public class SimulationService {

    @Autowired
    private ThreadPoolTaskExecutor simulationExecutor;

    @Autowired
    private ViewerFeignClient viewerFeignClient;

    @Autowired
    private MockService mockService;

    // 配置参数
    @Value("${simulation.use-real-service:false}")
    private boolean useRealService;

    @Value("${simulation.donate.min-amount:1}")
    private Integer minDonateAmount;

    @Value("${simulation.donate.max-amount:1000}")
    private Integer maxDonateAmount;

    @Value("${simulation.donate.default-gift-id:1}")
    private Long defaultGiftId;

    // 状态信息
    private Map<Long, RoomStatus> roomStatusMap = new ConcurrentHashMap<>();
    private AtomicLong nextViewerId = new AtomicLong(10000);
    private Map<String, Object> configMap = new ConcurrentHashMap<>();

    // 初始化配置
    @PostConstruct
    public void init() {
        configMap.put("useRealService", useRealService);
        configMap.put("minDonateAmount", minDonateAmount);
        configMap.put("maxDonateAmount", maxDonateAmount);
        configMap.put("defaultGiftId", defaultGiftId);
    }

    /**
     * 启动直播间
     */
    public boolean startRoom(StartRoomRequest request) {
        ViewerFeignClient client = getClient();
        Result<Boolean> result = client.startRoom(request);
        if (result.getCode() == 200 && result.getData()) {
            roomStatusMap.put(request.getRoomId(), new RoomStatus(request.getRoomId(), request.getRoomName(), true));
        }
        return result.getCode() == 200 && result.getData();
    }

    /**
     * 关闭直播间
     */
    public boolean stopRoom(Long roomId) {
        ViewerFeignClient client = getClient();
        Result<Boolean> result = client.stopRoom(roomId);
        if (result.getCode() == 200 && result.getData()) {
            roomStatusMap.remove(roomId);
        }
        return result.getCode() == 200 && result.getData();
    }

    /**
     * 模拟打赏
     */
    public boolean simulateDonate(DonateRequest request) {
        ViewerFeignClient client = getClient();
        Result<Boolean> result = client.donate(request);
        if (result.getCode() == 200 && result.getData() && roomStatusMap.containsKey(request.getRoomId())) {
            roomStatusMap.get(request.getRoomId()).addDonate(request.getAmount());
        }
        return result.getCode() == 200 && result.getData();
    }

    /**
     * 批量模拟打赏
     */
    public Map<String, Object> batchSimulateDonate(Long roomId, Integer count) {
        if (!roomStatusMap.containsKey(roomId)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "直播间不存在");
            return result;
        }

        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger totalAmount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            simulationExecutor.execute(() -> {
                try {
                    DonateRequest request = new DonateRequest();
                    request.setRoomId(roomId);
                    request.setViewerId(nextViewerId.incrementAndGet());
                    request.setAmount(generateRandomAmount());
                    request.setGiftId(defaultGiftId);

                    if (simulateDonate(request)) {
                        successCount.incrementAndGet();
                        totalAmount.addAndGet(request.getAmount());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRequests", count);
        result.put("successCount", successCount.get());
        result.put("totalAmount", totalAmount.get());
        result.put("durationMs", endTime - startTime);
        result.put("qps", successCount.get() * 1000.0 / (endTime - startTime + 1));

        return result;
    }

    /**
     * 观众进入直播间
     */
    public boolean simulateEnterRoom(Long roomId, Integer count) {
        if (!roomStatusMap.containsKey(roomId)) {
            return false;
        }

        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < count; i++) {
            simulationExecutor.execute(() -> {
                try {
                    ViewerFeignClient client = getClient();
                    Long viewerId = nextViewerId.incrementAndGet();
                    Result<Boolean> result = client.enterRoom(roomId, viewerId);
                    if (result.getCode() == 200 && result.getData()) {
                        successCount.incrementAndGet();
                        roomStatusMap.get(roomId).addViewer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return successCount.get() > 0;
    }

    /**
     * 查询当前配置
     */
    public Map<String, Object> getConfig() {
        return new HashMap<>(configMap);
    }

    /**
     * 更新配置
     */
    public boolean updateConfig(Map<String, Object> config) {
        try {
            if (config.containsKey("useRealService")) {
                useRealService = Boolean.parseBoolean(config.get("useRealService").toString());
                configMap.put("useRealService", useRealService);
            }
            if (config.containsKey("minDonateAmount")) {
                minDonateAmount = Integer.parseInt(config.get("minDonateAmount").toString());
                configMap.put("minDonateAmount", minDonateAmount);
            }
            if (config.containsKey("maxDonateAmount")) {
                maxDonateAmount = Integer.parseInt(config.get("maxDonateAmount").toString());
                configMap.put("maxDonateAmount", maxDonateAmount);
            }
            if (config.containsKey("defaultGiftId")) {
                defaultGiftId = Long.parseLong(config.get("defaultGiftId").toString());
                configMap.put("defaultGiftId", defaultGiftId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询当前模拟状态
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("totalRooms", roomStatusMap.size());
        statusMap.put("activeRooms", roomStatusMap.values().stream().filter(RoomStatus::isActive).count());
        statusMap.put("totalViewers", roomStatusMap.values().stream().mapToInt(RoomStatus::getViewerCount).sum());
        statusMap.put("totalDonations", roomStatusMap.values().stream().mapToInt(RoomStatus::getTotalDonations).sum());
        statusMap.put("totalDonateAmount", roomStatusMap.values().stream().mapToInt(RoomStatus::getTotalDonateAmount).sum());
        statusMap.put("roomDetails", new HashMap<>(roomStatusMap));
        return statusMap;
    }

    /**
     * 获取客户端（真实服务或Mock服务）
     */
    private ViewerFeignClient getClient() {
        return useRealService ? viewerFeignClient : mockService;
    }

    /**
     * 生成随机打赏金额
     */
    private Integer generateRandomAmount() {
        Random random = new Random();
        return random.nextInt(maxDonateAmount - minDonateAmount + 1) + minDonateAmount;
    }

    /**
     * 直播间状态类
     */
    private static class RoomStatus {
        private Long roomId;
        private String roomName;
        private boolean active;
        private AtomicInteger viewerCount = new AtomicInteger(0);
        private AtomicInteger totalDonations = new AtomicInteger(0);
        private AtomicInteger totalDonateAmount = new AtomicInteger(0);

        public RoomStatus(Long roomId, String roomName, boolean active) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.active = active;
        }

        public void addViewer() {
            viewerCount.incrementAndGet();
        }

        public void addDonate(Integer amount) {
            totalDonations.incrementAndGet();
            totalDonateAmount.addAndGet(amount);
        }

        public boolean isActive() {
            return active;
        }

        public int getViewerCount() {
            return viewerCount.get();
        }

        public int getTotalDonations() {
            return totalDonations.get();
        }

        public int getTotalDonateAmount() {
            return totalDonateAmount.get();
        }

        // Getters and toString()
        @Override
        public String toString() {
            return "RoomStatus{" +
                    "roomId=" + roomId +
                    ", roomName='" + roomName + '\'' +
                    ", active=" + active +
                    ", viewerCount=" + viewerCount.get() +
                    ", totalDonations=" + totalDonations.get() +
                    ", totalDonateAmount=" + totalDonateAmount.get() +
                    '}';
        }
    }
}