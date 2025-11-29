package com.library.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回结果状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "success"),

    // 失败
    FAILED(400, "操作失败"),

    // 未授权
    UNAUTHORIZED(401, "未授权访问"),

    // 无权限
    NO_PERMISSION(403, "无权限访问"),

    // 资源不存在
    NOT_FOUND(404, "资源不存在"),

    // 参数错误
    PARAM_ERROR(400, "参数错误"),

    // 用户相关
    USER_NOT_EXIST(400, "用户不存在"),
    USER_ALREADY_EXIST(400, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(400, "用户名或密码错误"),
    USER_DISABLED(400, "用户已被禁用"),

    // 图书相关
    BOOK_NOT_EXIST(400, "图书不存在"),
    BOOK_NOT_AVAILABLE(400, "图书暂无可借"),
    BOOK_ALREADY_RETURNED(400, "图书已归还"),
    BOOK_ALREADY_RENEWED(400, "图书已续借过"),
    BOOK_OVERDUE(400, "图书已逾期"),

    // 借阅相关
    BORROW_RECORD_NOT_EXIST(400, "借阅记录不存在"),
    BORROW_LIMIT_EXCEEDED(400, "已达到最大借阅数量"),
    BORROW_NOT_ALLOWED(400, "当前无法借阅"),

    // 作者相关
    AUTHOR_NOT_EXIST(400, "作者不存在"),
    AUTHOR_ALREADY_EXIST(400, "您已经是作者了"),
    QUESTION_NOT_EXIST(400, "问题不存在"),

    // AI相关
    KNOWLEDGE_BASE_NOT_FOUND(400, "知识库未找到"),
    AI_SERVICE_ERROR(500, "AI服务调用失败"),

    // 系统错误
    SYSTEM_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(500, "数据库操作失败"),
    REDIS_ERROR(500, "Redis操作失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String msg;
}
