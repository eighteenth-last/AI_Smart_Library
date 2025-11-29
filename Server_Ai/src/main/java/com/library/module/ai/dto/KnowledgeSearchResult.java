package com.library.module.ai.dto;

import com.library.module.ai.entity.KnowledgeBase;
import lombok.Data;

/**
 * 知识库检索结果DTO
 */
@Data
public class KnowledgeSearchResult {
    
    /**
     * 知识库条目
     */
    private KnowledgeBase knowledgeBase;
    
    /**
     * 相似度得分
     */
    private double similarityScore;
    
    /**
     * 检索方式：vector/fulltext/hybrid
     */
    private String searchMethod;
    
    public KnowledgeSearchResult(KnowledgeBase knowledgeBase, double similarityScore, String searchMethod) {
        this.knowledgeBase = knowledgeBase;
        this.similarityScore = similarityScore;
        this.searchMethod = searchMethod;
    }
}
