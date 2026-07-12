package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布/编辑公告 DTO
 *
 * @author Kou
 */
@Data
public class AnnouncementDTO {

    /** 公告标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 公告内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 是否置顶：0-否 1-是（默认1） */
    private Integer isTop;
}
