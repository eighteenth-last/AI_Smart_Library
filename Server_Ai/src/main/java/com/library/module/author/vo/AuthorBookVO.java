package com.library.module.author.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 作者作品视图对象
 */
@Data
@Schema(description = "作者作品信息")
public class AuthorBookVO {

    @Schema(description = "图书ID")
    private Long bookId;

    @Schema(description = "书名")
    private String title;

    @Schema(description = "ISBN")
    private String isbn;

    @Schema(description = "出版社")
    private String publisher;

    @Schema(description = "出版年份")
    private Integer publishYear;

    @Schema(description = "封面URL")
    private String coverUrl;

    @Schema(description = "馆藏总数")
    private Integer totalStock;

    @Schema(description = "可借数量")
    private Integer availableStock;

    @Schema(description = "借阅次数")
    private Integer borrowCount;

    @Schema(description = "评分")
    private BigDecimal averageRating;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}
