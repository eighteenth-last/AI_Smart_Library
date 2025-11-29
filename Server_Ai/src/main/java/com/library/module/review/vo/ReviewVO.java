package com.library.module.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价VO
 */
@Data
public class ReviewVO {

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookTitle;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 评分 1~5
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
