package com.library.module.ai.controller;

import com.library.common.result.Result;
import com.library.module.ai.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Embedding 测试控制器
 */
@Tag(name = "Embedding 测试接口")
@RestController
@RequestMapping("/test/embedding")
public class EmbeddingTestController {

    @Autowired
    private VectorService vectorService;

    @Operation(summary = "测试文本向量化")
    @PostMapping("/embed")
    public Result<EmbedResponse> embedText(@RequestBody EmbedRequest request) {
        List<Double> vector = vectorService.embedText(request.getText());
        
        EmbedResponse response = new EmbedResponse();
        response.setText(request.getText());
        response.setDimension(vector.size());
        response.setVector(vector.subList(0, Math.min(10, vector.size()))); // 只返回前10个元素
        response.setFullVectorSize(vector.size());
        
        return Result.success(response);
    }

    @Operation(summary = "测试相似度计算")
    @PostMapping("/similarity")
    public Result<SimilarityResponse> calculateSimilarity(@RequestBody SimilarityRequest request) {
        List<Double> vec1 = vectorService.embedText(request.getText1());
        List<Double> vec2 = vectorService.embedText(request.getText2());
        
        double similarity = vectorService.cosineSimilarity(vec1, vec2);
        
        SimilarityResponse response = new SimilarityResponse();
        response.setText1(request.getText1());
        response.setText2(request.getText2());
        response.setSimilarity(similarity);
        response.setPercentage(String.format("%.2f%%", similarity * 100));
        
        return Result.success(response);
    }

    @Operation(summary = "测试批量向量化")
    @PostMapping("/batch")
    public Result<BatchEmbedResponse> embedBatch(@RequestBody BatchEmbedRequest request) {
        List<List<Double>> vectors = vectorService.embedBatch(request.getTexts());
        
        BatchEmbedResponse response = new BatchEmbedResponse();
        response.setTextsCount(request.getTexts().size());
        response.setDimension(vectors.isEmpty() ? 0 : vectors.get(0).size());
        response.setProcessed(vectors.size());
        
        return Result.success(response);
    }

    @Data
    public static class EmbedRequest {
        private String text;
    }

    @Data
    public static class EmbedResponse {
        private String text;
        private Integer dimension;
        private List<Double> vector; // 前10个元素
        private Integer fullVectorSize;
    }

    @Data
    public static class SimilarityRequest {
        private String text1;
        private String text2;
    }

    @Data
    public static class SimilarityResponse {
        private String text1;
        private String text2;
        private Double similarity;
        private String percentage;
    }

    @Data
    public static class BatchEmbedRequest {
        private List<String> texts;
    }

    @Data
    public static class BatchEmbedResponse {
        private Integer textsCount;
        private Integer dimension;
        private Integer processed;
    }
}
