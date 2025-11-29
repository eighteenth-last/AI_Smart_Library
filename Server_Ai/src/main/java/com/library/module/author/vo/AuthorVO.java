package com.library.module.author.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者信息VO
 */
@Data
public class AuthorVO {

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

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
     * 认证状态
     */
    private Integer authStatus;

    /**
     * 作品数量
     */
    private Integer bookCount;

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
}
