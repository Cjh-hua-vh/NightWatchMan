package com.dragonraja.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表
 *
 * @author Kou
 */
@Data
@TableName("user")
public class User {

    /** 用户ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号，唯一） */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

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

    /** 最后活跃时间（心跳） */
    private LocalDateTime lastActiveTime;

    /** 角色：USER-普通用户 ADMIN-管理员 */
    private String role;

    /** 个性签名 */
    private String signature;

    /** 言灵 */
    private String yanling;

    /** 血型 */
    private String bloodType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
