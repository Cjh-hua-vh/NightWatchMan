package com.dragonraja.forum.vo;

import lombok.Data;
import java.util.List;

/**
 * 帖子详情 VO
 * 包含帖子完整信息 + 发布者用户信息
 * 日期格式：String（yyyy-MM-dd HH:mm:ss）
 *
 * @author Kou
 */
@Data
public class PostVO {

    /** 帖子ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 正文内容 */
    private String content;

    /** 分类：1-闲聊 2-史料 3-委托 4-交换 */
    private Integer category;

    /** 分类中文名 */
    private String categoryDesc;

    /** 发布者用户ID */
    private Long userId;

    /** 发布者用户名 */
    private String username;

    /** 发布者昵称 */
    private String nickname;

    /** 发布者头像 */
    private String avatar;

    /** 发布者血统等级 */
    private String bloodlineGrade;

    /** 发布者派系 */
    private String faction;

    /** 发布者言灵 */
    private String yanling;

    /** 浏览量 */
    private Integer viewCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 更新时间（yyyy-MM-dd HH:mm:ss） */
    private String updateTime;

    /** 帖子图片URL列表 */
    private List<String> images;
}
