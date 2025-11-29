package com.library.module.ai.service;

import com.library.module.ai.dto.ChatMessageDTO;
import java.util.List;

/**
 * 上下文管理服务接口
 */
public interface ContextManagerService {
    
    /**
     * 获取会话上下文
     * 
     * @param sessionId 会话ID
     * @return 上下文消息列表
     */
    List<ChatMessageDTO> getContext(String sessionId);
    
    /**
     * 添加消息到上下文
     * 
     * @param sessionId 会话ID
     * @param role 角色 (user/assistant)
     * @param content 消息内容
     */
    void addMessage(String sessionId, String role, String content);
    
    /**
     * 清空会话上下文
     * 
     * @param sessionId 会话ID
     */
    void clearContext(String sessionId);
    
    /**
     * 获取上下文 Token 统计
     * 
     * @param sessionId 会话ID
     * @return Token 数量
     */
    int getContextTokenCount(String sessionId);
    
    /**
     * 压缩上下文（根据配置策略）
     * 
     * @param sessionId 会话ID
     */
    void compressContext(String sessionId);
}
