package com.library.module.dashboard.vo;

import lombok.Data;

/**
 * 分类统计VO
 */
@Data
public class CategoryStatsVO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 借阅量
     */
    private Integer borrowCount;

    /**
     * 占比
     */
    private String percentage;
}
