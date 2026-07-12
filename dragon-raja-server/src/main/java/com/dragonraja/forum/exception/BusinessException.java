package com.dragonraja.forum.exception;

import com.dragonraja.forum.common.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在业务逻辑中抛出可预见的错误，由全局异常处理器统一捕获
 *
 * @author Kou
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /**
     * 构造业务异常（指定错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（默认 500 错误码）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    /**
     * 构造业务异常（使用 ResultCode 枚举）
     *
     * @param resultCode 结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 快速创建未授权异常（401）
     *
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    /**
     * 快速创建无权限异常（403）
     *
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN.getCode(), message);
    }
}
