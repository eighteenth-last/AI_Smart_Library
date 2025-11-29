# 本地 Embedding 模型集成指南

## 📋 概述

本指南说明如何将 HuggingFace 的 Embedding 模型（moka-ai/m3e-base）转换为 ONNX 格式，并集成到 Java 项目中。

---

## 🚀 快速开始

### 步骤 1: 下载并转换模型

运行 Python 脚本下载模型并转换为 ONNX：

```bash
cd Server_Ai/scripts
python download_embedding_model.py
```

**脚本功能**:
1. 安装必要的 Python 库（transformers, torch, optimum）
2. 从 HuggingFace 下载 m3e-base 模型
3. 转换为 ONNX 格式
4. 验证生成的文件

**预期输出文件**:
```
Server_Ai/scripts/models/m3e-base-onnx/
├── model.onnx          # ONNX 模型文件（~400MB）
├── tokenizer.json      # Tokenizer 配置
└── config.json         # 模型配置
```

---

### 步骤 2: 复制文件到 Java 项目

将生成的文件复制到 Spring Boot 项目的 resources 目录：

```bash
# 创建目标目录
mkdir -p Server_Ai/src/main/resources/models/m3e-base-onnx

# 复制文件
cp scripts/models/m3e-base-onnx/*.* Server_Ai/src/main/resources/models/m3e-base-onnx/
```

**目录结构**:
```
Server_Ai/src/main/resources/
└── models/
    └── m3e-base-onnx/
        ├── model.onnx
        ├── tokenizer.json
        └── config.json
```

---

### 步骤 3: 配置 Spring Boot

配置文件已在 `application.yml` 中设置：

```yaml
embedding:
  type: onnx  # 使用本地 ONNX 模型
  model:
    path: models/m3e-base-onnx/model.onnx
  tokenizer:
    path: models/m3e-base-onnx/tokenizer.json
  max-length: 512
```

---

### 步骤 4: 启动服务

重新编译并启动 Spring Boot 应用：

```bash
cd Server_Ai
mvn clean package -DskipTests
mvn spring-boot:run
```

**启动日志应显示**:
```
初始化 ONNX Embedding 服务...
模型路径: models/m3e-base-onnx/model.onnx
Tokenizer路径: models/m3e-base-onnx/tokenizer.json
✅ ONNX Embedding 服务初始化成功
模型输入: [input_ids, attention_mask]
模型输出: [last_hidden_state]
```

---

## 🔧 技术细节

### 模型信息

| 项目 | 值 |
|------|-----|
| 模型名称 | moka-ai/m3e-base |
| 向量维度 | 768 |
| 最大序列长度 | 512 |
| 语言支持 | 中文优化 |
| 模型大小 | ~400MB |

### 依赖库

```xml
<!-- ONNX Runtime -->
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime</artifactId>
    <version>1.16.3</version>
</dependency>

<!-- HuggingFace Tokenizers -->
<dependency>
    <groupId>ai.djl.huggingface</groupId>
    <artifactId>tokenizers</artifactId>
    <version>0.25.0</version>
</dependency>
```

### 服务实现

`OnnxEmbeddingService` 类实现了 `VectorService` 接口：

- **embedText(String text)**: 将文本转换为 768 维向量
- **embedBatch(List<String> texts)**: 批量文本向量化
- **cosineSimilarity(vec1, vec2)**: 计算余弦相似度
- **findTopKSimilar(...)**: TopK 相似度搜索

### 工作流程

```
文本输入
  ↓
Tokenizer (分词)
  ↓
ONNX 推理
  ↓
Mean Pooling
  ↓
L2 归一化
  ↓
768 维向量输出
```

---

## 🧪 测试

运行向量服务测试：

```bash
mvn test -Dtest=VectorServiceTest
```

---

## ⚠️ 故障排除

### 问题 1: 模型文件未找到

**错误**: `FileNotFoundException: models/m3e-base-onnx/model.onnx`

**解决**:
1. 确认文件已复制到 `resources/models/m3e-base-onnx/`
2. 检查文件路径配置

### 问题 2: 内存不足

**错误**: `OutOfMemoryError`

**解决**:
增加 JVM 堆内存：
```bash
java -Xmx2g -jar ai-smart-library.jar
```

### 问题 3: ONNX Runtime 加载失败

**错误**: `OrtException: Failed to load native library`

**解决**:
1. 确认 ONNX Runtime 依赖版本正确
2. 检查操作系统兼容性（Windows/Linux/Mac）

---

## 📊 性能对比

| 方案 | 首次调用 | 后续调用 | 成本 | 离线支持 |
|------|----------|----------|------|----------|
| 模拟向量 | <1ms | <1ms | 免费 | ✅ |
| ONNX 本地 | ~100ms | ~50ms | 免费 | ✅ |
| OpenAI API | ~200ms | ~200ms | ¥0.02/1M tokens | ❌ |

---

## 🎯 使用示例

### Java 代码

```java
@Autowired
private VectorService vectorService;

// 单个文本向量化
List<Double> vector = vectorService.embedText("推荐科幻小说");

// 批量向量化
List<String> texts = Arrays.asList("文本1", "文本2", "文本3");
List<List<Double>> vectors = vectorService.embedBatch(texts);

// 计算相似度
double similarity = vectorService.cosineSimilarity(vector1, vector2);

// TopK 搜索
List<SimilarityResult> results = vectorService.findTopKSimilar(
    queryVector, 
    candidateVectors, 
    5
);
```

---

## 📚 参考资料

- [ONNX Runtime 官方文档](https://onnxruntime.ai/)
- [HuggingFace Tokenizers](https://huggingface.co/docs/tokenizers/)
- [m3e-base 模型](https://huggingface.co/moka-ai/m3e-base)
- [Optimum 库文档](https://huggingface.co/docs/optimum/)

---

## ✅ 验证清单

- [ ] Python 环境已安装（Python 3.8+）
- [ ] 模型已下载并转换为 ONNX
- [ ] 文件已复制到 `resources/models/m3e-base-onnx/`
- [ ] Maven 依赖已添加
- [ ] `application.yml` 已配置
- [ ] 服务启动成功，日志显示 ✅
- [ ] 单元测试通过

---

**版本**: v1.0  
**最后更新**: 2025-11-28  
**维护者**: AI Smart Library 团队
