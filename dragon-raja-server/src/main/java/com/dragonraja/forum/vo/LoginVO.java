package com.dragonraja.forum.vo;

import lombok.Data;

/**
 * 登录返回 VO
 * 包含 JWT Token 和基本用户信息
 *
 * @author Kou
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色（USER/ADMIN） */
    private String role;

    /** 头像URL */
    private String avatar;
}
