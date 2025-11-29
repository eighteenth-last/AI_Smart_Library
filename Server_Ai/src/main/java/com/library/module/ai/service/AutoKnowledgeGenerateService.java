package com.library.module.ai.service;

import java.util.List;

/**
 * 知识库自动生成服务接口
 */
public interface AutoKnowledgeGenerateService {
    
    /**
     * 从单本图书生成知识库
     * 
     * @param bookId 图书ID
     * @return 生成的知识库条目数量
     */
    int generateKnowledgeFromBook(Long bookId);
    
    /**
     * 批量从图书生成知识库
     * 
     * @param bookIds 图书ID列表
     * @return 生成的知识库条目总数
     */
    int batchGenerateKnowledge(List<Long> bookIds);
    
    /**
     * 从所有上架图书生成知识库
     * 
     * @return 生成的知识库条目总数
     */
    int generateKnowledgeFromAllBooks();
    
    /**
     * 更新图书相关的知识库
     * 
     * @param bookId 图书ID
     * @return 更新的知识库条目数量
     */
    int updateBookKnowledge(Long bookId);
    
    /**
     * 删除图书相关的知识库
     * 
     * @param bookId 图书ID
     * @return 删除的知识库条目数量
     */
    int deleteBookKnowledge(Long bookId);
}
