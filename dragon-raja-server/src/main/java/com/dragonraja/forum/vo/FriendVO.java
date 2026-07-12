package com.dragonraja.forum.vo;

import lombok.Data;

@Data
public class FriendVO {
    private Long id;
    private Long friendId;
    private String username;
    private String nickname;
    private String avatar;
    private String bloodlineGrade;
    private String faction;
    private Integer onlineStatus;
    private Integer status;
    private String signature;
    private String yanling;
    private String createTime;
}
