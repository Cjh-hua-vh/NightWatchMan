package com.dragonraja.forum.vo;

import lombok.Data;
import java.util.List;

@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String sender;
    private String receiverName;
    private String senderName;
    private String type;
    private Long postId;
    private Long commentId;
    private Integer isRead;
    private String createTime;
    private Long unreadCount;
    private String avatar;
    private List<String> images;
}
