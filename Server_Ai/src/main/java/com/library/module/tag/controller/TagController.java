package com.library.module.tag.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.library.common.result.Result;
import com.library.module.tag.entity.Tag;
import com.library.module.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "图书标签管理接口")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @Operation(summary = "获取所有标签", description = "获取所有标签列表")
    @GetMapping
    public Result<List<Tag>> getAllTags() {
        List<Tag> list = tagService.getAllTags();
        return Result.success(list);
    }
    
    @Operation(summary = "获取所有标签", description = "获取所有标签列表（别名）")
    @GetMapping("/list")
    public Result<List<Tag>> getTagList() {
        List<Tag> list = tagService.getAllTags();
        return Result.success(list);
    }
    
    @Operation(summary = "新增标签", description = "管理员新增标签")
    @PostMapping
    @SaCheckRole("admin")
    public Result<Void> addTag(@RequestBody Tag tag) {
        tagService.addTag(tag);
        return Result.success("添加成功", null);
    }
    
    @Operation(summary = "更新标签", description = "管理员更新标签")
    @PutMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setTagId(id);
        tagService.updateTag(tag);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除标签", description = "管理员删除标签")
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除成功", null);
    }
}
