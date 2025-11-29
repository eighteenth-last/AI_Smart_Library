package com.library.module.ai.service.impl;

import com.library.common.utils.TokenCountUtil;
import com.library.config.ContextConfig;
import com.library.module.ai.dto.ChatMessageDTO;
import com.library.module.ai.service.ContextManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 上下文管理服务实现
 */
@Slf4j
@Service
public class ContextManagerServiceImpl implements ContextManagerService {
    
    @Autowired
    private ContextConfig contextConfig;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CONTEXT_KEY_PREFIX = "ai:context:";
    private static final long CONTEXT_TTL = 24; // 24小时过期
    
    @Override
    public List<ChatMessageDTO> getContext(String sessionId) {
        if (redisTemplate == null) {
            log.warn("RedisTemplate 未配置，返回空上下文");
            return new ArrayList<>();
        }
        
        String key = CONTEXT_KEY_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(key);
        
        if (obj instanceof List) {
            return (List<ChatMessageDTO>) obj;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public void addMessage(String sessionId, String role, String content) {
        if (redisTemplate == null) {
            log.warn("RedisTemplate 未配置，无法保存上下文");
            return;
        }
        
        String key = CONTEXT_KEY_PREFIX + sessionId;
        List<ChatMessageDTO> context = getContext(sessionId);
        
        // 计算消息的 Token 数量
        int tokenCount = TokenCountUtil.countTokens(content);
        
        ChatMessageDTO message = new ChatMessageDTO();
        message.setRole(role);
        message.setContent(content);
        message.setTokenCount(tokenCount);
        message.setTimestamp(System.currentTimeMillis());
        
        context.add(message);
        
        // 检查是否超过窗口大小
        if (context.size() > contextConfig.getWindowSize() * 2) {
            // 保留最近的 N 轮对话（每轮2条消息：user + assistant）
            int keepSize = contextConfig.getWindowSize() * 2;
            context = context.subList(context.size() - keepSize, context.size());
        }
        
        // 检查 Token 数量，如果超过限制则压缩
        int totalTokens = calculateTotalTokens(context);
        if (totalTokens > contextConfig.getToken().getMaxContext()) {
            context = compressContextMessages(context);
        }
        
        // 保存到 Redis
        redisTemplate.opsForValue().set(key, context, CONTEXT_TTL, TimeUnit.HOURS);
        
        log.debug("添加消息到上下文 - sessionId: {}, role: {}, tokens: {}, totalTokens: {}", 
                sessionId, role, tokenCount, totalTokens);
    }
    
    @Override
    public void clearContext(String sessionId) {
        if (redisTemplate == null) {
            log.warn("RedisTemplate 未配置，无法清空上下文");
            return;
        }
        
        String key = CONTEXT_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.info("清空会话上下文 - sessionId: {}", sessionId);
    }
    
    @Override
    public int getContextTokenCount(String sessionId) {
        List<ChatMessageDTO> context = getContext(sessionId);
        return calculateTotalTokens(context);
    }
    
    @Override
    public void compressContext(String sessionId) {
        if (redisTemplate == null) {
            log.warn("RedisTemplate 未配置，无法压缩上下文");
            return;
        }
        
        String key = CONTEXT_KEY_PREFIX + sessionId;
        List<ChatMessageDTO> context = getContext(sessionId);
        
        if (context.isEmpty()) {
            return;
        }
        
        List<ChatMessageDTO> compressed = compressContextMessages(context);
        redisTemplate.opsForValue().set(key, compressed, CONTEXT_TTL, TimeUnit.HOURS);
        
        log.info("压缩上下文 - sessionId: {}, before: {} messages, after: {} messages", 
                sessionId, context.size(), compressed.size());
    }
    
    /**
     * 计算总 Token 数
     */
    private int calculateTotalTokens(List<ChatMessageDTO> messages) {
        return messages.stream()
                .mapToInt(msg -> msg.getTokenCount() != null ? msg.getTokenCount() : 0)
                .sum();
    }
    
    /**
     * 压缩上下文消息
     */
    private List<ChatMessageDTO> compressContextMessages(List<ChatMessageDTO> messages) {
        if (messages.isEmpty()) {
            return messages;
        }
        
        ContextConfig.CompressionConfig compression = contextConfig.getCompression();
        
        // 如果未启用压缩，直接返回
        if (!compression.getEnabled()) {
            return messages;
        }
        
        String strategy = compression.getStrategy();
        
        if ("none".equals(strategy)) {
            return messages;
        } else if ("truncate".equals(strategy)) {
            return truncateContext(messages, compression);
        } else if ("summarize".equals(strategy)) {
            return summarizeContext(messages, compression);
        }
        
        return messages;
    }
    
    /**
     * 截断策略：保留首尾，移除中间
     */
    private List<ChatMessageDTO> truncateContext(List<ChatMessageDTO> messages, ContextConfig.CompressionConfig compression) {
        int total = messages.size();
        int keepFirst = compression.getKeepFirst() * 2; // 每轮2条消息
        int keepLast = compression.getKeepLast() * 2;
        
        // 如果总数不超过保留数，直接返回
        if (total <= keepFirst + keepLast) {
            return messages;
        }
        
        List<ChatMessageDTO> result = new ArrayList<>();
        
        // 保留首部
        result.addAll(messages.subList(0, Math.min(keepFirst, total)));
        
        // 添加省略标记
        ChatMessageDTO ellipsis = new ChatMessageDTO();
        ellipsis.setRole("system");
        ellipsis.setContent("...[省略 " + (total - keepFirst - keepLast) + " 条历史消息]...");
        ellipsis.setTokenCount(10);
        ellipsis.setTimestamp(System.currentTimeMillis());
        result.add(ellipsis);
        
        // 保留尾部
        result.addAll(messages.subList(Math.max(0, total - keepLast), total));
        
        log.debug("截断上下文 - total: {}, kept: {}, removed: {}", 
                total, result.size(), total - result.size() + 1);
        
        return result;
    }
    
    /**
     * 摘要策略：对中间部分生成摘要
     * 注意：此功能需要调用 AI 生成摘要，成本较高，当前实现为简单截断
     */
    private List<ChatMessageDTO> summarizeContext(List<ChatMessageDTO> messages, ContextConfig.CompressionConfig compression) {
        // TODO: 实现真正的摘要功能（调用 AI 生成摘要）
        // 当前先使用截断策略
        return truncateContext(messages, compression);
    }
    
    /**
     * 构建对话上下文字符串（用于发送给 AI）
     * 
     * @param sessionId 会话ID
     * @return 上下文字符串
     */
    public String buildContextString(String sessionId) {
        List<ChatMessageDTO> context = getContext(sessionId);
        
        if (context.isEmpty()) {
            return "";
        }
        
        return context.stream()
                .map(msg -> {
                    if ("user".equals(msg.getRole())) {
                        return "用户: " + msg.getContent();
                    } else if ("assistant".equals(msg.getRole())) {
                        return "助手: " + msg.getContent();
                    } else if ("system".equals(msg.getRole())) {
                        return msg.getContent();
                    }
                    return msg.getContent();
                })
                .collect(Collectors.joining("\n\n"));
    }
}
