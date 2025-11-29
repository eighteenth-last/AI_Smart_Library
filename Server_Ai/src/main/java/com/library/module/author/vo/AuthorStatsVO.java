package com.library.module.author.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 作者数据统计视图对象
 */
@Data
@Schema(description = "作者数据统计信息")
public class AuthorStatsVO {

    @Schema(description = "作品总数", example = "10")
    private Integer totalBooks;

    @Schema(description = "总借阅量", example = "1250")
    private Integer totalBorrows;

    @Schema(description = "总收藏量", example = "320")
    private Integer totalFavorites;

    @Schema(description = "平均评分", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "总评价数", example = "320")
    private Integer totalReviews;

    @Schema(description = "待回答问题数", example = "5")
    private Integer pendingQuestions;

    @Schema(description = "作品借阅排行TOP5")
    private List<BookBorrowStats> topBooks;

    @Schema(description = "月度借阅趋势")
    private List<MonthlyBorrowTrend> monthlyTrends;

    /**
     * 图书借阅统计
     */
    @Data
    @Schema(description = "图书借阅统计")
    public static class BookBorrowStats {
        @Schema(description = "图书ID")
        private Long bookId;

        @Schema(description = "图书标题")
        private String title;

        @Schema(description = "借阅次数")
        private Integer borrowCount;

        @Schema(description = "平均评分")
        private BigDecimal averageRating;
    }

    /**
     * 月度借阅趋势
     */
    @Data
    @Schema(description = "月度借阅趋势")
    public static class MonthlyBorrowTrend {
        @Schema(description = "月份", example = "2025-01")
        private String month;

        @Schema(description = "借阅次数")
        private Integer borrowCount;
    }
}
