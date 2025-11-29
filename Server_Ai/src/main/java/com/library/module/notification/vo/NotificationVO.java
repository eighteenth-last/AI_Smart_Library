package com.library.module.notification.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
public class NotificationVO {

    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 类型显示名称
     */
    private String typeName;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 关联ID
     */
    private Long relatedId;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
