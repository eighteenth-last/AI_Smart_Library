package com.library.module.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图书推荐视图对象
 */
@Data
@Schema(description = "图书推荐信息")
public class BookRecommendVO {

    @Schema(description = "图书ID", example = "2")
    private Long bookId;

    @Schema(description = "书名", example = "三体Ⅱ·黑暗森林")
    private String title;

    @Schema(description = "作者名称", example = "刘慈欣")
    private String authorName;

    @Schema(description = "封面URL", example = "https://example.com/covers/3body2.jpg")
    private String coverUrl;

    @Schema(description = "相似度", example = "0.85")
    private Double similarity;

    @Schema(description = "相似度百分比", example = "85%")
    private String similarityPercent;

    @Schema(description = "推荐理由", example = "因为共享标签：[\"科幻\", \"硬科幻\", \"宇宙\"]")
    private String reason;

    @Schema(description = "共享标签", example = "[\"科幻\", \"硬科幻\", \"宇宙\"]")
    private String[] sharedTags;
}