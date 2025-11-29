package com.library.module.borrow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 借阅请求DTO
 */
@Data
public class BorrowRequestDTO {

    /**
     * 图书ID
     */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    /**
     * 用户ID（从token获取，前端不传）
     */
    private Long userId;
}
