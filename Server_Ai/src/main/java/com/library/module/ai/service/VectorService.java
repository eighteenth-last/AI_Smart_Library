package com.library.module.ai.service;

import java.util.List;

/**
 * 向量化服务接口
 */
public interface VectorService {
    
    /**
     * 将文本转换为向量
     *
     * @param text 文本内容
     * @return 向量数组
     */
    List<Double> embedText(String text);
    
    /**
     * 批量文本向量化
     *
     * @param texts 文本列表
     * @return 向量列表
     */
    List<List<Double>> embedBatch(List<String> texts);
    
    /**
     * 计算两个向量的余弦相似度
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 相似度得分（0-1之间）
     */
    double cosineSimilarity(List<Double> vec1, List<Double> vec2);
    
    /**
     * 计算查询向量与多个候选向量的相似度，并返回最相似的K个
     *
     * @param queryVector 查询向量
     * @param candidateVectors 候选向量列表
     * @param topK 返回前K个
     * @return 索引和相似度得分的列表
     */
    List<SimilarityResult> findTopKSimilar(List<Double> queryVector, List<List<Double>> candidateVectors, int topK);
    
    /**
     * 相似度结果
     */
    class SimilarityResult {
        private int index;
        private double score;
        
        public SimilarityResult(int index, double score) {
            this.index = index;
            this.score = score;
        }
        
        public int getIndex() {
            return index;
        }
        
        public double getScore() {
            return score;
        }
    }
}
