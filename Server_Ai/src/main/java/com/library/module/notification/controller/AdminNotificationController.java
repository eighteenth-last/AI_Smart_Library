package com.library.module.notification.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.notification.dto.NotificationCreateDTO;
import com.library.module.notification.service.NotificationService;
import com.library.module.notification.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-通知管理控制器
 */
@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-通知管理", description = "管理员通知管理接口")
@SaCheckRole("admin")
public class AdminNotificationController {

    private final NotificationService notificationService;

    /**
     * 获取所有通知
     */
    @GetMapping
    @Operation(summary = "通知列表", description = "管理员查看所有通知，支持多条件筛选")
    public Result<Page<NotificationVO>> getAllNotifications(
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "类型筛选") @RequestParam(required = false) String type,
            @Parameter(description = "已读筛选：0未读 1已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理员查询通知: userId={}, type={}, isRead={}, page={}, size={}", 
                userId, type, isRead, page, size);
        
        Page<NotificationVO> result = notificationService.getAllNotifications(userId, type, isRead, page, size);
        return Result.success(result);
    }

    /**
     * 发送通知
     */
    @PostMapping
    @Operation(summary = "发送通知", description = "管理员发送系统通知")
    public Result<Void> sendNotification(@RequestBody @Valid NotificationCreateDTO createDTO) {
        log.info("管理员发送通知: {}", createDTO);
        notificationService.sendNotification(createDTO);
        return Result.success("发送成功", null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "管理员删除通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        log.info("管理员删除通知: notificationId={}", id);
        notificationService.deleteNotification(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/stats")
    @Operation(summary = "通知统计", description = "获取通知统计数据")
    public Result<NotificationService.NotificationStats> getNotificationStats() {
        log.info("获取通知统计数据");
        NotificationService.NotificationStats stats = notificationService.getNotificationStats();
        return Result.success(stats);
    }
}
