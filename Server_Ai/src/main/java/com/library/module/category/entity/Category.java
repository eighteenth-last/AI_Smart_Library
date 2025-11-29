package com.library.module.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 图书分类实体类
 */
@Data
@TableName("category")
public class Category {
    
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    @TableField(exist = false)
    private List<Category> children;
}
