package com.dragonraja.forum.vo;

import lombok.Data;

/**
 * 用户信息 VO
 * 用于返回用户详情（不含密码）
 * 日期格式：String（yyyy-MM-dd HH:mm:ss）
 *
 * @author Kou
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 血统等级：D/C/B/A/S */
    private String bloodlineGrade;

    /** 派系：狮心会/学生会/执行部/教授 */
    private String faction;

    /** 状态：0-待审核 1-正常 2-封禁 */
    private Integer status;

    /** 在线状态：0-离线 1-在线 */
    private Integer onlineStatus;

    /** 角色（USER/ADMIN） */
    private String role;

    /** 个性签名 */
    private String signature;
    private String yanling;
    private String bloodType;
    private String createTime;
}
