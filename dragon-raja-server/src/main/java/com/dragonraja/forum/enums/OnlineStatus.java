package com.dragonraja.forum.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 在线状态枚举
 * 0-离线 / 1-在线
 *
 * @author Kou
 */
@Getter
@AllArgsConstructor
public enum OnlineStatus {

    /** 离线 */
    OFFLINE(0, "离线"),
    /** 在线 */
    ONLINE(1, "在线");

    /** 状态码 */
    private final int code;

    /** 描述 */
    private final String desc;

    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return OnlineStatus 枚举，未找到返回 null
     */
    public static OnlineStatus fromCode(int code) {
        for (OnlineStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
