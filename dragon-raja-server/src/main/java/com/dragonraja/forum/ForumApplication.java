package com.dragonraja.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 龙族守夜人讨论区 - SpringBoot 启动类
 *
 * @author Kou
 */
@SpringBootApplication
@MapperScan("com.dragonraja.forum.mapper")
@EnableScheduling
public class ForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}
