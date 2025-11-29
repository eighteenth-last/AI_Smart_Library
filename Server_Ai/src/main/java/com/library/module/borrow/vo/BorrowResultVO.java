package com.library.module.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 借阅结果VO
 */
@Data
public class BorrowResultVO {

    /**
     * 借阅记录ID
     */
    private Long recordId;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookTitle;

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
}
