# 模拟服务模块工作文档

## 文档信息
- **作者**: 陈子聪
- **日期**: 2026-01-03
- **版本**: v1.0

## 1. 模块概述
模拟服务模块(Simulation Service)是直播平台后端系统的核心组件之一，主要负责模拟直播间的各种操作，包括直播间的启动与关闭、观众进入直播间、打赏等功能。该模块支持实时调用和定时自动模拟两种方式，可用于系统测试、性能压测和演示等场景。

## 2. 快速调用指南
   
### 2.1 服务地址
默认服务端口: 8084（支持自动端口冲突检测和分配）
基础URL: `http://localhost:8084/api/simulation`

### 2.2 主要API接口

#### 2.2.1 直播间管理

##### 启动直播间
```bash
POST /start-room
Content-Type: application/json

{
  "roomId": 10001,
  "hostId": 20001,
  "roomName": "测试直播间",
  "category": "游戏",
  "coverUrl": "http://example.com/cover.jpg"
}
```

##### 关闭直播间
```bash
POST /stop-room?roomId=10001
```

#### 2.2.2 模拟操作

##### 单条打赏模拟
```bash
POST /donate
Content-Type: application/json

{
  "roomId": 10001,
  "viewerId": 30001,
  "amount": 100,
  "giftId": 1
}
```

##### 批量打赏模拟
```bash
POST /batch-donate
Content-Type: application/json

{
  "roomId": 10001,
  "count": 100
}
```

##### 观众进入直播间
```bash
POST /enter-room?roomId=10001&count=500
```

#### 2.2.3 配置与状态查询

##### 查询当前配置
```bash
GET /config
```

##### 更新配置
```bash
POST /config
Content-Type: application/json

{
  "useRealService": true,
  "minDonateAmount": 1,
  "maxDonateAmount": 500
}
```

##### 查询当前模拟状态
```bash
GET /status
```

## 3. 架构设计

### 3.1 分层架构
模拟服务模块采用经典的分层架构设计：

1. **控制层(Controller)**：处理HTTP请求，提供REST API接口
2. **服务层(Service)**：实现核心业务逻辑
3. **客户端层(Client)**：与其他服务进行通信
4. **配置层(Config)**：管理服务配置
5. **数据传输层(DTO)**：定义数据传输对象

### 3.2 核心组件

| 组件 | 职责 | 实现类 |
|------|------|--------|
| 控制器 | 提供REST API接口 | SimulationController.java |
| 模拟服务 | 实现核心模拟逻辑 | SimulationService.java |
| 定时任务 | 自动执行模拟操作 | SimulationScheduledTasks.java |
| 线程池 | 处理并发模拟任务 | ThreadPoolConfig.java |
| Feign客户端 | 与观众服务通信 | ViewerFeignClient.java |
| Mock服务 | 提供模拟响应 | MockService.java |

### 3.3 技术栈
- **框架**: Spring Boot 2.7.12 + Spring Cloud
- **通信**: Feign
- **并发**: ThreadPoolTaskExecutor + CompletableFuture
- **定时任务**: Spring Scheduled
- **代码生成**: Lombok
- **构建工具**: Maven

## 4. 核心实现细节

### 4.1 直播间状态管理

使用`ConcurrentHashMap`来存储直播间状态信息，确保线程安全：

```java
private Map<Long, RoomStatus> roomStatusMap = new ConcurrentHashMap<>();
```

`RoomStatus`类封装了直播间的详细状态信息：
- 基本信息：房间ID、房间名称、活跃状态
- 观众信息：当前观众数
- 打赏信息：总打赏次数、总打赏金额

### 4.2 并发模拟实现

批量模拟操作采用线程池实现并发处理：

```java
@Bean("simulationExecutor")
public ThreadPoolTaskExecutor simulationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setKeepAliveSeconds(keepAliveSeconds);
    executor.setThreadNamePrefix("Simulation-Thread-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

使用`CountDownLatch`来等待所有模拟任务完成：

```java
CountDownLatch latch = new CountDownLatch(count);
// 提交模拟任务...
try {
    latch.await(60, TimeUnit.SECONDS);
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

### 4.3 真实服务与Mock服务切换

支持通过配置参数`useRealService`切换真实服务调用和Mock服务响应：

```java
private ViewerFeignClient getClient() {
    return useRealService ? viewerFeignClient : mockService;
}
```

### 4.4 定时自动模拟

通过Spring Scheduled实现定时自动模拟功能：

- 每30分钟自动启动一个新直播间
- 每5分钟自动执行一次批量打赏
- 每2分钟自动模拟观众进入直播间

### 4.5 端口冲突检测

实现了自动端口冲突检测和分配机制，避免服务启动失败：

```java
private static void checkAndSetPort() {
    // 尝试绑定默认端口
    // 如果失败，从默认端口+1开始寻找可用端口
    // 找到可用端口后设置为当前服务端口
}
```

## 5. 配置说明

### 5.1 核心配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| server.port | 8084 | 服务端口 |
| simulation.use-real-service | false | 是否使用真实服务 |
| simulation.donate.min-amount | 1 | 最小打赏金额 |
| simulation.donate.max-amount | 1000 | 最大打赏金额 |
| simulation.donate.default-gift-id | 1 | 默认礼物ID |
| simulation.thread-pool.core-pool-size | 10 | 线程池核心线程数 |
| simulation.thread-pool.max-pool-size | 50 | 线程池最大线程数 |
| simulation.thread-pool.queue-capacity | 1000 | 线程池队列容量 |
| simulation.thread-pool.keep-alive-seconds | 60 | 线程池线程存活时间(秒) |

### 5.2 配置文件位置

配置文件位于`simulation-service/src/main/resources/application.yml`

## 6. 部署与运行

### 6.1 构建项目

```bash
mvn clean package -DskipTests
```

### 6.2 启动服务

```bash
java -jar simulation-service-1.0.0-SNAPSHOT.jar
```

### 6.3 服务检查

服务启动后，可以通过以下地址检查服务状态：

```bash
GET http://localhost:8084/api/simulation/status
```

## 7. 代码文件结构

```
simulation-service/src/main/java/com/group/simulation/
├── SimulationApplication.java        # 应用启动类
├── client/
│   └── ViewerFeignClient.java        # 观众服务Feign客户端
├── config/
│   ├── FeignConfig.java              # Feign配置
│   └── ThreadPoolConfig.java         # 线程池配置
├── controller/
│   └── SimulationController.java     # REST接口控制器
├── dto/
│   ├── DonateRequest.java            # 打赏请求DTO
│   └── StartRoomRequest.java         # 启动直播间请求DTO
└── service/
    ├── MockService.java              # Mock服务实现
    ├── SimulationScheduledTasks.java # 定时任务
    └── SimulationService.java        # 核心服务实现
```

## 8. 监控与维护

### 8.1 日志输出

服务启动后会在控制台输出详细的日志信息，包括：
- 端口占用情况
- 模拟操作执行结果
- 定时任务执行情况

### 8.2 常见问题排查

1. **端口冲突**：服务会自动检测并分配可用端口
2. **模拟操作失败**：检查配置参数`useRealService`和相关服务是否可用
3. **性能问题**：调整线程池配置参数以优化性能

## 9. 版本历史

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0 | 2026-01-03 | 陈子聪 | 初始版本，实现所有核心功能 |

## 10. 联系方式

如有问题或建议，请联系模块负责人：陈子聪