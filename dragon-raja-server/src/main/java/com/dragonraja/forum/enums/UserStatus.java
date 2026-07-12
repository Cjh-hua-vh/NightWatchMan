package com.dragonraja.forum.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 0-待审核 / 1-正常 / 2-封禁
 *
 * @author Kou
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /** 待审核 */
    PENDING(0, "待审核"),
    /** 正常 */
    NORMAL(1, "正常"),
    /** 封禁 */
    BANNED(2, "封禁");

    /** 状态码 */
    private final int code;

    /** 描述 */
    private final String desc;

    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return UserStatus 枚举，未找到返回 null
     */
    public static UserStatus fromCode(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
