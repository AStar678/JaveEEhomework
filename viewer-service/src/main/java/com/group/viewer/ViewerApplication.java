package com.group.viewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan; // 导入这个

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.group.viewer", "com.group.common"})
public class ViewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewerApplication.class, args);
    }
}
