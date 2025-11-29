package com.library.module.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收藏VO
 */
@Data
@Schema(description = "收藏信息")
public class FavoriteVO {
    
    @Schema(description = "收藏ID")
    private Long favId;
    
    @Schema(description = "图书ID")
    private Long bookId;
    
    @Schema(description = "图书标题")
    private String bookTitle;
    
    @Schema(description = "作者名称")
    private String authorName;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "创建时间")
    private String createdAt;
}
