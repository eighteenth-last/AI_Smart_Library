package com.library.module.borrow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 续借结果VO
 */
@Data
public class RenewResultVO {

    /**
     * 借阅记录ID
     */
    private Long recordId;

    /**
     * 新的应还时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime newDueTime;
}
