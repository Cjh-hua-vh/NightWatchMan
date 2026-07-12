package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import java.util.List;

/**
 * 发布/编辑帖子 DTO
 *
 * @author Kou
 */
@Data
public class PostDTO {

    /** 标题（最长200字） */
    @NotBlank(message = "标题不能为空")
    @Length(max = 200, message = "标题最长200字")
    private String title;

    /** 正文内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类：1-闲聊 2-史料 3-委托 4-交换 */
    @NotNull(message = "分类不能为空")
    private Integer category;
    /** 图片URL列表（可选） */
    private List<String> images;
}
