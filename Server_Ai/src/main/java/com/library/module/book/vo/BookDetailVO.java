package com.library.module.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书详情视图对象
 */
@Data
@Schema(description = "图书详细信息")
public class BookDetailVO {

    @Schema(description = "图书ID", example = "1")
    private Long bookId;

    @Schema(description = "ISBN号", example = "9787530216783")
    private String isbn;

    @Schema(description = "书名", example = "三体")
    private String title;

    @Schema(description = "作者信息")
    private AuthorInfo author;

    @Schema(description = "分类信息")
    private CategoryInfo category;

    @Schema(description = "出版社", example = "重庆出版社")
    private String publisher;

    @Schema(description = "出版年份", example = "2008")
    private Integer publishYear;

    @Schema(description = "封面URL", example = "https://example.com/covers/3body.jpg")
    private String coverUrl;

    @Schema(description = "图书简介", example = "地球文明与三体文明的首次接触。")
    private String description;

    @Schema(description = "馆藏总量", example = "30")
    private Integer totalStock;

    @Schema(description = "可借数量", example = "26")
    private Integer availableStock;

    @Schema(description = "平均评分", example = "4.8")
    private Double averageRating;

    @Schema(description = "评价数量", example = "150")
    private Integer reviewCount;

    @Schema(description = "借阅次数", example = "300")
    private Integer borrowCount;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "标签列表", example = "[\"科幻\", \"硬科幻\", \"太空歌剧\"]")
    private String[] tags;

    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-11-24T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 作者信息内部类
     */
    @Data
    @Schema(description = "作者信息")
    public static class AuthorInfo {
        @Schema(description = "作者ID", example = "1")
        private Long authorId;

        @Schema(description = "作者姓名", example = "刘慈欣")
        private String name;

        @Schema(description = "国家", example = "中国")
        private String country;

        @Schema(description = "作者简介", example = "科幻作家，《三体》作者")
        private String intro;
    }

    /**
     * 分类信息内部类
     */
    @Data
    @Schema(description = "分类信息")
    public static class CategoryInfo {
        @Schema(description = "分类ID", example = "2")
        private Long categoryId;

        @Schema(description = "分类名称", example = "科幻")
        private String name;
    }
}