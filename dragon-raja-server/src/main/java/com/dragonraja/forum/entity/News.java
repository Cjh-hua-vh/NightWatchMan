package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻实体
 */
@Data
@TableName("news")
public class News {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String cover;
    private String summary;
    private String author;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
