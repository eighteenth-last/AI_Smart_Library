package com.library.module.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.library.common.result.Result;
import com.library.module.dashboard.service.DashboardService;
import com.library.module.dashboard.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据可视化看板控制器
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "数据可视化看板", description = "系统数据统计相关接口")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取系统总览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "系统总览", description = "获取系统总览数据")
    @SaCheckLogin
    public Result<OverviewVO> getOverview() {
        OverviewVO overviewVO = dashboardService.getOverview();
        return Result.success(overviewVO);
    }

    /**
     * 获取热门借阅图书TOP N
     */
    @GetMapping("/hot-books")
    @Operation(summary = "热门图书", description = "获取热门借阅图书TOP N")
    @SaCheckLogin
    public Result<List<HotBookVO>> getHotBooks(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<HotBookVO> hotBooks = dashboardService.getHotBooks(limit);
        return Result.success(hotBooks);
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/category-stats")
    @Operation(summary = "分类统计", description = "按分类统计借阅量")
    @SaCheckLogin
    public Result<List<CategoryStatsVO>> getCategoryStats() {
        List<CategoryStatsVO> categoryStats = dashboardService.getCategoryStats();
        return Result.success(categoryStats);
    }

    /**
     * 获取借阅趋势
     */
    @GetMapping("/borrow-trends")
    @Operation(summary = "借阅趋势", description = "获取借阅趋势数据")
    @SaCheckLogin
    public Result<List<BorrowTrendVO>> getBorrowTrends(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "12") Integer limit) {
        List<BorrowTrendVO> trends = dashboardService.getBorrowTrends(period, limit);
        return Result.success(trends);
    }

    /**
     * 获取作者作品数据
     */
    @GetMapping("/author-stats/{authorId}")
    @Operation(summary = "作者数据", description = "获取作者作品借阅数据")
    @SaCheckLogin
    public Result<AuthorStatsVO> getAuthorStats(@PathVariable("authorId") Long authorId) {
        AuthorStatsVO authorStats = dashboardService.getAuthorStats(authorId);
        return Result.success(authorStats);
    }

    /**
     * 获取最近操作记录
     */
    @GetMapping("/recent-activities")
    @Operation(summary = "最近操作", description = "获取最近的操作记录")
    @SaCheckLogin
    public Result<List<RecentActivityVO>> getRecentActivities(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RecentActivityVO> activities = dashboardService.getRecentActivities(limit);
        return Result.success(activities);
    }
}
