package com.library.module.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 统计分类下的图书数量
     */
    @Select("SELECT COUNT(*) FROM book WHERE category_id = #{categoryId}")
    int countBooksByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 查询分类树形结构
     */
    List<Category> selectCategoryTree();
    
    /**
     * 根据父分类ID查询子分类
     */
    List<Category> selectChildrenByParentId(@Param("parentId") Long parentId);
    
    /**
     * 检查是否有子分类
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{categoryId}")
    int countChildrenByCategoryId(@Param("categoryId") Long categoryId);
}
