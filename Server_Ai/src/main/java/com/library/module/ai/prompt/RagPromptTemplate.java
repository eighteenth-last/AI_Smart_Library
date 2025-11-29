package com.library.module.ai.prompt;

import com.library.module.ai.dto.KnowledgeSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG Prompt 模板类
 * 用于构造检索增强生成的 Prompt
 */
@Component
public class RagPromptTemplate {
    
    /**
     * 构造基于知识库的 RAG Prompt
     * 
     * @param question 用户问题
     * @param searchResults 检索到的知识库结果
     * @return 完整的 Prompt
     */
    public String buildKnowledgeBasePrompt(String question, List<KnowledgeSearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return question;
        }
        
        // 构造知识库上下文
        StringBuilder context = new StringBuilder();
        context.append("【参考知识库】\n\n");
        
        for (int i = 0; i < searchResults.size(); i++) {
            KnowledgeSearchResult result = searchResults.get(i);
            context.append(String.format("参考资料 %d (相似度: %.2f%%):\n", 
                    i + 1, 
                    result.getSimilarityScore() * 100));
            context.append("问题: ").append(result.getKnowledgeBase().getQuestion()).append("\n");
            context.append("答案: ").append(result.getKnowledgeBase().getAnswer()).append("\n\n");
        }
        
        // 构造完整 Prompt
        return String.format(
            "%s" +
            "【用户问题】\n%s\n\n" +
            "【回答要求】\n" +
            "1. 请基于上述参考知识库的内容准确回答用户问题\n" +
            "2. 如果知识库内容与问题相关，优先使用知识库信息\n" +
            "3. 如果知识库内容不够完整，可以适当补充你的知识，但要说明\n" +
            "4. 保持回答简洁、准确、友好\n" +
            "5. 如果知识库完全无法回答问题，请诚实说明并提供你能提供的帮助\n" +
            "6. **使用数字列表格式**，如：1. 第一点  2. 第二点\n" +
            "7. **书名请用《》括起来**，如：《三体》\n" +
            "8. **重要内容用**加粗**，如：**重点信息**\n" +
            "9. **分段分类**，使用【标签】分隔不同部分\n\n" +
            "请开始回答：",
            context.toString(),
            question
        );
    }
    
    /**
     * 构造图书推荐的 RAG Prompt
     * 
     * @param question 用户问题
     * @param searchResults 检索到的知识库结果
     * @param userPreferences 用户偏好信息（可选）
     * @return 完整的 Prompt
     */
    public String buildBookRecommendPrompt(String question, 
                                          List<KnowledgeSearchResult> searchResults,
                                          String userPreferences) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加知识库上下文
        if (searchResults != null && !searchResults.isEmpty()) {
            prompt.append("【参考推荐案例】\n\n");
            for (KnowledgeSearchResult result : searchResults) {
                prompt.append("案例: ").append(result.getKnowledgeBase().getQuestion()).append("\n");
                prompt.append("推荐: ").append(result.getKnowledgeBase().getAnswer()).append("\n\n");
            }
        }
        
        // 添加用户偏好
        if (userPreferences != null && !userPreferences.isEmpty()) {
            prompt.append("【用户阅读偏好】\n").append(userPreferences).append("\n\n");
        }
        
        // 添加问题和要求
        prompt.append("【用户问题】\n").append(question).append("\n\n");
        prompt.append("【推荐要求】\n");
        prompt.append("1. 基于参考案例和用户偏好给出个性化推荐\n");
        prompt.append("2. 推荐 3-5 本图书\n");
        prompt.append("3. 说明推荐理由\n");
        prompt.append("4. 如果有具体书籍信息，请包含书名和作者\n");
        prompt.append("5. **使用数字列表格式**，如：1. 《书名》 - 作者\n");
        prompt.append("6. **书名用《》括起来**\n");
        prompt.append("7. **重点信息加粗**，如：**经典作品**\n");
        prompt.append("8. **使用【标签】分类**，如：【科幻类】【悬疑类】\n\n");
        prompt.append("请开始推荐：");
        
        return prompt.toString();
    }
    
    /**
     * 构造作者问答的 RAG Prompt
     * 
     * @param question 用户问题
     * @param searchResults 检索到的知识库结果
     * @param authorInfo 作者信息
     * @return 完整的 Prompt
     */
    public String buildAuthorQAPrompt(String question, 
                                     List<KnowledgeSearchResult> searchResults,
                                     String authorInfo) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加作者信息
        if (authorInfo != null && !authorInfo.isEmpty()) {
            prompt.append("【作者信息】\n").append(authorInfo).append("\n\n");
        }
        
        // 添加历史问答参考
        if (searchResults != null && !searchResults.isEmpty()) {
            prompt.append("【作者历史问答】\n\n");
            for (KnowledgeSearchResult result : searchResults) {
                prompt.append("Q: ").append(result.getKnowledgeBase().getQuestion()).append("\n");
                prompt.append("A: ").append(result.getKnowledgeBase().getAnswer()).append("\n\n");
            }
        }
        
        // 添加问题和要求
        prompt.append("【读者问题】\n").append(question).append("\n\n");
        prompt.append("【回答要求】\n");
        prompt.append("1. 以作者的口吻回答\n");
        prompt.append("2. 参考历史问答保持回答风格一致\n");
        prompt.append("3. 如果涉及作品创作，可以分享创作心得\n");
        prompt.append("4. 保持专业、真诚、友好的态度\n\n");
        prompt.append("请开始回答：");
        
        return prompt.toString();
    }
    
    /**
     * 构造纯 LLM 模式的 Prompt（当知识库无匹配时）
     * 
     * @param question 用户问题
     * @param context 上下文信息（可选）
     * @return Prompt
     */
    public String buildPureLLMPrompt(String question, String context) {
        if (context == null || context.isEmpty()) {
            return String.format(
                "【用户问题】\n%s\n\n" +
                "【回答要求】\n" +
                "1. 尽力回答用户问题\n" +
                "2. 如果不确定，请诚实说明\n" +
                "3. 保持回答简洁、准确、友好\n\n" +
                "请开始回答：",
                question
            );
        } else {
            return String.format(
                "【上下文信息】\n%s\n\n" +
                "【用户问题】\n%s\n\n" +
                "【回答要求】\n" +
                "1. 结合上下文信息回答\n" +
                "2. 保持回答简洁、准确、友好\n\n" +
                "请开始回答：",
                context,
                question
            );
        }
    }
    
    /**
     * 提取知识库内容摘要（用于日志记录）
     * 
     * @param searchResults 检索结果
     * @return 摘要文本
     */
    public String extractKnowledgeSummary(List<KnowledgeSearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "无匹配知识库";
        }
        
        return searchResults.stream()
            .map(r -> String.format("[ID:%d, 相似度:%.2f%%]", 
                    r.getKnowledgeBase().getKbId(),
                    r.getSimilarityScore() * 100))
            .collect(Collectors.joining(", "));
    }
}
