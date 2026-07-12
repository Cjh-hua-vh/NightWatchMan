package com.dragonraja.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 注册请求 DTO
 *
 * @author Kou
 */
@Data
public class RegisterDTO {

    /** 用户名（3-20位） */
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;

    /** 密码（6-20位） */
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /** 昵称（可选，最多8位） */
    @Length(max = 10, message = "昵称长度最多10个字符")
    private String nickname;

    /** 血统等级（可选，默认D）：D/C/B/A/S */
    private String bloodlineGrade;

    /** 派系（可选）：狮心会/学生会/执行部/教授 */
    private String faction;
    private String yanling;
    private String bloodType;
    private String avatar;
}
