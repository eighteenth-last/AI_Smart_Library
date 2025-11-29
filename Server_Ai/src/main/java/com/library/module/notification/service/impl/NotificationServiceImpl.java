package com.library.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.module.notification.dto.NotificationCreateDTO;
import com.library.module.notification.entity.Notification;
import com.library.module.notification.mapper.NotificationMapper;
import com.library.module.notification.service.NotificationService;
import com.library.module.notification.vo.NotificationVO;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    private static final Map<String, String> TYPE_NAME_MAP = new HashMap<>();
    
    static {
        TYPE_NAME_MAP.put("borrow_due", "借阅到期提醒");
        TYPE_NAME_MAP.put("overdue", "逾期提醒");
        TYPE_NAME_MAP.put("system_announcement", "系统公告");
    }

    @Override
    public Page<NotificationVO> getAllNotifications(Long userId, String type, Integer isRead, Integer page, Integer size) {
        Page<Notification> notificationPage = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(userId != null, Notification::getUserId, userId)
                .eq(type != null, Notification::getType, type)
                .eq(isRead != null, Notification::getIsRead, isRead)
                .orderByDesc(Notification::getCreatedAt);

        Page<Notification> resultPage = notificationMapper.selectPage(notificationPage, wrapper);
        return convertToVO(resultPage);
    }

    @Override
    public void sendNotification(NotificationCreateDTO createDTO) {
        if (createDTO.getUserId() != null) {
            // 发送给指定用户
            createSingleNotification(createDTO.getUserId(), createDTO);
        } else {
            // 发送给所有用户（只查询状态正常的用户）
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)  // 只查询状态为1（正常）的用户
                .select(User::getUserId);  // 只查询userId字段，提升性能
            
            List<User> allUsers = userMapper.selectList(userWrapper);
            
            // 去重：防止同一个userId收到多条通知
            allUsers.stream()
                .map(User::getUserId)
                .distinct()
                .forEach(userId -> createSingleNotification(userId, createDTO));
        }
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        notificationMapper.deleteById(notificationId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        // 验证通知是否属于当前用户
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该通知");
        }
        // 标记为已读
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        // 查询当前用户所有未读通知
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        
        List<Notification> unreadNotifications = notificationMapper.selectList(wrapper);
        
        // 批量标记为已读
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        });
    }

    @Override
    public NotificationStats getNotificationStats() {
        NotificationStats stats = new NotificationStats();
        
        // 总数
        stats.total = notificationMapper.selectCount(null);
        
        // 未读数
        stats.unread = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>().eq(Notification::getIsRead, 0)
        );
        
        // 各类型统计
        stats.systemAnnouncement = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>().eq(Notification::getType, "system_announcement")
        );
        stats.borrowDue = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>().eq(Notification::getType, "borrow_due")
        );
        stats.overdue = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>().eq(Notification::getType, "overdue")
        );
        
        return stats;
    }

    /**
     * 创建单个通知
     */
    private void createSingleNotification(Long userId, NotificationCreateDTO createDTO) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(createDTO.getType());
        notification.setTitle(createDTO.getTitle());
        notification.setContent(createDTO.getContent());
        notification.setRelatedId(createDTO.getRelatedId());
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationMapper.insert(notification);
    }

    /**
     * 转换为VO
     */
    private Page<NotificationVO> convertToVO(Page<Notification> notificationPage) {
        Page<NotificationVO> voPage = new Page<>();
        BeanUtils.copyProperties(notificationPage, voPage, "records");

        List<NotificationVO> voList = notificationPage.getRecords().stream().map(notification -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(notification, vo);

            // 查询用户名
            User user = userMapper.selectById(notification.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            // 设置类型显示名称
            vo.setTypeName(TYPE_NAME_MAP.getOrDefault(notification.getType(), notification.getType()));

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
