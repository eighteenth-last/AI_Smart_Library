# Token 统计功能优化文档

## 📋 优化概述

本次优化将 Token 统计功能从基于 `ai_chat_log` 表改为使用专门的 `token_usage_stats` 表，实现更精确的用户和角色级别的 Token 统计。

---

## 🎯 优化目标

1. **实时记录**：每次 AI 对话时自动记录 Token 使用情况到 `token_usage_stats` 表
2. **用户关联**：正确关联用户 ID 和用户角色信息
3. **角色统计**：按角色（读者、管理员、作者）分类统计 Token 使用量
4. **前端展示**：优化前端显示，区分教师和学生的 Token 消耗

---

## 📦 新增文件

### 1. 后端实体类

**文件路径**：`Server_Ai/src/main/java/com/library/module/ai/entity/TokenUsageStats.java`

```java
@TableName("token_usage_stats")
@Data
public class TokenUsageStats {
    @TableId(type = IdType.AUTO)
    private Long statId;
    
    private Long userId;              // 用户ID
    private String userRole;          // 用户角色
    private String sessionId;         // 会话ID
    private Integer inputTokens;      // 输入Token
    private Integer outputTokens;     // 输出Token
    private Integer totalTokens;      // 总Token
    private String modelName;         // 模型名称
    private BigDecimal estimatedCost; // 估算成本
    private LocalDate statDate;       // 统计日期
    private Integer statHour;         // 统计小时
    private LocalDateTime createdAt;  // 创建时间
}
```

### 2. Mapper 接口

**文件路径**：`Server_Ai/src/main/java/com/library/module/ai/mapper/TokenUsageStatsMapper.java`

```java
@Mapper
public interface TokenUsageStatsMapper extends BaseMapper<TokenUsageStats> {
}
```

---

## 🔧 修改文件

### 1. AiServiceImpl.java

**主要修改**：

#### （1）添加依赖注入

```java
@Autowired
private TokenUsageStatsMapper tokenUsageStatsMapper;

@Autowired
private UserMapper userMapper;
```

#### （2）新增 recordTokenUsage 方法

在每次对话后自动调用，记录 Token 使用统计：

```java
private void recordTokenUsage(String sessionId, ChatResponseVO responseVO) {
    try {
        // 1. 查询会话信息获取用户ID和角色
        AiSession session = aiSessionMapper.selectById(sessionId);
        Long userId = null;
        String userRole = null;
        
        if (session != null) {
            userId = session.getUserId();
            userRole = session.getUserRole();
        }
        
        // 2. 如果会话中没有角色，从用户表获取
        if (userId != null && userRole == null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                userRole = user.getRole();
            }
        }
        
        // 3. 创建统计记录
        TokenUsageStats stats = new TokenUsageStats();
        stats.setUserId(userId);
        stats.setUserRole(userRole);
        stats.setSessionId(sessionId);
        stats.setInputTokens(responseVO.getInputTokens());
        stats.setOutputTokens(responseVO.getOutputTokens());
        stats.setTotalTokens(responseVO.getTotalTokens());
        stats.setModelName("deepseek-chat");
        
        // 4. 计算成本（DeepSeek: ¥0.001/1K tokens）
        double cost = (stats.getTotalTokens() / 1000.0) * 0.001;
        stats.setEstimatedCost(BigDecimal.valueOf(cost));
        
        // 5. 设置时间信息
        LocalDateTime now = LocalDateTime.now();
        stats.setStatDate(now.toLocalDate());
        stats.setStatHour(now.getHour());
        stats.setCreatedAt(now);
        
        // 6. 保存记录
        tokenUsageStatsMapper.insert(stats);
        
    } catch (Exception e) {
        log.error("记录 Token 统计失败", e);
        // 不抛出异常，避免影响主流程
    }
}
```

#### （3）修改 saveChatLog 方法

```java
private void saveChatLog(String sessionId, String question, ChatResponseVO responseVO) {
    // ... 保存对话日志
    aiChatLogMapper.insert(chatLog);
    
    // 同时记录 Token 统计
    recordTokenUsage(sessionId, responseVO);
}
```

#### （4）修改统计方法

**calculateTotalStats()**：从 `token_usage_stats` 表读取数据

```java
private TokenStatsDTO.TotalTokenStats calculateTotalStats() {
    // 从 token_usage_stats 表查询
    List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
    
    // 使用 LocalDate 查询今日和昨日数据
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    
    long todayTokens = tokenUsageStatsMapper.selectList(
        new LambdaQueryWrapper<TokenUsageStats>()
            .eq(TokenUsageStats::getStatDate, today)
    ).stream()...
}
```

**calculateDailyTrend()**：优化日期查询

```java
private List<TokenStatsDTO.DailyTokenStats> calculateDailyTrend(int days) {
    LocalDate endDate = LocalDate.now();
    
    for (int i = days - 1; i >= 0; i--) {
        LocalDate currentDate = endDate.minusDays(i);
        
        // 查询当日统计
        List<TokenUsageStats> dayStats = tokenUsageStatsMapper.selectList(
            new LambdaQueryWrapper<TokenUsageStats>()
                .eq(TokenUsageStats::getStatDate, currentDate)
        );
        ...
    }
}
```

**calculateUserRanking()**：正确关联用户信息

```java
private List<TokenStatsDTO.UserTokenStats> calculateUserRanking(int topN) {
    List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
    
    // 按 user_id 分组统计
    Map<Long, List<TokenUsageStats>> userGroups = allStats.stream()
        .filter(stats -> stats.getUserId() != null)
        .collect(Collectors.groupingBy(TokenUsageStats::getUserId));
    
    // 查询用户信息
    for (Map.Entry<Long, List<TokenUsageStats>> entry : userGroups.entrySet()) {
        Long userId = entry.getKey();
        User user = userMapper.selectById(userId);
        
        userStats.setUsername(user.getUsername());
        userStats.setNickname(user.getNickname());
        ...
    }
}
```

#### （5）新增 calculateRoleStats 方法

```java
private List<TokenStatsDTO.RoleTokenStats> calculateRoleStats() {
    List<TokenUsageStats> allStats = tokenUsageStatsMapper.selectList(null);
    
    // 按 user_role 分组统计
    Map<String, List<TokenUsageStats>> roleGroups = allStats.stream()
        .filter(stats -> stats.getUserRole() != null)
        .collect(Collectors.groupingBy(TokenUsageStats::getUserRole));
    
    // 角色中文映射
    Map<String, String> roleNameMap = new HashMap<>();
    roleNameMap.put("reader", "读者");
    roleNameMap.put("admin", "管理员");
    roleNameMap.put("author", "作者");
    
    // 统计每个角色的 Token、对话次数、平均值、用户数
    for (Map.Entry<String, List<TokenUsageStats>> entry : roleGroups.entrySet()) {
        String role = entry.getKey();
        List<TokenUsageStats> stats = entry.getValue();
        
        long roleTokens = stats.stream()...
        long roleChats = stats.size();
        double avgTokens = roleChats > 0 ? (double) roleTokens / roleChats : 0.0;
        long userCount = stats.stream()...distinct().count();
        ...
    }
}
```

### 2. TokenStatsDTO.java

**新增角色统计内部类**：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public static class RoleTokenStats {
    private String role;        // 角色类型（reader/admin/author）
    private String roleName;    // 角色中文名称
    private Long tokens;        // Token 使用量
    private Long chats;         // 对话次数
    private Double avgTokens;   // 平均 Token
    private Long userCount;     // 用户数
}
```

**修改主 DTO**：

```java
public class TokenStatsDTO {
    private TotalTokenStats totalStats;
    private List<DailyTokenStats> dailyTrend;
    private List<UserTokenStats> userRanking;
    private List<RoleTokenStats> roleStats;  // 新增
}
```

---

## 🎨 前端修改

### 1. tokenStats.ts

**新增类型定义**：

```typescript
export interface RoleTokenStats {
  role: string
  roleName: string
  tokens: number
  chats: number
  avgTokens: number
  userCount: number
}

export interface TokenStatsDTO {
  totalStats: TotalTokenStats
  dailyTrend: DailyTokenStats[]
  userRanking: UserTokenStats[]
  roleStats: RoleTokenStats[]  // 新增
}
```

### 2. TokenStats/index.vue

**修改 loadTokenStats 函数**：

```typescript
const loadTokenStats = async () => {
  const stats = await tokenStatsAPI.getStats(statsDays.value);
  
  // 更新角色统计
  if (stats.roleStats && stats.roleStats.length > 0) {
    const readerStats = stats.roleStats.find(r => r.role === 'reader');
    const adminStats = stats.roleStats.find(r => r.role === 'admin');
    const authorStats = stats.roleStats.find(r => r.role === 'author');
    
    // 教师 = 管理员 + 作者
    const teacherTokens = (adminStats?.tokens || 0) + (authorStats?.tokens || 0);
    const teacherChats = (adminStats?.chats || 0) + (authorStats?.chats || 0);
    statsOverview.teacherAvg = teacherChats > 0 
      ? Math.round(teacherTokens / teacherChats) 
      : 0;
    
    // 学生 = 读者
    const studentTokens = readerStats?.tokens || 0;
    const studentChats = readerStats?.chats || 0;
    statsOverview.studentAvg = studentChats > 0 
      ? Math.round(studentTokens / studentChats) 
      : 0;
    
    // 更新角色分布
    roleDistribution.teacher = teacherTokens;
    roleDistribution.student = studentTokens;
    roleDistribution.teacherPercent = totalTokens > 0 
      ? Math.round((teacherTokens / totalTokens) * 10000) / 100 
      : 0;
    roleDistribution.studentPercent = totalTokens > 0 
      ? Math.round((studentTokens / totalTokens) * 10000) / 100 
      : 0;
    
    // 更新图表
    updateRoleChart();
  }
}
```

**新增 updateRoleChart 函数**：

```typescript
const updateRoleChart = () => {
  if (!roleChart) return;
  
  const option = {
    series: [{
      type: 'pie',
      data: [
        { value: roleDistribution.teacher, name: '教师' },
        { value: roleDistribution.student, name: '学生' }
      ]
    }]
  };
  
  roleChart.setOption(option);
};
```

---

## 📊 数据流程

### 1. Token 记录流程

```
用户发起 AI 对话
    ↓
AiServiceImpl.chat()
    ↓
handleRagMode() / handleTraditionalMode()
    ↓
callDeepSeekChat() (获取 Token 数据)
    ↓
saveChatLog()
    ├─ 保存对话日志到 ai_chat_log
    └─ recordTokenUsage()
           ├─ 查询 ai_session 获取 userId 和 userRole
           ├─ 如无角色，查询 user 表获取 role
           ├─ 创建 TokenUsageStats 对象
           ├─ 计算成本
           └─ 插入到 token_usage_stats 表
```

### 2. Token 统计流程

```
前端请求 /admin/token-stats
    ↓
TokenStatsController.getStats()
    ↓
AiServiceImpl.getTokenStats()
    ├─ calculateTotalStats()      → 从 token_usage_stats 查询总量
    ├─ calculateDailyTrend()       → 按日期分组统计
    ├─ calculateUserRanking()      → 按用户分组统计 + 关联用户表
    └─ calculateRoleStats()        → 按角色分组统计
        ↓
返回 TokenStatsDTO
    ↓
前端接收并展示
    ├─ 总体统计卡片
    ├─ 教师/学生平均消耗
    ├─ 角色分布饼图
    ├─ 每日趋势图
    └─ 用户排行榜
```

---

## 🔑 关键优化点

### 1. 数据源优化

- **之前**：从 `ai_chat_log` 表统计，缺少用户和角色信息
- **现在**：使用专门的 `token_usage_stats` 表，包含完整的用户和角色信息

### 2. 用户关联优化

- **之前**：通过 `session_id` → `ai_session` → `userId`，无法获取角色
- **现在**：直接在记录时查询并保存 `userId` 和 `userRole`

### 3. 统计性能优化

- **之前**：每次统计需要多表关联查询
- **现在**：直接查询单表，按字段分组即可

### 4. 角色分类优化

- **之前**：无法区分不同角色的 Token 使用量
- **现在**：
  - **读者** → 学生
  - **管理员 + 作者** → 教师
  - 支持按角色统计 Token、对话次数、平均值、用户数

---

## 📈 统计指标

### 总体统计
- 总输入 Token
- 总输出 Token
- 总 Token（输入+输出）
- 总对话次数
- 平均每次对话 Token
- 今日 Token / 昨日 Token / 增长率

### 每日趋势
- 日期
- 当日 Token 使用量
- 当日对话次数
- 当日平均 Token

### 用户排行
- 排名
- 用户ID / 用户名 / 昵称
- Token 使用量
- 对话次数

### 角色统计（新增）
- 角色类型 / 角色名称
- Token 使用量
- 对话次数
- 平均 Token
- 用户数量

---

## 🧪 测试指南

### 1. 后端测试

**启动后端**：
```bash
cd Server_Ai
mvn spring-boot:run
```

**测试 Token 记录**：
```bash
# 发起 AI 对话
POST http://localhost:8080/api/user/ai/chat
{
  "question": "推荐一本科幻小说",
  "sessionId": "test-session-001"
}

# 检查数据库
SELECT * FROM token_usage_stats ORDER BY created_at DESC LIMIT 10;
```

**测试统计 API**：
```bash
# 获取完整统计
GET http://localhost:8080/api/admin/token-stats?days=30

# 检查返回数据
{
  "code": 200,
  "data": {
    "totalStats": { ... },
    "dailyTrend": [ ... ],
    "userRanking": [ ... ],
    "roleStats": [
      {
        "role": "reader",
        "roleName": "读者",
        "tokens": 5000,
        "chats": 20,
        "avgTokens": 250.0,
        "userCount": 5
      }
    ]
  }
}
```

### 2. 前端测试

**启动前端**：
```bash
cd fronted/web-admin
pnpm dev
```

**测试页面**：
1. 访问 `http://localhost:5174/token-stats`
2. 检查统计卡片数据是否正确
3. 检查角色分布饼图是否显示
4. 检查教师/学生平均消耗是否准确
5. 测试刷新按钮
6. 测试自动刷新（30秒）

---

## 📝 注意事项

### 1. 数据迁移

如果已有历史数据在 `ai_chat_log` 表，需要迁移到 `token_usage_stats`：

```sql
INSERT INTO token_usage_stats (
    user_id, user_role, session_id, 
    input_tokens, output_tokens, total_tokens, 
    model_name, stat_date, stat_hour, created_at
)
SELECT 
    s.user_id, 
    s.user_role, 
    c.session_id,
    c.input_tokens, 
    c.output_tokens, 
    c.total_tokens,
    c.model_name,
    DATE(c.created_at),
    HOUR(c.created_at),
    c.created_at
FROM ai_chat_log c
LEFT JOIN ai_session s ON c.session_id = s.session_id
WHERE c.created_at >= '2025-11-01';
```

### 2. 成本估算

DeepSeek 价格：¥0.001 / 1K tokens

```java
double cost = (totalTokens / 1000.0) * 0.001;
```

### 3. 性能优化

- 定期清理历史数据（超过1年）
- 为 `stat_date` 和 `user_id` 创建索引（已在建表SQL中）
- 考虑定期归档统计数据

---

## 🚀 后续优化建议

1. **成本预警**：当月 Token 使用量超过阈值时发送通知
2. **按小时统计**：支持按小时查看 Token 使用趋势
3. **按课程统计**：如果有课程信息，可按课程统计 Token
4. **导出报表**：支持导出 Excel 统计报表
5. **实时监控**：WebSocket 实时推送 Token 使用情况

---

## 📚 相关文档

- [Token统计功能文档.md](./Token统计功能文档.md)
- [AI知识库系统设计文档.md](../AI知识库系统设计文档.md)
- [数据库设计](../database/ai_smart_library.sql)

---

## ✅ 完成清单

- [x] 创建 `TokenUsageStats` 实体类
- [x] 创建 `TokenUsageStatsMapper` 接口
- [x] 添加 `recordTokenUsage` 方法
- [x] 修改 `saveChatLog` 方法
- [x] 优化 `calculateTotalStats` 方法
- [x] 优化 `calculateDailyTrend` 方法
- [x] 优化 `calculateUserRanking` 方法
- [x] 新增 `calculateRoleStats` 方法
- [x] 添加 `RoleTokenStats` DTO
- [x] 更新前端 API 类型定义
- [x] 修改前端 `loadTokenStats` 函数
- [x] 添加 `updateRoleChart` 函数
- [x] 编写优化文档

---

**优化完成时间**：2025-11-29

**优化负责人**：AI Assistant

**测试状态**：待测试
