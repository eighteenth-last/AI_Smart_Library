# 神阁慧境阁 AI智能图书馆系统

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.12-4FC08D.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)

一个集图书管理、在线阅读、AI智能问答于一体的现代化图书馆管理系统

</div>

---

## 📚 项目简介

**神阁慧境阁**是一个功能完善的智能图书馆管理系统，采用前后端分离架构，为读者、作者、管理员三种角色提供专业化的服务体验。系统集成了AI智能问答、知识库管理、多轮对话等先进功能，致力于打造智能化、便捷化的图书管理平台。

### ✨ 核心特性

- 📖 **海量图书管理** - 图书分类、借阅、收藏、评分、评论
- 🤖 **AI智能助手** - 基于DeepSeek的24小时在线AI客服
- 📊 **知识库系统** - 自动生成、向量检索、Excel/CSV导入导出
- 💬 **多轮对话** - 上下文管理、Token统计、会话历史
- 👥 **三端分离** - 用户端、作者端、管理端独立部署
- 🔐 **权限控制** - 基于Sa-Token的细粒度权限管理
- 📈 **数据统计** - Token使用统计、用户行为分析、可视化报表

---

## 🏗️ 系统架构

### 技术栈

#### 后端技术
- **框架**: Spring Boot 3.2.1
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis-Plus 3.5.7
- **权限**: Sa-Token 1.37.0
- **AI集成**: Spring AI 1.0.0-M3
- **文档**: Knife4j OpenAPI 3
- **Token计数**: jtokkit 1.0.0
- **文件处理**: Apache POI 5.2.5、OpenCSV 5.9

#### 前端技术
- **框架**: Vue 3.5.12 + TypeScript 5.5
- **构建工具**: Vite 5.4.8
- **UI框架**: Naive UI 2.39.0
- **状态管理**: Pinia 2.2.6
- **路由**: Vue Router 4.4.5
- **HTTP**: Axios 1.7.7
- **图表**: ECharts 5.5.1

### 项目结构

```
AI_Smart_Library/
├── Server_Ai/                  # 后端服务
│   ├── src/main/java/com/library/
│   │   ├── common/            # 公共模块（异常、结果、工具类）
│   │   ├── config/            # 配置类（Sa-Token、Redis、MyBatis等）
│   │   └── module/            # 业务模块
│   │       ├── ai/            # AI智能问答
│   │       ├── author/        # 作者管理
│   │       ├── book/          # 图书管理
│   │       ├── borrow/        # 借阅管理
│   │       ├── category/      # 分类管理
│   │       ├── dashboard/     # 数据统计
│   │       ├── favorite/      # 收藏管理
│   │       ├── notification/  # 通知管理
│   │       ├── review/        # 评论管理
│   │       ├── tag/           # 标签管理
│   │       └── user/          # 用户管理
│   └── docs/                  # 项目文档
├── fronted/                    # 前端项目
│   ├── web-user/              # 用户端（端口:5173）
│   ├── web-author/            # 作者端（端口:5174）
│   └── web-admin/             # 管理端（端口:5175）
├── database/                   # 数据库脚本
│   └── ai_smart_library.sql
└── upload_img/                 # 文件上传目录
```

---

## 🎯 功能模块

### 👤 用户端功能

#### 图书浏览
- ✅ 热门图书推荐
- ✅ 图书分类浏览
- ✅ 关键词搜索
- ✅ 图书详情查看
- ✅ 评分与评论

#### 借阅管理
- ✅ 在线借阅图书
- ✅ 借阅历史记录
- ✅ 归还提醒
- ✅ 借阅状态追踪

#### 个人中心
- ✅ 个人信息管理
- ✅ 收藏夹管理
- ✅ 借阅记录
- ✅ 评论管理

#### AI智能助手
- ✅ 24小时在线问答
- ✅ 图书推荐
- ✅ 多轮对话
- ✅ 历史会话管理
- ✅ 上下文Token统计

### ✍️ 作者端功能

#### 作者认证
- ✅ 作者注册申请
- ✅ 认证状态查看
- ✅ 个人资料管理

#### 读者互动
- ✅ 读者问答管理
- ✅ 问题回复
- ✅ 互动统计

#### AI助手
- ✅ 创作辅助
- ✅ 内容咨询
- ✅ 数据分析

### 🛠️ 管理端功能

#### 用户管理
- ✅ 用户列表（分页、搜索）
- ✅ 角色管理（读者、作者、管理员）
- ✅ 状态控制（启用/禁用）
- ✅ 用户信息编辑

#### 图书管理
- ✅ 图书CRUD操作
- ✅ 分类管理
- ✅ 标签管理
- ✅ 库存管理
- ✅ 批量操作

#### 作者管理
- ✅ 作者认证审核
- ✅ 作者列表管理
- ✅ 认证状态切换
- ✅ 问答监控

#### 借阅管理
- ✅ 借阅记录查询
- ✅ 借阅审批
- ✅ 归还管理
- ✅ 逾期处理

#### 评论管理
- ✅ 评论审核
- ✅ 内容审查
- ✅ 评论删除

#### 通知管理
- ✅ 系统通知发布
- ✅ 个人通知推送
- ✅ 通知模板管理

#### AI知识库
- ✅ 知识库管理
- ✅ Excel/CSV导入导出
- ✅ 问答对编辑
- ✅ 向量生成与搜索
- ✅ 自动化生成任务

#### Token统计
- ✅ Token使用总览
- ✅ 每日趋势图表
- ✅ 用户排行榜
- ✅ 增长率分析

#### AI助手
- ✅ 管理端专属AI问答
- ✅ 数据查询辅助
- ✅ 操作建议

---

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17+
- **Node.js**: 16.0+
- **MySQL**: 8.0+
- **Redis**: Latest
- **Maven**: 3.6+

### 后端部署

1. **导入数据库**
```bash
# 创建数据库
CREATE DATABASE ai_smart_library CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

# 导入SQL脚本
mysql -u root -p ai_smart_library < database/ai_smart_library.sql
```

2. **配置application.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_smart_library?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: your_username
    password: your_password
    
  data:
    redis:
      host: localhost
      port: 6379
      
# AI配置（DeepSeek）
spring:
  ai:
    openai:
      api-key: your_deepseek_api_key
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
```

3. **启动后端服务**
```bash
cd Server_Ai
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在: `http://localhost:8080`

API文档访问: `http://localhost:8080/doc.html`

### 前端部署

#### 用户端
```bash
cd fronted/web-user
npm install
npm run dev
```
访问地址: `http://localhost:5173`

#### 作者端
```bash
cd fronted/web-author
npm install
npm run dev
```
访问地址: `http://localhost:5174`

#### 管理端
```bash
cd fronted/web-admin
npm install
npm run dev
```
访问地址: `http://localhost:5175`

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 管理端登录 |
| 作者 | author1 | 123456 | 作者端登录（需认证通过） |
| 读者 | reader1 | 123456 | 用户端登录 |

---

## 📖 核心功能说明

### 1. AI智能问答

#### RAG知识库检索
- 基于向量相似度的语义搜索
- 自动生成图书相关问答对
- 支持手动编辑知识库内容

#### 多轮对话管理
- 保留最近10轮对话历史
- 最大6000 tokens上下文窗口
- 自动压缩超限上下文
- Redis 24小时自动过期

#### Token统计
- 实时统计输入/输出Token
- 可视化Token使用趋势
- 用户级别Token排行
- 成本分析（按$0.001/1K tokens）

### 2. 知识库管理

#### 自动化生成
- 定时任务每天凌晨3点执行
- 图书更新即时触发生成
- 每本书生成3-7条问答对
- 自动生成embedding向量

#### 批量导入导出
- **支持格式**: Excel (.xlsx, .xls)、CSV
- **导入校验**: 字段验证、重复检测
- **导出功能**: 按分类导出、全量导出
- **错误报告**: 详细的行号和错误原因

#### 管理接口
- 查询知识库（分页、搜索、分类筛选）
- 新增/编辑/删除问答对
- 批量删除
- 手动触发生成任务

### 3. 作者认证工作流

#### 注册流程
1. 用户注册时选择"作者"角色
2. 系统自动创建author记录（状态：待审核）
3. 用户无法登录，提示"等待审核"

#### 审核流程
1. 管理员在【作者审核】页面查看申请
2. 点击"通过"或"拒绝"
3. 状态变更：通过(1) / 拒绝(2)

#### 登录控制
- 待审核(0)：提示"等待审核"
- 已拒绝(2)：提示"认证被拒绝"
- 已通过(1)：正常登录

---

## 📊 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | user_id, username, role, status |
| author | 作者表 | author_id, user_id, name, auth_status |
| book | 图书表 | book_id, title, author_id, category_id |
| category | 分类表 | category_id, name, parent_id, level |
| borrow_record | 借阅记录 | record_id, user_id, book_id, status |
| favorite | 收藏表 | favorite_id, user_id, book_id |
| review | 评论表 | review_id, book_id, user_id, rating |
| ai_chat_log | AI对话日志 | log_id, session_id, question, answer, tokens |
| ai_session | AI会话表 | session_id, user_id, context_messages |
| kb_library | 知识库 | kb_id, question, answer, embedding |
| token_usage_stats | Token统计 | stat_id, user_id, total_tokens, date |

---

## 🔌 API接口

### 认证相关
```
POST   /auth/register           # 用户注册
POST   /auth/login              # 用户登录
POST   /auth/logout             # 退出登录
GET    /auth/current            # 获取当前用户
```

### 图书相关
```
GET    /books                   # 图书列表（分页、搜索）
GET    /books/{id}              # 图书详情
POST   /admin/books             # 新增图书（管理员）
PUT    /admin/books/{id}        # 编辑图书（管理员）
DELETE /admin/books/{id}        # 删除图书（管理员）
```

### AI问答
```
POST   /ai/chat                 # 发送消息
GET    /ai/sessions             # 获取会话列表
POST   /ai/sessions             # 新建会话
DELETE /ai/sessions/{id}        # 删除会话
GET    /ai/context/{sessionId}  # 获取上下文统计
```

### 知识库管理
```
GET    /admin/knowledge         # 查询知识库
POST   /admin/knowledge         # 新增问答
PUT    /admin/knowledge/{id}    # 编辑问答
DELETE /admin/knowledge/{id}    # 删除问答
POST   /admin/knowledge/import  # 导入Excel/CSV
GET    /admin/knowledge/export  # 导出Excel
POST   /admin/knowledge/generate # 手动触发生成
```

### Token统计
```
GET    /admin/token-stats            # 完整统计
GET    /admin/token-stats/total      # 总量统计
GET    /admin/token-stats/daily-trend # 每日趋势
GET    /admin/token-stats/user-ranking # 用户排行
```

完整API文档: `http://localhost:8080/doc.html`

---

## 🛡️ 权限控制

### 角色定义
- **reader**: 普通读者，可借阅图书、收藏、评论
- **author**: 签约作者，拥有读者权限 + 问答管理
- **admin**: 管理员，拥有所有权限

### 接口权限
```java
@SaCheckLogin              // 需要登录
@SaCheckRole("admin")      // 需要管理员角色
@SaCheckRole("author")     // 需要作者角色
```

### 路由守卫
```typescript
meta: { 
  requiresAuth: true,      // 需要认证
  roles: ['admin']         // 允许的角色
}
```

---

## 📂 配置文件说明

### application.yml（后端）
```yaml
spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/ai_smart_library
    username: root
    password: yourpassword
    
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      
  # AI配置
  ai:
    openai:
      api-key: sk-xxx
      base-url: https://api.deepseek.com
      
# 文件上传配置
upload:
  path: ./upload_img
  max-size: 10MB
  
# Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 2592000  # 30天
```

### .env.development（前端）
```bash
# API基础地址
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 📈 性能优化

### 后端优化
- ✅ MyBatis-Plus分页插件
- ✅ Redis缓存热点数据
- ✅ 数据库索引优化
- ✅ 连接池配置优化
- ✅ 异步任务处理

### 前端优化
- ✅ 路由懒加载
- ✅ 组件按需引入
- ✅ 图片懒加载
- ✅ Vite构建优化
- ✅ CDN加速

---

## 🧪 测试

### 功能测试
```bash
# 后端单元测试
cd Server_Ai
mvn test

# 前端测试
cd fronted/web-admin
npm run test
```

### API测试
使用Knife4j在线文档进行接口测试：`http://localhost:8080/doc.html`

---

## 📝 开发规范

### 代码规范
- **后端**: 遵循阿里巴巴Java开发手册
- **前端**: Vue 3 Composition API + TypeScript
- **命名**: 驼峰命名法，见名知意
- **注释**: 必要的注释和文档

### Git提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
perf: 性能优化
test: 测试
chore: 构建/工具变动
```

---

## 🔧 故障排查

### 常见问题

**1. 后端启动失败**
- 检查MySQL是否启动
- 检查Redis是否启动
- 确认配置文件中的数据库连接信息

**2. 前端无法访问后端**
- 检查后端是否正常运行
- 确认跨域配置是否正确
- 查看浏览器控制台网络请求

**3. AI问答无响应**
- 检查DeepSeek API Key是否有效
- 确认网络连接正常
- 查看后端日志错误信息

**4. Token统计不准确**
- 确认jtokkit依赖已正确引入
- 检查encoding model是否为cl100k_base
- 查看ai_chat_log表中tokens字段

---

## 📦 部署指南

### 生产环境部署

#### 后端打包
```bash
cd Server_Ai
mvn clean package -DskipTests
java -jar target/ai-smart-library-1.0.0.jar
```

#### 前端打包
```bash
cd fronted/web-admin
npm run build
# dist目录部署到Nginx
```

#### Nginx配置示例
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    # 前端静态文件
    location / {
        root /var/www/web-admin/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建新分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'feat: add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交Pull Request

---

## 📄 开源协议

本项目采用 [MIT](LICENSE) 开源协议

---

## 👥 开发团队

- **项目负责人**: [Your Name]
- **后端开发**: [Backend Team]
- **前端开发**: [Frontend Team]

---

## 📞 联系方式

- **项目地址**: https://github.com/yourusername/AI_Smart_Library
- **问题反馈**: [Issues](https://github.com/yourusername/AI_Smart_Library/issues)
- **邮箱**: support@example.com

---

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Naive UI](https://www.naiveui.com/)
- [MyBatis-Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)
- [DeepSeek](https://www.deepseek.com/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个⭐️Star支持一下！**

Made with ❤️ by AI Smart Library Team

</div>
