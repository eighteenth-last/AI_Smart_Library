package com.library.module.borrow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 */
@TableName("borrow_record")
@Data
public class BorrowRecord {

    /**
     * 借阅记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 借阅时间
     */
    private LocalDateTime borrowTime;

    /**
     * 应还时间（默认30天后）
     */
    private LocalDateTime dueTime;

    /**
     * 归还时间
     */
    private LocalDateTime returnTime;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批人id
     */
    private Long approverId;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 状态：pending-待审批, borrowed-借阅中, returned-已归还, overdue-逾期, rejected-已拒绝
     */
    private String status;

    /**
     * 是否已续借
     */
    private Integer isRenewed;

    /**
     * 逾期费用
     */
    private BigDecimal overdueFee;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer deleted;
}
