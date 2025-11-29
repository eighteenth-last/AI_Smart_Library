package com.library.module.favorite.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@TableName("favorite")
public class Favorite {
    
    /**
     * 收藏ID
     */
    @TableId(value = "fav_id", type = IdType.AUTO)
    private Long favId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 图书ID
     */
    private Long bookId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
