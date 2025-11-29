package com.library.module.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@TableName("tag")
public class Tag {
    
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;
    
    private String name;
    
    private String category;
    
    private LocalDateTime createdAt;
}
