package com.library.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI对话请求DTO
 */
@Data
public class ChatRequestDTO {

    /**
     * 问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 会话ID（可选，用于关联上下文）
     */
    private String sessionId;
    
    /**
     * 用户ID（可选，由后端设置）
     */
    private Long userId;
}
