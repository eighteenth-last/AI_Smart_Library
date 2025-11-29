package com.library.module.favorite.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏用户VO - 用于展示收藏某本书的用户信息
 */
@Data
@Schema(description = "收藏用户信息")
public class FavoriteUserVO {
    
    @Schema(description = "收藏ID")
    private Long favId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户角色")
    private String role;
    
    @Schema(description = "收藏时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
