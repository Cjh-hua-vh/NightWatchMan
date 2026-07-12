package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * 对应数据库 comment 表
 *
 * @author Kou
 */
@Data
@TableName("comment")
public class Comment {

    /** 评论ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子ID */
    private Long postId;

    /** 评论者用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 回复的评论ID（一级评论为null） */
    private Long replyToId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
