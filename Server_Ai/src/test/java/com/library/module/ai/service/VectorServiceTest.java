package com.library.module.ai.service;

import com.library.module.ai.service.impl.ApiEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量服务测试类
 */
class VectorServiceTest {

    private VectorService vectorService;

    @BeforeEach
    void setUp() {
        vectorService = new ApiEmbeddingService();
    }

    /**
     * 测试余弦相似度计算 - 完全相同的向量
     */
    @Test
    void testCosineSimilarity_IdenticalVectors() {
        List<Double> vec1 = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        List<Double> vec2 = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        // 完全相同的向量，相似度应该接近1.0
        assertEquals(1.0, similarity, 0.0001, "相同向量的余弦相似度应该为1.0");
    }

    /**
     * 测试余弦相似度计算 - 正交向量
     */
    @Test
    void testCosineSimilarity_OrthogonalVectors() {
        List<Double> vec1 = Arrays.asList(1.0, 0.0, 0.0);
        List<Double> vec2 = Arrays.asList(0.0, 1.0, 0.0);
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        // 正交向量，相似度应该为0
        assertEquals(0.0, similarity, 0.0001, "正交向量的余弦相似度应该为0.0");
    }

    /**
     * 测试余弦相似度计算 - 相反方向的向量
     */
    @Test
    void testCosineSimilarity_OppositeVectors() {
        List<Double> vec1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> vec2 = Arrays.asList(-1.0, -2.0, -3.0);
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        // 相反方向的向量，相似度应该为-1
        assertEquals(-1.0, similarity, 0.0001, "相反向量的余弦相似度应该为-1.0");
    }

    /**
     * 测试余弦相似度计算 - 相似向量
     */
    @Test
    void testCosineSimilarity_SimilarVectors() {
        List<Double> vec1 = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        List<Double> vec2 = Arrays.asList(1.1, 2.1, 2.9, 4.0);
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        // 相似向量，相似度应该接近1.0
        assertTrue(similarity > 0.99, "相似向量的余弦相似度应该接近1.0，实际值: " + similarity);
    }

    /**
     * 测试余弦相似度计算 - 不同长度的向量应该抛出异常
     */
    @Test
    void testCosineSimilarity_DifferentLengths() {
        List<Double> vec1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> vec2 = Arrays.asList(1.0, 2.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            vectorService.cosineSimilarity(vec1, vec2);
        }, "不同长度的向量应该抛出异常");
    }

    /**
     * 测试余弦相似度计算 - 空向量
     */
    @Test
    void testCosineSimilarity_NullVectors() {
        List<Double> vec1 = null;
        List<Double> vec2 = Arrays.asList(1.0, 2.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            vectorService.cosineSimilarity(vec1, vec2);
        }, "空向量应该抛出异常");
    }

    /**
     * 测试余弦相似度计算 - 零向量
     */
    @Test
    void testCosineSimilarity_ZeroVector() {
        List<Double> vec1 = Arrays.asList(0.0, 0.0, 0.0);
        List<Double> vec2 = Arrays.asList(1.0, 2.0, 3.0);
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        // 零向量，相似度应该为0
        assertEquals(0.0, similarity, 0.0001, "零向量的余弦相似度应该为0.0");
    }

    /**
     * 测试文本向量化
     */
    @Test
    void testEmbedText() {
        String text = "这是一个测试文本";
        
        List<Double> vector = vectorService.embedText(text);
        
        assertNotNull(vector, "向量不应该为空");
        assertTrue(vector.size() > 0, "向量维度应该大于0");
        
        // 测试相同文本生成相同向量
        List<Double> vector2 = vectorService.embedText(text);
        assertEquals(vector, vector2, "相同文本应该生成相同向量");
    }

    /**
     * 测试文本向量化 - 不同文本生成不同向量
     */
    @Test
    void testEmbedText_DifferentTexts() {
        String text1 = "推荐科幻小说";
        String text2 = "推荐爱情小说";
        
        List<Double> vector1 = vectorService.embedText(text1);
        List<Double> vector2 = vectorService.embedText(text2);
        
        assertNotEquals(vector1, vector2, "不同文本应该生成不同向量");
        
        // 计算相似度（应该小于1.0）
        double similarity = vectorService.cosineSimilarity(vector1, vector2);
        assertTrue(similarity < 1.0, "不同文本的相似度应该小于1.0");
    }

    /**
     * 测试批量向量化
     */
    @Test
    void testEmbedBatch() {
        List<String> texts = Arrays.asList(
            "推荐科幻小说",
            "推荐爱情小说",
            "推荐历史小说"
        );
        
        List<List<Double>> vectors = vectorService.embedBatch(texts);
        
        assertEquals(3, vectors.size(), "应该返回3个向量");
        
        // 验证每个向量维度相同
        int dimension = vectors.get(0).size();
        for (List<Double> vector : vectors) {
            assertEquals(dimension, vector.size(), "所有向量维度应该相同");
        }
    }

    /**
     * 测试 TopK 相似度搜索
     */
    @Test
    void testFindTopKSimilar() {
        // 查询向量
        List<Double> queryVector = Arrays.asList(1.0, 0.0, 0.0);
        
        // 候选向量
        List<List<Double>> candidateVectors = Arrays.asList(
            Arrays.asList(1.0, 0.0, 0.0),  // 相似度 = 1.0
            Arrays.asList(0.9, 0.1, 0.0),  // 相似度 ≈ 0.99
            Arrays.asList(0.0, 1.0, 0.0),  // 相似度 = 0.0
            Arrays.asList(0.5, 0.5, 0.0),  // 相似度 ≈ 0.71
            Arrays.asList(-1.0, 0.0, 0.0)  // 相似度 = -1.0
        );
        
        // 获取前3个最相似的
        List<VectorService.SimilarityResult> results = 
            vectorService.findTopKSimilar(queryVector, candidateVectors, 3);
        
        assertEquals(3, results.size(), "应该返回3个结果");
        
        // 验证结果按相似度降序排列
        assertTrue(results.get(0).getScore() >= results.get(1).getScore(), 
                  "结果应该按相似度降序排列");
        assertTrue(results.get(1).getScore() >= results.get(2).getScore(), 
                  "结果应该按相似度降序排列");
        
        // 验证最高相似度接近1.0（第一个向量）
        assertEquals(0, results.get(0).getIndex(), "最高相似度应该是第0个向量");
        assertEquals(1.0, results.get(0).getScore(), 0.0001, "最高相似度应该接近1.0");
    }

    /**
     * 测试 TopK 相似度搜索 - 空候选列表
     */
    @Test
    void testFindTopKSimilar_EmptyCandidates() {
        List<Double> queryVector = Arrays.asList(1.0, 0.0, 0.0);
        List<List<Double>> candidateVectors = Arrays.asList();
        
        List<VectorService.SimilarityResult> results = 
            vectorService.findTopKSimilar(queryVector, candidateVectors, 3);
        
        assertTrue(results.isEmpty(), "空候选列表应该返回空结果");
    }

    /**
     * 测试向量归一化（间接测试）
     */
    @Test
    void testVectorNormalization() {
        String text = "测试文本";
        List<Double> vector = vectorService.embedText(text);
        
        // 计算向量的L2范数
        double norm = Math.sqrt(vector.stream()
                .mapToDouble(v -> v * v)
                .sum());
        
        // 归一化后的向量，L2范数应该为1.0
        assertEquals(1.0, norm, 0.0001, "归一化向量的L2范数应该为1.0");
    }

    /**
     * 性能测试 - 大规模向量相似度计算
     */
    @Test
    void testPerformance_LargeScale() {
        // 生成100个文本
        List<String> texts = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            texts.add("测试文本" + i);
        }
        
        long startTime = System.currentTimeMillis();
        
        // 批量向量化
        List<List<Double>> vectors = vectorService.embedBatch(texts);
        
        // 查询向量
        List<Double> queryVector = vectorService.embedText("查询文本");
        
        // TopK搜索
        List<VectorService.SimilarityResult> results = 
            vectorService.findTopKSimilar(queryVector, vectors, 10);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("处理100个文本的TopK搜索耗时: " + duration + "ms");
        
        assertEquals(10, results.size(), "应该返回10个结果");
        assertTrue(duration < 5000, "处理时间应该在5秒内，实际耗时: " + duration + "ms");
    }
}
