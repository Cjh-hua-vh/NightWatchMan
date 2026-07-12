package com.dragonraja.forum.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新个人资料 DTO
 * 所有字段可选，仅更新传入的字段
 *
 * @author Kou
 */
@Data
public class UpdateProfileDTO {

    /** 昵称（最多8位） */
    @Length(max = 10, message = "昵称长度最多10个字符")
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 个性签名 */
    private String signature;

    /** 血统等级：D/C/B/A/S */
    private String bloodlineGrade;

    /** 派系：狮心会/学生会/执行部/教授 */
    private String faction;

    /** 言灵 */
    private String yanling;
}
