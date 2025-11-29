package com.library.module.category.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.library.common.result.Result;
import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "图书分类管理接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "获取分类树", description = "获取树形分类列表")
    @GetMapping
    public Result<List<Category>> getTreeList() {
        List<Category> list = categoryService.getTreeList();
        return Result.success(list);
    }
    
    @Operation(summary = "获取分类树", description = "获取树形分类列表（别名）")
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        List<Category> list = categoryService.getTreeList();
        return Result.success(list);
    }
    
    @Operation(summary = "新增分类", description = "管理员新增分类")
    @PostMapping
    @SaCheckRole("admin")
    public Result<Void> addCategory(@RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success("添加成功", null);
    }
    
    @Operation(summary = "更新分类", description = "管理员更新分类")
    @PutMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setCategoryId(id);
        categoryService.updateCategory(category);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除分类", description = "管理员删除分类")
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
