package com.library.module.ai.task;

import com.library.config.AutoKnowledgeConfig;
import com.library.module.ai.service.AutoKnowledgeGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 知识库自动更新定时任务
 */
@Slf4j
@Component
public class AutoKnowledgeSyncTask {
    
    @Autowired
    private AutoKnowledgeGenerateService autoKnowledgeGenerateService;
    
    @Autowired
    private AutoKnowledgeConfig config;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 定时同步图书信息到知识库
     * 默认：每天凌晨3点执行
     */
    @Scheduled(cron = "${auto-knowledge.schedule.cron:0 0 3 * * ?}")
    public void syncBookKnowledge() {
        if (!config.getEnabled()) {
            log.debug("知识库自动更新已禁用，跳过同步");
            return;
        }
        
        if (!config.getBookSync().getEnabled()) {
            log.debug("图书同步已禁用，跳过同步");
            return;
        }
        
        log.info("========== 开始执行知识库自动同步任务 ==========");
        log.info("执行时间: {}", LocalDateTime.now().format(FORMATTER));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 从所有上架图书生成知识库
            int count = autoKnowledgeGenerateService.generateKnowledgeFromAllBooks();
            
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            log.info("知识库同步完成 - 生成: {} 条, 耗时: {} 秒", count, duration);
            log.info("========== 知识库自动同步任务结束 ==========");
            
        } catch (Exception e) {
            log.error("知识库同步失败", e);
            log.info("========== 知识库自动同步任务异常结束 ==========");
        }
    }
    
    /**
     * 手动触发同步（用于测试）
     * 
     * @return 生成的知识库条目数
     */
    public int manualSync() {
        log.info("手动触发知识库同步");
        return autoKnowledgeGenerateService.generateKnowledgeFromAllBooks();
    }
}
