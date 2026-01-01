# 🎥 Live Streaming Backend System (直播平台后端系统)

## 📖 项目简介
这是我们的 JavaEE 课程作业项目——直播平台后端微服务系统。
项目基于 **Spring Boot 2.7.12 + Spring Cloud Alibaba** 构建，采用 Maven 多模块架构。

目前**基础架构**和**观众服务 (Viewer Service)** 已经搭建完成并测试通过。请各位合作者按照下方的任务分配，认领并开发各自的模块。

---

## 🏗 项目结构

```text
live-streaming-parent (根目录，父工程)
├── common-module       [已完成] 公共模块 (工具类、统一异常处理、统一返回结果、拦截器)
├── viewer-service      [已完成] 观众服务 (处理观众打赏、信息管理)
├── finance-service     [待开发] 财务服务 (处理钱包、充值、提现)
├── analysis-service    [待开发] 数据分析服务 (处理直播数据统计、榜单)
└── simulation-service  [待开发] 模拟服务 (模拟高并发流量、模拟直播间互动)

```

---

## ✅ 我已经完成的工作 (Status Update)

1. **项目骨架搭建**：
* 配置了父工程 `pom.xml`，统一管理了 Spring Boot (2.7.12)、Spring Cloud (2021.0.x) 及 MySQL、MyBatis-Plus 的版本。
* 解决了 JDK 17 与 Lombok 的兼容性问题。


2. **公共模块 (Common Module)**：
* 封装了统一响应对象 `Result<T>`。
* 实现了全局异常处理 `GlobalExceptionHandler`。
* 配置了 `TraceIdInterceptor` 用于全链路日志追踪。


3. **基础设施对接**：
* 集成了 **Consul** 作为服务注册中心。
* 集成了 **MyBatis-Plus** 进行数据库操作。
* 集成了 **Redis** 基础配置。


4. **观众服务 (Viewer Service)**：
* 完成了观众打赏接口 `/viewer/donate` 的开发与联调。
* 验证了数据库写入和服务注册流程。



---

## 💻 快速开始 (环境配置)

在开始写代码前，请确保你的本地环境满足以下要求，否则代码会报错。

### 1. 依赖环境

* **JDK**: 必须使用 **JDK 17** (推荐 Microsoft OpenJDK 17)。
* **Maven**: IDEA 自带或本地安装均可。
* **MySQL**: 8.0+，请导入 `sql` 文件夹下的初始脚本。
* **Redis**: 启动本地 Redis (默认端口 6379)。
* **Consul**: 必须安装并启动 Consul。
* 启动命令：`consul agent -dev`
* 访问控制台：`http://localhost:8500`



### 2. IDEA 配置 (关键！)

1. **Clone 项目**后，右键根目录的 `pom.xml` -> **Add as Maven Project**。
2. 等待 Maven 依赖下载完成（如果爆红，点击 Maven 面板的刷新按钮）。
3. **Project Structure 设置**：
* Project SDK: 选 **17**。
* Modules -> Dependencies: 确保每个模块的 SDK 也选了 **17**。


4. **Lombok 插件**：确保 IDEA 安装了 Lombok 插件并开启了 "Enable annotation processing"。

---

## 🚀 任务分配 (Task Assignments)

请大家认领以下模块进行开发。开发前请参考 `viewer-service` 的代码结构。

### 👤 合作者 A：财务服务 (Finance Service)

* **目标**：管理用户钱包和资金流向。
* **工作目录**：`finance-service`
* **核心功能**：
* 创建 `Wallet` 表（参考 SQL）。
* 实现“用户充值”接口。
* 监听 viewer-service 的打赏动作（可通过 Feign 调用或消息队列，目前先写好扣款接口）。
* **注意**：涉及金额计算请使用 `BigDecimal`。



### 👤 合作者 B：数据分析服务 (Analysis Service)

* **目标**：统计直播间数据，生成报表。
* **工作目录**：`analysis-service`
* **核心功能**：
* 统计今日打赏总额。
* 统计活跃观众人数。
* 提供“主播日榜/周榜”查询接口。
* **建议**：可以尝试用 Redis 的 `ZSet` 来做排行榜。



### 👤 合作者 C：模拟/直播服务 (Simulation Service)

* **目标**：模拟直播间的业务场景或生成测试数据。
* **工作目录**：`simulation-service`
* **核心功能**：
* 模拟主播开播/关播（修改状态）。
* 编写定时任务（Scheduled Task），模拟大量观众同时发弹幕或打赏的场景，以此测试系统压力。



---

## ⚠️ 开发注意事项 (必读)

1. **包扫描问题**：
   如果你在服务中新建了 `Application` 启动类，请务必加上包扫描注解，否则 Common 模块的拦截器不生效！
```java
@ComponentScan(basePackages = {"com.group.你的服务名", "com.group.common"})

```


2. **配置文件**：
   修改 `application.yml` 时，请检查数据库密码是否和你的本地一致（目前默认为 `12345678`）。
3. **依赖引用**：
   如果需要用公共工具类，请在 `pom.xml` 中引入：
```xml
<dependency>
    <groupId>com.group</groupId>
    <artifactId>common-module</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

```






