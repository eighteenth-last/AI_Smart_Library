package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库自动更新配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auto-knowledge")
public class AutoKnowledgeConfig {
    
    /**
     * 是否启用自动更新
     */
    private Boolean enabled = true;
    
    /**
     * 定时同步配置
     */
    private ScheduleConfig schedule = new ScheduleConfig();
    
    /**
     * 向量缓存配置
     */
    private VectorCacheConfig vectorCache = new VectorCacheConfig();
    
    /**
     * 图书同步配置
     */
    private BookSyncConfig bookSync = new BookSyncConfig();
    
    @Data
    public static class ScheduleConfig {
        /**
         * Cron 表达式（每天凌晨3点执行）
         */
        private String cron = "0 0 3 * * ?";
    }
    
    @Data
    public static class VectorCacheConfig {
        /**
         * 是否启用向量缓存
         */
        private Boolean enabled = true;
        
        /**
         * 缓存过期时间（小时）
         */
        private Integer ttl = 24;
    }
    
    @Data
    public static class BookSyncConfig {
        /**
         * 是否同步图书信息
         */
        private Boolean enabled = true;
        
        /**
         * 批量处理数量
         */
        private Integer batchSize = 100;
    }
}
