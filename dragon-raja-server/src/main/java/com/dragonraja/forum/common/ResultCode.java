package com.dragonraja.forum.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回码枚举
 * 统一定义系统中使用的 HTTP 状态码与业务码
 *
 * @author Kou
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未授权（未登录或Token失效） */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /** 无权限（已登录但角色不足） */
    FORBIDDEN(403, "无权限执行此操作"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /** 业务错误（通用） */
    BUSINESS_ERROR(1000, "业务处理失败");

    /** 状态码 */
    private final int code;

    /** 描述信息 */
    private final String message;
}
