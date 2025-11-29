package com.library.module.author.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作者详情VO
 */
@Data
public class AuthorDetailVO {

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者名称
     */
    private String name;

    /**
     * 国家
     */
    private String country;

    /**
     * 出生年份
     */
    private Integer birthYear;

    /**
     * 作者简介
     */
    private String intro;

    /**
     * 作品列表
     */
    private List<BookVO> books;

    /**
     * 总借阅量
     */
    private Integer totalBorrows;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 作品VO
     */
    @Data
    public static class BookVO {
        private Long bookId;
        private String title;
        private String coverUrl;
        private Integer publishYear;
        private Integer borrowCount;
        private Double averageRating;
    }
}
