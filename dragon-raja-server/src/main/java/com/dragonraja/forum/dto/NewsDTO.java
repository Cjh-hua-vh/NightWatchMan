package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewsDTO {
    @NotBlank(message = "新闻标题不能为空")
    private String title;
    private String content;
    private String cover;
    private String summary;
    private Integer isTop;
}
