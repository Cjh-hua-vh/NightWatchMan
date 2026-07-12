package com.dragonraja.forum.vo;

import lombok.Data;

/**
 * 公告 VO
 * 日期格式：String（yyyy-MM-dd HH:mm:ss）
 *
 * @author Kou
 */
@Data
public class AnnouncementVO {

    /** 公告ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;

    /** 更新时间（yyyy-MM-dd HH:mm:ss） */
    private String updateTime;
}
