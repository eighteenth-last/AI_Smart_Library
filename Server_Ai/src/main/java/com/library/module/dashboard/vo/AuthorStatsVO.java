package com.library.module.dashboard.vo;

import lombok.Data;
import java.util.List;

/**
 * 作者统计数据VO
 */
@Data
public class AuthorStatsVO {

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者名称
     */
    private String authorName;

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
     * 作品列表
     */
    private List<BookStatsVO> books;

    @Data
    public static class BookStatsVO {
        private Long bookId;
        private String title;
        private Integer borrowCount;
        private Double averageRating;
    }
}
