package com.library.module.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.library.module.ai.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 基于 API 的 Embedding 服务
 * 使用 Qwen3-Embedding-8B 模型
 */
@Slf4j
@Service
public class ApiEmbeddingService implements VectorService {
    
    @Value("${embedding.api.base-url}")
    private String baseUrl;
    
    @Value("${embedding.api.api-key}")
    private String apiKey;
    
    @Value("${embedding.api.model}")
    private String model;
    
    @Value("${embedding.api.encoding-format:float}")
    private String encodingFormat;
    
    @Value("${embedding.api.timeout:30000}")
    private long timeout;
    
    private final HttpClient httpClient;
    
    public ApiEmbeddingService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
    
    @Override
    public List<Double> embedText(String text) {
        try {
            log.debug("调用 Embedding API，文本长度: {}", text.length());
            
            // 构造请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("input", text);
            requestBody.put("encoding_format", encodingFormat);
            
            // 构造 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(timeout))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                    .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 检查响应状态
            if (response.statusCode() != 200) {
                log.error("Embedding API 请求失败，状态码: {}, 响应: {}", 
                         response.statusCode(), response.body());
                return generateFallbackVector(text);
            }
            
            // 解析响应
            JSONObject responseJson = JSON.parseObject(response.body());
            JSONArray data = responseJson.getJSONArray("data");
            
            if (data == null || data.isEmpty()) {
                log.error("Embedding API 返回数据为空");
                return generateFallbackVector(text);
            }
            
            // 获取第一个 embedding 向量
            JSONObject firstEmbedding = data.getJSONObject(0);
            JSONArray embeddingArray = firstEmbedding.getJSONArray("embedding");
            
            // 转换为 List<Double>
            List<Double> vector = new ArrayList<>(embeddingArray.size());
            for (int i = 0; i < embeddingArray.size(); i++) {
                vector.add(embeddingArray.getDoubleValue(i));
            }
            
            log.debug("✅ 成功获取向量，维度: {}", vector.size());
            return vector;
            
        } catch (IOException | InterruptedException e) {
            log.error("Embedding API 调用异常", e);
            return generateFallbackVector(text);
        }
    }
    
    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            log.debug("批量调用 Embedding API，文本数量: {}", texts.size());
            
            // 构造请求体（批量）
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("input", texts);
            requestBody.put("encoding_format", encodingFormat);
            
            // 构造 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(timeout))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                    .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 检查响应状态
            if (response.statusCode() != 200) {
                log.error("批量 Embedding API 请求失败，状态码: {}, 响应: {}", 
                         response.statusCode(), response.body());
                return texts.stream()
                        .map(this::generateFallbackVector)
                        .collect(Collectors.toList());
            }
            
            // 解析响应
            JSONObject responseJson = JSON.parseObject(response.body());
            JSONArray data = responseJson.getJSONArray("data");
            
            if (data == null || data.isEmpty()) {
                log.error("批量 Embedding API 返回数据为空");
                return texts.stream()
                        .map(this::generateFallbackVector)
                        .collect(Collectors.toList());
            }
            
            // 转换所有向量
            List<List<Double>> vectors = new ArrayList<>(data.size());
            for (int i = 0; i < data.size(); i++) {
                JSONObject embeddingObj = data.getJSONObject(i);
                JSONArray embeddingArray = embeddingObj.getJSONArray("embedding");
                
                List<Double> vector = new ArrayList<>(embeddingArray.size());
                for (int j = 0; j < embeddingArray.size(); j++) {
                    vector.add(embeddingArray.getDoubleValue(j));
                }
                vectors.add(vector);
            }
            
            log.debug("✅ 成功获取批量向量，数量: {}", vectors.size());
            return vectors;
            
        } catch (IOException | InterruptedException e) {
            log.error("批量 Embedding API 调用异常", e);
            return texts.stream()
                    .map(this::generateFallbackVector)
                    .collect(Collectors.toList());
        }
    }
    
    @Override
    public double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量不能为空且维度必须相同");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            double v1 = vec1.get(i);
            double v2 = vec2.get(i);
            
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    @Override
    public List<SimilarityResult> findTopKSimilar(List<Double> queryVector,
                                                   List<List<Double>> candidateVectors,
                                                   int topK) {
        if (queryVector == null || candidateVectors == null || candidateVectors.isEmpty()) {
            return new ArrayList<>();
        }
        
        return IntStream.range(0, candidateVectors.size())
                .mapToObj(i -> {
                    double similarity = cosineSimilarity(queryVector, candidateVectors.get(i));
                    return new SimilarityResult(i, similarity);
                })
                .sorted(Comparator.comparingDouble(SimilarityResult::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }
    
    /**
     * 生成降级向量（API 调用失败时）
     */
    private List<Double> generateFallbackVector(String text) {
        log.warn("使用降级向量（模拟），生产环境请检查 API 配置");
        
        int dimension = 1024; // Qwen3-Embedding-8B 的维度
        List<Double> vector = new ArrayList<>(dimension);
        int hash = text.hashCode();
        
        for (int i = 0; i < dimension; i++) {
            double value = Math.sin(hash + i) * Math.cos(hash * i);
            vector.add(value);
        }
        
        return normalizeVector(vector);
    }
    
    /**
     * 向量归一化
     */
    private List<Double> normalizeVector(List<Double> vector) {
        double norm = Math.sqrt(vector.stream()
                .mapToDouble(v -> v * v)
                .sum());
        
        if (norm == 0.0) {
            return vector;
        }
        
        return vector.stream()
                .map(v -> v / norm)
                .collect(Collectors.toList());
    }
}
