package com.library.module.notification.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.notification.service.NotificationService;
import com.library.module.notification.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器（普通用户）
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "通知管理", description = "用户通知相关接口")
@SaCheckLogin
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping
    @Operation(summary = "我的通知", description = "获取当前登录用户的通知列表")
    public Result<Page<NotificationVO>> getMyNotifications(
            @Parameter(description = "类型筛选") @RequestParam(required = false) String type,
            @Parameter(description = "已读筛选：0未读 1已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        log.info("用户查询通知: userId={}, type={}, isRead={}, page={}, size={}", 
                userId, type, isRead, page, size);
        
        Page<NotificationVO> result = notificationService.getAllNotifications(userId, type, isRead, page, size);
        return Result.success(result);
    }

    /**
     * 获取系统公告（当前用户的系统公告）
     */
    @GetMapping("/announcements")
    @Operation(summary = "系统公告", description = "获取当前用户的系统公告")
    public Result<Page<NotificationVO>> getAnnouncements(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        log.info("查询系统公告: userId={}, page={}, size={}", userId, page, size);
        
        // 查询当前用户的系统公告类型通知
        Page<NotificationVO> result = notificationService.getAllNotifications(userId, "system_announcement", null, page, size);
        return Result.success(result);
    }

    /**
     * 标记通知已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记指定通知为已读")
    public Result<Void> markAsRead(@PathVariable("id") Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("标记通知已读: userId={}, notificationId={}", userId, notificationId);
        
        notificationService.markAsRead(notificationId, userId);
        return Result.success("标记成功", null);
    }

    /**
     * 批量标记已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "批量标记当前用户所有通知为已读")
    public Result<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("批量标记已读: userId={}", userId);
        
        notificationService.markAllAsRead(userId);
        return Result.success("标记成功", null);
    }
}
