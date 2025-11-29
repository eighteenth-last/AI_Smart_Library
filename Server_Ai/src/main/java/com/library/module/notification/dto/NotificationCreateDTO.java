package com.library.module.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建通知DTO
 */
@Data
public class NotificationCreateDTO {

    /**
     * 用户ID（为null表示发送给所有用户）
     */
    private Long userId;

    /**
     * 通知类型
     */
    @NotBlank(message = "通知类型不能为空")
    private String type;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 关联ID
     */
    private Long relatedId;
}
