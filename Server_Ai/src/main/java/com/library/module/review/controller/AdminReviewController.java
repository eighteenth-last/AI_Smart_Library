package com.library.module.review.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.review.service.ReviewService;
import com.library.module.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-评价管理控制器
 */
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-评价管理", description = "管理员评价管理接口")
@SaCheckRole("admin")
public class AdminReviewController {

    private final ReviewService reviewService;

    /**
     * 获取所有评价
     */
    @GetMapping
    @Operation(summary = "评价列表", description = "管理员查看所有评价，支持多条件筛选")
    public Result<Page<ReviewVO>> getAllReviews(
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "图书ID筛选") @RequestParam(required = false) Long bookId,
            @Parameter(description = "评分筛选：1-5") @RequestParam(required = false) Integer rating,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理员查询评价: userId={}, bookId={}, rating={}, page={}, size={}", 
                userId, bookId, rating, page, size);
        
        Page<ReviewVO> result = reviewService.getAllReviews(userId, bookId, rating, page, size);
        return Result.success(result);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "管理员删除不当评价")
    public Result<Void> deleteReview(@PathVariable Long id) {
        log.info("管理员删除评价: reviewId={}", id);
        reviewService.deleteReview(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取评价统计
     */
    @GetMapping("/stats")
    @Operation(summary = "评价统计", description = "获取评价统计数据")
    public Result<ReviewService.ReviewStats> getReviewStats() {
        log.info("获取评价统计数据");
        ReviewService.ReviewStats stats = reviewService.getReviewStats();
        return Result.success(stats);
    }
}
