package com.library.common.result;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    public Result() {}

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error() {
        return error(500, "服务器内部错误");
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 分页返回
     */
    public static <T> Result<PageResult<T>> page(PageResult<T> pageData) {
        return new Result<>(200, "success", pageData);
    }

    // Getter and Setter methods
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // Chain methods for fluent API
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
}