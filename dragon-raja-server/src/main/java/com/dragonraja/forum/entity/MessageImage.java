package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_image")
public class MessageImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long messageId;
    private String url;
    private Integer sortOrder;
    private LocalDateTime createdTime;
}
