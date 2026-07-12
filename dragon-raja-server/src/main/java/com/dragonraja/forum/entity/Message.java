package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String sender;
    private String type;
    private Long postId;
    private Long commentId;
    private Integer isRead;
    private LocalDateTime createTime;
}
