package com.dragonraja.forum.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子分类枚举
 * 1-闲聊 / 2-史料 / 3-委托 / 4-交换
 *
 * @author Kou
 */
@Getter
@AllArgsConstructor
public enum PostCategory {

    /** 闲聊 */
    CHAT(1, "闲聊"),
    /** 史料 */
    HISTORY(2, "史料"),
    /** 委托 */
    QUEST(3, "委托"),
    /** 交换 */
    EXCHANGE(4, "交换");

    /** 分类码 */
    private final int code;

    /** 描述 */
    private final String desc;

    /**
     * 根据 code 获取枚举
     *
     * @param code 分类码
     * @return PostCategory 枚举，未找到返回 null
     */
    public static PostCategory fromCode(int code) {
        for (PostCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取分类描述
     *
     * @param code 分类码
     * @return 分类中文名，未找到返回 "未知"
     */
    public static String getDescByCode(int code) {
        PostCategory category = fromCode(code);
        return category != null ? category.desc : "未知";
    }
}
