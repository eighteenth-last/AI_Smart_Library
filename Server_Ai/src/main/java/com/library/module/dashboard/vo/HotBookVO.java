package com.library.module.dashboard.vo;

import lombok.Data;

/**
 * 热门图书VO
 */
@Data
public class HotBookVO {

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 借阅次数
     */
    private Integer borrowCount;

    /**
     * 排名
     */
    private Integer rank;
}
