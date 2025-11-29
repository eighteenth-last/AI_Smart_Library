package com.library.module.author.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答视图对象
 */
@Data
@Schema(description = "问答信息")
public class QuestionVO {

    @Schema(description = "问答ID")
    private Long qaId;

    @Schema(description = "提问用户ID")
    private Long userId;

    @Schema(description = "提问用户名")
    private String username;

    @Schema(description = "问题内容")
    private String question;

    @Schema(description = "回答内容")
    private String answer;

    @Schema(description = "状态：pending-待回复, answered-已回复")
    private String status;

    @Schema(description = "是否公开：1公开 0私密")
    private Integer isPublic;

    @Schema(description = "提问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "回答时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answeredAt;
}
