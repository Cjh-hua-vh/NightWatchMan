package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 对应数据库 post 表
 *
 * @author Kou
 */
@Data
@TableName("post")
public class Post {

    /** 帖子ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 正文内容 */
    private String content;

    /** 分类：1-闲聊 2-史料 3-委托 4-交换 */
    private Integer category;

    /** 发布者用户ID */
    private Long userId;

    /** 浏览量 */
    private Integer viewCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
