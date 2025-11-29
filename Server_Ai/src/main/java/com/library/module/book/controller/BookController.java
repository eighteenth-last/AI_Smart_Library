package com.library.module.book.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.book.dto.BookCreateDTO;
import com.library.module.book.dto.BookSearchDTO;
import com.library.module.book.dto.BookUpdateDTO;
import com.library.module.book.service.BookService;
import com.library.module.book.vo.BookDetailVO;
import com.library.module.book.vo.BookRecommendVO;
import com.library.module.book.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 图书控制器
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "图书管理", description = "图书相关接口")
@Validated
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "获取图书列表", description = "支持分页、搜索和筛选")
    public Result<PageResult<BookVO>> getBookList(BookSearchDTO searchDTO) {
        PageResult<BookVO> result = bookService.getBookList(searchDTO);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情", description = "根据图书ID获取详细信息")
    public Result<BookDetailVO> getBookById(
            @Parameter(description = "图书ID", required = true)
            @PathVariable @NotNull(message = "图书ID不能为空") Long id) {
        BookDetailVO bookDetail = bookService.getBookById(id);
        return Result.success(bookDetail);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索图书", description = "全文搜索图书")
    public Result<PageResult<BookVO>> searchBooks(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam @NotNull(message = "搜索关键词不能为空") String keyword,
            @Parameter(description = "搜索类型：title/author/isbn")
            @RequestParam(required = false) String type,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<BookVO> result = bookService.searchBooks(keyword, type, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}/recommend")
    @Operation(summary = "获取相似图书推荐", description = "基于Jaccard相似度算法推荐相似图书")
    public Result<List<BookRecommendVO>> getRecommendBooks(
            @Parameter(description = "图书ID", required = true)
            @PathVariable @NotNull(message = "图书ID不能为空") Long id,
            @Parameter(description = "推荐数量")
            @RequestParam(defaultValue = "5") Integer limit) {
        List<BookRecommendVO> recommendations = bookService.getRecommendBooks(id, limit);
        return Result.success(recommendations);
    }



    @GetMapping("/hot")
    @Operation(summary = "获取热门借阅图书", description = "获取热门借阅图书TOP N")
    public Result<List<BookVO>> getHotBooks(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量最小为1") Integer limit) {
        List<BookVO> hotBooks = bookService.getHotBooks(limit);
        return Result.success(hotBooks);
    }
}