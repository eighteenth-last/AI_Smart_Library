<template>
  <div class="ai-assistant-container">
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧历史对话列表 -->
      <div class="history-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <!-- 收缩按钮 -->
        <div class="collapse-button" @click="sidebarCollapsed = !sidebarCollapsed">
          <n-icon size="20">
            <MenuOutlined v-if="sidebarCollapsed" />
            <CloseOutlined v-else />
          </n-icon>
        </div>
        
        <n-card :bordered="false" class="history-card" v-show="!sidebarCollapsed">
          <template #header>
            <div class="card-header">💬 历史对话</div>
          </template>
          
          <!-- 新建对话按钮 -->
          <n-button type="primary" @click="startNewChat" class="new-chat-btn" block>
            <template #icon>
              <n-icon size="18">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
                </svg>
              </n-icon>
            </template>
            新建对话
          </n-button>
          
          <n-spin :show="loadingHistory">
            <n-scrollbar style="max-height: calc(100vh - 350px);">
              <n-list v-if="chatHistory.length > 0">
                <!-- 显示最新4条或全部 -->
                <n-list-item
                  v-for="(chat, index) in (showAllHistory ? chatHistory : chatHistory.slice(0, 4))"
                  :key="chat.id"
                  class="history-item"
                  :class="{ active: currentChatId === chat.id }"
                  @click="selectChat(chat.id)"
                >
                  <div class="history-item-content">
                    <div class="history-title">{{ chat.title }}</div>
                    <div class="history-time">{{ formatDate(chat.createdAt) }}</div>
                  </div>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无历史对话" size="medium" />
            </n-scrollbar>
            
            <!-- 展开/收起按钮 -->
            <div v-if="chatHistory.length > 4" class="expand-button-wrapper">
              <n-button text @click="showAllHistory = !showAllHistory" class="expand-btn">
                <template #icon>
                  <n-icon size="16">
                    <svg v-if="!showAllHistory" viewBox="0 0 24 24">
                      <path fill="currentColor" d="M7.41,8.58L12,13.17L16.59,8.58L18,10L12,16L6,10L7.41,8.58Z"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24">
                      <path fill="currentColor" d="M7.41,15.41L12,10.83L16.59,15.41L18,14L12,8L6,14L7.41,15.41Z"/>
                    </svg>
                  </n-icon>
                </template>
                {{ showAllHistory ? '收起' : `查看全部 (${chatHistory.length})` }}
              </n-button>
            </div>
          </n-spin>
        </n-card>
      </div>

      <!-- 右侧对话区域 -->
      <div class="chat-area">
        <n-card :bordered="false" class="chat-card">
          <template #header>
            <div class="card-header">🤖 AI助手</div>
          </template>

          <!-- 对话消息列表 -->
          <n-scrollbar style="max-height: calc(100vh - 380px);" ref="scrollbarRef">
            <div class="messages-container">
              <!-- 欢迎界面 -->
              <div v-if="messages.length === 0" class="welcome-screen">
                <div class="welcome-icon">🤖</div>
                <h2 class="welcome-title">您好！我是AI助手</h2>
                <p class="welcome-subtitle">我可以帮助您解答关于系统管理的问题，包括：</p>
                <div class="quick-actions">
                  <n-button type="info" size="medium" ghost @click="sendQuickQuestion('如何查看系统运营数据？')">
                    📈 如何查看系统运营数据？
                  </n-button>
                  <n-button type="info" size="medium" ghost @click="sendQuickQuestion('如何管理用户账号？')">
                    👥 如何管理用户账号？
                  </n-button>
                  <n-button type="info" size="medium" ghost @click="sendQuickQuestion('如何审核作者申请？')">
                    ✅ 如何审核作者申请？
                  </n-button>
                  <n-button type="info" size="medium" ghost @click="sendQuickQuestion('如何设置AI知识库？')">
                    🧠 如何设置AI知识库？
                  </n-button>
                </div>
              </div>

              <!-- 正常消息列表 -->
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-wrapper"
                :class="msg.role"
              >
                <div class="message-bubble">
                  <div class="message-content" style="white-space: pre-wrap;">{{ decodeMessage(msg.content) }}</div>
                  <div class="message-time">{{ formatDate(msg.createdAt) }}</div>
                </div>
              </div>

              <div v-if="isTyping" class="message-wrapper assistant">
                <div class="message-bubble typing">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            </div>
          </n-scrollbar>

          <!-- 输入区域 -->
          <div class="input-area">
            <div class="input-wrapper">
              <n-input
                v-model:value="inputMessage"
                placeholder="输入您的问题..."
                @keyup.enter.prevent="handleSend"
              />
              <n-button
                type="primary"
                :loading="sending"
                :disabled="!inputMessage.trim()"
                @click="handleSend"
                class="send-button"
              >
                <template #icon>
                  <n-icon size="20">
                    <svg viewBox="0 0 24 24">
                      <path fill="currentColor" d="M2,21L23,12L2,3V10L17,12L2,14V21Z"/>
                    </svg>
                  </n-icon>
                </template>
              </n-button>
            </div>
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import {
  NCard,
  NInput,
  NButton,
  NList,
  NListItem,
  NSpin,
  NEmpty,
  NScrollbar,
  NIcon,
  useMessage
} from 'naive-ui'
import { MenuOutlined, CloseOutlined } from '@vicons/antd'
import { chatWithAI, getSessions, saveSession, getSessionMessages, type SessionVO, type ChatHistoryItem } from '@/api/admin/ai'

const message = useMessage()

// 历史对话列表
const chatHistory = ref<any[]>([])
const loadingHistory = ref(false)

// 侧边栏收缩状态
const sidebarCollapsed = ref(false)

// 历史会话展开状态
const showAllHistory = ref(false)

// 当前对话ID
const currentChatId = ref<string | null>(null)

// 消息列表
const messages = ref<any[]>([])

// 输入消息
const inputMessage = ref('')
const sending = ref(false)
const isTyping = ref(false)

// 滚动条引用
const scrollbarRef = ref()

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 解码消息内容（处理转义字符）
const decodeMessage = (text: string) => {
  if (!text) return ''
  try {
    // 将字符串中的 \n 转换为真正的换行符
    let decoded = text.replace(/\\n/g, '\n')
    // 处理 Unicode 转义序列（如 \uD83D\uDE0A）
    decoded = decoded.replace(/\\u([0-9a-fA-F]{4})/g, (match, code) => {
      return String.fromCharCode(parseInt(code, 16))
    })
    return decoded
  } catch (error) {
    console.error('Decode message error:', error)
    return text
  }
}

// 加载历史对话
const loadChatHistory = async () => {
  loadingHistory.value = true
  try {
    const data = await getSessions()
    // 确保 data 是数组
    const sessions = Array.isArray(data) ? data : []
    chatHistory.value = sessions.map((session: SessionVO) => ({
      id: session.sessionId,
      title: session.title || '新对话',
      createdAt: session.lastActiveAt
    }))
  } catch (error: any) {
    console.error('Load chat history error:', error)
    message.error(error.message || '加载历史对话失败')
  } finally {
    loadingHistory.value = false
  }
}

// 开始新对话
const startNewChat = () => {
  // 不再立即创建会话，只清空当前状态
  currentChatId.value = null
  messages.value = []
  inputMessage.value = ''
  message.success('请输入您的问题开始新对话')
}

// 选择对话
const selectChat = async (chatId: string) => {
  currentChatId.value = chatId
  // 加载该会话的历史消息
  try {
    const response = await getSessionMessages(chatId)
    messages.value = response.records.map((item: ChatHistoryItem) => [
      {
        id: `${item.logId}-user`,
        role: 'user',
        content: item.question,
        createdAt: item.createdAt
      },
      {
        id: `${item.logId}-assistant`,
        role: 'assistant',
        content: item.answer,
        createdAt: item.createdAt
      }
    ]).flat()
    message.success('已加载历史消息')
  } catch (error: any) {
    console.error('Load session messages error:', error)
    message.error(error.message || '加载历史消息失败')
    messages.value = []
  }
}

// 发送快捷问题
const sendQuickQuestion = async (question: string) => {
  if (!question.trim() || sending.value) return
  
  // 直接调用handleSend逻辑
  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: question,
    createdAt: new Date().toISOString()
  }

  // 检查是否是第一条消息
  const isFirstMessage = messages.value.length === 0

  messages.value.push(userMessage)

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 显示输入中状态
  isTyping.value = true
  sending.value = true

  try {
    const response = await chatWithAI({
      question,
      sessionId: currentChatId.value || undefined
    })

    let aiContent = response.answer
    
    // 如果是第一条消息，在AI回复前添加欢迎信息
    if (isFirstMessage) {
      aiContent = `你好！我是你的AI助手🤖

我可以帮助你：
• 分析系统数据和运营指标
• 提供图书管理建议
• 解答平台使用问题
• 帮助处理用户反馈
• 提供系统优化建议

---

` + aiContent
    }

    const aiMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: aiContent,
      createdAt: new Date().toISOString()
    }

    messages.value.push(aiMessage)

    // 如果是新会话，更新sessionId并保存
    if (response.sessionId && response.sessionId !== currentChatId.value) {
      currentChatId.value = response.sessionId
      // 保存会话，使用第一条用户消息作为标题
      const title = question.length > 20 ? question.substring(0, 20) + '...' : question
      await saveSession({
        sessionId: response.sessionId,
        title: title
      })
      await loadChatHistory()
    }

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    console.error('Send message error:', error)
    message.error(error.message || '发送消息失败')
  } finally {
    isTyping.value = false
    sending.value = false
  }
}

// 发送消息
const handleSend = async () => {
  if (!inputMessage.value.trim()) return

  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value.trim(),
    createdAt: new Date().toISOString()
  }

  // 检查是否是第一条消息
  const isFirstMessage = messages.value.length === 0

  messages.value.push(userMessage)
  const question = inputMessage.value.trim()
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 显示输入中状态
  isTyping.value = true
  sending.value = true

  try {
    const response = await chatWithAI({
      question,
      sessionId: currentChatId.value || undefined
    })

    let aiContent = response.answer
    
    // 如果是第一条消息，在AI回复前添加欢迎信息
    if (isFirstMessage) {
      aiContent = `你好！我是你的AI助手🤖

我可以帮助你：
• 分析系统数据和运营指标
• 提供图书管理建议
• 解答平台使用问题
• 帮助处理用户反馈
• 提供系统优化建议

---

` + aiContent
    }

    const aiMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: aiContent,
      createdAt: new Date().toISOString()
    }

    messages.value.push(aiMessage)

    // 如果是新会话，更新sessionId并保存
    if (response.sessionId && response.sessionId !== currentChatId.value) {
      currentChatId.value = response.sessionId
      // 保存会话，使用第一条用户消息作为标题
      const title = question.length > 20 ? question.substring(0, 20) + '...' : question
      await saveSession({
        sessionId: response.sessionId,
        title: title
      })
      await loadChatHistory()
    }

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    console.error('Send message error:', error)
    message.error(error.message || '发送消息失败')
  } finally {
    isTyping.value = false
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  scrollbarRef.value?.scrollTo({ top: 999999, behavior: 'smooth' })
}

onMounted(async () => {
  await loadChatHistory()
})
</script>

<style scoped>
.ai-assistant-container {
  padding: 0;
  background: #f5f7fa;
  min-height: 82vh;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
  margin-bottom: 40px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 48px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.2));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 新建对话按钮（移到侧边栏） */
.new-chat-btn {
  margin-bottom: 16px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.new-chat-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #7c92f5 0%, #8b5cb5 100%);
}

.new-chat-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* 展开/收起按钮 */
.expand-button-wrapper {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

.expand-btn {
  color: #667eea;
  font-size: 13px;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  color: #764ba2;
  background: rgba(102, 126, 234, 0.05);
}

/* 主内容区域 */
.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 140px);
  padding: 0 24px 24px 24px;
}

/* 左侧历史对话 */
.history-sidebar {
  width: 300px;
  flex-shrink: 0;
  transition: all 0.3s ease;
  position: relative;
}

.history-sidebar.collapsed {
  width: 60px;
}

/* 收缩按钮 */
.collapse-button {
  position: absolute;
  top: 20px;
  right: -15px;
  width: 30px;
  height: 30px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.collapse-button:hover {
  background: #f3f4f6;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.history-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  height: 100%;
}

.history-item {
  cursor: pointer;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.history-item:hover {
  background: #f3f4f6;
}

.history-item.active {
  background: #e0f2fe;
  border-left: 3px solid #0ea5e9;
}

.history-item-content {
  width: 100%;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
}

/* 右侧对话区域 */
.chat-area {
  flex: 1;
  min-width: 0;
}

.chat-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* 消息容器 */
.messages-container {
  padding: 20px;
}

/* 欢迎界面 */
.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  text-align: center;
}

.welcome-icon {
  font-size: 80px;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 32px;
  max-width: 400px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  max-width: 600px;
}

.quick-actions :deep(.n-button) {
  border-radius: 20px;
  transition: all 0.3s ease;
}

.quick-actions :deep(.n-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.message-wrapper {
  display: flex;
  margin-bottom: 20px;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-wrapper.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-wrapper.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-wrapper.assistant .message-bubble {
  background: white;
  border: 1px solid #e5e7eb;
  color: #1f2937;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 6px;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
}

/* 输入中动画 */
.typing {
  padding: 16px 20px;
}

.typing-indicator {
  display: flex;
  gap: 6px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
  height: 80px;
  display: flex;
  align-items: center;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-wrapper :deep(.n-input__input-el) {
  height: 48px;
  font-size: 14px;
}

.send-button {
  position: absolute;
  right: 4px;
  height: 40px;
  min-width: 90px;
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24, 160, 88, 0.3);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #18a058 0%, #0d8646 100%);
  border: none;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 160, 88, 0.4);
  background: linear-gradient(135deg, #1ab560 0%, #0f9a50 100%);
}

.send-button:active:not(:disabled) {
  transform: translateY(0);
}

.send-button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  box-shadow: none;
}
</style>
