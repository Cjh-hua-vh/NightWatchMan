package com.dragonraja.forum.vo;

import lombok.Data;

import java.util.List;

/**
 * 帖子列表项 VO
 * 列表展示用的精简版帖子信息，关联用户昵称和头像
 * 日期格式：String（yyyy-MM-dd HH:mm:ss）
 *
 * @author Kou
 */
@Data
public class PostListVO {

    /** 帖子ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 分类：1-闲聊 2-史料 3-委托 4-交换 */
    private Integer category;

    /** 分类中文名 */
    private String categoryDesc;

    /** 发布者昵称 */
    private String nickname;
    /** 发布者用户ID */
    private Long userId;

    /** 发布者头像 */
    private String avatar;

    /** 浏览量 */
    private Integer viewCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 帖子图片URL列表 */
    private List<String> images;

    /** 内容片段（搜索时返回，用于关键词高亮展示） */
    private String content;
}
