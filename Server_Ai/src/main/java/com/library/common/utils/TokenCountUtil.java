package com.library.common.utils;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;

/**
 * Token 计数工具类
 * 使用 jtokkit 库（类似 Python 的 tiktoken）来计算文本的 Token 数量
 */
@Slf4j
public class TokenCountUtil {
    
    /**
     * 编码注册器（单例）
     */
    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    
    /**
     * 默认使用 cl100k_base 编码器（GPT-3.5/GPT-4/DeepSeek 等模型通用）
     */
    private static final Encoding ENCODING = REGISTRY.getEncoding(EncodingType.CL100K_BASE);
    
    /**
     * 计算文本的 Token 数量
     * 
     * @param text 输入文本
     * @return Token 数量
     */
    public static int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        try {
            return ENCODING.encode(text).size();
        } catch (Exception e) {
            log.error("Token 计数失败: {}", e.getMessage());
            // 降级方案：按字符数估算（中文约 1.5 倍，英文约 0.25 倍）
            return estimateTokensByLength(text);
        }
    }
    
    /**
     * 批量计算多个文本的 Token 总数
     * 
     * @param texts 文本列表
     * @return Token 总数
     */
    public static int countTokens(String... texts) {
        if (texts == null || texts.length == 0) {
            return 0;
        }
        
        int totalTokens = 0;
        for (String text : texts) {
            totalTokens += countTokens(text);
        }
        return totalTokens;
    }
    
    /**
     * 计算对话消息的 Token 数量（考虑消息格式）
     * OpenAI/DeepSeek 的消息格式：{"role": "user", "content": "..."}
     * 
     * @param role 角色（system/user/assistant）
     * @param content 消息内容
     * @return Token 数量
     */
    public static int countMessageTokens(String role, String content) {
        // 消息格式会增加一些额外的 Token
        // 格式：<|im_start|>role\ncontent<|im_end|>
        int formatTokens = 4; // role, content, im_start, im_end
        int contentTokens = countTokens(content);
        
        return formatTokens + contentTokens;
    }
    
    /**
     * 降级方案：根据字符长度估算 Token 数量
     * 
     * @param text 文本
     * @return 估算的 Token 数量
     */
    private static int estimateTokensByLength(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseCount = 0;
        int englishCount = 0;
        
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FA5) {
                // 中文字符
                chineseCount++;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                // 英文字符
                englishCount++;
            }
        }
        
        // 中文约 1.5 个字符 = 1 Token，英文约 4 个字符 = 1 Token
        int chineseTokens = (int) Math.ceil(chineseCount / 1.5);
        int englishTokens = englishCount / 4;
        
        return chineseTokens + englishTokens;
    }
    
    /**
     * 获取编码器名称
     * 
     * @return 编码器名称
     */
    public static String getEncodingName() {
        return ENCODING.getName();
    }
}
