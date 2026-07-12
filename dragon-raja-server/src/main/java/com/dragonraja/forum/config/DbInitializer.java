package com.dragonraja.forum.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化：自动补齐缺失字段
 */
@Slf4j
@Component
public class DbInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DbInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE message ADD COLUMN comment_id BIGINT NULL COMMENT '评论ID' AFTER post_id");
            log.info("已添加 message.comment_id 字段");
        } catch (Exception e) {
            // 字段已存在则忽略
        }
    }
}
