package com.library.module.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 作者回复问题DTO
 */
@Data
@Schema(description = "作者回复问题请求")
public class QuestionReplyDTO {

    @Schema(description = "问答ID", required = true)
    @NotNull(message = "问答ID不能为空")
    private Long qaId;

    @Schema(description = "回复内容", required = true)
    @NotBlank(message = "回复内容不能为空")
    private String answer;

    @Schema(description = "是否公开：1公开 0私密", example = "1")
    private Integer isPublic = 1;
}
