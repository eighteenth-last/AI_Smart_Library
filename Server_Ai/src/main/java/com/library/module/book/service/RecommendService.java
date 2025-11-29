package com.library.module.book.service;

import com.library.module.book.vo.BookRecommendVO;

import java.util.List;
import java.util.Set;

/**
 * 推荐算法服务接口
 */
public interface RecommendService {

    /**
     * 基于Jaccard相似度计算相似图书
     * J(A, B) = |A ∩ B| / |A ∪ B|
     */
    List<BookRecommendVO> getSimilarBooksByJaccard(Long bookId, Integer limit);

    /**
     * 计算两个标签集合的Jaccard相似度
     */
    double calculateJaccardSimilarity(Set<Long> tags1, Set<Long> tags2);

    /**
     * 获取图书的标签集合
     */
    Set<Long> getBookTagIds(Long bookId);

    /**
     * 获取推荐理由
     */
    String generateRecommendationReason(Set<Long> sharedTags);
}