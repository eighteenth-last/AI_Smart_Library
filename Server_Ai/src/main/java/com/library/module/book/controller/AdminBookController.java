package com.library.module.book.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.book.dto.BookCreateDTO;
import com.library.module.book.dto.BookSearchDTO;
import com.library.module.book.dto.BookUpdateDTO;
import com.library.module.book.service.BookService;
import com.library.module.book.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理端图书控制器
 */
@RestController
@RequestMapping("/admin/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-图书管理", description = "管理员图书管理接口")
@Validated
public class AdminBookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情", description = "管理员/作者查看图书详情")
    public Result<BookVO> getBookDetail(
            @Parameter(description = "图书ID", required = true)
            @PathVariable @NotNull(message = "图书ID不能为空") Long id) {
        log.info("===== 无需admin权限 - 查询图书详情: id={} =====", id);
        BookVO bookVO = bookService.getBookDetail(id);
        return Result.success(bookVO);
    }

    @GetMapping
    @Operation(summary = "获取图书列表", description = "管理员查看图书列表，支持分页、搜索和筛选")
    @SaCheckRole("admin")
    public Result<PageResult<BookVO>> getBookList(BookSearchDTO searchDTO) {
        log.info("管理员查询图书列表: {}", searchDTO);
        PageResult<BookVO> result = bookService.getBookList(searchDTO);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "新增图书", description = "管理员新增图书")
    @SaCheckRole("admin")
    public Result<Long> addBook(@Valid @RequestBody BookCreateDTO createDTO) {
        log.info("管理员新增图书: {}", createDTO.getTitle());
        Long bookId = bookService.addBook(createDTO);
        return Result.success(bookId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑图书", description = "管理员/作者编辑图书信息")
    public Result<Void> updateBook(
            @Parameter(description = "图书ID", required = true)
            @PathVariable @NotNull(message = "图书ID不能为空") Long id,
            @Valid @RequestBody BookUpdateDTO updateDTO) {
        log.info("管理员编辑图书: id={}, title={}", id, updateDTO.getTitle());
        bookService.updateBook(id, updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图书", description = "管理员下架/删除图书")
    @SaCheckRole("admin")
    public Result<Void> deleteBook(
            @Parameter(description = "图书ID", required = true)
            @PathVariable @NotNull(message = "图书ID不能为空") Long id) {
        log.info("管理员删除图书: id={}", id);
        bookService.deleteBook(id);
        return Result.success();
    }
}
