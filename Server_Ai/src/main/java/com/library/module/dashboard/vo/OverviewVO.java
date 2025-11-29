package com.library.module.dashboard.vo;

import lombok.Data;

/**
 * 系统总览数据VO
 */
@Data
public class OverviewVO {

    /**
     * 用户总数
     */
    private Long totalUsers;

    /**
     * 图书总数
     */
    private Long totalBooks;

    /**
     * 借阅总数
     */
    private Long totalBorrows;

    /**
     * 当前借阅中数量
     */
    private Long activeBorrows;

    /**
     * 逾期数量
     */
    private Long overdueCount;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 今日新增借阅
     */
    private Long todayNewBorrows;
}
