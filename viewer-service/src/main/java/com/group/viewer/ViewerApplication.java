package com.group.viewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@ComponentScan(basePackages = {"com.group.viewer", "com.group.common"})
public class ViewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewerApplication.class, args);
    }
}
