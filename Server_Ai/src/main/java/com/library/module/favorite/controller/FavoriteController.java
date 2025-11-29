package com.library.module.favorite.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.favorite.service.FavoriteService;
import com.library.module.favorite.vo.FavoriteVO;
import com.library.module.favorite.vo.FavoriteUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@Tag(name = "收藏管理", description = "用户收藏图书相关接口")
@RestController
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    @Operation(summary = "添加收藏", description = "收藏一本图书")
    @PostMapping("/favorites/{bookId}")
    @SaCheckLogin
    public Result<Void> addFavorite(
            @Parameter(description = "图书ID") @PathVariable Long bookId) {
        favoriteService.addFavorite(bookId);
        return Result.success("收藏成功", null);
    }
    
    @Operation(summary = "取消收藏", description = "取消收藏一本图书")
    @DeleteMapping("/favorites/{bookId}")
    @SaCheckLogin
    public Result<Void> removeFavorite(
            @Parameter(description = "图书ID") @PathVariable Long bookId) {
        favoriteService.removeFavorite(bookId);
        return Result.success("取消收藏成功", null);
    }
    
    @Operation(summary = "我的收藏列表", description = "获取当前用户的收藏列表")
    @GetMapping("/user/favorites")
    @SaCheckLogin
    public Result<PageResult<FavoriteVO>> getMyFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long size) {
        PageResult<FavoriteVO> result = favoriteService.getMyFavorites(page, size);
        return Result.success(result);
    }
    
    @Operation(summary = "检查是否收藏", description = "检查某本图书是否已收藏")
    @GetMapping("/favorites/{bookId}/check")
    @SaCheckLogin
    public Result<Boolean> checkFavorite(
            @Parameter(description = "图书ID") @PathVariable Long bookId) {
        boolean isFavorite = favoriteService.isFavorite(bookId);
        return Result.success(isFavorite);
    }
    
    @Operation(summary = "图书收藏用户列表", description = "获取收藏某本图书的用户列表")
    @GetMapping("/books/{bookId}/favorites")
    public Result<PageResult<FavoriteUserVO>> getBookFavorites(
            @Parameter(description = "图书ID") @PathVariable Long bookId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long size) {
        PageResult<FavoriteUserVO> result = favoriteService.getBookFavorites(bookId, page, size);
        return Result.success(result);
    }
}
