package com.group.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    RedisAutoConfiguration.class,
    RedisReactiveAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.group.simulation", "com.group.common"})
@EnableFeignClients
@EnableScheduling
@EnableDiscoveryClient(autoRegister = false)
public class SimulationApplication {
    public static void main(String[] args) {
        // 检查端口是否被占用，如果被占用则自动增加端口号
        checkAndSetPort();
        SpringApplication.run(SimulationApplication.class, args);
    }
    
    /**
     * 检查并处理端口冲突问题
     */
    private static void checkAndSetPort() {
        try {
            // 从配置文件中获取默认端口
            int defaultPort = 8084;
            
            // 尝试绑定到默认端口，检查是否可用
            java.net.ServerSocket serverSocket = null;
            try {
                serverSocket = new java.net.ServerSocket(defaultPort);
                serverSocket.close();
                // 如果成功绑定，说明端口可用
                System.out.println("使用默认端口: " + defaultPort);
                return;
            } catch (java.net.BindException e) {
                // 端口绑定失败，说明已被占用
                System.out.println("端口 " + defaultPort + " 已被占用，正在寻找可用端口...");
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
            }
            
            // 寻找可用端口（从默认端口+1开始尝试）
            for (int port = defaultPort + 1; port <= 8099; port++) {
                try {
                    serverSocket = new java.net.ServerSocket(port);
                    serverSocket.close();
                    // 找到可用端口
                    System.out.println("找到可用端口: " + port);
                    System.setProperty("server.port", String.valueOf(port));
                    return;
                } catch (java.net.BindException e) {
                    // 端口不可用，继续尝试下一个
                    continue;
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (Exception e) {
                            // 忽略关闭异常
                        }
                    }
                }
            }
            
            System.err.println("未找到可用端口，请手动检查端口占用情况！");
            System.exit(1);
            
        } catch (Exception e) {
            System.err.println("端口检查失败: " + e.getMessage());
        }
    }
}
