package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体类
 * 对应数据库 announcement 表
 *
 * @author Kou
 */
@Data
@TableName("announcement")
public class Announcement {

    /** 公告ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 发布者用户ID（管理员） */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
