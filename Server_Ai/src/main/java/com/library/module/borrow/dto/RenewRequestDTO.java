package com.library.module.borrow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 续借请求DTO
 */
@Data
public class RenewRequestDTO {

    /**
     * 借阅记录ID
     */
    @NotNull(message = "借阅记录ID不能为空")
    private Long recordId;

    /**
     * 用户ID（从token获取，前端不传）
     */
    private Long userId;
}
