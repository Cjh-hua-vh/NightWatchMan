package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class MessageDTO {
    private Long receiverId;
    @NotBlank(message = "消息标题不能为空")
    private String title;
    @NotBlank(message = "消息内容不能为空")
    private String content;
    private List<String> images;
}
