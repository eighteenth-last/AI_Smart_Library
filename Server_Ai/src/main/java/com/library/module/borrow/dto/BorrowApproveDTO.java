package com.library.module.borrow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 借阅审批请求DTO
 */
@Data
@Schema(description = "借阅审批请求")
public class BorrowApproveDTO {

    @Schema(description = "借阅记录ID", required = true)
    @NotNull(message = "借阅记录ID不能为空")
    private Long recordId;

    @Schema(description = "审批结果：true-同意，false-拒绝", required = true)
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}
