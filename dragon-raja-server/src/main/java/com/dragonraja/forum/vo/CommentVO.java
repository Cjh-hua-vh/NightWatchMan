package com.dragonraja.forum.vo;

import lombok.Data;

/**
 * 评论 VO
 * 包含评论信息 + 评论者用户信息
 * 日期格式：String（yyyy-MM-dd HH:mm:ss）
 *
 * @author Kou
 */
@Data
public class CommentVO {

    /** 评论ID */
    private Long id;

    /** 所属帖子ID */
    private Long postId;

    /** 评论者用户ID */
    private Long userId;

    /** 评论者用户名 */
    private String username;

    /** 评论者昵称 */
    private String nickname;

    /** 评论者头像 */
    private String avatar;

    /** 评论者血统等级 */
    private String bloodlineGrade;

    /** 评论内容 */
    private String content;

    /** 回复的评论ID */
    private Long replyToId;

    /** 被回复者用户名 */
    private String replyToUsername;

    /** 被回复者昵称 */
    private String replyToNickname;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;
}
