package com.library.module.author.vo;

import lombok.Data;
import java.util.List;

/**
 * 作者数据看板VO
 */
@Data
public class AuthorDashboardVO {

    /**
     * 作品总数
     */
    private Integer totalBooks;

    /**
     * 总借阅量
     */
    private Integer totalBorrows;

    /**
     * 总评价数
     */
    private Integer totalReviews;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 待回答问题数
     */
    private Integer pendingQuestions;

    /**
     * 月度借阅趋势
     */
    private List<MonthlyTrendVO> monthlyBorrowTrend;

    @Data
    public static class MonthlyTrendVO {
        private String month;
        private Integer count;
    }
}
