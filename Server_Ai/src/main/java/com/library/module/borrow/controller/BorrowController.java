package com.library.module.borrow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.borrow.dto.*;
import com.library.module.borrow.service.BorrowService;
import com.library.module.borrow.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅管理控制器（用户端）
 */
@RestController
@RequestMapping("/borrow")
@Tag(name = "借阅管理", description = "用户借阅相关接口")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    /**
     * 借阅图书
     */
    @PostMapping("/{bookId}")
    @Operation(summary = "借阅图书", description = "用户借阅指定图书")
    @SaCheckLogin
    public Result<BorrowResultVO> borrowBook(@PathVariable("bookId") Long bookId) {
        BorrowRequestDTO requestDTO = new BorrowRequestDTO();
        requestDTO.setUserId(StpUtil.getLoginIdAsLong());
        requestDTO.setBookId(bookId);
        
        BorrowResultVO result = borrowService.borrowBook(requestDTO);
        return Result.success(result);
    }

    /**
     * 归还图书
     */
    @PostMapping("/return/{recordId}")
    @Operation(summary = "归还图书", description = "用户归还借阅的图书")
    @SaCheckLogin
    public Result<ReturnResultVO> returnBook(@PathVariable("recordId") Long recordId) {
        ReturnRequestDTO requestDTO = new ReturnRequestDTO();
        requestDTO.setUserId(StpUtil.getLoginIdAsLong());
        requestDTO.setRecordId(recordId);
        
        ReturnResultVO result = borrowService.returnBook(requestDTO);
        return Result.success(result);
    }

    /**
     * 续借图书
     */
    @PostMapping("/renew/{recordId}")
    @Operation(summary = "续借图书", description = "用户续借已借阅的图书")
    @SaCheckLogin
    public Result<RenewResultVO> renewBook(@PathVariable("recordId") Long recordId) {
        RenewRequestDTO requestDTO = new RenewRequestDTO();
        requestDTO.setUserId(StpUtil.getLoginIdAsLong());
        requestDTO.setRecordId(recordId);
        
        RenewResultVO result = borrowService.renewBook(requestDTO);
        return Result.success(result);
    }
    
    /**
     * 获取图书的借阅记录（公开接口）
     */
    @GetMapping("/books/{bookId}/records")
    @Operation(summary = "图书借阅记录", description = "获取某本图书的借阅记录")
    public Result<Page<BorrowRecordVO>> getBookBorrowRecords(
            @PathVariable("bookId") Long bookId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<BorrowRecordVO> result = borrowService.getAllBorrowRecords(null, bookId, status, page, size);
        return Result.success(result);
    }

    /**
     * 审批借阅申请（管理员）
     */
    @PostMapping("/approve")
    @Operation(summary = "审批借阅申请", description = "管理员审批用户的借阅申请")
    @SaCheckLogin
    public Result<Boolean> approveBorrow(@RequestBody @Valid BorrowApproveDTO requestDTO) {
        Long approverId = StpUtil.getLoginIdAsLong();
        boolean result = borrowService.approveBorrow(
            requestDTO.getRecordId(),
            requestDTO.getApproved(),
            requestDTO.getRejectReason(),
            approverId
        );
        
        String message = requestDTO.getApproved() ? "审批通过" : "已拒绝";
        return Result.success(message, result);
    }
}
