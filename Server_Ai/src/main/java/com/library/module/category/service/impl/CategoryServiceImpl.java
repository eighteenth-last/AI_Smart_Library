package com.library.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.exception.BusinessException;
import com.library.module.category.entity.Category;
import com.library.module.category.mapper.CategoryMapper;
import com.library.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getTreeList() {
        // 查询所有分类
        List<Category> allCategories = categoryMapper.selectList(null);
        
        // 构建树形结构
        return buildTree(allCategories, null);
    }
    
    @Override
    public void addCategory(Category category) {
        // 如果有父级，验证父级是否存在
        if (category.getParentId() != null) {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }
        
        categoryMapper.insert(category);
    }
    
    @Override
    public void updateCategory(Category category) {
        Category existing = categoryMapper.selectById(category.getCategoryId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        
        categoryMapper.updateById(category);
    }
    
    @Override
    public void deleteCategory(Long categoryId) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, categoryId);
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        
        categoryMapper.deleteById(categoryId);
    }
    
    /**
     * 递归构建树形结构
     */
    private List<Category> buildTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(category -> {
                    if (parentId == null) {
                        return category.getParentId() == null;
                    }
                    return parentId.equals(category.getParentId());
                })
                .peek(category -> {
                    List<Category> children = buildTree(allCategories, category.getCategoryId());
                    if (!children.isEmpty()) {
                        category.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }
}
