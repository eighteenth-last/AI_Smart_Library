package com.library.module.dashboard.service;

import com.library.module.dashboard.vo.*;

import java.util.List;

/**
 * 数据可视化看板服务接口
 */
public interface DashboardService {

    /**
     * 获取系统总览数据
     *
     * @return 总览数据
     */
    OverviewVO getOverview();

    /**
     * 获取热门借阅图书TOP N
     *
     * @param limit 数量限制
     * @return 热门图书列表
     */
    List<HotBookVO> getHotBooks(Integer limit);

    /**
     * 获取分类统计
     *
     * @return 分类统计列表
     */
    List<CategoryStatsVO> getCategoryStats();

    /**
     * 获取借阅趋势
     *
     * @param period 时间周期（day/week/month/year）
     * @param limit 数据点数量
     * @return 借阅趋势列表
     */
    List<BorrowTrendVO> getBorrowTrends(String period, Integer limit);

    /**
     * 获取作者作品数据
     *
     * @param authorId 作者ID
     * @return 作者数据
     */
    AuthorStatsVO getAuthorStats(Long authorId);

    /**
     * 获取最近操作记录
     *
     * @param limit 数量限制
     * @return 最近操作列表
     */
    List<RecentActivityVO> getRecentActivities(Integer limit);
}
