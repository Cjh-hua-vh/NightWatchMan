package com.dragonraja.forum.common;

import lombok.Data;

/**
 * 统一返回结果封装
 * 格式：{code, message, data}
 *
 * @param <T> 数据体类型
 * @author Kou
 */
@Data
public class Result<T> {

    /** 状态码：200成功，其他失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 数据体 */
    private T data;

    /**
     * 成功返回（带数据，默认消息 "success"）
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    /**
     * 成功返回（自定义消息 + 数据）
     *
     * @param message 提示消息
     * @param data    返回数据
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 成功返回（仅消息，无数据）
     *
     * @param message 提示消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> success(String message) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        return r;
    }

    /**
     * 失败返回（自定义状态码 + 消息）
     *
     * @param code    状态码
     * @param message 提示消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 失败返回（默认 500 + 消息）
     *
     * @param message 提示消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
