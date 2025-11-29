package com.library.module.category.service;

import com.library.module.category.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 获取树形分类列表
     */
    List<Category> getTreeList();
    
    /**
     * 新增分类
     */
    void addCategory(Category category);
    
    /**
     * 更新分类
     */
    void updateCategory(Category category);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId);
}
