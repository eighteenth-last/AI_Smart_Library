package com.library.module.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.module.ai.dto.ChatRequestDTO;
import com.library.module.ai.dto.KnowledgeSearchResult;
import com.library.module.ai.dto.TokenStatsDTO;
import com.library.module.ai.entity.AiChatLog;
import com.library.module.ai.entity.AiSession;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.vo.ChatResponseVO;
import com.library.module.ai.vo.SessionVO;

import java.util.List;

/**
 * AI智能客服服务接口
 */
public interface AiService extends IService<KnowledgeBase> {

    /**
     * AI对话
     *
     * @param requestDTO 对话请求
     * @return 对话响应
     */
    ChatResponseVO chat(ChatRequestDTO requestDTO);

    /**
     * 获取对话历史
     *
     * @param userId 用户ID（匿名用户为null）
     * @param sessionId 会话ID
     * @param page 页码
     * @param size 每页数量
     * @return 对话历史分页
     */
    Page<AiChatLog> getChatHistory(Long userId, String sessionId, Integer page, Integer size);

    /**
     * 添加知识库条目
     *
     * @param knowledgeBase 知识库条目
     * @return 是否成功
     */
    boolean addKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * 更新知识库条目
     *
     * @param kbId 知识库ID
     * @param knowledgeBase 更新内容
     * @return 是否成功
     */
    boolean updateKnowledgeBase(Long kbId, KnowledgeBase knowledgeBase);

    /**
     * 删除知识库条目
     *
     * @param kbId 知识库ID
     * @return 是否成功
     */
    boolean deleteKnowledgeBase(Long kbId);

    /**
     * 获取知识库列表
     *
     * @param category 分类
     * @param page 页码
     * @param size 每页数量
     * @return 知识库列表
     */
    Page<KnowledgeBase> getKnowledgeBaseList(String category, Integer page, Integer size);

    /**
     * 搜索知识库（使用向量相似度）
     *
     * @param keyword 关键词
     * @return 匹配的知识库条目
     */
    KnowledgeBase searchKnowledgeBase(String keyword);

    /**
     * 向量化搜索知识库（增强版）
     *
     * @param keyword 关键词
     * @param topK 返回前K个结果
     * @param minSimilarity 最小相似度阈值
     * @return 匹配的知识库条目列表（按相似度排序）
     */
    List<KnowledgeSearchResult> vectorSearchKnowledgeBase(String keyword, int topK, double minSimilarity);

    /**
     * 增加知识库命中次数
     *
     * @param kbId 知识库ID
     */
    void incrementHitCount(Long kbId);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SessionVO> getUserSessions(Long userId);

    /**
     * 根据会话ID获取会话信息
     *
     * @param sessionId 会诚ID
     * @return 会话信息
     */
    AiSession getSessionById(String sessionId);

    /**
     * 保存或更新会话
     *
     * @param session 会话信息
     * @return 是否成功
     */
    boolean saveOrUpdateSession(AiSession session);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteSession(String sessionId, Long userId);

    /**
     * 清空用户所有会话
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearAllSessions(Long userId);
    
    /**
     * 获取 Token 统计数据
     *
     * @param days 统计天数（默认 30 天）
     * @return Token 统计数据
     */
    TokenStatsDTO getTokenStats(Integer days);
}
