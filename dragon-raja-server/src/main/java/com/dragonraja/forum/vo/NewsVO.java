package com.dragonraja.forum.vo;

import lombok.Data;

@Data
public class NewsVO {
    private Long id;
    private String title;
    private String content;
    private String cover;
    private String summary;
    private String author;
    private Integer isTop;
    private String createTime;
    private String updateTime;
}
