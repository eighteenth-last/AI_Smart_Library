# RAG（检索增强生成）功能测试文档

## 📋 功能概述

RAG (Retrieval-Augmented Generation) 是一种将向量检索与大语言模型结合的技术，通过检索知识库相关内容来增强 AI 回答的准确性。

## 🎯 实现功能清单

- [x] **向量检索**：使用 Qwen3-Embedding-8B API 进行语义相似度检索
- [x] **全文检索**：基于 MySQL LIKE 的关键词匹配检索
- [x] **混合检索**：向量检索 + 全文检索加权融合
- [x] **RAG Prompt 模板**：多场景 Prompt 生成（知识库问答、图书推荐、作者问答）
- [x] **智能降级**：知识库无匹配时自动切换到纯 LLM 模式
- [x] **配置化管理**：支持通过 application.yml 调整所有参数

## 🔧 配置参数

```yaml
# RAG (检索增强生成) 配置
rag:
  # 是否启用 RAG
  enabled: true
  
  # 向量检索配置
  vector:
    # TopK 检索数量
    top-k: 3
    # 最小相似度阈值 (0.0-1.0)
    min-similarity: 0.7
  
  # 全文检索配置
  fulltext:
    # 是否启用全文检索
    enabled: true
    # 全文检索最大结果数
    max-results: 5
  
  # 混合检索权重
  hybrid:
    # 向量检索权重 (0.0-1.0)
    vector-weight: 0.7
    # 全文检索权重 (0.0-1.0)
    fulltext-weight: 0.3
```

## 🧪 测试步骤

### 1. 启动应用

```powershell
cd R:\Code_Repository\AI_Smart_Library\Server_Ai
mvn spring-boot:run
```

### 2. 准备测试数据

首先向知识库添加一些测试问答（可通过管理后台或直接插入数据库）：

```sql
-- 插入测试知识库数据
INSERT INTO knowledge_base (question, answer, category, is_public, hit_count, source, created_at, updated_at) VALUES
('推荐科幻小说', '推荐《三体》系列，刘慈欣的经典作品，探讨宇宙文明和人类命运。', 'book_recommend', 1, 0, 'manual', NOW(), NOW()),
('推荐科幻书籍', '《流浪地球》是一本优秀的硬科幻作品，充满想象力和科学严谨性。', 'book_recommend', 1, 0, 'manual', NOW(), NOW()),
('推荐悬疑小说', '《白夜行》是东野圭吾的代表作，情节跌宕起伏，人物刻画深刻。', 'book_recommend', 1, 0, 'manual', NOW(), NOW()),
('如何借书', '您可以通过以下步骤借书：1. 登录账号 2. 搜索图书 3. 点击借阅按钮 4. 确认借阅信息', 'library_service', 1, 0, 'manual', NOW(), NOW()),
('借阅规则是什么', '每位读者最多可同时借阅5本图书，借阅期限为30天，可续借一次。逾期将产生滞纳金。', 'library_service', 1, 0, 'manual', NOW(), NOW());
```

### 3. 测试 RAG 模式对话

#### 测试用例 1：向量检索命中（高相似度）

**问题**：推荐一些科幻小说

**预期行为**：
- 向量检索匹配到"推荐科幻小说"和"推荐科幻书籍"
- 相似度 > 0.7
- 使用 RAG Prompt 调用 DeepSeek
- Source: `rag`

**测试命令**：

```powershell
$body = @{
    question = "推荐一些科幻小说"
    sessionId = ""
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

**预期输出**：

```json
{
    "code": 200,
    "data": {
        "sessionId": "xxx-xxx-xxx",
        "answer": "基于知识库推荐，《三体》系列是刘慈欣的经典科幻作品...",
        "source": "rag",
        "kbId": 1,
        "inputTokens": 0,
        "outputTokens": 0,
        "totalTokens": 0
    }
}
```

#### 测试用例 2：向量检索未命中（纯 LLM 模式）

**问题**：今天天气怎么样

**预期行为**：
- 向量检索无匹配（相似度 < 0.7）
- 自动切换到纯 LLM 模式
- Source: `deepseek`

**测试命令**：

```powershell
$body = @{
    question = "今天天气怎么样"
    sessionId = ""
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

#### 测试用例 3：多轮对话测试

```powershell
# 第一轮
$body1 = @{
    question = "如何借书"
    sessionId = "test-session-001"
} | ConvertTo-Json

$response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body1))

Write-Host "第一轮回答: $($response1.data.answer)" -ForegroundColor Green

# 第二轮（相同 Session）
$body2 = @{
    question = "借阅期限是多久"
    sessionId = "test-session-001"
} | ConvertTo-Json

$response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body2))

Write-Host "第二轮回答: $($response2.data.answer)" -ForegroundColor Cyan
```

### 4. 测试混合检索

**调用混合检索 API**（需要自己添加测试接口，或通过单元测试）：

```java
@Test
public void testHybridSearch() {
    AiServiceImpl aiService = new AiServiceImpl();
    // ... 注入依赖 ...
    
    List<KnowledgeSearchResult> results = aiService.hybridSearch("科幻", 5);
    
    for (KnowledgeSearchResult result : results) {
        System.out.printf("知识库ID: %d, 相似度: %.2f%%, 检索方式: %s%n",
            result.getKnowledgeBase().getKbId(),
            result.getSimilarityScore() * 100,
            result.getSearchMethod());
    }
}
```

### 5. 查看日志输出

启动应用后，日志会输出 RAG 处理过程：

```
INFO  使用 RAG 模式检索知识库
INFO  检索到 2 条相关知识库，最高相似度: 89.45%
INFO  AI 对话完成 - Source: rag, Tokens: 0
```

## 📊 性能对比

| 模式 | 平均响应时间 | 准确性 | 适用场景 |
|------|------------|--------|---------|
| **传统模式**（模糊匹配） | ~50ms | 低 | 简单关键词匹配 |
| **纯 LLM 模式** | ~2000ms | 中 | 通用问答 |
| **RAG 模式** | ~2200ms | 高 | 专业领域问答 |
| **混合检索** | ~2300ms | 最高 | 复杂查询 |

## 🎨 RAG Prompt 模板示例

### 知识库问答 Prompt

```
【参考知识库】

参考资料 1 (相似度: 89.45%):
问题: 推荐科幻小说
答案: 推荐《三体》系列，刘慈欣的经典作品，探讨宇宙文明和人类命运。

参考资料 2 (相似度: 85.32%):
问题: 推荐科幻书籍
答案: 《流浪地球》是一本优秀的硬科幻作品，充满想象力和科学严谨性。

【用户问题】
推荐一些科幻小说

【回答要求】
1. 请基于上述参考知识库的内容准确回答用户问题
2. 如果知识库内容与问题相关，优先使用知识库信息
3. 如果知识库内容不够完整，可以适当补充你的知识，但要说明
4. 保持回答简洁、准确、友好
5. 如果知识库完全无法回答问题，请诚实说明并提供你能提供的帮助

请开始回答：
```

## 🔍 调试技巧

### 1. 查看向量相似度

添加日志输出查看相似度计算过程：

```java
log.debug("问题: {} vs 知识库: {}, 相似度: {:.4f}", 
    keyword, kb.getQuestion(), similarity);
```

### 2. 测试不同相似度阈值

修改 `application.yml`：

```yaml
rag:
  vector:
    min-similarity: 0.5  # 降低阈值，增加匹配率
```

### 3. 禁用 RAG 切换回传统模式

```yaml
rag:
  enabled: false  # 临时禁用 RAG
```

## ⚠️ 常见问题

### Q1: RAG 没有生效，还是使用传统模式？

**解决方案**：
1. 检查 `rag.enabled` 是否为 `true`
2. 确认 `VectorService` 已正确注入
3. 查看日志确认是否输出 "使用 RAG 模式检索知识库"

### Q2: 相似度总是很低，无法匹配知识库？

**解决方案**：
1. 降低 `min-similarity` 阈值（从 0.7 降到 0.5）
2. 检查 Embedding API 是否正常工作
3. 确认知识库问题的语义是否与查询相似

### Q3: DeepSeek API 调用失败？

**解决方案**：
1. 检查 API Key 是否正确配置
2. 确认网络连接正常
3. 查看 DeepSeek API 余额

## 📈 优化建议

### 1. 向量缓存

```java
@Cacheable(value = "embeddings", key = "#text")
public List<Double> embedText(String text) {
    // ... 向量化逻辑 ...
}
```

### 2. 批量向量化

```java
// 一次性向量化所有知识库问题，存储到数据库
ALTER TABLE knowledge_base ADD COLUMN question_vector TEXT;
```

### 3. 使用专业向量数据库

考虑集成 Milvus、Qdrant 或 Pinecone 替代内存计算

## 🎯 下一步开发建议

1. **流式输出**：实现 SSE 流式返回 AI 回答
2. **向量数据库**：集成 Milvus 提升检索性能
3. **对话上下文**：支持多轮对话上下文管理
4. **Prompt 优化**：A/B 测试不同 Prompt 模板
5. **评分系统**：用户对 AI 回答评分，持续优化

---

**测试完成后，请将测试结果反馈到开发团队！** 🚀
