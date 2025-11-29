package com.library.module.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.module.notification.dto.NotificationCreateDTO;
import com.library.module.notification.vo.NotificationVO;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 获取所有通知（管理员）
     */
    Page<NotificationVO> getAllNotifications(Long userId, String type, Integer isRead, Integer page, Integer size);

    /**
     * 发送通知
     */
    void sendNotification(NotificationCreateDTO createDTO);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId);

    /**
     * 标记通知已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记已读（当前用户所有未读通知）
     */
    void markAllAsRead(Long userId);

    /**
     * 获取通知统计
     */
    NotificationStats getNotificationStats();

    /**
     * 通知统计VO
     */
    class NotificationStats {
        public Long total;
        public Long unread;
        public Long systemAnnouncement;
        public Long borrowDue;
        public Long overdue;
    }
}
