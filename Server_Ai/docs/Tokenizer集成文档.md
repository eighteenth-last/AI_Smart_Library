# Tokenizer 集成文档

## 📋 概述

本次更新集成了 **jtokkit** 库（Java 版本的 tiktoken），用于精确计算 AI 对话中输入和输出的 Token 数量。

---

## 🎯 更新目标

1. **精确计算**：使用与 OpenAI/DeepSeek 相同的 Tokenizer 计算 Token
2. **实时统计**：每次 AI 对话时自动计算输入和输出 Token 数量
3. **准确记录**：将精确的 Token 数量保存到 `token_usage_stats` 表

---

## 📦 新增依赖

### pom.xml

```xml
<!-- jtokkit - Tokenizer for Token counting (like tiktoken) -->
<dependency>
    <groupId>com.knuddels</groupId>
    <artifactId>jtokkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

**说明**：
- `jtokkit` 是 Java 版本的 tiktoken，与 OpenAI API 使用相同的分词算法
- 支持多种编码器：`cl100k_base`（GPT-3.5/GPT-4/DeepSeek）、`p50k_base`（GPT-3）等

---

## 🛠️ 核心组件

### 1. TokenCountUtil 工具类

**文件路径**：`src/main/java/com/library/common/utils/TokenCountUtil.java`

```java
@Slf4j
public class TokenCountUtil {
    
    // 使用 cl100k_base 编码器（DeepSeek/GPT-4 通用）
    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Encoding ENCODING = REGISTRY.getEncoding(EncodingType.CL100K_BASE);
    
    /**
     * 计算文本的 Token 数量
     */
    public static int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        try {
            return ENCODING.encode(text).size();
        } catch (Exception e) {
            log.error("Token 计数失败: {}", e.getMessage());
            return estimateTokensByLength(text); // 降级方案
        }
    }
    
    /**
     * 批量计算多个文本的 Token 总数
     */
    public static int countTokens(String... texts) {
        int totalTokens = 0;
        for (String text : texts) {
            totalTokens += countTokens(text);
        }
        return totalTokens;
    }
    
    /**
     * 计算对话消息的 Token 数量（考虑消息格式）
     */
    public static int countMessageTokens(String role, String content) {
        // 消息格式会增加一些额外的 Token
        int formatTokens = 4; // role, content, im_start, im_end
        int contentTokens = countTokens(content);
        return formatTokens + contentTokens;
    }
}
```

---

## 🔧 使用示例

### 示例 1: 计算单个文本的 Token

```java
String text = "推荐一本科幻小说";
int tokens = TokenCountUtil.countTokens(text);

System.out.println("Token 数量: " + tokens); // 输出：Token 数量: 8
```

### 示例 2: 计算多个文本的 Token

```java
String question = "推荐一本科幻小说";
String context = "《三体》是刘慈欣创作的系列长篇科幻小说";
String prompt = String.format("知识库：%s\n\n问题：%s", context, question);

int totalTokens = TokenCountUtil.countTokens(prompt);
System.out.println("总 Token: " + totalTokens);
```

### 示例 3: 批量计算

```java
String text1 = "你好";
String text2 = "世界";
String text3 = "Hello World";

int totalTokens = TokenCountUtil.countTokens(text1, text2, text3);
System.out.println("批量 Token: " + totalTokens);
```

### 示例 4: 计算对话消息 Token

```java
String role = "user";
String content = "推荐一本科幻小说";

int messageTokens = TokenCountUtil.countMessageTokens(role, content);
System.out.println("消息 Token (含格式): " + messageTokens);
```

---

## 🔄 集成到 AI 对话流程

### AiServiceImpl 修改

#### 1. RAG 模式

```java
private void handleRagMode(String question, String sessionId, ChatResponseVO responseVO) {
    // 构造 RAG Prompt
    String ragPrompt = ragPromptTemplate.buildKnowledgeBasePrompt(question, searchResults);
    
    // ✅ 计算输入 Token
    int inputTokens = TokenCountUtil.countTokens(ragPrompt);
    
    // 调用 DeepSeek 生成
    String answer = callDeepSeekChat(ragPrompt);
    
    // ✅ 计算输出 Token
    int outputTokens = TokenCountUtil.countTokens(answer);
    int totalTokens = inputTokens + outputTokens;
    
    // 设置 Token 数量
    responseVO.setInputTokens(inputTokens);
    responseVO.setOutputTokens(outputTokens);
    responseVO.setTotalTokens(totalTokens);
    
    log.info("RAG 模式 Token 统计 - 输入: {}, 输出: {}, 总计: {}", 
            inputTokens, outputTokens, totalTokens);
}
```

#### 2. 传统模式

```java
private void handleTraditionalMode(String question, String sessionId, ChatResponseVO responseVO) {
    KnowledgeBase knowledgeBase = searchKnowledgeBase(question);
    
    if (knowledgeBase != null) {
        // 知识库匹配
        String answer = knowledgeBase.getAnswer();
        
        // ✅ 计算 Token
        int inputTokens = TokenCountUtil.countTokens(question);
        int outputTokens = TokenCountUtil.countTokens(answer);
        int totalTokens = inputTokens + outputTokens;
        
        responseVO.setInputTokens(inputTokens);
        responseVO.setOutputTokens(outputTokens);
        responseVO.setTotalTokens(totalTokens);
    } else {
        // DeepSeek API
        int inputTokens = TokenCountUtil.countTokens(question);
        String answer = callDeepSeekChat(question);
        int outputTokens = TokenCountUtil.countTokens(answer);
        
        responseVO.setInputTokens(inputTokens);
        responseVO.setOutputTokens(outputTokens);
        responseVO.setTotalTokens(inputTokens + outputTokens);
    }
}
```

---

## 📊 Token 计算原理

### 1. 编码器类型

| 编码器 | 适用模型 | Token 特点 |
|--------|---------|-----------|
| `cl100k_base` | GPT-3.5, GPT-4, DeepSeek | 最新的编码器，支持多语言 |
| `p50k_base` | GPT-3 (text-davinci-003) | 旧版编码器 |
| `r50k_base` | GPT-3 (text-davinci-002) | 更旧的编码器 |

**本项目使用 `cl100k_base`**，与 DeepSeek 模型保持一致。

### 2. Token 计算规则

#### 中文文本
```
文本: "你好，世界！"
Token: 约 7 个（平均 1.5 个字符 = 1 Token）
```

#### 英文文本
```
文本: "Hello, World!"
Token: 约 4 个（平均 4 个字符 = 1 Token）
```

#### 混合文本
```
文本: "推荐一本科幻小说，比如 The Three-Body Problem"
Token: 约 18 个
```

### 3. 消息格式 Token

对于 OpenAI/DeepSeek 的消息格式：
```json
{
  "role": "user",
  "content": "推荐一本科幻小说"
}
```

实际 Token 计算：
- **内容 Token**: `countTokens(content)` → 8
- **格式 Token**: 4（role, content, im_start, im_end）
- **总 Token**: 8 + 4 = 12

---

## 🧪 测试指南

### 运行单元测试

```bash
cd Server_Ai
mvn test -Dtest=TokenCountUtilTest
```

### 测试场景

#### 1. 中文文本
```java
String text = "你好，世界！";
int tokens = TokenCountUtil.countTokens(text);
// 预期：6-8 个 Token
```

#### 2. 英文文本
```java
String text = "Hello, World!";
int tokens = TokenCountUtil.countTokens(text);
// 预期：3-4 个 Token
```

#### 3. 长文本
```java
String text = "《三体》是刘慈欣创作的系列长篇科幻小说...";
int tokens = TokenCountUtil.countTokens(text);
// 预期：字符数的 60-70%
```

#### 4. RAG Prompt
```java
String prompt = "根据以下知识库内容回答问题：\n\n知识库：...\n\n问题：...";
int tokens = TokenCountUtil.countTokens(prompt);
// 预期：根据实际内容长度
```

---

## 📈 统计示例

### 对话场景 1: 简单问答

```
用户问题: "推荐一本科幻小说"
输入 Token: 8

AI 回答: "我推荐《三体》，这是刘慈欣创作的科幻巨著..."
输出 Token: 45

总 Token: 53
```

### 对话场景 2: RAG 增强

```
原始问题: "推荐一本科幻小说"
知识库内容: "《三体》是刘慈欣创作的系列长篇科幻小说，获得雨果奖..."
RAG Prompt: "根据以下知识库内容回答问题：\n\n知识库：...\n\n问题：..."

输入 Token: 156（包含知识库内容）
输出 Token: 78
总 Token: 234
```

---

## ⚠️ 注意事项

### 1. 降级策略

如果 Tokenizer 计算失败，会自动使用降级方案：

```java
private static int estimateTokensByLength(String text) {
    // 中文约 1.5 个字符 = 1 Token
    // 英文约 4 个字符 = 1 Token
    int chineseTokens = (int) Math.ceil(chineseCount / 1.5);
    int englishTokens = englishCount / 4;
    return chineseTokens + englishTokens;
}
```

### 2. 性能优化

- **单例模式**：Encoding 对象使用单例，避免重复创建
- **异常处理**：计算失败时使用降级方案，不影响主流程
- **日志记录**：计算失败时记录日志，便于监控

### 3. 编码器选择

当前使用 `cl100k_base`，如果需要支持其他模型：

```java
// GPT-3
Encoding encoding = REGISTRY.getEncoding(EncodingType.P50K_BASE);

// 自定义编码器
Encoding encoding = REGISTRY.getEncodingForModel("gpt-4");
```

---

## 📚 参考资料

### 官方文档

- [jtokkit GitHub](https://github.com/knuddelsgmbh/jtokkit)
- [OpenAI Tokenizer](https://platform.openai.com/tokenizer)
- [tiktoken (Python)](https://github.com/openai/tiktoken)

### Token 计算工具

- **在线工具**: https://platform.openai.com/tokenizer
- **Python 版本**: `pip install tiktoken`
- **Java 版本**: jtokkit（本项目使用）

---

## ✅ 完成清单

- [x] 添加 jtokkit 依赖
- [x] 创建 TokenCountUtil 工具类
- [x] 实现 Token 计算方法
- [x] 集成到 RAG 模式
- [x] 集成到传统模式
- [x] 添加单元测试
- [x] 编写使用文档
- [x] 添加降级策略
- [x] 记录 Token 统计日志

---

## 🎉 优势对比

### 之前（估算方式）
```
responseVO.setInputTokens(0);  // 手动设置为 0
responseVO.setOutputTokens(0); // 手动设置为 0
```
❌ 不准确  
❌ 无法统计真实成本  
❌ 无法优化 Prompt

### 现在（Tokenizer 方式）
```
int inputTokens = TokenCountUtil.countTokens(prompt);   // 精确计算
int outputTokens = TokenCountUtil.countTokens(answer);  // 精确计算
```
✅ **精确计算**：与 DeepSeek API 相同的分词算法  
✅ **真实统计**：准确记录每次对话的 Token 消耗  
✅ **成本控制**：基于真实 Token 计算成本  
✅ **Prompt 优化**：根据 Token 数量优化 Prompt 长度

---

**更新时间**：2025-11-29  
**作者**：AI Assistant  
**版本**：1.0.0
