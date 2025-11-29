package com.library.module.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 归还结果VO
 */
@Data
public class ReturnResultVO {

    /**
     * 借阅记录ID
     */
    private Long recordId;

    /**
     * 归还时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 逾期费用
     */
    private BigDecimal overdueFee;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;
}
