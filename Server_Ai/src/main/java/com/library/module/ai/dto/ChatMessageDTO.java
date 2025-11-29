package com.library.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    /**
     * 角色: user(用户), assistant(AI助手), system(系统)
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * Token 数量
     */
    private Integer tokenCount;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}
