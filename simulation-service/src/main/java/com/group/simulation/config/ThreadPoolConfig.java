/**
 * @FileName: ThreadPoolConfig.java
 * @Author: 陈子聪
 * @Date: 2026-01-03
 * @Description: 线程池配置类，用于配置模拟服务的线程池参数
 * @History:
 * 2026-01-03 陈子聪 创建文件并配置模拟服务线程池
 */
package com.group.simulation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${simulation.thread-pool.core-pool-size:10}")
    private Integer corePoolSize;

    @Value("${simulation.thread-pool.max-pool-size:50}")
    private Integer maxPoolSize;

    @Value("${simulation.thread-pool.queue-capacity:1000}")
    private Integer queueCapacity;

    @Value("${simulation.thread-pool.keep-alive-seconds:60}")
    private Integer keepAliveSeconds;

    /**
     * 模拟服务线程池
     */
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
}