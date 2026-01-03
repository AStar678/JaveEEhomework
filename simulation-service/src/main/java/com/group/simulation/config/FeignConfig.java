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