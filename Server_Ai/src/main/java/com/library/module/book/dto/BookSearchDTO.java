package com.library.module.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图书搜索DTO
 */
@Data
@Schema(description = "图书搜索请求")
public class BookSearchDTO {

    @Schema(description = "搜索关键词", example = "三体")
    private String keyword;

    @Schema(description = "作者ID", example = "1")
    private Long authorId;

    @Schema(description = "分类ID", example = "2")
    private Long categoryId;

    @Schema(description = "页码", example = "1")
    private Long page = 1L;

    @Schema(description = "每页数量", example = "10")
    private Long size = 10L;
}