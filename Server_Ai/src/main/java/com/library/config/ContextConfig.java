package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 上下文管理配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "context")
public class ContextConfig {
    
    /**
     * 上下文窗口大小（最近 N 轮对话）
     */
    private Integer windowSize = 10;
    
    /**
     * Token 配置
     */
    private TokenConfig token = new TokenConfig();
    
    /**
     * 压缩配置
     */
    private CompressionConfig compression = new CompressionConfig();
    
    @Data
    public static class TokenConfig {
        /**
         * 最大 Token 数（包括上下文 + 当前问题 + 系统 prompt）
         */
        private Integer maxTotal = 8000;
        
        /**
         * 上下文最大 Token 数
         */
        private Integer maxContext = 6000;
        
        /**
         * 预留 Token（用于系统 prompt 和回答）
         */
        private Integer reserved = 2000;
    }
    
    @Data
    public static class CompressionConfig {
        /**
         * 是否启用压缩
         */
        private Boolean enabled = true;
        
        /**
         * 压缩策略: none(不压缩), truncate(截断), summarize(摘要)
         */
        private String strategy = "truncate";
        
        /**
         * 保留首部对话轮数
         */
        private Integer keepFirst = 2;
        
        /**
         * 保留尾部对话轮数
         */
        private Integer keepLast = 5;
    }
}
