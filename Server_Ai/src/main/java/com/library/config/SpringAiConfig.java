package com.library.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI配置类
 */
@Configuration
@Slf4j
public class SpringAiConfig {

    /**
     * 配置ChatClient.Builder
     * 只有当ChatModel Bean存在时才创建（即配置了API Key）
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        log.info("初始化ChatClient.Builder，AI服务已启用");
        return ChatClient.builder(chatModel);
    }
}

