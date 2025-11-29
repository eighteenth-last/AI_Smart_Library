package com.library.module.borrow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.borrow.service.BorrowService;
import com.library.module.borrow.vo.BorrowRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端借阅记录控制器
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户端-借阅记录", description = "用户查询个人借阅记录接口")
public class UserBorrowController {

    @Autowired
    private BorrowService borrowService;

    /**
     * 获取个人借阅记录
     */
    @GetMapping("/borrows")
    @Operation(summary = "个人借阅记录", description = "获取当前用户的借阅记录")
    @SaCheckLogin
    public Result<Page<BorrowRecordVO>> getUserBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        Page<BorrowRecordVO> result = borrowService.getUserBorrowRecords(userId, status, page, size);
        return Result.success(result);
    }
}
