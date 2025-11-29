package com.library.module.dashboard.vo;

import lombok.Data;

/**
 * 借阅趋势VO
 */
@Data
public class BorrowTrendVO {

    /**
     * 时间周期（如：2025-01）
     */
    private String period;

    /**
     * 借阅量
     */
    private Integer borrowCount;

    /**
     * 归还量
     */
    private Integer returnCount;
}
