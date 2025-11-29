package com.library.module.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录VO
 */
@Data
public class BorrowRecordVO {

    /**
     * 借阅记录ID
     */
    private Long recordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookTitle;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 借阅时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowTime;

    /**
     * 应还时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueTime;

    /**
     * 归还时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 剩余天数（负数表示逾期天数）
     */
    private Integer daysRemaining;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
