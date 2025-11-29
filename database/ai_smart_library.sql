/*
 Navicat Premium Dump SQL

 Source Server         : WSL_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44-0ubuntu0.24.04.1)
 Source Host           : 172.31.142.67:3306
 Source Schema         : ai_smart_library

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44-0ubuntu0.24.04.1)
 File Encoding         : 65001

 Date: 29/11/2025 18:25:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_log`;
CREATE TABLE `ai_chat_log`  (
  `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（匿名为NULL）',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色：reader/author/admin',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AI回答',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源：knowledge_base/deepseek/web_search',
  `kb_id` bigint NULL DEFAULT NULL COMMENT '知识库ID（如果来源是知识库）',
  `question_vector` json NULL COMMENT '问题向量',
  `similarity_score` float NULL DEFAULT NULL COMMENT '相似度得分',
  `retrieved_docs` json NULL COMMENT '检索到的文档列表',
  `input_tokens` int NULL DEFAULT 0 COMMENT '输入Token数',
  `output_tokens` int NULL DEFAULT 0 COMMENT '输出Token数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总Token数',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用的模型名称',
  `context_messages` json NULL COMMENT '上下文消息列表',
  `is_context_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用上下文',
  `user_feedback` tinyint NULL DEFAULT NULL COMMENT '用户反馈：1满意/-1不满意/NULL未反馈',
  `feedback_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '反馈备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_source`(`source` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI对话日志表（增强版）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_chat_log
-- ----------------------------
INSERT INTO `ai_chat_log` VALUES (1, 'session_1764341483221', 4, 'reader', '你好', '你好！😊 很高兴见到你！\n\n我是DeepSeek，由深度求索公司创造的AI助手。我可以帮你解答问题、协助思考、进行对话，还支持文件上传和处理等多种功能。\n\n有什么我可以帮助你的吗？无论是学习、工作还是生活中的问题，我都很乐意为你提供帮助！✨', 'deepseek', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-28 23:04:37', 0);
INSERT INTO `ai_chat_log` VALUES (2, 'session_1764343446844', 4, 'reader', '你好', '你好！😊 很高兴见到你！\n\n我是DeepSeek，由深度求索公司创造的AI助手。我可以帮你解答问题、处理文档、进行对话交流，还能为你提供各种信息和建议。\n\n有什么我可以帮助你的吗？无论是学习、工作还是生活中的问题，我都很乐意为你提供帮助！✨', 'deepseek', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-28 23:24:20', 0);
INSERT INTO `ai_chat_log` VALUES (3, 'session_1764343797624', 4, 'reader', '你好', '你好！很高兴见到你！😊\n\n有什么我可以帮助你的吗？无论是回答问题、聊天交流，还是协助处理一些任务，我都很乐意为你提供帮助。请随时告诉我你需要什么！', 'deepseek', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-28 23:30:05', 0);
INSERT INTO `ai_chat_log` VALUES (4, 'session_1764395216617', 4, 'reader', '推荐一下科幻小说', '根据知识库信息，为您推荐以下科幻小说：\n\n1. **《三体》系列** - 刘慈欣的经典作品，深入探讨宇宙文明与人类命运的宏大主题。\n2. **《流浪地球》** - 同为刘慈欣的硬科幻作品，以科学严谨性和丰富的想象力著称。\n\n以上推荐均基于知识库内容。若您对特定科幻子类型（如太空歌剧、赛博朋克等）有兴趣，我可以进一步补充推荐其他作品！', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 13:48:03', 0);
INSERT INTO `ai_chat_log` VALUES (5, 'session_1764395308845', 4, 'reader', '推荐一下科幻小说', '根据知识库信息，为您推荐以下科幻小说：\n\n1. **《三体》系列** - 刘慈欣的经典作品，深入探讨宇宙文明与人类命运的宏大主题。（来自参考资料1）\n2. **《流浪地球》** - 刘慈欣的硬科幻代表作，以科学严谨性构建充满想象力的未来场景。（来自参考资料2）\n\n以上两本都是国内顶尖的科幻作品，适合不同阅读偏好的读者。若您需要更多推荐（如国外科幻经典），我可以进一步补充说明，欢迎告诉我您的具体兴趣方向！', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 13:48:49', 0);
INSERT INTO `ai_chat_log` VALUES (6, 'session_1764395509538', 4, 'reader', '推荐一下科幻小说', '【科幻小说推荐】\n\n根据参考知识库信息，为您推荐以下两部优秀的科幻作品：\n\n1. **《三体》系列** - 刘慈欣的**经典代表作**，深刻探讨宇宙文明与人类命运的宏大主题\n2. **《流浪地球》** - 刘慈欣的**硬科幻佳作**，以丰富的想象力和科学严谨性著称\n\n【补充推荐】\n基于我的知识补充两部同样值得阅读的作品：\n3. **《银河帝国》系列** - 阿西莫夫的经典巨作，开创了机器人三大定律\n4. **《沙丘》** - 弗兰克·赫伯特的史诗级科幻，融合生态与宗教元素\n\n【阅读建议】\n以上作品都是科幻领域的**标杆之作**，其中《三体》和《流浪地球》已有改编影视作品，可以结合观看获得更丰富的体验。如果您对特定科幻子类型感兴趣，我很乐意为您提供更具体的推荐！', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 13:55:51', 0);
INSERT INTO `ai_chat_log` VALUES (7, 'session_1764395509538', 4, 'reader', '推荐一下科幻小说', '【科幻小说推荐】\n\n基于知识库信息，为您推荐以下科幻作品：\n\n1. **《三体》系列** - **刘慈欣**代表作，以宏大的宇宙观探讨文明冲突与人类存亡，包含《三体》《三体Ⅱ·黑暗森林》《三体Ⅲ·死神永生》三部曲\n2. **《流浪地球》** - 同属刘慈欣创作的**硬科幻经典**，以太阳系危机为背景，展现人类集体拯救计划的壮阔历程\n\n【补充推荐】\n（知识库内容较精简，以下为基于个人知识的拓展推荐）\n3. **《沙丘》系列** - 弗兰克·赫伯特创作的太空史诗，融合生态、宗教与权力斗争\n4. **《仿生人会梦见电子羊吗？》** - 菲利普·迪克探讨人工智能与人性界限的哲学科幻\n\n【阅读建议】\n• 优先阅读《三体》系列感受宏观宇宙叙事\n• 《流浪地球》适合体验紧凑的灾难科幻场景\n• 所有推荐作品均有中文译本且被广泛认可为科幻必读\n\n需要了解某部作品更详细的内容特点，欢迎随时追问！', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 13:56:19', 0);
INSERT INTO `ai_chat_log` VALUES (8, 'session_1764395786288', 4, 'reader', '推荐一下科幻小说', '【科幻小说推荐】\n\n根据参考知识库，为您推荐以下科幻作品：\n\n1. **《三体》系列** - 刘慈欣的**经典作品**，深刻探讨宇宙文明与人类命运的宏大主题\n2. **《流浪地球》** - 刘慈欣的**硬科幻作品**，以丰富的想象力和科学严谨性著称\n\n【补充推荐】\n基于我的知识补充几部广受好评的科幻小说：\n3. **《沙丘》** - 弗兰克·赫伯特的史诗级科幻巨著\n4. **《神经漫游者》** - 威廉·吉布森的开创性赛博朋克作品\n\n【阅读建议】\n建议从《三体》开始阅读，这是目前华语科幻界**最具影响力的作品**，曾获得雨果奖等国际大奖认可。如果您更喜欢中短篇作品，《流浪地球》也是很好的选择！\n\n希望这些推荐能帮助您开启精彩的科幻阅读之旅！如果需要更多特定类型的科幻推荐，欢迎随时告诉我~', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 13:56:50', 0);
INSERT INTO `ai_chat_log` VALUES (9, 'session_1764396022482', 4, 'reader', '刘慈欣是谁', '刘慈欣是中国著名的科幻作家，代表作包括《三体》系列、《流浪地球》等。他的作品以宏大的世界观和科学想象著称，曾多次获得国际科幻大奖，其中《三体》更是首部获得“雨果奖”的亚洲科幻小说。他被许多读者视为中国科幻文学的重要推动者。\n\n如果有更多细节想了解，我很乐意补充！ 😊', 'deepseek', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 14:00:53', 0);
INSERT INTO `ai_chat_log` VALUES (10, 'session_1764396638569', 4, 'reader', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？', 'deepseek', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 14:25:42', 0);
INSERT INTO `ai_chat_log` VALUES (11, 'session_1764396638569', 4, 'reader', '科幻小说推荐一下', '根据参考知识库的信息，为您推荐以下科幻小说：\n\n【硬科幻经典】\n1.  **《三体》系列**：这是**刘慈欣的经典作品**，其核心主题是**探讨宇宙文明和人类命运**，格局宏大，想象力丰富。\n2.  **《流浪地球》**：同样是刘慈欣的作品，是一部**优秀的硬科幻作品**，以其**充满想象力和科学严谨性**而著称。\n\n【补充推荐】\n（基于知识库外的信息补充）如果您喜欢上述作品，可能也会对以下经典科幻小说感兴趣：\n1.  **《沙丘》**：弗兰克·赫伯特的史诗级作品，融合了生态、宗教与政治。\n2.  **《基地》系列**：艾萨克·阿西莫夫的经典，讲述了宏大的银河帝国兴衰史。\n\n希望这些推荐能帮助您找到喜欢的科幻世界！', 'rag', 1, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 1, NULL, NULL, '2025-11-29 14:28:46', 0);
INSERT INTO `ai_chat_log` VALUES (12, 'session_1764396638569', 1, 'admin', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力提供准确的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 46, 109, NULL, NULL, 1, NULL, NULL, '2025-11-29 14:34:42', 0);
INSERT INTO `ai_chat_log` VALUES (13, 'session-1764401009866', 1, 'admin', '你好', '你好！很高兴为你提供帮助。  \n\n请问有什么问题需要我解答吗？我会尽力为你提供准确、有用的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 53, 116, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:24:14', 0);
INSERT INTO `ai_chat_log` VALUES (14, 'session-1764401009866', 1, 'admin', '你好', '你好！很高兴再次与你交流。  \n\n有什么我可以帮助你的吗？ 😊', 'deepseek', NULL, NULL, NULL, NULL, 125, 33, 158, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:24:44', 0);
INSERT INTO `ai_chat_log` VALUES (15, 'session-1764401009866', 1, 'admin', '你好', '你好！欢迎回来～  \n今天有什么想了解或需要帮忙的吗？ 😊', 'deepseek', NULL, NULL, NULL, NULL, 170, 33, 203, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:27:29', 0);
INSERT INTO `ai_chat_log` VALUES (16, 'session-1764401009866', 1, 'admin', '你好', '你好！欢迎再次回来～  \n今天想聊些什么呢？ 😊', 'deepseek', NULL, NULL, NULL, NULL, 215, 28, 243, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:28:23', 0);
INSERT INTO `ai_chat_log` VALUES (17, 'session-1764401410574', 1, 'admin', '你好', '你好！很高兴为你提供帮助。  \n\n请问有什么问题需要我解答吗？我会尽力为你提供准确的信息。 😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 50, 113, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:30:30', 0);
INSERT INTO `ai_chat_log` VALUES (18, 'session-1764401458352', 1, 'admin', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力为你提供准确的信息。', 'deepseek', NULL, NULL, NULL, NULL, 63, 46, 109, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:31:10', 0);
INSERT INTO `ai_chat_log` VALUES (19, 'session-1764401458352', 1, 'admin', '你好', '你好！很高兴再次与你交流。请问今天有什么可以帮你的吗？', 'deepseek', NULL, NULL, NULL, NULL, 117, 31, 148, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:31:42', 0);
INSERT INTO `ai_chat_log` VALUES (20, 'session-1764401578995', 1, 'admin', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力提供准确的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 46, 109, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:33:15', 0);
INSERT INTO `ai_chat_log` VALUES (21, 'session-1764401731910', 1, 'admin', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？', 'deepseek', NULL, NULL, NULL, NULL, 63, 31, 94, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:35:45', 0);
INSERT INTO `ai_chat_log` VALUES (22, 'session-1764401759701', 1, 'admin', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力提供准确的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 46, 109, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:43:08', 0);
INSERT INTO `ai_chat_log` VALUES (23, 'session-1764402185792', 5, 'author', '你', '你好！😊 我很乐意帮助你，不过看起来你的消息里只有“你”这个字。我不太确定你想问什么具体内容呢。\n\n请随时告诉我你想了解什么，我会尽力为你提供准确、有用的回答！可以是学习、工作、生活中的任何问题哦～', 'deepseek', NULL, NULL, NULL, NULL, 62, 110, 172, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:43:17', 0);
INSERT INTO `ai_chat_log` VALUES (24, 'session-1764401759701', 1, 'admin', '你好', '你好！很高兴再次与你交流。请问今天有什么想了解或需要帮助的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 118, 39, 157, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:48:15', 0);
INSERT INTO `ai_chat_log` VALUES (25, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，有什么我可以帮你的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 169, 25, 194, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:50:31', 0);
INSERT INTO `ai_chat_log` VALUES (26, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，今天有什么可以帮你的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 206, 26, 232, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:53:22', 0);
INSERT INTO `ai_chat_log` VALUES (27, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，有什么我可以帮你的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 244, 25, 269, NULL, NULL, 1, NULL, NULL, '2025-11-29 15:57:36', 0);
INSERT INTO `ai_chat_log` VALUES (28, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，需要我为你做些什么吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 281, 25, 306, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:01:48', 0);
INSERT INTO `ai_chat_log` VALUES (29, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，今天有什么可以帮你的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 318, 26, 344, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:03:40', 0);
INSERT INTO `ai_chat_log` VALUES (30, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，需要我为你做些什么吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 356, 25, 381, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:06:55', 0);
INSERT INTO `ai_chat_log` VALUES (31, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，有什么我可以帮你的吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 393, 25, 418, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:10:15', 0);
INSERT INTO `ai_chat_log` VALUES (32, 'session-1764401759701', 1, 'admin', '你好', '你好！欢迎回来，需要我为你做些什么吗？😊', 'deepseek', NULL, NULL, NULL, NULL, 430, 25, 455, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:13:05', 0);
INSERT INTO `ai_chat_log` VALUES (33, 'session_1764403505510', 4, 'reader', '你好', '你好！很高兴为你提供帮助。  \n\n请问有什么问题需要我解答吗？我会尽力为你提供准确的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 50, 113, NULL, NULL, 1, NULL, NULL, '2025-11-29 16:15:50', 0);
INSERT INTO `ai_chat_log` VALUES (34, '1b5c27fb-9c9c-4977-b3ea-c8379a790acd', 11, 'author', '你好', '你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力提供准确的信息。', 'deepseek', NULL, NULL, NULL, NULL, 63, 44, 107, NULL, NULL, 1, NULL, NULL, '2025-11-29 17:09:07', 0);
INSERT INTO `ai_chat_log` VALUES (35, '3f7f7362-9d2f-4179-81e9-76d5063e6332', 5, 'author', '你好', '你好！很高兴为你提供帮助。  \n\n请问有什么问题需要我解答吗？我会尽力为你提供准确的信息。😊', 'deepseek', NULL, NULL, NULL, NULL, 63, 50, 113, NULL, NULL, 1, NULL, NULL, '2025-11-29 17:35:50', 0);

-- ----------------------------
-- Table structure for ai_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_session`;
CREATE TABLE `ai_session`  (
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID（UUID）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（匿名为NULL）',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色',
  `session_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '新会话' COMMENT '会话标题',
  `session_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'general' COMMENT '会话类型：general/book_recommend/qa',
  `context_window` int NULL DEFAULT 10 COMMENT '上下文窗口大小',
  `context_messages` json NULL COMMENT '上下文消息数组',
  `total_messages` int NULL DEFAULT 0 COMMENT '总消息数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '会话总Token数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态：active/archived/deleted',
  `last_active_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_active`(`last_active_at` DESC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI会话管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_session
-- ----------------------------
INSERT INTO `ai_session` VALUES ('1b5c27fb-9c9c-4977-b3ea-c8379a790acd', 11, 'author', '新对话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:09:07', '2025-11-29 17:08:58', 0);
INSERT INTO `ai_session` VALUES ('3f7f7362-9d2f-4179-81e9-76d5063e6332', 5, 'author', '新对话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:35:50', '2025-11-29 17:35:42', 0);
INSERT INTO `ai_session` VALUES ('session_1764343797624', 3, NULL, '你好', 'general', 10, '[{\"role\": \"user\", \"content\": \"你好\", \"timestamp\": 1764343802112}, {\"role\": \"assistant\", \"content\": \"你好！很高兴见到你！😊\\n\\n有什么我可以帮助你的吗？无论是回答问题、聊天交流，还是协助处理一些任务，我都很乐意为你提供帮助。请随时告诉我你需要什么！\", \"timestamp\": 1764343804644}]', 2, 0, 'active', '2025-11-28 23:30:07', '2025-11-28 23:30:05', 0);
INSERT INTO `ai_session` VALUES ('session_1764395308845', 4, NULL, '推荐一下科幻小说', 'general', 10, '[{\"role\": \"user\", \"content\": \"推荐一下科幻小说\", \"timestamp\": 1764395318757}, {\"role\": \"assistant\", \"content\": \"根据知识库信息，为您推荐以下科幻小说：\\n\\n1. **《三体》系列** - 刘慈欣的经典作品，深入探讨宇宙文明与人类命运的宏大主题。（来自参考资料1）\\n2. **《流浪地球》** - 刘慈欣的硬科幻代表作，以科学严谨性构建充满想象力的未来场景。（来自参考资料2）\\n\\n以上两本都是国内顶尖的科幻作品，适合不同阅读偏好的读者。若您需要更多推荐（如国外科幻经典），我可以进一步补充说明，欢迎告诉我您的具体兴趣方向！\", \"timestamp\": 1764395328719}]', 2, 0, 'deleted', '2025-11-29 13:48:49', '2025-11-29 13:48:49', 0);
INSERT INTO `ai_session` VALUES ('session_1764396022482', 4, NULL, '刘慈欣是谁', 'general', 10, '[{\"role\": \"user\", \"content\": \"刘慈欣是谁\", \"timestamp\": 1764396041445}, {\"role\": \"assistant\", \"content\": \"刘慈欣是中国著名的科幻作家，代表作包括《三体》系列、《流浪地球》等。他的作品以宏大的世界观和科学想象著称，曾多次获得国际科幻大奖，其中《三体》更是首部获得“雨果奖”的亚洲科幻小说。他被许多读者视为中国科幻文学的重要推动者。\\n\\n如果有更多细节想了解，我很乐意补充！ 😊\", \"timestamp\": 1764396053481}]', 2, 0, 'deleted', '2025-11-29 14:00:54', '2025-11-29 14:00:54', 0);
INSERT INTO `ai_session` VALUES ('session_1764396638569', 4, NULL, '你好', 'general', 10, '[{\"role\": \"user\", \"content\": \"你好\", \"timestamp\": 1764397532516}, {\"role\": \"assistant\", \"content\": \"你好！很高兴为你提供帮助。请问有什么问题需要解答吗？\", \"timestamp\": 1764397541661}, {\"role\": \"user\", \"content\": \"科幻小说推荐一下\", \"timestamp\": 1764397709150}, {\"role\": \"assistant\", \"content\": \"根据参考知识库的信息，为您推荐以下科幻小说：\\n\\n【硬科幻经典】\\n1.  **《三体》系列**：这是**刘慈欣的经典作品**，其核心主题是**探讨宇宙文明和人类命运**，格局宏大，想象力丰富。\\n2.  **《流浪地球》**：同样是刘慈欣的作品，是一部**优秀的硬科幻作品**，以其**充满想象力和科学严谨性**而著称。\\n\\n【补充推荐】\\n（基于知识库外的信息补充）如果您喜欢上述作品，可能也会对以下经典科幻小说感兴趣：\\n1.  **《沙丘》**：弗兰克·赫伯特的史诗级作品，融合了生态、宗教与政治。\\n2.  **《基地》系列**：艾萨克·阿西莫夫的经典，讲述了宏大的银河帝国兴衰史。\\n\\n希望这些推荐能帮助您找到喜欢的科幻世界！\", \"timestamp\": 1764397725657}, {\"role\": \"user\", \"content\": \"你好\", \"timestamp\": 1764398069710}, {\"role\": \"assistant\", \"content\": \"你好！很高兴为你提供帮助。请问有什么问题需要解答吗？我会尽力提供准确的信息。😊\", \"timestamp\": 1764398082275}]', 6, 0, 'deleted', '2025-11-29 14:34:42', '2025-11-29 14:25:42', 0);
INSERT INTO `ai_session` VALUES ('session_1764403505510', 4, 'reader', '你好', 'general', 10, '[{\"role\": \"user\", \"content\": \"你好\", \"timestamp\": 1764404136680}, {\"role\": \"assistant\", \"content\": \"你好！很高兴为你提供帮助。  \\n\\n请问有什么问题需要我解答吗？我会尽力为你提供准确的信息。😊\", \"timestamp\": 1764404150273}]', 2, 0, 'active', '2025-11-29 16:15:55', '2025-11-29 16:15:37', 0);
INSERT INTO `ai_session` VALUES ('session-1764401009866', 5, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:23:30', '2025-11-29 15:23:30', 0);
INSERT INTO `ai_session` VALUES ('session-1764401410574', 5, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:30:11', '2025-11-29 15:30:11', 0);
INSERT INTO `ai_session` VALUES ('session-1764401458352', 1, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:30:58', '2025-11-29 15:30:58', 0);
INSERT INTO `ai_session` VALUES ('session-1764401578995', 5, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:32:59', '2025-11-29 15:32:59', 0);
INSERT INTO `ai_session` VALUES ('session-1764401731910', 5, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:35:32', '2025-11-29 15:35:32', 0);
INSERT INTO `ai_session` VALUES ('session-1764401759701', 1, 'admin', '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:36:00', '2025-11-29 15:36:00', 0);
INSERT INTO `ai_session` VALUES ('session-1764402185792', 5, NULL, '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 15:43:17', '2025-11-29 15:43:06', 0);
INSERT INTO `ai_session` VALUES ('session-1764408053161', 1, 'admin', '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:20:53', '2025-11-29 17:20:53', 0);
INSERT INTO `ai_session` VALUES ('session-1764408058510', 5, 'author', '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:20:59', '2025-11-29 17:20:59', 0);
INSERT INTO `ai_session` VALUES ('session-1764408181789', 5, 'author', '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:23:02', '2025-11-29 17:23:02', 0);
INSERT INTO `ai_session` VALUES ('session-1764408253308', 5, 'author', '新会话', 'general', 10, NULL, 0, 0, 'active', '2025-11-29 17:24:13', '2025-11-29 17:24:13', 0);

-- ----------------------------
-- Table structure for author
-- ----------------------------
DROP TABLE IF EXISTS `author`;
CREATE TABLE `author`  (
  `author_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作者ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联用户ID（若作者可登录）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者名称',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
  `birth_year` int NULL DEFAULT NULL COMMENT '出生年份',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `auth_status` tinyint NOT NULL DEFAULT 0 COMMENT '认证状态：0-待审核, 1-已通过, 2-已拒绝',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除：1已删除 0未删除',
  PRIMARY KEY (`author_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  CONSTRAINT `fk_author_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of author
-- ----------------------------
INSERT INTO `author` VALUES (1, 5, '刘慈欣', '中国', 1963, '科幻作家，《三体》作者', 1, '2025-11-25 15:18:41', '2025-11-25 23:32:44', 0);
INSERT INTO `author` VALUES (2, NULL, 'J. K. Rowling', '英国', 1965, '《哈利·波特》作者', 1, '2025-11-25 15:18:41', '2025-11-25 23:32:46', 0);
INSERT INTO `author` VALUES (3, NULL, '村上春树', '日本', 1949, '日本当代作家', 1, '2025-11-25 15:18:41', '2025-11-25 23:32:48', 0);
INSERT INTO `author` VALUES (4, NULL, 'George Orwell', '英国', 1903, '《1984》与《动物农场》作者', 2, '2025-11-25 15:18:41', '2025-11-25 23:32:49', 0);
INSERT INTO `author` VALUES (5, 6, '余华', '中国', 1960, '代表作《活着》《许三观卖血记》', 1, '2025-11-25 15:18:41', '2025-11-25 23:32:51', 0);
INSERT INTO `author` VALUES (6, 11, '测试', NULL, NULL, NULL, 1, '2025-11-29 17:08:34', '2025-11-29 17:08:34', 0);

-- ----------------------------
-- Table structure for author_qa
-- ----------------------------
DROP TABLE IF EXISTS `author_qa`;
CREATE TABLE `author_qa`  (
  `qa_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问答ID',
  `author_id` bigint UNSIGNED NOT NULL COMMENT '作者ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '提问用户ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作者回答',
  `status` enum('pending','answered') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `is_public` tinyint NOT NULL DEFAULT 1 COMMENT '是否公开显示：1公开 0私密',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `answered_at` datetime NULL DEFAULT NULL COMMENT '回答时间',
  PRIMARY KEY (`qa_id`) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_qa_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`author_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qa_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作者问答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of author_qa
-- ----------------------------
INSERT INTO `author_qa` VALUES (1, 1, 2, '三体第四部什么时候出版？', '目前没有创作第四部的计划，三体系列已经是一个完整的故事。', 'answered', 1, '2025-11-14 14:50:27', '2025-11-16 14:50:27');
INSERT INTO `author_qa` VALUES (2, 5, 3, '《活着》中福贵的原型是谁？', '福贵是虚构人物,但融合了我在农村看到的许多普通人的影子。', 'answered', 1, '2025-11-19 14:50:27', '2025-11-21 14:50:27');
INSERT INTO `author_qa` VALUES (3, 1, 4, '如何看待科幻与现实的关系？', 'dawdaw ', 'answered', 1, '2025-11-22 14:50:27', '2025-11-26 12:18:43');

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ISBN',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '书名',
  `author_id` bigint UNSIGNED NOT NULL COMMENT '作者ID',
  `category_id` bigint UNSIGNED NOT NULL COMMENT '分类ID',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出版社',
  `publish_year` int NULL DEFAULT NULL COMMENT '出版年份',
  `cover_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图书简介',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '馆藏总量',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可借数量',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `average_rating` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '平均评分',
  `review_count` int NOT NULL DEFAULT 0 COMMENT '评价数量',
  `borrow_count` int NOT NULL DEFAULT 0 COMMENT '借阅次数',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_id`) USING BTREE,
  UNIQUE INDEX `uk_isbn`(`isbn` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_book_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`author_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图书信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book
-- ----------------------------
INSERT INTO `book` VALUES (1, '9787530216783', '三体', 1, 2, '重庆出版社', 2008, '/uploads/Books_img/642657a0-003f-4fc1-996b-2591c6bf382a.jpg', '地球文明与三体文明的首次接触。', 30, 29, 0, 0.00, 1, 1, 1, 1, '2025-11-24 14:50:27', '2025-11-26 20:27:20');
INSERT INTO `book` VALUES (2, '9787544270878', '三体Ⅱ·黑暗森林', 1, 2, '重庆出版社', 2008, '/uploads/Books_img/85c3999a-31c9-4429-8837-1fe08697cb22.jpg', '“黑暗森林法则”提出。', 30, 30, 0, 0.00, 0, 0, 2, 1, '2025-11-24 14:50:27', '2025-11-29 16:30:33');
INSERT INTO `book` VALUES (3, '9787530216790', '三体Ⅲ·死神永生', 1, 2, '重庆出版社', 2010, '/uploads/Books_img/b7eade04-33a8-49b6-8127-273997538be8.jpg', '文明终局的史诗。', 30, 30, 0, 0.00, 0, 0, 1, 1, '2025-11-24 14:50:27', '2025-11-26 22:09:02');
INSERT INTO `book` VALUES (4, '9780747532699', '哈利·波特与魔法石', 2, 3, 'Bloomsbury', 1997, '/uploads/Books_img/0189fe2c-4e65-48be-9d2b-aef30ae576ab.jpg', '魔法世界的开端。', 40, 39, 0, 0.00, 0, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 20:27:30');
INSERT INTO `book` VALUES (5, '9780747538493', '哈利·波特与密室', 2, 3, 'Bloomsbury', 1998, '/uploads/Books_img/7774be4d-ae50-4557-84e6-fbb2409d23f5.jpg', '密室开启，蛇怪现身。', 40, 40, 0, 0.00, 0, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 22:09:25');
INSERT INTO `book` VALUES (6, '9784101001548', '挪威的森林', 3, 1, '讲谈社', 1987, '/uploads/Books_img/3a0557de-c919-49e1-a282-f0cb3889c7c5.jpg', '青春与孤独的故事。', 25, 25, 0, 0.00, 1, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 22:09:30');
INSERT INTO `book` VALUES (7, '9780141036144', '1984', 4, 5, 'Secker & Warburg', 1949, '/uploads/Books_img/fe129910-82c3-4778-866c-775ba2e52ad6.jpg', '极权主义反乌托邦小说。', 50, 49, 0, 0.00, 2, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 20:27:41');
INSERT INTO `book` VALUES (8, '9787544276320', '动物农场', 4, 5, 'Harvill Secker', 1945, '/uploads/Books_img/46a1aeda-f39e-45b9-bbf0-f1ada18dd559.jpg', '寓言式讽刺小说。', 40, 40, 0, 0.00, 0, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 22:09:45');
INSERT INTO `book` VALUES (9, '9787506365064', '活着', 5, 1, '作家出版社', 1993, '/uploads/Books_img/1640d2d5-d448-4f50-a53f-625d764ed93c.jpg', '生命苦难的叙述。', 35, 34, 0, 0.00, 0, 0, 0, 1, '2025-11-24 14:50:27', '2025-11-26 20:27:44');

-- ----------------------------
-- Table structure for book_tag
-- ----------------------------
DROP TABLE IF EXISTS `book_tag`;
CREATE TABLE `book_tag`  (
  `book_tag_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `book_id` bigint UNSIGNED NOT NULL COMMENT '图书ID',
  `tag_id` bigint UNSIGNED NOT NULL COMMENT '标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_tag_id`) USING BTREE,
  UNIQUE INDEX `uk_book_tag`(`book_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_bt_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图书标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_tag
-- ----------------------------

-- ----------------------------
-- Table structure for borrow_record
-- ----------------------------
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record`  (
  `record_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '借阅记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `book_id` bigint UNSIGNED NOT NULL COMMENT '图书ID',
  `borrow_time` datetime NOT NULL COMMENT '借书时间',
  `due_time` datetime NOT NULL COMMENT '应还时间',
  `return_time` datetime NULL DEFAULT NULL COMMENT '实际归还时间',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approver_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '审批人ID',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `status` enum('pending','borrowed','returned','overdue','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审批，borrowed-借阅中，returned-已归还，overdue-已逾期，rejected-已拒绝',
  `is_renewed` tinyint NOT NULL DEFAULT 0 COMMENT '是否已续借',
  `overdue_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '逾期费用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除：1已删除 0未删除',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_borrow_time`(`borrow_time` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_approver`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `fk_br_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_br_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '借阅记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_record
-- ----------------------------
INSERT INTO `borrow_record` VALUES (1, 2, 1, '2025-11-14 14:50:27', '2025-12-14 14:50:27', NULL, NULL, NULL, NULL, 'borrowed', 0, 0.00, '2025-11-25 15:10:21', '2025-11-25 15:10:21', 0);
INSERT INTO `borrow_record` VALUES (2, 3, 4, '2025-11-04 14:50:27', '2025-11-19 14:50:27', '2025-11-20 14:50:27', NULL, NULL, NULL, 'returned', 0, 0.00, '2025-11-25 15:10:21', '2025-11-25 15:10:21', 0);
INSERT INTO `borrow_record` VALUES (3, 3, 7, '2025-10-15 14:50:27', '2025-11-14 14:50:27', NULL, NULL, NULL, NULL, 'overdue', 0, 0.00, '2025-11-25 15:10:21', '2025-11-25 15:10:21', 0);
INSERT INTO `borrow_record` VALUES (4, 2, 9, '2025-11-19 14:50:27', '2025-12-19 14:50:27', NULL, NULL, NULL, NULL, 'borrowed', 0, 0.00, '2025-11-25 15:10:21', '2025-11-25 15:10:21', 0);

-- ----------------------------
-- Table structure for browse_history
-- ----------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history`  (
  `history_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `book_id` bigint UNSIGNED NOT NULL COMMENT '图书ID',
  `view_count` int NOT NULL DEFAULT 1 COMMENT '浏览次数',
  `last_view` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后浏览时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  UNIQUE INDEX `uk_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_last_view`(`last_view` ASC) USING BTREE,
  CONSTRAINT `fk_history_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浏览历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of browse_history
-- ----------------------------
INSERT INTO `browse_history` VALUES (1, 2, 1, 5, '2025-11-24 14:50:27');
INSERT INTO `browse_history` VALUES (2, 2, 2, 3, '2025-11-23 14:50:27');
INSERT INTO `browse_history` VALUES (3, 2, 7, 2, '2025-11-22 14:50:27');
INSERT INTO `browse_history` VALUES (4, 3, 4, 4, '2025-11-24 14:50:27');
INSERT INTO `browse_history` VALUES (5, 3, 6, 1, '2025-11-21 14:50:27');
INSERT INTO `browse_history` VALUES (6, 4, 7, 6, '2025-11-24 14:50:27');
INSERT INTO `browse_history` VALUES (7, 4, 8, 2, '2025-11-23 14:50:27');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `category_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父级分类',
  `level` tinyint NULL DEFAULT 1 COMMENT '分类层级',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `category` (`category_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图书分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '文学', NULL, 1);
INSERT INTO `category` VALUES (2, '科幻', 1, 2);
INSERT INTO `category` VALUES (3, '奇幻', 1, 2);
INSERT INTO `category` VALUES (4, '历史', NULL, 1);
INSERT INTO `category` VALUES (5, '社会学', NULL, 1);
INSERT INTO `category` VALUES (6, '玄幻', NULL, 1);
INSERT INTO `category` VALUES (7, '修真', 6, 2);

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `fav_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `book_id` bigint UNSIGNED NOT NULL COMMENT '图书ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`fav_id`) USING BTREE,
  UNIQUE INDEX `uk_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  CONSTRAINT `fk_fav_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 2, 1, '2025-11-24 14:50:27');
INSERT INTO `favorite` VALUES (2, 2, 7, '2025-11-24 14:50:27');
INSERT INTO `favorite` VALUES (4, 4, 2, '2025-11-29 16:30:34');

-- ----------------------------
-- Table structure for knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
  `kb_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `question` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答案',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类：图书馆业务/作者问答/图书推荐',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '来源：system/author/admin',
  `author_id` bigint NULL DEFAULT NULL COMMENT '作者ID（如果是作者回复）',
  `is_public` tinyint(1) NULL DEFAULT 1 COMMENT '是否公开：1公开/0私密',
  `hit_count` int NULL DEFAULT 0 COMMENT '命中次数',
  `question_vector` json NULL COMMENT '问题向量（JSON格式存储）',
  `vector_dim` int NULL DEFAULT 1536 COMMENT '向量维度',
  `embedding_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text-embedding-3-small' COMMENT '使用的Embedding模型',
  `tags` json NULL COMMENT '标签数组 [\"图书推荐\",\"科幻\"]',
  `book_ids` json NULL COMMENT '关联图书ID数组（用于图书推荐）',
  `priority` int NULL DEFAULT 0 COMMENT '优先级：数值越大优先级越高',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`kb_id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_source`(`source` ASC) USING BTREE,
  INDEX `idx_author`(`author_id` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` DESC) USING BTREE,
  FULLTEXT INDEX `ft_question`(`question`) WITH PARSER `ngram`,
  FULLTEXT INDEX `ft_answer`(`answer`) WITH PARSER `ngram`
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI知识库表（增强版）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_base
-- ----------------------------
INSERT INTO `knowledge_base` VALUES (1, '推荐科幻小说', '推荐《三体》系列，刘慈欣的经典作品，探讨宇宙文明和人类命运。', 'book_recommend', 'manual', NULL, 1, 6, NULL, 1536, 'text-embedding-3-small', NULL, NULL, 0, '2025-11-29 13:47:24', '2025-11-29 13:47:24', 0);
INSERT INTO `knowledge_base` VALUES (2, '推荐科幻书籍', '《流浪地球》是一本优秀的硬科幻作品，充满想象力和科学严谨性。', 'book_recommend', 'manual', NULL, 1, 0, NULL, 1536, 'text-embedding-3-small', NULL, NULL, 0, '2025-11-29 13:47:24', '2025-11-29 13:47:24', 0);
INSERT INTO `knowledge_base` VALUES (3, '推荐悬疑小说', '《白夜行》是东野圭吾的代表作，情节跌宕起伏，人物刻画深刻。', 'book_recommend', 'manual', NULL, 1, 0, NULL, 1536, 'text-embedding-3-small', NULL, NULL, 0, '2025-11-29 13:47:24', '2025-11-29 13:47:24', 0);
INSERT INTO `knowledge_base` VALUES (4, '如何借书', '您可以通过以下步骤借书：1. 登录账号 2. 搜索图书 3. 点击借阅按钮 4. 确认借阅信息', 'library_service', 'manual', NULL, 1, 0, NULL, 1536, 'text-embedding-3-small', NULL, NULL, 0, '2025-11-29 13:47:24', '2025-11-29 13:47:24', 0);
INSERT INTO `knowledge_base` VALUES (5, '借阅规则是什么', '每位读者最多可同时借阅5本图书，借阅期限为30天，可续借一次。逾期将产生滞纳金。', 'library_service', 'manual', NULL, 1, 0, NULL, 1536, 'text-embedding-3-small', NULL, NULL, 0, '2025-11-29 13:47:24', '2025-11-29 13:47:24', 0);

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型：borrow_due/overdue/system_announcement',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `related_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联ID（如借阅记录ID）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：1已读 0未读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 2, 'borrow_due', '借阅到期提醒', '您借阅的《三体》将在 10 天后到期，请及时归还。', 1, 0, '2025-11-24 14:50:27');
INSERT INTO `notification` VALUES (2, 3, 'overdue', '逾期通知', '您借阅的《1984》已逾期 30 天，请尽快归还并缴纳逾期费用。', 3, 1, '2025-11-24 14:50:27');
INSERT INTO `notification` VALUES (3, 2, 'system_announcement', '系统公告', '图书馆将于本周六进行系统维护，维护期间暂停借阅服务。', NULL, 1, '2025-11-23 14:50:27');
INSERT INTO `notification` VALUES (4, 3, 'system_announcement', '新书上架', '本月新增科幻类图书 20 本，欢迎借阅！', NULL, 1, '2025-11-24 14:50:27');
INSERT INTO `notification` VALUES (5, 1, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (6, 2, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (7, 3, 'system_announcement', '测试', '测试', NULL, 1, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (8, 4, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (9, 5, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (10, 6, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (11, 7, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (12, 8, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (13, 9, 'system_announcement', '测试', '测试', NULL, 0, '2025-11-26 22:39:36');
INSERT INTO `notification` VALUES (14, 11, 'system_announcement', '完善信息', '完善信息', NULL, 1, '2025-11-29 17:09:42');

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review`  (
  `review_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `book_id` bigint UNSIGNED NOT NULL COMMENT '图书ID',
  `rating` tinyint NOT NULL COMMENT '评分 1~5',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  CONSTRAINT `fk_review_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图书评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review
-- ----------------------------
INSERT INTO `review` VALUES (1, 2, 1, 5, '真正的硬科幻杰作！', '2025-11-24 14:50:27');
INSERT INTO `review` VALUES (2, 3, 6, 4, '文学性很强，但有点压抑。', '2025-11-24 14:50:27');
INSERT INTO `review` VALUES (3, 4, 7, 5, '写得太真实了，有点恐怖。', '2025-11-24 14:50:27');
INSERT INTO `review` VALUES (4, 3, 7, 5, '很好', '2025-11-25 22:01:04');

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签分类',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '科幻', '类型', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (2, '硬科幻', '类型', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (3, '太空歌剧', '风格', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (4, '奇幻', '类型', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (5, '魔法', '元素', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (6, '成长', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (7, '反乌托邦', '类型', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (8, '政治讽刺', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (9, '文学', '类型', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (10, '青春', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (11, '孤独', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (12, '生命', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (13, '苦难', '主题', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (14, '人工智能', '元素', '2025-11-24 14:50:27');
INSERT INTO `tag` VALUES (15, '宇宙', '元素', '2025-11-24 14:50:27');

-- ----------------------------
-- Table structure for token_usage_stats
-- ----------------------------
DROP TABLE IF EXISTS `token_usage_stats`;
CREATE TABLE `token_usage_stats`  (
  `stat_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（NULL表示匿名用户）',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话ID',
  `input_tokens` int NULL DEFAULT 0 COMMENT '输入Token',
  `output_tokens` int NULL DEFAULT 0 COMMENT '输出Token',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总Token',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `estimated_cost` decimal(10, 6) NULL DEFAULT 0.000000 COMMENT '估算成本（元）',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_hour` tinyint NULL DEFAULT NULL COMMENT '统计小时（0-23）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`stat_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_date`(`stat_date` DESC) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Token使用统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of token_usage_stats
-- ----------------------------
INSERT INTO `token_usage_stats` VALUES (1, NULL, NULL, 'session_1764396638569', 0, 0, 0, 'deepseek-chat', 0.000000, '2025-11-29', 14, '2025-11-29 14:25:42');
INSERT INTO `token_usage_stats` VALUES (2, 4, 'reader', 'session_1764396638569', 0, 0, 0, 'deepseek-chat', 0.000000, '2025-11-29', 14, '2025-11-29 14:28:46');
INSERT INTO `token_usage_stats` VALUES (3, 4, 'reader', 'session_1764396638569', 63, 46, 109, 'deepseek-chat', 0.000109, '2025-11-29', 14, '2025-11-29 14:34:42');
INSERT INTO `token_usage_stats` VALUES (4, 5, 'author', 'session-1764401009866', 63, 53, 116, 'deepseek-chat', 0.000116, '2025-11-29', 15, '2025-11-29 15:24:14');
INSERT INTO `token_usage_stats` VALUES (5, 5, 'author', 'session-1764401009866', 125, 33, 158, 'deepseek-chat', 0.000158, '2025-11-29', 15, '2025-11-29 15:24:44');
INSERT INTO `token_usage_stats` VALUES (6, 5, 'author', 'session-1764401009866', 170, 33, 203, 'deepseek-chat', 0.000203, '2025-11-29', 15, '2025-11-29 15:27:29');
INSERT INTO `token_usage_stats` VALUES (7, 5, 'author', 'session-1764401009866', 215, 28, 243, 'deepseek-chat', 0.000243, '2025-11-29', 15, '2025-11-29 15:28:23');
INSERT INTO `token_usage_stats` VALUES (8, 5, 'author', 'session-1764401410574', 63, 50, 113, 'deepseek-chat', 0.000113, '2025-11-29', 15, '2025-11-29 15:30:30');
INSERT INTO `token_usage_stats` VALUES (9, 1, 'admin', 'session-1764401458352', 63, 46, 109, 'deepseek-chat', 0.000109, '2025-11-29', 15, '2025-11-29 15:31:10');
INSERT INTO `token_usage_stats` VALUES (10, 1, 'admin', 'session-1764401458352', 117, 31, 148, 'deepseek-chat', 0.000148, '2025-11-29', 15, '2025-11-29 15:31:42');
INSERT INTO `token_usage_stats` VALUES (11, 5, 'author', 'session-1764401578995', 63, 46, 109, 'deepseek-chat', 0.000109, '2025-11-29', 15, '2025-11-29 15:33:15');
INSERT INTO `token_usage_stats` VALUES (12, 5, 'author', 'session-1764401731910', 63, 31, 94, 'deepseek-chat', 0.000094, '2025-11-29', 15, '2025-11-29 15:35:45');
INSERT INTO `token_usage_stats` VALUES (13, 1, 'admin', 'session-1764401759701', 63, 46, 109, 'deepseek-chat', 0.000109, '2025-11-29', 15, '2025-11-29 15:43:08');
INSERT INTO `token_usage_stats` VALUES (14, 5, 'author', 'session-1764402185792', 62, 110, 172, 'deepseek-chat', 0.000172, '2025-11-29', 15, '2025-11-29 15:43:17');
INSERT INTO `token_usage_stats` VALUES (15, 1, 'admin', 'session-1764401759701', 118, 39, 157, 'deepseek-chat', 0.000157, '2025-11-29', 15, '2025-11-29 15:48:15');
INSERT INTO `token_usage_stats` VALUES (16, 1, 'admin', 'session-1764401759701', 169, 25, 194, 'deepseek-chat', 0.000194, '2025-11-29', 15, '2025-11-29 15:50:32');
INSERT INTO `token_usage_stats` VALUES (17, 1, 'admin', 'session-1764401759701', 206, 26, 232, 'deepseek-chat', 0.000232, '2025-11-29', 15, '2025-11-29 15:54:48');
INSERT INTO `token_usage_stats` VALUES (18, 1, 'admin', 'session-1764401759701', 244, 25, 269, 'deepseek-chat', 0.000269, '2025-11-29', 15, '2025-11-29 15:57:54');
INSERT INTO `token_usage_stats` VALUES (19, 1, 'admin', 'session-1764401759701', 281, 25, 306, 'deepseek-chat', 0.000306, '2025-11-29', 16, '2025-11-29 16:03:11');
INSERT INTO `token_usage_stats` VALUES (20, 1, 'admin', 'session-1764401759701', 318, 26, 344, 'deepseek-chat', 0.000344, '2025-11-29', 16, '2025-11-29 16:03:43');
INSERT INTO `token_usage_stats` VALUES (21, 1, 'admin', 'session-1764401759701', 356, 25, 381, 'deepseek-chat', 0.000381, '2025-11-29', 16, '2025-11-29 16:07:27');
INSERT INTO `token_usage_stats` VALUES (22, 1, 'admin', 'session-1764401759701', 393, 25, 418, 'deepseek-chat', 0.000418, '2025-11-29', 16, '2025-11-29 16:10:15');
INSERT INTO `token_usage_stats` VALUES (23, 1, 'admin', 'session-1764401759701', 430, 25, 455, 'deepseek-chat', 0.000455, '2025-11-29', 16, '2025-11-29 16:14:04');
INSERT INTO `token_usage_stats` VALUES (24, 4, 'reader', 'session_1764403505510', 63, 50, 113, 'deepseek-chat', 0.000113, '2025-11-29', 16, '2025-11-29 16:15:50');
INSERT INTO `token_usage_stats` VALUES (25, 11, 'author', '1b5c27fb-9c9c-4977-b3ea-c8379a790acd', 63, 44, 107, 'deepseek-chat', 0.000107, '2025-11-29', 17, '2025-11-29 17:09:07');
INSERT INTO `token_usage_stats` VALUES (26, 5, 'author', '3f7f7362-9d2f-4179-81e9-76d5063e6332', 63, 50, 113, 'deepseek-chat', 0.000113, '2025-11-29', 17, '2025-11-29 17:35:50');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `role` enum('reader','admin','author') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色类型',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `profile_picture` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123456', '系统管理员', 'admin', '18800000001', 'admin@lib.com', '/uploads/Profile_picture/82c75bae-e524-430e-a1c2-92ee858af95f.jpg', 1, '2025-11-24 14:50:27', '2025-11-26 23:09:56');
INSERT INTO `user` VALUES (2, 'reader1', '123456', '小明', 'reader', '18800000002', 'reader1@lib.com', NULL, 1, '2025-11-24 14:50:27', '2025-11-24 14:51:27');
INSERT INTO `user` VALUES (3, 'reader2', '123456', '小红帽', 'reader', '18800000003', 'reader2@lib.com', '/uploads/Profile_picture/479ac78a-2375-4053-80a9-c947adfeec73.png', 1, '2025-11-24 14:50:27', '2025-11-28 21:56:13');
INSERT INTO `user` VALUES (4, 'reader3', '123456', '小李', 'reader', '18800000004', 'reader3@lib.com', NULL, 1, '2025-11-24 14:50:27', '2025-11-24 14:51:30');
INSERT INTO `user` VALUES (5, 'liucixin', '123456', '刘慈欣', 'author', '18800000005', 'liucixin@lib.com', '/uploads/Profile_picture/9cdfad75-0527-49e2-b396-2237a062d370.png', 1, '2025-11-24 14:50:27', '2025-11-26 13:16:29');
INSERT INTO `user` VALUES (6, 'yuhua', '123456', '余华', 'author', '18800000006', 'yuhua@lib.com', NULL, 1, '2025-11-24 14:50:27', '2025-11-24 14:51:32');
INSERT INTO `user` VALUES (7, 'test_1763976709437', 'JDC9GFYJs3/O0l+TIX13dMDZCg28wgkgV0jMdoVzvK6CViuBRkFDJm1XD75sd7V7', NULL, 'reader', NULL, 'test@example.com', NULL, 1, '2025-11-24 17:31:49', '2025-11-24 17:31:49');
INSERT INTO `user` VALUES (8, 'test0111', 'zMLj71h3ezT1afTmsqE/oDZycNZAz9kO6/8/nWsqeNuH3bM7cnVq1UbYCxWUQ7cP', '测试0111', 'reader', '13252526363', 'eighteenthstuai@gmail.com', NULL, 1, '2025-11-25 15:32:40', '2025-11-25 17:29:11');
INSERT INTO `user` VALUES (9, 'dbwayudbaw', 'A7J84YXB4Q6ZDtcfjS3XmSpnHL2QxLk4+0RkDzEZu7Rzdo6vN+dh7rLrUbYFqS33', 'dbwayudbaw', 'reader', '13252526363', 'dbwayudbaw@qq.com', NULL, 1, '2025-11-25 22:47:47', '2025-11-25 22:47:47');
INSERT INTO `user` VALUES (10, 'Eighteen', 'aw9InSOrjHfh1+zMoS8ukuTJqP8RuDf+0FhspT302hUBbBnCOhhNTrVfePrL6o5y', '封灵天', 'author', NULL, '3273495516@qq.com', NULL, 1, '2025-11-29 16:52:20', '2025-11-29 16:52:20');
INSERT INTO `user` VALUES (11, 'test', 'k31wq7ApKLU9Op7LP13cRvjlTWMbjeiA5cg3QuWB3yZ07vwaf1kPXkdyxun8Mu1z', '测试', 'author', '13252526363', '32734955@qq.com', NULL, 1, '2025-11-29 17:08:34', '2025-11-29 17:08:34');

-- ----------------------------
-- Table structure for vector_index
-- ----------------------------
DROP TABLE IF EXISTS `vector_index`;
CREATE TABLE `vector_index`  (
  `vector_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '向量ID',
  `entity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实体类型：knowledge/book/author_qa',
  `entity_id` bigint NOT NULL COMMENT '实体ID',
  `vector_data` json NOT NULL COMMENT '向量数据（JSON数组）',
  `vector_dim` int NOT NULL COMMENT '向量维度',
  `embedding_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Embedding模型',
  `text_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原始文本（用于调试）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`vector_id`) USING BTREE,
  UNIQUE INDEX `uk_entity`(`entity_type` ASC, `entity_id` ASC) USING BTREE,
  INDEX `idx_type`(`entity_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '向量索引表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vector_index
-- ----------------------------

-- ----------------------------
-- Triggers structure for table borrow_record
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_borrow_count`;
delimiter ;;
CREATE TRIGGER `trg_update_borrow_count` AFTER INSERT ON `borrow_record` FOR EACH ROW BEGIN
    -- 当新增借阅记录时，增加对应图书的借阅次数并减少可用库存
    UPDATE `book` 
    SET `borrow_count` = `borrow_count` + 1,
        `available_stock` = `available_stock` - 1
    WHERE `book_id` = NEW.book_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table borrow_record
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_book_stock_on_return`;
delimiter ;;
CREATE TRIGGER `trg_update_book_stock_on_return` AFTER UPDATE ON `borrow_record` FOR EACH ROW BEGIN
    -- 当借阅状态从借阅中变为已归还时，增加可用库存
    IF OLD.status IN ('borrowed', 'overdue') AND NEW.status = 'returned' THEN
        UPDATE `book` 
        SET `available_stock` = `available_stock` + 1
        WHERE `book_id` = NEW.book_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table favorite
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_favorite_count_insert`;
delimiter ;;
CREATE TRIGGER `trg_update_favorite_count_insert` AFTER INSERT ON `favorite` FOR EACH ROW BEGIN
    -- 当用户收藏图书时，增加对应图书的收藏次数
    UPDATE `book` 
    SET `favorite_count` = `favorite_count` + 1
    WHERE `book_id` = NEW.book_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table favorite
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_favorite_count_delete`;
delimiter ;;
CREATE TRIGGER `trg_update_favorite_count_delete` AFTER DELETE ON `favorite` FOR EACH ROW BEGIN
    -- 当用户取消收藏时，减少对应图书的收藏次数
    UPDATE `book` 
    SET `favorite_count` = `favorite_count` - 1
    WHERE `book_id` = OLD.book_id AND `favorite_count` > 0;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table review
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_review_count_insert`;
delimiter ;;
CREATE TRIGGER `trg_update_review_count_insert` AFTER INSERT ON `review` FOR EACH ROW BEGIN
    -- 当用户评价图书时，增加对应图书的评价次数
    UPDATE `book` 
    SET `review_count` = `review_count` + 1
    WHERE `book_id` = NEW.book_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table review
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_update_review_count_delete`;
delimiter ;;
CREATE TRIGGER `trg_update_review_count_delete` AFTER DELETE ON `review` FOR EACH ROW BEGIN
    -- 当用户删除评价时，减少对应图书的评价次数
    UPDATE `book` 
    SET `review_count` = `review_count` - 1
    WHERE `book_id` = OLD.book_id AND `review_count` > 0;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
