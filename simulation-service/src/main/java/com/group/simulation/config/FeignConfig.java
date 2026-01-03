/**
 * @FileName: FeignConfig.java
 * @Author: 陈子聪
 * @Date: 2026-01-03
 * @Description: Feign客户端配置类，用于配置Feign请求的超时时间等参数
 * @History:
 * 2026-01-03 陈子聪 创建文件并配置Feign请求超时时间
 */
package com.group.simulation.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignOptions() {
        int connectTimeoutMillis = 5000;
        int readTimeoutMillis = 10000;
        return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
    }
}