package com.library.module.review.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.review.service.ReviewService;
import com.library.module.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 用户端-评价控制器
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "用户端-图书评价", description = "用户评价图书接口")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping("/reviews/{bookId}")
    @Operation(summary = "评价图书", description = "用户评价图书")
    @SaCheckLogin
    public Result<Long> createReview(
            @PathVariable @Parameter(description = "图书ID") Long bookId,
            @RequestBody @Valid ReviewCreateRequest request) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户{}评价图书{}: rating={}, content={}", userId, bookId, request.getRating(), request.getContent());
        
        Long reviewId = reviewService.createReview(userId, bookId, request.getRating(), request.getContent());
        return Result.success("评价成功", reviewId);
    }

    /**
     * 获取图书评价列表
     */
    @GetMapping("/books/{bookId}/reviews")
    @Operation(summary = "图书评价列表", description = "获取图书的评价列表")
    public Result<Page<ReviewVO>> getBookReviews(
            @PathVariable @Parameter(description = "图书ID") Long bookId,
            @RequestParam(required = false) @Parameter(description = "评分筛选1-5") Integer rating,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size) {
        
        log.info("获取图书{}评价列表: rating={}, page={}, size={}", bookId, rating, page, size);
        
        Page<ReviewVO> result = reviewService.getBookReviews(bookId, rating, page, size);
        return Result.success(result);
    }

    /**
     * 评价创建请求
     */
    @lombok.Data
    public static class ReviewCreateRequest {
        
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        private Integer rating;
        
        private String content;
    }
}
