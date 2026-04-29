package com.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.forum.mapper")  // 启用 Mapper 扫描
@EnableAsync
@EnableAspectJAutoProxy
public class ForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
        System.out.println("========================================");
        System.out.println("Forum System Started Successfully!");
        System.out.println("Test API: http://localhost:8080/api/test/ping");
        System.out.println("User API: http://localhost:8080/api/user");
        System.out.println("========================================");
    }
}