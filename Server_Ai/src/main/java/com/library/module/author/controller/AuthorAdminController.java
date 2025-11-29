package com.library.module.author.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.author.entity.Author;
import com.library.module.author.mapper.AuthorMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作者管理控制器（管理员端）
 */
@RestController
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
@Tag(name = "作者管理模块", description = "管理员管理作者相关接口")
@SaCheckRole("admin")
public class AuthorAdminController {

    private final AuthorMapper authorMapper;

    /**
     * 获取作者列表（管理员）
     */
    @GetMapping
    @Operation(summary = "获取作者列表", description = "管理员获取所有作者列表，支持关键词搜索和分页")
    public Result<PageResult<Author>> getAuthors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        
        Page<Author> pageParam = new Page<>(page, size);
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索（作者姓名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword.trim());
        }
        
        // deleted字段由@TableLogic自动处理，不需要手动添加
        queryWrapper.orderByDesc("created_at");
        
        Page<Author> authorPage = authorMapper.selectPage(pageParam, queryWrapper);
        
        PageResult<Author> pageResult = new PageResult<>();
        pageResult.setRecords(authorPage.getRecords());
        pageResult.setTotal(authorPage.getTotal());
        pageResult.setPage(authorPage.getCurrent());
        pageResult.setSize(authorPage.getSize());
        
        return Result.success(pageResult);
    }

    /**
     * 获取作者列表（分页，管理员）
     */
    @GetMapping("/list")
    @Operation(summary = "获取作者列表（分页）", description = "管理员获取作者列表，支持按认证状态筛选")
    public Result<PageResult<Author>> getAuthorsForAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer authStatus) {
        
        Page<Author> pageParam = new Page<>(page, size);
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        
        // 按认证状态筛选
        if (authStatus != null) {
            queryWrapper.eq("auth_status", authStatus);
        }
        
        // deleted字段由@TableLogic自动处理，不需要手动添加
        queryWrapper.orderByDesc("created_at");
        
        Page<Author> authorPage = authorMapper.selectPage(pageParam, queryWrapper);
        
        PageResult<Author> pageResult = new PageResult<>();
        pageResult.setRecords(authorPage.getRecords());
        pageResult.setTotal(authorPage.getTotal());
        pageResult.setPage(authorPage.getCurrent());
        pageResult.setSize(authorPage.getSize());
        
        return Result.success(pageResult);
    }

    /**
     * 审核作者
     */
    @PutMapping("/review/{authorId}")
    @Operation(summary = "审核作者", description = "管理员审核作者认证申请")
    public Result<Void> reviewAuthor(
            @PathVariable Long authorId,
            @RequestParam Integer authStatus) {
        
        Author author = authorMapper.selectById(authorId);
        if (author == null) {
            return Result.error(404, "作者不存在");
        }
        
        // 更新认证状态
        author.setAuthStatus(authStatus);
        authorMapper.updateById(author);
        
        return Result.success();
    }
}
