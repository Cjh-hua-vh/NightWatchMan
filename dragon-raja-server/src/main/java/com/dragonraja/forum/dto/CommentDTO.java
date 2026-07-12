package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 发表评论 DTO
 *
 * @author Kou
 */
@Data
public class CommentDTO {

    /** 所属帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 评论内容（最长1000字） */
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 1000, message = "评论最长1000字")
    private String content;

    /** 回复的评论ID（可选，回复评论时填写） */
    private Long replyToId;
}
