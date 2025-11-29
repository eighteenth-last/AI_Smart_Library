# Token 统计可视化功能文档

## 📊 功能概述

Token 统计可视化是管理端的重要数据分析功能，帮助管理员实时监控 AI 服务的 Token 使用情况，包括：
- ✅ 总 Token 使用量统计
- ✅ 每日 Token 使用趋势
- ✅ 用户 Token 消耗排行
- ✅ 实时数据刷新（30秒自动更新）

---

## 🏗️ 架构设计

```
┌────────────────────────────────────────┐
│         管理端前端页面                    │
│    (TokenStats/index.vue)              │
│  ┌──────────────────────────────────┐  │
│  │ • 总量统计卡片                     │  │
│  │ • ECharts 趋势图表                │  │
│  │ • 用户排行表格                     │  │
│  │ • 自动刷新机制                     │  │
│  └──────────────────────────────────┘  │
└────────────┬───────────────────────────┘
             ↓ API 调用
┌────────────────────────────────────────┐
│       后端 API 接口                      │
│   (TokenStatsController)               │
│  ┌──────────────────────────────────┐  │
│  │ GET /admin/token-stats           │  │
│  │ GET /admin/token-stats/total     │  │
│  │ GET /admin/token-stats/daily-trend│  │
│  │ GET /admin/token-stats/user-ranking│  │
│  └──────────────────────────────────┘  │
└────────────┬───────────────────────────┘
             ↓ 业务逻辑
┌────────────────────────────────────────┐
│       服务层 (AiServiceImpl)            │
│  ┌──────────────────────────────────┐  │
│  │ getTokenStats()                  │  │
│  │ calculateTotalStats()            │  │
│  │ calculateDailyTrend()            │  │
│  │ calculateUserRanking()           │  │
│  └──────────────────────────────────┘  │
└────────────┬───────────────────────────┘
             ↓ 数据查询
┌────────────────────────────────────────┐
│       数据库 (ai_chat_log)              │
│  • log_id, session_id                  │
│  • question, answer                    │
│  • input_tokens, output_tokens         │
│  • total_tokens, created_at            │
└────────────────────────────────────────┘
```

---

## 🎨 前端实现

### 页面结构

```vue
<template>
  <div class="token-stats-page">
    <!-- 1. 页面头部 -->
    <div class="page-header">
      <h1>Token使用量统计</h1>
      <n-button @click="handleRefresh">刷新数据</n-button>
    </div>

    <!-- 2. 总量统计卡片 -->
    <n-grid :cols="4">
      <n-card>总Token: {{ totalTokens }}</n-card>
      <n-card>总对话: {{ totalChats }}</n-card>
      <n-card>平均Token: {{ avgTokens }}</n-card>
      <n-card>增长率: {{ growthRate }}%</n-card>
    </n-grid>

    <!-- 3. 趋势图表 (ECharts) -->
    <n-card title="Token消耗趋势">
      <div ref="trendChartRef" style="height: 350px"></div>
    </n-card>

    <!-- 4. 用户排行榜 -->
    <n-card title="用户消耗排行">
      <n-data-table :columns="columns" :data="rankings" />
    </n-card>
  </div>
</template>
```

### 关键功能

#### 1. 数据加载函数

```typescript
const loadTokenStats = async () => {
  loading.value = true;
  try {
    const stats = await tokenStatsAPI.getStats(statsDays.value);
    
    // 更新总体统计
    statsOverview.totalTokens = stats.totalStats.totalTokens;
    statsOverview.tokenGrowth = stats.totalStats.growthRate;
    
    // 更新每日趋势
    dailyTrendData.value = stats.dailyTrend;
    updateTrendChart();
    
    // 更新用户排行
    userRankings.value = stats.userRanking;
    
    message.success('数据刷新成功');
  } catch (error) {
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};
```

#### 2. 自动刷新机制

```typescript
// 启动自动刷新（30秒一次）
const startAutoRefresh = () => {
  refreshTimer = window.setInterval(() => {
    loadTokenStats();
  }, 30000); // 30秒
};

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

// 组件挂载时启动
onMounted(() => {
  loadTokenStats();
  startAutoRefresh();
});

// 组件卸载时停止
onUnmounted(() => {
  stopAutoRefresh();
});
```

#### 3. ECharts 图表

```typescript
const updateTrendChart = () => {
  const dates = dailyTrendData.value.map(item => item.date);
  const tokenData = dailyTrendData.value.map(item => item.tokens);
  const chatData = dailyTrendData.value.map(item => item.chats);

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['Token 使用量', '对话次数'] },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 45 }
    },
    yAxis: [
      {
        type: 'value',
        name: 'Token',
        position: 'left'
      },
      {
        type: 'value',
        name: '对话次数',
        position: 'right'
      }
    ],
    series: [
      {
        name: 'Token 使用量',
        type: 'line',
        smooth: true,
        data: tokenData,
        yAxisIndex: 0,
        areaStyle: { /* 渐变填充 */ }
      },
      {
        name: '对话次数',
        type: 'bar',
        data: chatData,
        yAxisIndex: 1
      }
    ]
  };

  trendChart.setOption(option);
};
```

---

## 🔌 后端 API

### 1. 获取完整统计数据

**接口**: `GET /admin/token-stats`

**参数**:
- `days` (可选): 统计天数，默认 30

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalStats": {
      "totalInputTokens": 12345,
      "totalOutputTokens": 23456,
      "totalTokens": 35801,
      "totalChats": 150,
      "avgTokensPerChat": 238.67,
      "todayTokens": 1200,
      "yesterdayTokens": 1100,
      "growthRate": 9.09
    },
    "dailyTrend": [
      {
        "date": "2025-11-01",
        "tokens": 1150,
        "chats": 5,
        "avgTokens": 230.0
      },
      // ... 更多日期数据
    ],
    "userRanking": [
      {
        "userId": 1,
        "username": "user001",
        "nickname": "张三",
        "tokens": 5000,
        "chats": 20,
        "rank": 1
      },
      // ... Top 10 用户
    ]
  }
}
```

### 2. 获取总量统计

**接口**: `GET /admin/token-stats/total`

**响应**: `TotalTokenStats` 对象

### 3. 获取每日趋势

**接口**: `GET /admin/token-stats/daily-trend?days=30`

**响应**: `DailyTokenStats` 数组

### 4. 获取用户排行

**接口**: `GET /admin/token-stats/user-ranking`

**响应**: `UserTokenStats` 数组（Top 10）

---

## 📦 数据模型

### TokenStatsDTO

```java
public class TokenStatsDTO {
    private TotalTokenStats totalStats;
    private List<DailyTokenStats> dailyTrend;
    private List<UserTokenStats> userRanking;
}
```

### TotalTokenStats

```java
public static class TotalTokenStats {
    private Long totalInputTokens;      // 总输入 Token
    private Long totalOutputTokens;     // 总输出 Token
    private Long totalTokens;           // 总 Token
    private Long totalChats;            // 总对话次数
    private Double avgTokensPerChat;    // 平均每次对话 Token
    private Long todayTokens;           // 今日 Token
    private Long yesterdayTokens;       // 昨日 Token
    private Double growthRate;          // 增长率 (%)
}
```

### DailyTokenStats

```java
public static class DailyTokenStats {
    private String date;          // 日期 (yyyy-MM-dd)
    private Long tokens;          // 当日 Token
    private Long chats;           // 当日对话次数
    private Double avgTokens;     // 当日平均 Token
}
```

### UserTokenStats

```java
public static class UserTokenStats {
    private Long userId;
    private String username;
    private String nickname;
    private Long tokens;
    private Long chats;
    private Integer rank;
}
```

---

## 🧪 测试指南

### 1. 后端测试

**启动后端**:
```bash
cd r:\Code_Repository\AI_Smart_Library\Server_Ai
mvn spring-boot:run
```

**测试接口**:
```bash
# 获取完整统计
curl http://localhost:8080/api/admin/token-stats?days=30

# 获取总量
curl http://localhost:8080/api/admin/token-stats/total

# 获取每日趋势
curl http://localhost:8080/api/admin/token-stats/daily-trend?days=7

# 获取用户排行
curl http://localhost:8080/api/admin/token-stats/user-ranking
```

### 2. 前端测试

**启动前端**:
```bash
cd fronted/web-admin
npm run dev
```

**访问页面**:
```
http://localhost:5174/token-stats
```

**测试场景**:
1. ✅ 页面加载时自动获取数据
2. ✅ 点击"刷新数据"按钮手动刷新
3. ✅ 30秒后自动刷新数据
4. ✅ 图表正确显示每日趋势
5. ✅ 用户排行表格正确显示 Top 10
6. ✅ 增长率计算正确

---

## 📊 可视化效果

### 总量统计卡片

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ 总 Token     │ 总对话次数   │ 平均 Token   │ 增长率       │
│ 35,801      │ 150         │ 238.67      │ +9.09%      │
│ ↑ +100%     │ ↑ +100%     │ ↑ +100%     │ 较昨日       │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

### 趋势图表

```
Token
 ^
 |     ╱╲
 |    ╱  ╲      ╱╲
 |   ╱    ╲    ╱  ╲
 |  ╱      ╲  ╱    ╲
 | ╱        ╲╱      ╲___
 +─────────────────────────> 日期
  11-01  11-05  11-10  11-15
```

### 用户排行榜

```
┌────┬──────────┬────────┬────────┐
│ 排名│ 用户名    │ Token  │ 对话数  │
├────┼──────────┼────────┼────────┤
│ 🥇1 │ user001  │ 5,000  │ 20     │
│ 🥈2 │ user002  │ 4,500  │ 18     │
│ 🥉3 │ user003  │ 4,200  │ 17     │
│  4  │ user004  │ 3,800  │ 15     │
│  5  │ user005  │ 3,500  │ 14     │
└────┴──────────┴────────┴────────┘
```

---

## 🔐 权限说明

- **访问权限**: 仅管理员可访问
- **数据权限**: 可查看所有用户的 Token 使用情况
- **操作权限**: 只读，不可修改历史数据

---

## ⚡ 性能优化

### 1. 数据缓存

**建议**: 统计数据可缓存 5 分钟
```java
@Cacheable(value = "tokenStats", key = "#days", unless = "#result == null")
public TokenStatsDTO getTokenStats(Integer days) {
    // ... 统计逻辑
}
```

### 2. 分页查询

**建议**: 用户排行榜限制 Top 10，减少数据传输

### 3. 异步计算

**建议**: 统计计算耗时较长时，使用异步处理
```java
@Async
public CompletableFuture<TokenStatsDTO> getTokenStatsAsync(Integer days) {
    // ... 统计逻辑
    return CompletableFuture.completedFuture(statsDTO);
}
```

---

## 📈 扩展功能建议

### 1. 导出功能
- 导出 Excel 报表
- 导出 PDF 分析报告

### 2. 告警功能
- Token 使用量超过阈值时邮件告警
- 异常用户行为检测

### 3. 预测功能
- 基于历史数据预测未来 Token 消耗
- 成本预估

### 4. 对比功能
- 不同时间段对比
- 不同用户组对比

---

## 🐛 常见问题

### Q1: 数据不刷新？

**解决方案**:
1. 检查自动刷新是否启动
2. 检查浏览器控制台是否有错误
3. 检查后端 API 是否正常

### Q2: 图表不显示？

**解决方案**:
1. 检查 ECharts 是否正确引入
2. 检查数据格式是否正确
3. 检查图表容器高度是否设置

### Q3: 统计数据不准确？

**解决方案**:
1. 检查数据库 ai_chat_log 表数据
2. 检查 total_tokens 字段是否有值
3. 检查时间范围筛选逻辑

---

## 📚 相关文档

- [RAG 功能测试文档](./RAG_功能测试文档.md)
- [AI 回复格式化示例](./AI回复格式化示例.md)
- [前端超时问题修复文档](./前端超时问题修复文档.md)

---

**更新时间**: 2025-11-29  
**版本**: v1.0  
**状态**: ✅ 已完成
