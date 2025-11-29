package com.library.module.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.common.result.ResultCode;
import com.library.common.utils.TokenCountUtil;
import com.library.module.ai.dto.ChatRequestDTO;
import com.library.module.ai.dto.KnowledgeSearchResult;
import com.library.module.ai.dto.TokenStatsDTO;
import com.library.module.ai.entity.AiChatLog;
import com.library.module.ai.entity.AiSession;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.entity.TokenUsageStats;
import com.library.module.ai.mapper.AiChatLogMapper;
import com.library.module.ai.mapper.AiSessionMapper;
import com.library.module.ai.mapper.KnowledgeBaseMapper;
import com.library.module.ai.mapper.TokenUsageStatsMapper;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;

import com.library.module.ai.prompt.RagPromptTemplate;
import com.library.module.ai.service.AiService;
import com.library.module.ai.service.ContextManagerService;
import com.library.module.ai.service.VectorService;
import com.library.module.ai.vo.ChatResponseVO;
import com.library.module.ai.vo.SessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI智能客服服务实现类
 */
@Slf4j
@Service
public class AiServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements AiService {

    @Autowired(required = false)
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private AiChatLogMapper aiChatLogMapper;

    @Autowired
    private AiSessionMapper aiSessionMapper;

    @Autowired
    private TokenUsageStatsMapper tokenUsageStatsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private VectorService vectorService;
    
    @Autowired
    private RagPromptTemplate ragPromptTemplate;
    
    @Autowired
    private ContextManagerService contextManager;
    
    // RAG 配置
    @Value("${rag.enabled:true}")
    private boolean ragEnabled;
    
    @Value("${rag.vector.top-k:3}")
    private int vectorTopK;
    
    @Value("${rag.vector.min-similarity:0.7}")
    private double minSimilarity;
    
    @Value("${rag.fulltext.enabled:true}")
    private boolean fulltextEnabled;
    
    @Value("${rag.fulltext.max-results:5}")
    private int fulltextMaxResults;
    
    @Value("${rag.hybrid.vector-weight:0.7}")
    private double vectorWeight;
    
    @Value("${rag.hybrid.fulltext-weight:0.3}")
    private double fulltextWeight;

    @Override
    @Transactional
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String question = requestDTO.getQuestion();
        String sessionId = requestDTO.getSessionId();
        Long userId = requestDTO.getUserId();
        
        // 生成会话ID（如果没有）
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        // 确保会话存在并设置用户信息（在对话前创建）
        AiSession existingSession = aiSessionMapper.selectById(sessionId);
        if (existingSession == null) {
            // 创建新会话
            AiSession newSession = new AiSession();
            newSession.setSessionId(sessionId);
            newSession.setSessionTitle("新对话");
            newSession.setStatus("active");
            
            // 设置用户信息（如果已登录）
            if (userId != null) {
                newSession.setUserId(userId);
                // 查询用户角色
                User user = userMapper.selectById(userId);
                if (user != null) {
                    newSession.setUserRole(user.getRole());
                }
            }
            
            newSession.setCreatedAt(LocalDateTime.now());
            newSession.setLastActiveAt(LocalDateTime.now());
            aiSessionMapper.insert(newSession);
            
            log.info("创建新会话 - SessionId: {}, UserId: {}, UserRole: {}", 
                sessionId, userId, newSession.getUserRole());
        }
        
        log.info("开始处理 AI 对话 - Session: {}, Question: {}", sessionId, question);
        
        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setSessionId(sessionId);
        
        // 判断是否启用 RAG
        if (ragEnabled && vectorService != null) {
            // RAG 模式：向量检索 + 增强生成
            handleRagMode(question, sessionId, responseVO);
        } else {
            // 传统模式：模糊匹配 + LLM
            handleTraditionalMode(question, sessionId, responseVO);
        }
        
        // 记录对话日志
        saveChatLog(sessionId, question, responseVO);
        
        log.info("AI 对话完成 - Source: {}, Tokens: {}", 
                responseVO.getSource(), responseVO.getTotalTokens());
        
        return responseVO;
    }
    
    /**
     * RAG 模式处理
     */
    private void handleRagMode(String question, String sessionId, ChatResponseVO responseVO) {
        log.info("使用 RAG 模式检索知识库");
        
        // 1. 向量检索知识库
        List<KnowledgeSearchResult> searchResults = 
            vectorSearchKnowledgeBase(question, vectorTopK, minSimilarity);
        
        if (searchResults != null && !searchResults.isEmpty()) {
            // 2. 找到匹配的知识库 -> RAG 增强生成
            KnowledgeSearchResult topResult = searchResults.get(0);
            
            log.info("检索到 {} 条相关知识库，最高相似度: {:.2f}%", 
                    searchResults.size(), topResult.getSimilarityScore() * 100);
            
            // 增加知识库命中次数
            incrementHitCount(topResult.getKnowledgeBase().getKbId());
            
            // 获取上下文（如果有）
            String contextStr = "";
            try {
                ContextManagerServiceImpl contextService = (ContextManagerServiceImpl) contextManager;
                contextStr = contextService.buildContextString(sessionId);
                if (contextStr != null && !contextStr.isEmpty()) {
                    log.info("附加上下文 - 长度: {} 字符", contextStr.length());
                }
            } catch (Exception e) {
                log.warn("获取上下文失败: {}", e.getMessage());
            }
            
            // 构造 RAG Prompt（附加上下文）
            String ragPrompt = ragPromptTemplate.buildKnowledgeBasePrompt(question, searchResults);
            if (contextStr != null && !contextStr.isEmpty()) {
                ragPrompt = "历史上下文:\n" + contextStr + "\n\n" + ragPrompt;
            }
            
            // 计算输入 Token 数量
            int inputTokens = TokenCountUtil.countTokens(ragPrompt);
            
            // 调用 DeepSeek 生成
            String answer = callDeepSeekChat(ragPrompt);
            
            // 计算输出 Token 数量
            int outputTokens = TokenCountUtil.countTokens(answer);
            int totalTokens = inputTokens + outputTokens;
            
            responseVO.setAnswer(answer);
            responseVO.setSource("rag");
            responseVO.setKbId(topResult.getKnowledgeBase().getKbId());
            responseVO.setInputTokens(inputTokens);
            responseVO.setOutputTokens(outputTokens);
            responseVO.setTotalTokens(totalTokens);
            
            // 更新上下文：添加用户问题和 AI 回答
            try {
                contextManager.addMessage(sessionId, "user", question);
                contextManager.addMessage(sessionId, "assistant", answer);
                
                int contextTokens = contextManager.getContextTokenCount(sessionId);
                responseVO.setContextTokens(contextTokens);
                
                log.info("上下文已更新 - 当前 Token 数: {}", contextTokens);
            } catch (Exception e) {
                log.warn("更新上下文失败: {}", e.getMessage());
            }
            
            log.info("RAG 模式 Token 统计 - 输入: {}, 输出: {}, 总计: {}", 
                    inputTokens, outputTokens, totalTokens);
            
        } else {
            // 3. 未找到匹配的知识库 -> 纯 LLM 模式
            log.info("未找到相关知识库，使用纯 LLM 模式");
            
            // 获取上下文
            String contextStr = "";
            try {
                ContextManagerServiceImpl contextService = (ContextManagerServiceImpl) contextManager;
                contextStr = contextService.buildContextString(sessionId);
            } catch (Exception e) {
                log.warn("获取上下文失败: {}", e.getMessage());
            }
            
            String llmPrompt = ragPromptTemplate.buildPureLLMPrompt(question, contextStr);
            
            // 计算输入 Token 数量
            int inputTokens = TokenCountUtil.countTokens(llmPrompt);
            
            String answer = callDeepSeekChat(llmPrompt);
            
            // 计算输出 Token 数量
            int outputTokens = TokenCountUtil.countTokens(answer);
            int totalTokens = inputTokens + outputTokens;
            
            responseVO.setAnswer(answer);
            responseVO.setSource("deepseek");
            responseVO.setInputTokens(inputTokens);
            responseVO.setOutputTokens(outputTokens);
            responseVO.setTotalTokens(totalTokens);
            
            // 更新上下文
            try {
                contextManager.addMessage(sessionId, "user", question);
                contextManager.addMessage(sessionId, "assistant", answer);
                
                int contextTokens = contextManager.getContextTokenCount(sessionId);
                responseVO.setContextTokens(contextTokens);
            } catch (Exception e) {
                log.warn("更新上下文失败: {}", e.getMessage());
            }
            
            log.info("纯 LLM 模式 Token 统计 - 输入: {}, 输出: {}, 总计: {}", 
                    inputTokens, outputTokens, totalTokens);
        }
    }
    
    /**
     * 传统模式处理（模糊匹配 + LLM）
     */
    private void handleTraditionalMode(String question, String sessionId, ChatResponseVO responseVO) {
        log.info("使用传统模式（模糊匹配）");
        
        // 1. 先查询知识库
        KnowledgeBase knowledgeBase = searchKnowledgeBase(question);
        
        if (knowledgeBase != null) {
            // 2. 如果知识库有匹配，直接返回
            String answer = knowledgeBase.getAnswer();
            
            // 计算 Token（知识库直接匹配，输入 Token 为问题）
            int inputTokens = TokenCountUtil.countTokens(question);
            int outputTokens = TokenCountUtil.countTokens(answer);
            int totalTokens = inputTokens + outputTokens;
            
            responseVO.setAnswer(answer);
            responseVO.setSource("knowledge_base");
            responseVO.setKbId(knowledgeBase.getKbId());
            responseVO.setInputTokens(inputTokens);
            responseVO.setOutputTokens(outputTokens);
            responseVO.setTotalTokens(totalTokens);
            
            incrementHitCount(knowledgeBase.getKbId());
            
            log.info("知识库匹配 Token 统计 - 输入: {}, 输出: {}, 总计: {}", 
                    inputTokens, outputTokens, totalTokens);
        } else {
            // 3. 如果知识库没有匹配，调用Deepseek API
            
            // 计算输入 Token
            int inputTokens = TokenCountUtil.countTokens(question);
            
            String answer = callDeepSeekChat(question);
            
            // 计算输出 Token
            int outputTokens = TokenCountUtil.countTokens(answer);
            int totalTokens = inputTokens + outputTokens;
            
            responseVO.setAnswer(answer);
            responseVO.setSource("deepseek");
            responseVO.setInputTokens(inputTokens);
            responseVO.setOutputTokens(outputTokens);
            responseVO.setTotalTokens(totalTokens);
            
            log.info("DeepSeek API Token 统计 - 输入: {}, 输出: {}, 总计: {}", 
                    inputTokens, outputTokens, totalTokens);
        }
    }
    
    /**
     * 调用 DeepSeek Chat API
     */
    private String callDeepSeekChat(String prompt) {
        if (chatClientBuilder == null) {
            log.warn("ChatClient 未配置，返回默认回复");
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
        
        try {
            ChatClient chatClient = chatClientBuilder.build();
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, 
                    "AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存对话日志
     */
    private void saveChatLog(String sessionId, String question, ChatResponseVO responseVO) {
        AiChatLog chatLog = new AiChatLog();
        chatLog.setSessionId(sessionId);
        chatLog.setQuestion(question);
        chatLog.setAnswer(responseVO.getAnswer());
        chatLog.setSource(responseVO.getSource());
        chatLog.setKbId(responseVO.getKbId());
        chatLog.setInputTokens(responseVO.getInputTokens());
        chatLog.setOutputTokens(responseVO.getOutputTokens());
        chatLog.setTotalTokens(responseVO.getTotalTokens());
        chatLog.setCreatedAt(LocalDateTime.now());
        
        // 查询会话信息获取用户ID和角色
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session != null) {
            chatLog.setUserId(session.getUserId());
            chatLog.setUserRole(session.getUserRole());
            
            // 如果会话中没有用户角色，尝试从用户表获取
            if (session.getUserId() != null && session.getUserRole() == null) {
                User user = userMapper.selectById(session.getUserId());
                if (user != null) {
                    chatLog.setUserRole(user.getRole());
                    
                    // 同时更新会话的 userRole
                    session.setUserRole(user.getRole());
                    aiSessionMapper.updateById(session);
                    log.info("补充会话用户角色 - SessionId: {}, UserRole: {}", sessionId, user.getRole());
                }
            }
        }
        
        aiChatLogMapper.insert(chatLog);
        
        // 同时记录 Token 统计到 token_usage_stats 表
        recordTokenUsage(sessionId, responseVO);
    }

    @Override
    public Page<AiChatLog> getChatHistory(Long userId, String sessionId, Integer page, Integer size) {
        Page<AiChatLog> chatLogPage = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatLog> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatLog>()
                .eq(userId != null, AiChatLog::getUserId, userId)
                .eq(sessionId != null, AiChatLog::getSessionId, sessionId)
                .orderByDesc(AiChatLog::getCreatedAt);
        
        return aiChatLogMapper.selectPage(chatLogPage, wrapper);
    }

    @Override
    @Transactional
    public boolean addKnowledgeBase(KnowledgeBase knowledgeBase) {
        // 设置默认值
        if (knowledgeBase.getHitCount() == null) {
            knowledgeBase.setHitCount(0);
        }
        if (knowledgeBase.getIsPublic() == null) {
            knowledgeBase.setIsPublic(1);
        }
        if (knowledgeBase.getSource() == null) {
            knowledgeBase.setSource("system");
        }
        
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        
        return knowledgeBaseMapper.insert(knowledgeBase) > 0;
    }

    @Override
    @Transactional
    public boolean updateKnowledgeBase(Long kbId, KnowledgeBase knowledgeBase) {
        KnowledgeBase existing = knowledgeBaseMapper.selectById(kbId);
        if (existing == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        
        knowledgeBase.setKbId(kbId);
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        
        return knowledgeBaseMapper.updateById(knowledgeBase) > 0;
    }

    @Override
    @Transactional
    public boolean deleteKnowledgeBase(Long kbId) {
        KnowledgeBase existing = knowledgeBaseMapper.selectById(kbId);
        if (existing == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        
        return knowledgeBaseMapper.deleteById(kbId) > 0;
    }

    @Override
    public Page<KnowledgeBase> getKnowledgeBaseList(String category, Integer page, Integer size) {
        Page<KnowledgeBase> knowledgeBasePage = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase>()
                .eq(category != null, KnowledgeBase::getCategory, category)
                .eq(KnowledgeBase::getIsPublic, 1)
                .orderByDesc(KnowledgeBase::getHitCount);
        
        return knowledgeBaseMapper.selectPage(knowledgeBasePage, wrapper);
    }

    @Override
    public KnowledgeBase searchKnowledgeBase(String keyword) {
        // 简单的模糊匹配（实际可以使用更复杂的匹配算法）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase>()
                .like(KnowledgeBase::getQuestion, keyword)
                .or()
                .like(KnowledgeBase::getAnswer, keyword)
                .eq(KnowledgeBase::getIsPublic, 1)
                .orderByDesc(KnowledgeBase::getHitCount)
                .last("LIMIT 1");
        
        return knowledgeBaseMapper.selectOne(wrapper);
    }

    @Override
    public List<KnowledgeSearchResult> vectorSearchKnowledgeBase(String keyword, int topK, double minSimilarity) {
        // 1. 获取所有公开的知识库条目
        List<KnowledgeBase> allKnowledge = knowledgeBaseMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getIsPublic, 1)
        );

        if (allKnowledge.isEmpty() || vectorService == null) {
            // 如果没有知识库数据或VectorService未启用，返回空列表
            return new ArrayList<>();
        }

        // 2. 将查询问题向量化
        List<Double> queryVector = vectorService.embedText(keyword);

        // 3. 计算每个知识库条目的相似度
        List<KnowledgeSearchResult> results = new ArrayList<>();
        
        for (KnowledgeBase kb : allKnowledge) {
            // 将知识库问题向量化
            List<Double> kbVector = vectorService.embedText(kb.getQuestion());
            
            // 计算余弦相似度
            double similarity = vectorService.cosineSimilarity(queryVector, kbVector);
            
            // 只保留相似度大于阈值的结果
            if (similarity >= minSimilarity) {
                results.add(new KnowledgeSearchResult(kb, similarity, "vector"));
            }
        }

        // 4. 按相似度排序并返回前K个
        return results.stream()
                .sorted(Comparator.comparingDouble(KnowledgeSearchResult::getSimilarityScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
       public void incrementHitCount(Long kbId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(kbId);
        if (knowledgeBase != null) {
            knowledgeBase.setHitCount(knowledgeBase.getHitCount() + 1);
            knowledgeBaseMapper.updateById(knowledgeBase);
        }
    }
    
    /**
     * 记录 Token 使用统计
     */
    private void recordTokenUsage(String sessionId, ChatResponseVO responseVO) {
        try {
            // 查询会话信息获取用户ID和角色
            AiSession session = aiSessionMapper.selectById(sessionId);
            Long userId = null;
            String userRole = null;
            
            if (session != null) {
                userId = session.getUserId();
                userRole = session.getUserRole();
            }
            
            // 如果会话中没有用户信息，尝试从用户表获取
            if (userId != null && userRole == null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    userRole = user.getRole();
                }
            }
            
            // 创建 Token 统计记录
            TokenUsageStats stats = new TokenUsageStats();
            stats.setUserId(userId);
            stats.setUserRole(userRole);
            stats.setSessionId(sessionId);
            stats.setInputTokens(responseVO.getInputTokens() != null ? responseVO.getInputTokens() : 0);
            stats.setOutputTokens(responseVO.getOutputTokens() != null ? responseVO.getOutputTokens() : 0);
            stats.setTotalTokens(responseVO.getTotalTokens() != null ? responseVO.getTotalTokens() : 0);
            stats.setModelName("deepseek-chat"); // 可以从配置中获取
            
            // 计算估算成本（DeepSeek 价格：¥0.001/1K tokens）
            int totalTokens = stats.getTotalTokens();
            double cost = (totalTokens / 1000.0) * 0.001;
            stats.setEstimatedCost(BigDecimal.valueOf(cost));
            
            // 设置统计日期和小时
            LocalDateTime now = LocalDateTime.now();
            stats.setStatDate(now.toLocalDate());
            stats.setStatHour(now.getHour());
            stats.setCreatedAt(now);
            
            log.info("准备保存Token统计 - ModelName: {}, UserRole: {}, TotalTokens: {}", 
                stats.getModelName(), stats.getUserRole(), stats.getTotalTokens());
            
            // 保存统计记录
            tokenUsageStatsMapper.insert(stats);
            
            log.info("Token 统计记录成功 - Session: {}, User: {}, Tokens: {}", 
                    sessionId, userId, totalTokens);
        } catch (Exception e) {
            log.error("记录 Token 统计失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public List<SessionVO> getUserSessions(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiSession> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiSession>()
                .eq(userId != null, AiSession::getUserId, userId)
                .eq(AiSession::getStatus, "active")
                .orderByDesc(AiSession::getLastActiveAt);

        List<AiSession> sessions = aiSessionMapper.selectList(wrapper);
        return sessions.stream().map(session -> {
            SessionVO vo = new SessionVO();
            vo.setSessionId(session.getSessionId());
            vo.setSessionTitle(session.getSessionTitle());
            vo.setTotalMessages(session.getTotalMessages());
            vo.setLastActiveAt(session.getLastActiveAt());
            vo.setCreatedAt(session.getCreatedAt());

            // 获取预览（第一条用户消息）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatLog> logWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatLog>()
                    .eq(AiChatLog::getSessionId, session.getSessionId())
                    .orderByAsc(AiChatLog::getCreatedAt)
                    .last("LIMIT 1");
            AiChatLog firstLog = aiChatLogMapper.selectOne(logWrapper);
            if (firstLog != null) {
                String preview = firstLog.getQuestion();
                vo.setPreview(preview.length() > 50 ? preview.substring(0, 50) + "..." : preview);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AiSession getSessionById(String sessionId) {
        return aiSessionMapper.selectById(sessionId);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateSession(AiSession session) {
        AiSession existing = aiSessionMapper.selectById(session.getSessionId());
        if (existing != null) {
            // 更新时，只更新非空字段
            if (session.getSessionTitle() != null) {
                existing.setSessionTitle(session.getSessionTitle());
            }
            if (session.getTotalMessages() != null) {
                existing.setTotalMessages(session.getTotalMessages());
            }
            if (session.getContextMessages() != null) {
                existing.setContextMessages(session.getContextMessages());
            }
            if (session.getTotalTokens() != null) {
                existing.setTotalTokens(session.getTotalTokens());
            }
            // 如果传入了新的 userId 和 userRole，才更新
            if (session.getUserId() != null) {
                existing.setUserId(session.getUserId());
            }
            if (session.getUserRole() != null) {
                existing.setUserRole(session.getUserRole());
            }
            existing.setLastActiveAt(LocalDateTime.now());
            return aiSessionMapper.updateById(existing) > 0;
        } else {
            // 新增
            if (session.getStatus() == null) {
                session.setStatus("active");
            }
            if (session.getLastActiveAt() == null) {
                session.setLastActiveAt(LocalDateTime.now());
            }
            if (session.getCreatedAt() == null) {
                session.setCreatedAt(LocalDateTime.now());
            }
            return aiSessionMapper.insert(session) > 0;
        }
    }

    @Override
    @Transactional
    public boolean deleteSession(String sessionId, Long userId) {
        // 验证权限
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            return false;
        }
        if (userId != null && !userId.equals(session.getUserId())) {
            throw new BusinessException(ResultCode.FAILED, "无权删除此会话");
        }

        // 软删除：修改状态为deleted
        session.setStatus("deleted");
        return aiSessionMapper.updateById(session) > 0;
    }

    @Override
    @Transactional
    public boolean clearAllSessions(Long userId) {
        if (userId == null) {
            return false;
        }

        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiSession> wrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiSession>()
                .eq(AiSession::getUserId, userId)
                .eq(AiSession::getStatus, "active")
                .set(AiSession::getStatus, "deleted");

        return aiSessionMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 混合检索（向量检索 + 全文检索）
     * 
     * @param keyword 关键词
     * @param topK 返回数量
     * @return 混合检索结果
     */
    public List<KnowledgeSearchResult> hybridSearch(String keyword, int topK) {
        log.info("执行混合检索 - 关键词: {}", keyword);
        
        Map<Long, KnowledgeSearchResult> mergedResults = new java.util.HashMap<>();
        
        // 1. 向量检索
        if (vectorService != null) {
            List<KnowledgeSearchResult> vectorResults = 
                vectorSearchKnowledgeBase(keyword, vectorTopK, minSimilarity);
            
            for (KnowledgeSearchResult result : vectorResults) {
                Long kbId = result.getKnowledgeBase().getKbId();
                // 向量检索赋予权重
                result.setSimilarityScore(result.getSimilarityScore() * vectorWeight);
                result.setSearchMethod("vector");
                mergedResults.put(kbId, result);
            }
            
            log.info("向量检索到 {} 条结果", vectorResults.size());
        }
        
        // 2. 全文检索
        if (fulltextEnabled) {
            List<KnowledgeSearchResult> fulltextResults = fulltextSearch(keyword, fulltextMaxResults);
            
            for (KnowledgeSearchResult result : fulltextResults) {
                Long kbId = result.getKnowledgeBase().getKbId();
                
                if (mergedResults.containsKey(kbId)) {
                    // 已存在，合并得分
                    KnowledgeSearchResult existing = mergedResults.get(kbId);
                    double combinedScore = existing.getSimilarityScore() + 
                                         (result.getSimilarityScore() * fulltextWeight);
                    existing.setSimilarityScore(combinedScore);
                    existing.setSearchMethod("hybrid");
                } else {
                    // 新结果，赋予权重
                    result.setSimilarityScore(result.getSimilarityScore() * fulltextWeight);
                    result.setSearchMethod("fulltext");
                    mergedResults.put(kbId, result);
                }
            }
            
            log.info("全文检索到 {} 条结果", fulltextResults.size());
        }
        
        // 3. 排序并返回 TopK
        List<KnowledgeSearchResult> finalResults = new ArrayList<>(mergedResults.values());
        finalResults.sort(Comparator.comparingDouble(
            KnowledgeSearchResult::getSimilarityScore).reversed());
        
        List<KnowledgeSearchResult> topResults = finalResults.stream()
            .limit(topK)
            .collect(Collectors.toList());
        
        log.info("混合检索完成，返回 {} 条结果", topResults.size());
        
        return topResults;
    }
    
    /**
     * 全文检索（基于 MySQL LIKE）
     * 
     * @param keyword 关键词
     * @param maxResults 最大结果数
     * @return 检索结果
     */
    private List<KnowledgeSearchResult> fulltextSearch(String keyword, int maxResults) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeBase>()
                .like(KnowledgeBase::getQuestion, keyword)
                .or()
                .like(KnowledgeBase::getAnswer, keyword)
                .eq(KnowledgeBase::getIsPublic, 1)
                .orderByDesc(KnowledgeBase::getHitCount)
                .last("LIMIT " + maxResults);
        
        List<KnowledgeBase> kbList = knowledgeBaseMapper.selectList(wrapper);
        
        // 转换为 KnowledgeSearchResult，使用简单的计分规则
        return kbList.stream()
            .map(kb -> {
                // 简单计分：问题匹配度 + 命中次数加权
                double score = calculateTextMatchScore(kb, keyword);
                return new KnowledgeSearchResult(kb, score, "fulltext");
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 计算文本匹配得分
     */
    private double calculateTextMatchScore(KnowledgeBase kb, String keyword) {
        String question = kb.getQuestion().toLowerCase();
        String answer = kb.getAnswer().toLowerCase();
        String keywordLower = keyword.toLowerCase();
        
        double score = 0.0;
        
        // 1. 完全匹配：1.0 分
        if (question.equals(keywordLower)) {
            score += 1.0;
        } 
        // 2. 包含匹配：0.5 分
        else if (question.contains(keywordLower)) {
            score += 0.5;
        }
        
        // 3. 答案匹配：0.3 分
        if (answer.contains(keywordLower)) {
            score += 0.3;
        }
        
        // 4. 命中次数加权（正规化到 0-0.2）
        int hitCount = kb.getHitCount() != null ? kb.getHitCount() : 0;
        score += Math.min(hitCount / 100.0, 0.2);
        
        return Math.min(score, 1.0); // 最高 1.0 分
    }
    
    @Override
    public TokenStatsDTO getTokenStats(Integer days) {
        if (days == null || days <= 0) {
            days = 30; // 默认 30 天
        }
        
        log.info("开始统计 Token 使用情况，统计天数: {}", days);
        
        TokenStatsDTO statsDTO = new TokenStatsDTO();
        
        // 1. 统计总 Token 使用量
        statsDTO.setTotalStats(calculateTotalStats());
        
        // 2. 统计每日 Token 趋势
        statsDTO.setDailyTrend(calculateDailyTrend(days));
        
        // 3. 统计用户 Token 排行
        statsDTO.setUserRanking(calculateUserRanking(10)); // Top 10
        
        // 4. 统计角色 Token 使用情况
        statsDTO.setRoleStats(calculateRoleStats());
        
        return statsDTO;
    }
    
    /**
     * 计算总 Token 统计
     */
    private TokenStatsDTO.TotalTokenStats calculateTotalStats() {
        // 从 token_usage_stats 表查询所有统计记录
        List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
        
        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        long totalChats = allStats.size();
        
        for (TokenUsageStats stats : allStats) {
            totalInputTokens += (stats.getInputTokens() != null ? stats.getInputTokens() : 0);
            totalOutputTokens += (stats.getOutputTokens() != null ? stats.getOutputTokens() : 0);
        }
        
        long totalTokens = totalInputTokens + totalOutputTokens;
        double avgTokensPerChat = totalChats > 0 ? (double) totalTokens / totalChats : 0.0;
        
        // 统计今日和昨日 Token
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        long todayTokens = tokenUsageStatsMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TokenUsageStats>()
                .eq(TokenUsageStats::getStatDate, today)
        ).stream()
            .mapToLong(stats -> (stats.getTotalTokens() != null ? stats.getTotalTokens() : 0))
            .sum();
        
        long yesterdayTokens = tokenUsageStatsMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TokenUsageStats>()
                .eq(TokenUsageStats::getStatDate, yesterday)
        ).stream()
            .mapToLong(stats -> (stats.getTotalTokens() != null ? stats.getTotalTokens() : 0))
            .sum();
        
        // 计算增长率
        double growthRate = 0.0;
        if (yesterdayTokens > 0) {
            growthRate = ((double) (todayTokens - yesterdayTokens) / yesterdayTokens) * 100;
        }
        
        TokenStatsDTO.TotalTokenStats totalStats = new TokenStatsDTO.TotalTokenStats();
        totalStats.setTotalInputTokens(totalInputTokens);
        totalStats.setTotalOutputTokens(totalOutputTokens);
        totalStats.setTotalTokens(totalTokens);
        totalStats.setTotalChats(totalChats);
        totalStats.setAvgTokensPerChat(avgTokensPerChat);
        totalStats.setTodayTokens(todayTokens);
        totalStats.setYesterdayTokens(yesterdayTokens);
        totalStats.setGrowthRate(growthRate);
        
        return totalStats;
    }
    
    /**
     * 计算每日 Token 趋势
     */
    private List<TokenStatsDTO.DailyTokenStats> calculateDailyTrend(int days) {
        List<TokenStatsDTO.DailyTokenStats> dailyTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate endDate = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate currentDate = endDate.minusDays(i);
            
            // 从 token_usage_stats 表查询当日统计
            List<TokenUsageStats> dayStats = tokenUsageStatsMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TokenUsageStats>()
                    .eq(TokenUsageStats::getStatDate, currentDate)
            );
            
            long dayTokens = dayStats.stream()
                .mapToLong(stats -> (stats.getTotalTokens() != null ? stats.getTotalTokens() : 0))
                .sum();
            
            long dayChats = dayStats.size();
            double dayAvgTokens = dayChats > 0 ? (double) dayTokens / dayChats : 0.0;
            
            TokenStatsDTO.DailyTokenStats dailyStats = new TokenStatsDTO.DailyTokenStats();
            dailyStats.setDate(currentDate.format(formatter));
            dailyStats.setTokens(dayTokens);
            dailyStats.setChats(dayChats);
            dailyStats.setAvgTokens(dayAvgTokens);
            
            dailyTrend.add(dailyStats);
        }
        
        return dailyTrend;
    }
    
    /**
     * 计算用户 Token 排行
     */
    private List<TokenStatsDTO.UserTokenStats> calculateUserRanking(int topN) {
        // 从 token_usage_stats 表查询所有统计记录
        List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
        
        // 按 user_id 分组统计
        Map<Long, List<TokenUsageStats>> userGroups = allStats.stream()
            .filter(stats -> stats.getUserId() != null) // 过滤匿名用户
            .collect(Collectors.groupingBy(TokenUsageStats::getUserId));
        
        List<TokenStatsDTO.UserTokenStats> userStatsList = new ArrayList<>();
        
        for (Map.Entry<Long, List<TokenUsageStats>> entry : userGroups.entrySet()) {
            Long userId = entry.getKey();
            List<TokenUsageStats> stats = entry.getValue();
            
            // 计算用户总 Token 和对话次数
            long userTokens = stats.stream()
                .mapToLong(s -> (s.getTotalTokens() != null ? s.getTotalTokens() : 0))
                .sum();
            
            long userChats = stats.size();
            
            // 查询用户信息
            User user = userMapper.selectById(userId);
            
            TokenStatsDTO.UserTokenStats userStats = new TokenStatsDTO.UserTokenStats();
            userStats.setUserId(userId);
            
            if (user != null) {
                userStats.setUsername(user.getUsername());
                userStats.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            } else {
                userStats.setUsername("User" + userId);
                userStats.setNickname("User" + userId);
            }
            
            userStats.setTokens(userTokens);
            userStats.setChats(userChats);
            
            userStatsList.add(userStats);
        }
        
        // 按 Token 数量降序排序，取 Top N
        List<TokenStatsDTO.UserTokenStats> topUsers = userStatsList.stream()
            .sorted(Comparator.comparingLong(TokenStatsDTO.UserTokenStats::getTokens).reversed())
            .limit(topN)
            .collect(Collectors.toList());
        
        // 设置排名
        for (int i = 0; i < topUsers.size(); i++) {
            topUsers.get(i).setRank(i + 1);
        }
        
        return topUsers;
    }
    
    /**
     * 计算角色 Token 统计
     */
    private List<TokenStatsDTO.RoleTokenStats> calculateRoleStats() {
        // 从 token_usage_stats 表查询所有统计记录
        List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
        
        // 按 user_role 分组统计
        Map<String, List<TokenUsageStats>> roleGroups = allStats.stream()
            .filter(stats -> stats.getUserRole() != null) // 过滤无角色的记录
            .collect(Collectors.groupingBy(TokenUsageStats::getUserRole));
        
        List<TokenStatsDTO.RoleTokenStats> roleStatsList = new ArrayList<>();
        
        // 角色中文名称映射
        Map<String, String> roleNameMap = new HashMap<>();
        roleNameMap.put("reader", "读者");
        roleNameMap.put("admin", "管理员");
        roleNameMap.put("author", "作者");
        
        for (Map.Entry<String, List<TokenUsageStats>> entry : roleGroups.entrySet()) {
            String role = entry.getKey();
            List<TokenUsageStats> stats = entry.getValue();
            
            // 计算该角色总 Token 和对话次数
            long roleTokens = stats.stream()
                .mapToLong(s -> (s.getTotalTokens() != null ? s.getTotalTokens() : 0))
                .sum();
            
            long roleChats = stats.size();
            
            // 计算平均 Token
            double avgTokens = roleChats > 0 ? (double) roleTokens / roleChats : 0.0;
            
            // 统计该角色的用户数（去重）
            long userCount = stats.stream()
                .map(TokenUsageStats::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .count();
            
            TokenStatsDTO.RoleTokenStats roleStats = new TokenStatsDTO.RoleTokenStats();
            roleStats.setRole(role);
            roleStats.setRoleName(roleNameMap.getOrDefault(role, role));
            roleStats.setTokens(roleTokens);
            roleStats.setChats(roleChats);
            roleStats.setAvgTokens(avgTokens);
            roleStats.setUserCount(userCount);
            
            roleStatsList.add(roleStats);
        }
        
        // 按 Token 数量降序排序
        roleStatsList.sort(Comparator.comparingLong(TokenStatsDTO.RoleTokenStats::getTokens).reversed());
        
        return roleStatsList;
    }
}
