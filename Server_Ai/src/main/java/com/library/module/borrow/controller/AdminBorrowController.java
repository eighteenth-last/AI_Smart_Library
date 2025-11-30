package com.library.module.borrow.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.borrow.service.BorrowService;
import com.library.module.borrow.vo.BorrowRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端借阅管理控制器
 */
@RestController
@RequestMapping("/admin/borrows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端-借阅管理", description = "管理员借阅记录管理接口")
@SaCheckRole("admin")
public class AdminBorrowController {

    private final BorrowService borrowService;

    /**
     * 获取所有借阅记录（管理员）
     */
    @GetMapping
    @Operation(summary = "借阅记录管理", description = "管理员查看所有借阅记录，支持多条件筛选")
    public Result<Page<BorrowRecordVO>> getAllBorrowRecords(
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "图书ID筛选") @RequestParam(required = false) Long bookId,
            @Parameter(description = "状态筛选：borrowed/returned/overdue") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理员查询借阅记录: userId={}, bookId={}, status={}, page={}, size={}", 
                userId, bookId, status, page, size);
        
        Page<BorrowRecordVO> result = borrowService.getAllBorrowRecords(userId, bookId, status, page, size);
        return Result.success(result);
    }

    /**
     * 获取逾期记录
     */
    @GetMapping("/overdue")
    @Operation(summary = "逾期记录", description = "获取所有逾期的借阅记录")
    public Result<Page<BorrowRecordVO>> getOverdueRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理员查询逾期记录: page={}, size={}", page, size);
        
        Page<BorrowRecordVO> result = borrowService.getAllBorrowRecords(null, null, "overdue", page, size);
        return Result.success(result);
    }

    /**
     * 获取借阅统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "借阅统计", description = "获取借阅统计数据")
    public Result<Object> getBorrowStatistics() {
        log.info("管理员查询借阅统计");
        
        // TODO: 实现借阅统计逻辑
        return Result.success("统计功能待实现");
    }

    /**
     * 审批借阅申请
     */
    @PostMapping("/approve")
    @Operation(summary = "审批借阅申请", description = "管理员审批借阅申请，同意或拒绝")
    public Result<Void> approveBorrow(@RequestBody com.library.module.borrow.dto.BorrowApproveDTO approveDTO) {
        log.info("管理员审批借阅申请: recordId={}, approved={}", approveDTO.getRecordId(), approveDTO.getApproved());
        
        // 获取当前管理员ID
        Long approverId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        
        boolean success = borrowService.approveBorrow(
            approveDTO.getRecordId(),
            approveDTO.getApproved(),
            approveDTO.getRejectReason(),
            approverId
        );
        
        if (success) {
            return Result.success();
        } else {
            return Result.error("审批失败");
        }
    }
}
