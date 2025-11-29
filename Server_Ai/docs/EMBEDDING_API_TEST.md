# 测试 Embedding API

## 测试 1: 测试文本向量化

```bash
curl -X POST http://localhost:8080/api/test/embedding/embed \
  -H "Content-Type: application/json" \
  -d '{
    "text": "推荐科幻小说"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "text": "推荐科幻小说",
    "dimension": 1024,
    "vector": [0.123, -0.456, ...],
    "fullVectorSize": 1024
  }
}
```

---

## 测试 2: 测试相似度计算

```bash
curl -X POST http://localhost:8080/api/test/embedding/similarity \
  -H "Content-Type: application/json" \
  -d '{
    "text1": "推荐科幻小说",
    "text2": "推荐科幻书籍"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "text1": "推荐科幻小说",
    "text2": "推荐科幻书籍",
    "similarity": 0.9523,
    "percentage": "95.23%"
  }
}
```

---

## 测试 3: 测试批量向量化

```bash
curl -X POST http://localhost:8080/api/test/embedding/batch \
  -H "Content-Type: application/json" \
  -d '{
    "texts": ["推荐科幻小说", "推荐爱情小说", "推荐历史书籍"]
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "textsCount": 3,
    "dimension": 1024,
    "processed": 3
  }
}
```

---

## 测试 4: 直接调用 Embedding API（验证配置）

```bash
curl --request POST \
  --url https://api.ppinfra.com/openai/v1/embeddings \
  --header 'Authorization: Bearer sk_FjDpRv6r8U0hmfuDNBGHhthr2DP_p4bzvWK85bcSaXg' \
  --header 'Content-Type: application/json' \
  --data '{
    "model": "qwen/qwen3-embedding-8b",
    "input": "测试文本",
    "encoding_format": "float"
  }'
```

---

## PowerShell 测试命令

### 测试 1 (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/test/embedding/embed" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"text": "推荐科幻小说"}'
```

### 测试 2 (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/test/embedding/similarity" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"text1": "推荐科幻小说", "text2": "推荐科幻书籍"}'
```

### 测试 3 (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/test/embedding/batch" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"texts": ["推荐科幻小说", "推荐爱情小说", "推荐历史书籍"]}'
```

---

## 访问 Swagger UI

浏览器打开: http://localhost:8080/api/doc.html

在 "Embedding 测试接口" 分组中测试以上接口。

---

## 预期结果

1. ✅ 向量维度应该是 **1024**（Qwen3-Embedding-8B）
2. ✅ 相似文本的相似度应该 > **0.85**
3. ✅ 不同类型文本的相似度应该 < **0.5**
4. ✅ 批量处理应该成功返回所有向量

---

**创建时间**: 2025-11-29  
**模型**: qwen/qwen3-embedding-8b  
**API**: https://api.ppinfra.com/openai/v1
