package com.library.module.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 图书更新DTO
 */
@Data
@Schema(description = "图书更新请求")
public class BookUpdateDTO {

    @Schema(description = "书名", example = "三体（修订版）")
    @Size(max = 100, message = "书名长度不能超过100个字符")
    private String title;

    @Schema(description = "作者ID", example = "1")
    @Positive(message = "作者ID必须为正数")
    private Long authorId;

    @Schema(description = "分类ID", example = "2")
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    @Schema(description = "出版社", example = "重庆出版社")
    @Size(max = 100, message = "出版社名称长度不能超过100个字符")
    private String publisher;

    @Schema(description = "出版年份", example = "2008")
    @Min(value = 1900, message = "出版年份不能早于1900年")
    @Max(value = 2100, message = "出版年份不能晚于2100年")
    private Integer publishYear;

    @Schema(description = "封面URL", example = "https://example.com/covers/3body.jpg")
    @Size(max = 500, message = "封面URL长度不能超过500个字符")
    private String coverUrl;

    @Schema(description = "图书简介", example = "地球文明与三体文明的首次接触。")
    @Size(max = 2000, message = "图书简介长度不能超过2000个字符")
    private String description;

    @Schema(description = "馆藏总量", example = "30")
    @Min(value = 1, message = "馆藏总量至少为1")
    @Max(value = 1000, message = "馆藏总量不能超过1000")
    private Integer totalStock;

    @Schema(description = "状态", example = "1")
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status;

    @Schema(description = "标签ID数组", example = "[1, 2, 3]")
    private List<Long> tagIds;
}