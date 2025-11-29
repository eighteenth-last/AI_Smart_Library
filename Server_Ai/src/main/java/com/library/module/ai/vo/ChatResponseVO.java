package com.library.module.ai.vo;

import lombok.Data;

/**
 * AI对话响应VO
 */
@Data
public class ChatResponseVO {

    /**
     * 答案
     */
    private String answer;

    /**
     * 来源：knowledge_base-知识库, deepseek-Deepseek API
     */
    private String source;

    /**
     * 知识库ID（如果来源是知识库）
     */
    private Long kbId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 输入Token数
     */
    private Integer inputTokens;

    /**
     * 输出Token数
     */
    private Integer outputTokens;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 上下文Token数
     */
    private Integer contextTokens;
}
