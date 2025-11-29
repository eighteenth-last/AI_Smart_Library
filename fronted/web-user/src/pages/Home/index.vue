<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="navbar-brand" @click="router.push('/')">
          <img src="/favicon.ico" alt="Logo" class="brand-logo" />
          <span class="brand-name">神阁慧境阁</span>
        </div>
        <div class="navbar-menu">
          <router-link to="/" class="menu-item active">首页</router-link>
          <router-link to="/books" class="menu-item">图书馆</router-link>
          <router-link to="/favorites" class="menu-item">我的收藏</router-link>
          <router-link to="/borrow-history" class="menu-item">借阅记录</router-link>
        </div>
        <div class="navbar-actions">
          <NavBar />
        </div>
      </div>
    </nav>

    <!-- 英雄区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">神阁慧境阁</h1>
        <p class="hero-subtitle">海量图书资源，智能推荐，让阅读更简单</p>
        <div class="hero-search">
          <n-input-group>
            <n-input 
              v-model:value="searchKeyword" 
              placeholder="搜索图书、作者、ISBN..." 
              size="large" 
              style="flex: 1"
              @keydown.enter="handleSearch"
            />
            <n-button type="success" size="large" @click="handleSearch">
              <template #icon>
                <n-icon><search-outline /></n-icon>
              </template>
              搜索
            </n-button>
          </n-input-group>
        </div>
      </div>
    </div>

    <!-- 热门图书 -->
    <div class="content-section">
      <div class="section-header">
        <div>
          <h2 class="section-title">
            {{ isSearchMode ? `🔍 搜索结果` : '🔥 热门图书' }}
          </h2>
          <span v-if="isSearchMode" class="search-tip">
            关键词: "{{ searchKeyword }}" 
            <n-button text type="primary" size="small" @click="clearSearch" style="margin-left: 10px">
              清空搜索
            </n-button>
          </span>
        </div>
        <router-link to="/books">
          <n-button text type="primary">查看更多 →</n-button>
        </router-link>
      </div>
      
      <n-spin :show="loadingBooks">
        <n-grid :cols="3" :s-cols="4" :m-cols="5" :l-cols="10" :xl-cols="7" :x-gap="0" :y-gap="60">
          <n-grid-item v-for="book in hotBooks" :key="book.bookId">
            <n-card class="book-card" hoverable @click="goToBookDetail(book.bookId)">
              <div class="book-cover">
                <n-image
                  :src="getCoverUrl(book.coverUrl)"
                  :fallback-src="'/default-book-cover.jpg'"
                  :alt="book.title"
                  object-fit="cover"
                  :preview-disabled="true"
                />
              </div>
              <div class="book-info">
                <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
                <p class="book-author">{{ book.authorName }}</p>
                <div class="book-rating">
                  <n-rate :value="book.averageRating || 0" readonly allow-half size="small" />
                  <span class="rating-text">{{ (book.averageRating || 0).toFixed(1) }}</span>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
        
        <n-empty v-if="!loadingBooks && hotBooks.length === 0" description="暂无图书" style="margin: 40px 0" />
      </n-spin>
    </div>

    <!-- 系统介绍 -->
    <div class="content-section">
      <div class="section-header">
        <h2 class="section-title">📚 系统介绍</h2>
      </div>
      
      <n-card class="system-intro-card">
        <div class="intro-content">
          <div class="intro-section">
            <div class="intro-icon">
              <n-icon size="48" color="#667eea">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12,3L1,9L12,15L21,10.09V17H23V9M5,13.18V17.18L12,21L19,17.18V13.18L12,17L5,13.18Z"/>
                </svg>
              </n-icon>
            </div>
            <h3 class="intro-title">欢迎来到神阁慧境阁智能图书馆</h3>
            <p class="intro-text">
              神阁慧境阁是一个集图书管理、在线阅读、AI智能问答于一体的现代化图书馆系统。
              我们致力于为读者提供便捷的图书借阅服务，丰富的阅读资源，以及智能化的阅读体验。
            </p>
          </div>
          
          <div class="intro-features">
            <div class="feature-item">
              <n-icon size="24" color="#667eea">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19,2L14,6.5V17.5L19,13V2M6.5,5C4.55,5 2.45,5.4 1,6.5V21.16C1,21.41 1.25,21.66 1.5,21.66C1.6,21.66 1.65,21.59 1.75,21.59C3.1,20.94 5.05,20.5 6.5,20.5C8.45,20.5 10.55,20.9 12,22C13.35,21.15 15.8,20.5 17.5,20.5C19.15,20.5 20.85,20.81 22.25,21.56C22.35,21.61 22.4,21.59 22.5,21.59C22.75,21.59 23,21.34 23,21.09V6.5C22.4,6.05 21.75,5.75 21,5.5V19C19.9,18.65 18.7,18.5 17.5,18.5C15.8,18.5 13.35,19.15 12,20V6.5C10.55,5.4 8.45,5 6.5,5Z"/>
                </svg>
              </n-icon>
              <div>
                <h4>海量图书</h4>
                <p>涵盖多个分类，满足不同阅读需求</p>
              </div>
            </div>
            
            <div class="feature-item">
              <n-icon size="24" color="#52c41a">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M21.71 20.29L20.29 21.71A1 1 0 0 1 18.88 21.71L7 9.85A3.81 3.81 0 0 1 6 10A4 4 0 0 1 2 6A4 4 0 0 1 6 2A4 4 0 0 1 10 6A3.81 3.81 0 0 1 9.15 7L10.88 8.73L14.43 5.18A1 1 0 0 1 15.84 5.18L17.26 6.6L20.29 3.57L21.71 5L18.68 8.03L20.1 9.45A1 1 0 0 1 20.1 10.86L16.55 14.41L18.29 16.15A3.81 3.81 0 0 1 19 16A4 4 0 0 1 23 20A4 4 0 0 1 19 24A4 4 0 0 1 15 20A3.81 3.81 0 0 1 15.15 19L13.41 17.26L11.29 19.38A1 1 0 0 1 9.88 19.38L8.46 18L5.43 21L4 19.57L7.03 16.54L5.61 15.12A1 1 0 0 1 5.61 13.71L9.16 10.16L7.85 8.85A4 4 0 0 1 6 9A4 4 0 0 1 6 1Z"/>
                </svg>
              </n-icon>
              <div>
                <h4>AI智能问答</h4>
                <p>24小时在线AI助手，即时解答疑问</p>
              </div>
            </div>
            
            <div class="feature-item">
              <n-icon size="24" color="#ff9800">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.45,13.97L5.82,21L12,17.27Z"/>
                </svg>
              </n-icon>
              <div>
                <h4>个性化推荐</h4>
                <p>根据阅读偏好，推荐心仪图书</p>
              </div>
            </div>
            
            <div class="feature-item">
              <n-icon size="24" color="#f5222d">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
                </svg>
              </n-icon>
              <div>
                <h4>收藏管理</h4>
                <p>轻松收藏喜爱的图书，随时阅读</p>
              </div>
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- AI聊天浮窗 -->
    <div class="ai-chat-float" @click="showAIChatDialog = true">
      <n-badge :value="0" :show="false">
        <n-icon size="30" color="#fff">
          <chatbubbles-outline />
        </n-icon>
      </n-badge>
    </div>

    <!-- AI聊天弹窗 - 自定义弹窗 -->
    <transition name="slide-fade">
      <div v-if="showAIChatDialog" class="ai-chat-modal-wrapper" @click.self="showAIChatDialog = false">
        <div class="ai-chat-modal">
          <!-- 主会话界面 -->
          <n-card
            v-if="!showHistoryView"
            title="AI智能客服"
            :bordered="false"
            :segmented="{
              content: true,
              footer: 'soft'
            }"
            class="ai-chat-card"
          >
            <template #header-extra>
              <div class="header-actions">
                <n-tooltip placement="bottom">
                  <template #trigger>
                    <n-button text @click="handleShowHistory" class="action-btn history-btn">
                      <n-icon size="22">
                        <time-outline />
                      </n-icon>
                    </n-button>
                  </template>
                  历史聊天
                </n-tooltip>
                <n-tooltip placement="bottom">
                  <template #trigger>
                    <n-button text @click="handleNewSession" class="action-btn new-session-btn">
                      <n-icon size="22">
                        <add-circle-outline />
                      </n-icon>
                    </n-button>
                  </template>
                  新建会话
                </n-tooltip>
                <n-button text @click="showAIChatDialog = false" class="action-btn close-btn">
                  <n-icon size="22">
                    <close-outline />
                  </n-icon>
                </n-button>
              </div>
            </template>
            <div class="ai-chat-dialog-content">
        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <div 
            v-for="(msg, index) in chatMessages" 
            :key="index"
            :class="['message-item', msg.role]"
          >
            <div class="message-avatar">
              <n-avatar v-if="msg.role === 'assistant'" style="background-color: #667eea">
                AI
              </n-avatar>
              <n-avatar v-else style="background-color: #52c41a">
                {{ userStore.userInfo?.username?.[0] || 'U' }}
              </n-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="message-text" v-html="formatMessage(msg.content)"></div>
                <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              </div>
            </div>
          </div>

          <!-- 输入中指示器 -->
          <div v-if="isAITyping" class="message-item assistant typing">
            <div class="message-avatar">
              <n-avatar style="background-color: #667eea">AI</n-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 欢迎消息 -->
          <div v-if="chatMessages.length === 0" class="welcome-message">
            <div class="welcome-icon">🤖</div>
            <h2>您好！我是AI图书馆助手</h2>
            <p>我可以帮您解答关于图书馆的各类问题，包括：</p>
            <div class="quick-topics">
              <n-tag type="info" size="small">图书借阅</n-tag>
              <n-tag type="info" size="small">开放时间</n-tag>
              <n-tag type="info" size="small">会员服务</n-tag>
              <n-tag type="info" size="small">推荐图书</n-tag>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div class="quick-questions" v-if="quickQuestions.length > 0 && chatMessages.length === 0">
          <n-space>
            <n-button 
              v-for="question in quickQuestions" 
              :key="question.id"
              type="info" 
              size="small" 
              ghost
              @click="sendQuickQuestion(question.question)"
            >
              {{ question.question }}
            </n-button>
          </n-space>
        </div>
        
        <!-- 上下文 Token 统计 -->
        <div v-if="chatMessages.length > 0" class="context-stats">
          <n-space align="center" size="small">
            <n-icon size="16" color="#667eea">
              <chatbubbles-outline />
            </n-icon>
            <span class="stats-text">
              对话上下文: {{ contextTokens }} / 6000 tokens
            </span>
            <n-progress 
              type="line" 
              :percentage="contextTokenPercentage" 
              :show-indicator="false"
              :height="4"
              :color="contextTokenColor"
              style="width: 120px"
            />
          </n-space>
        </div>

        <!-- 输入框 -->
        <div class="chat-input-container">
          <n-input
            v-model:value="aiUserInput"
            type="text"
            placeholder="尽管问..."
            :disabled="isAITyping"
            @keydown.enter.prevent.stop="handleAISend"
            class="chat-input"
          />
          <n-button
            circle
            type="primary"
            :loading="isAITyping"
            :disabled="!aiUserInput.trim() || isAITyping"
            @click.stop="handleAISend"
            class="send-btn"
          >
            <template #icon>
              <n-icon size="18">
                <arrow-up-circle-outline />
              </n-icon>
            </template>
          </n-button>
        </div>
      </div>
          </n-card>

          <!-- 历史会话界面 -->
          <n-card
            v-if="showHistoryView"
            :bordered="false"
            class="ai-chat-card history-card"
          >
            <template #header>
              <div class="history-header">
                <n-button text @click="handleBackToChat" class="back-btn">
                  <n-icon size="20">
                    <arrow-back-outline />
                  </n-icon>
                </n-button>
                <span class="history-title">会话历史</span>
              </div>
            </template>
            <template #header-extra>
              <n-button text @click="handleClearHistory" type="error" size="small">
                清空历史
              </n-button>
            </template>
            
            <div class="history-content">
              <n-empty v-if="chatHistory.length === 0" description="暂无历史会话" />
              
              <div v-else class="history-list">
                <div 
                  v-for="(session, index) in chatHistory" 
                  :key="session.sessionId"
                  class="history-item"
                  @click="loadHistorySession(session)"
                >
                  <div class="history-item-header">
                    <div class="session-title">
                      <n-icon size="18" color="#667eea">
                        <chatbubbles-outline />
                      </n-icon>
                      <span>{{ session.sessionTitle }}</span>
                    </div>
                    <div class="session-time">{{ formatHistoryTime(session.lastActiveAt) }}</div>
                  </div>
                  <div class="history-item-preview">
                    {{ session.preview }}
                  </div>
                  <n-divider v-if="index < chatHistory.length - 1" style="margin: 12px 0" />
                </div>
              </div>
            </div>
          </n-card>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { SearchOutline, BookOutline, ChatbubblesOutline, CloseOutline, TimeOutline, AddCircleOutline, ArrowBackOutline, ArrowUpCircleOutline } from '@vicons/ionicons5';
import { getBookList, getCategories, type Book, type BookListResponse, type Category } from '@/api/user/books';
import { chatWithAI, getChatHistory, getQuickQuestions, type QuickQuestion } from '@/api/user/ai';
import { getSessions, saveSession, deleteSession, clearAllSessions, type SessionInfo } from '@/api/user/session';
import { useUserStore } from '@/store/user';
import NavBar from '@/components/NavBar.vue';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const searchKeyword = ref('');
const hotBooks = ref<Book[]>([]);
const categories = ref<Category[]>([]);
const loadingBooks = ref(false);
const loadingCategories = ref(false);
const isSearchMode = ref(false); // 是否处于搜索模式

// AI聊天相关状态
const showAIChatDialog = ref(false);
const showHistoryView = ref(false);
const aiUserInput = ref('');
const isAITyping = ref(false);
const chatMessages = ref<Array<{
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}>>([]);
const quickQuestions = ref<QuickQuestion[]>([]);
const messagesContainer = ref<HTMLElement>();
const aiSessionId = ref(`session_${Date.now()}`);
const contextTokens = ref(0);
const contextTokenPercentage = ref(0);
const contextTokenColor = ref('#18a058');

// 历史会话管理
interface ChatSession {
  sessionId: string;
  title: string;
  preview: string;
  timestamp: number;
  messages: Array<{
    role: 'user' | 'assistant';
    content: string;
    timestamp: number;
  }>;
}

const chatHistory = ref<SessionInfo[]>([]);

// 格式化消息内容（支持 Markdown 样式）
const formatMessage = (content: string) => {
  if (!content) return '';
  
  let formatted = content;
  
  // 1. 处理换行
  formatted = formatted.replace(/\n/g, '<br>');
  
  // 2. 处理粗体 **文本**
  formatted = formatted.replace(/\*\*([^*]+)\*\*/g, '<strong class="highlight-text">$1</strong>');
  
  // 3. 处理数字列表 1. 2. 3.
  formatted = formatted.replace(/(\d+)\.\s+([^<\n]+)/g, '<div class="list-item"><span class="list-number">$1</span><span class="list-content">$2</span></div>');
  
  // 4. 处理书名《》
  formatted = formatted.replace(/《([^》]+)》/g, '<span class="book-title">《$1》</span>');
  
  // 5. 处理括号内容（来源信息）
  formatted = formatted.replace(/（([^）]+)）/g, '<span class="source-info">（$1）</span>');
  
  // 6. 处理【】标签
  formatted = formatted.replace(/【([^】]+)】/g, '<div class="section-tag">$1</div>');
  
  return formatted;
};

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

// 添加消息
const addChatMessage = (role: 'user' | 'assistant', content: string) => {
  chatMessages.value.push({
    role,
    content,
    timestamp: Date.now()
  });
  scrollToBottom();
};

// 加载快捷问题
const loadQuickQuestions = async () => {
  try {
    const response = await getQuickQuestions();
    quickQuestions.value = response.slice(0, 6);
  } catch (error) {
    console.error('加载快捷问题失败', error);
  }
};

// 防抖标识
const isSending = ref(false);

// 发送AI消息
const handleAISend = async () => {
  const question = aiUserInput.value.trim();
  if (!question || isAITyping.value || isSending.value) return;

  // 防止重复发送
  isSending.value = true;
  addChatMessage('user', question);
  aiUserInput.value = '';
  isAITyping.value = true;

  try {
    const response = await chatWithAI({
      question,
      sessionId: aiSessionId.value
    });

    aiSessionId.value = response.sessionId;
    addChatMessage('assistant', response.answer);
    
    // 更新上下文 Token 统计
    if (response.contextTokens) {
      contextTokens.value = response.contextTokens;
      contextTokenPercentage.value = Math.min((response.contextTokens / 6000) * 100, 100);
      
      // 根据 Token 数量设置颜色
      if (contextTokenPercentage.value < 60) {
        contextTokenColor.value = '#18a058'; // 绿色
      } else if (contextTokenPercentage.value < 85) {
        contextTokenColor.value = '#f0a020'; // 黄色
      } else {
        contextTokenColor.value = '#d03050'; // 红色
      }
    }
    
    // 自动保存会话
    saveCurrentSession();
  } catch (error: any) {
    console.error('AI 对话错误:', error);
    
    // 根据错误类型给出不同提示
    let errorMessage = '抱歉，我遇到了一些问题，请稍后再试。';
    let toastMessage = '发送失败';
    
    if (error.message && error.message.includes('超时')) {
      errorMessage = '抱歉，AI 回复用时较长，请稍后再试或简化您的问题。';
      toastMessage = 'AI 回复超时，请稍后再试';
    } else if (error.message) {
      toastMessage = error.message;
    }
    
    addChatMessage('assistant', errorMessage);
    message.error(toastMessage);
  } finally {
    isAITyping.value = false;
    // 延迟解除防抖锁，防止快速重复点击
    setTimeout(() => {
      isSending.value = false;
    }, 500);
  }
};

// 发送快捷问题
const sendQuickQuestion = async (question: string) => {
  if (isAITyping.value) return;
  
  addChatMessage('user', question);
  isAITyping.value = true;

  try {
    const response = await chatWithAI({
      question,
      sessionId: aiSessionId.value
    });

    aiSessionId.value = response.sessionId;
    addChatMessage('assistant', response.answer);
  } catch (error: any) {
    console.error('AI 对话错误:', error);
    
    let errorMessage = '抱歉，我遇到了一些问题，请稍后再试。';
    let toastMessage = '发送失败';
    
    if (error.message && error.message.includes('超时')) {
      errorMessage = '抱歉，AI 回复用时较长，请稍后再试或简化您的问题。';
      toastMessage = 'AI 回复超时，请稍后再试';
    } else if (error.message) {
      toastMessage = error.message;
    }
    
    addChatMessage('assistant', errorMessage);
    message.error(toastMessage);
  } finally {
    isAITyping.value = false;
  }
};

// 加载历史会话
const loadChatHistory = async () => {
  if (!userStore.isLoggedIn()) return;
  
  try {
    const sessions = await getSessions();
    chatHistory.value = sessions;
  } catch (error) {
    console.error('加载历史会话失败', error);
  }
};

// 保存当前会话到历史
const saveCurrentSession = async () => {
  // 如果没有消息或未登录，不保存
  if (chatMessages.value.length === 0 || !userStore.isLoggedIn()) {
    return;
  }

  const firstUserMessage = chatMessages.value.find(msg => msg.role === 'user');
  const title = firstUserMessage ? firstUserMessage.content.substring(0, 30) : '新会话';

  try {
    await saveSession({
      sessionId: aiSessionId.value,
      sessionTitle: title,
      totalMessages: chatMessages.value.length,
      contextMessages: JSON.stringify(chatMessages.value)
    });
  } catch (error) {
    console.error('保存会话失败', error);
  }
};

// 处理历史聊天
const handleShowHistory = async () => {
  await loadChatHistory();
  showHistoryView.value = true;
};

// 处理新建会话
const handleNewSession = async () => {
  // 保存当前会话
  await saveCurrentSession();
  
  // 清空消息和创建新会话
  chatMessages.value = [];
  aiSessionId.value = `session_${Date.now()}`;
  contextTokens.value = 0;
  contextTokenPercentage.value = 0;
  contextTokenColor.value = '#18a058';
  message.success('已创建新会话');
};

// 返回聊天界面
const handleBackToChat = () => {
  showHistoryView.value = false;
};

// 加载历史会话
const loadHistorySession = async (session: SessionInfo) => {
  // 保存当前会话
  await saveCurrentSession();
  
  // 从后端获取会话消息
  try {
    const response = await getChatHistory({
      sessionId: session.sessionId,
      page: 1,
      size: 50
    });
    
    // 转换为聊天消息格式
    chatMessages.value = response.records.flatMap((item: any) => [
      {
        role: 'user',
        content: item.question,
        time: item.createdAt
      },
      {
        role: 'assistant',
        content: item.answer,
        time: item.createdAt
      }
    ]);
    
    aiSessionId.value = session.sessionId;
    showHistoryView.value = false;
    message.success('已加载历史会话');
  } catch (error) {
    console.error('加载会话失败', error);
    message.error('加载会话失败');
  }
};

// 清空历史
const handleClearHistory = async () => {
  if (!userStore.isLoggedIn()) {
    message.warning('请先登录');
    return;
  }
  
  try {
    await clearAllSessions();
    chatHistory.value = [];
    message.success('已清空历史会话');
  } catch (error) {
    console.error('清空失败', error);
    message.error('清空失败');
  }
};

// 格式化历史时间
const formatHistoryTime = (timestamp: string) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) {
    return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays < 7) {
    return `${diffDays}天前`;
  } else {
    return date.toLocaleDateString('zh-CN');
  }
};

// 获取封面完整URL
const getCoverUrl = (url?: string) => {
  if (!url) return '/default-book-cover.jpg';
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

const loadHotBooks = async () => {
  loadingBooks.value = true;
  try {
    const response: BookListResponse = await getBookList({ page: 1, size: 10 });
    hotBooks.value = response.records;
  } catch (error) {
    console.error('加载热门图书失败', error);
    message.error('加载图书失败');
  } finally {
    loadingBooks.value = false;
  }
};

// 搜索图书
const searchBooks = async () => {
  if (!searchKeyword.value.trim()) {
    message.warning('请输入搜索关键词');
    return;
  }
  
  loadingBooks.value = true;
  isSearchMode.value = true;
  try {
    const response: BookListResponse = await getBookList({ 
      page: 1, 
      size: 20,
      keyword: searchKeyword.value 
    });
    hotBooks.value = response.records;
    if (response.records.length === 0) {
      message.info(`未找到与"${searchKeyword.value}"相关的图书`);
    }
  } catch (error) {
    console.error('搜索图书失败', error);
    message.error('搜索失败，请重试');
  } finally {
    loadingBooks.value = false;
  }
};

// 清空搜索，返回热门图书
const clearSearch = () => {
  searchKeyword.value = '';
  isSearchMode.value = false;
  loadHotBooks();
};

const loadCategories = async () => {
  loadingCategories.value = true;
  try {
    const response = await getCategories();
    // API返回结构: { code: 200, msg: 'success', data: [...] }
    const categoryList = response.data || [];
    categories.value = categoryList.slice(0, 12);
  } catch (error) {
    console.error('加载分类失败', error);
    message.error('加载分类失败');
  } finally {
    loadingCategories.value = false;
  }
};

const handleSearch = () => {
  searchBooks();
};

const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`);
};

const goToCategory = (categoryId: number) => {
  router.push({
    path: '/books',
    query: { categoryId }
  });
};

onMounted(() => {
  loadHotBooks();
  loadCategories();
  loadQuickQuestions();
  loadChatHistory();
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏样式 */
.navbar {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

.navbar-menu {
  display: flex;
  gap: 30px;
}

.menu-item {
  font-size: 15px;
  color: #666;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
}

.menu-item:hover {
  color: #667eea;
  background: #f0f2f5;
}

.menu-item.active {
  color: #667eea;
  font-weight: 600;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.user-btn:hover {
  background: rgba(102, 126, 234, 0.08);
  color: #667eea;
}

/* 英雄区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px 60px;
  text-align: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  animation: fadeInDown 0.8s ease-out;
  letter-spacing: 2px;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-subtitle {
  font-size: 17px;
  margin: 0 0 35px 0;
  opacity: 0.92;
  animation: fadeIn 1s ease-out 0.3s both;
  font-weight: 400;
  letter-spacing: 0.5px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 0.95;
  }
}

.hero-search {
  max-width: 600px;
  margin: 0 auto;
  animation: fadeInUp 1s ease-out 0.5s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 内容区域 */
.content-section {
  max-width: 1400px;
  margin: 50px auto;
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.search-tip {
  font-size: 14px;
  color: #666;
  font-weight: 400;
}

/* 图书卡片 */
.book-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: 200px;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.book-card :deep(.n-card__content) {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.book-cover {
  width: 200px;
  height: 210px;
  overflow: hidden;
  background: #f0f0f0;
  position: relative;
  flex-shrink: 0;
}

.book-cover :deep(.n-image) {
  width: 100%;
  height: 100%;
}

.book-cover :deep(.n-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.book-card:hover .book-cover :deep(.n-image img) {
  transform: scale(1.05);
}

.book-info {
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 90px;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #333;
  line-height: 1.4;
  max-height: 2.8em;
}

.book-author {
  font-size: 12px;
  color: #666;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating-text {
  font-size: 13px;
  color: #ff9800;
  font-weight: 600;
}

/* 分类卡片 */
.category-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.category-content {
  padding: 24px 16px;
}

.category-icon {
  margin-bottom: 15px;
  transition: transform 0.3s;
}

.category-card:hover .category-icon {
  transform: scale(1.1);
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.category-count {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 系统介绍卡片 */
.system-intro-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
}

.intro-content {
  padding: 20px;
}

.intro-section {
  text-align: center;
  margin-bottom: 40px;
}

.intro-icon {
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.intro-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.intro-text {
  font-size: 15px;
  line-height: 1.8;
  color: #666;
  max-width: 800px;
  margin: 0 auto;
}

.intro-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
}

.feature-item > div {
  flex: 1;
}

.feature-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.feature-item p {
  font-size: 13px;
  color: #999;
  margin: 0;
  line-height: 1.6;
}

/* AI聊天浮窗 */
.ai-chat-float {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
  z-index: 1000;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 4px 24px rgba(102, 126, 234, 0.6);
  }
}

.ai-chat-float:hover {
  transform: scale(1.15);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .book-card {
    width: 180px;
    height: 280px;
  }
  
  .book-cover {
    width: 180px;
    height: 200px;
  }
}

@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .book-card {
    width: 160px;
    height: 260px;
  }
  
  .book-cover {
    width: 160px;
    height: 180px;
  }
  
  .book-title {
    font-size: 13px;
  }
  
  .book-author {
    font-size: 11px;
  }
  
  /* 移动端 AI弹窗适配 */
  .ai-chat-modal-wrapper {
    padding: 5px;
  }
  
  .ai-chat-modal {
    width: 100vw;
    height: 100vh;
    max-width: none;
    max-height: none;
  }
}

/* AI聊天弹窗样式 */
.ai-chat-modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  padding: 0;
}

.ai-chat-modal {
  width: 560px;
  height: 700px;
  max-width: calc(100vw - 0px);
  max-height: 100vh;
  box-shadow: -4px 0 16px rgba(0, 0, 0, 0.15);
  border-radius: 0;
  overflow: hidden;
  background: white;
}

.ai-chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ai-chat-card :deep(.n-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.ai-chat-card :deep(.n-card__content) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.slide-fade-leave-to .ai-chat-modal {
  transform: translateY(20px) scale(0.95);
  opacity: 0;
}

.ai-chat-dialog-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.ai-chat-dialog-content .chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
  min-height: 0;
}

.ai-chat-dialog-content .message-item {
  display: flex;
  margin-bottom: 16px;
  gap: 10px;
}

.ai-chat-dialog-content .message-item.user {
  flex-direction: row-reverse;
}

.ai-chat-dialog-content .message-avatar {
  flex-shrink: 0;
}

.ai-chat-dialog-content .message-content {
  max-width: 75%;
}

.ai-chat-dialog-content .message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
}

.ai-chat-dialog-content .message-item.assistant .message-bubble {
  background: white;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.ai-chat-dialog-content .message-item.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ai-chat-dialog-content .message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

/* AI 消息格式化样式 */
.ai-chat-dialog-content .message-text :deep(.highlight-text) {
  color: #667eea;
  font-weight: 600;
  font-size: 15px;
}

.ai-chat-dialog-content .message-text :deep(.book-title) {
  color: #18a058;
  font-weight: 600;
  background: linear-gradient(135deg, rgba(24, 160, 88, 0.1) 0%, rgba(24, 160, 88, 0.05) 100%);
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin: 0 2px;
}

.ai-chat-dialog-content .message-text :deep(.source-info) {
  color: #999;
  font-size: 12px;
  font-style: italic;
}

.ai-chat-dialog-content .message-text :deep(.section-tag) {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  margin: 8px 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.ai-chat-dialog-content .message-text :deep(.list-item) {
  display: flex;
  align-items: flex-start;
  margin: 8px 0;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  border-left: 3px solid #667eea;
  transition: all 0.3s ease;
}

.ai-chat-dialog-content .message-text :deep(.list-item:hover) {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
}

.ai-chat-dialog-content .message-text :deep(.list-number) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 13px;
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
}

.ai-chat-dialog-content .message-text :deep(.list-content) {
  flex: 1;
  line-height: 1.6;
  color: #333;
  font-size: 14px;
}

.ai-chat-dialog-content .message-time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.ai-chat-dialog-content .welcome-message {
  text-align: center;
  padding: 30px 16px;
}

.ai-chat-dialog-content .welcome-icon {
  font-size: 42px;
  margin-bottom: 16px;
}

.ai-chat-dialog-content .welcome-message h2 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.ai-chat-dialog-content .welcome-message p {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}

.ai-chat-dialog-content .quick-topics {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-chat-dialog-content .quick-questions {
  background: white;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 上下文 Token 统计样式 */
.context-stats {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  padding: 10px 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  border: 1px solid rgba(102, 126, 234, 0.15);
  transition: all 0.3s ease;
}

.context-stats:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.12) 100%);
  border-color: rgba(102, 126, 234, 0.3);
}

.context-stats .stats-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.ai-chat-dialog-content .quick-title {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px 0;
}

/* Header Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  transform: scale(1.1);
  background: rgba(102, 126, 234, 0.1);
}



.new-session-btn:hover {
  color: #52c41a;
}

.close-btn:hover {
  color: #ff6b6b;
}


/* 输入框容器 */
.chat-input-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 24px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.chat-input-container:hover {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.chat-input-container:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 输入框样式 */
.chat-input {
  flex: 1;
}

.ai-chat-dialog-content .chat-input-container :deep(.n-input) {
  background: transparent;
}

.ai-chat-dialog-content .chat-input-container :deep(.n-input .n-input__border),
.ai-chat-dialog-content .chat-input-container :deep(.n-input .n-input__state-border) {
  border: none !important;
}

.ai-chat-dialog-content .chat-input-container :deep(.n-input input) {
  font-size: 14px;
  padding: 0;
  background: transparent;
  color: #333;
}

.ai-chat-dialog-content .chat-input-container :deep(.n-input input::placeholder) {
  color: #9ca3af;
}

.ai-chat-dialog-content .chat-input-container :deep(.n-input--focus) {
  box-shadow: none !important;
}

/* 发送按钮 */
.send-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  transition: all 0.3s ease;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.1);
}

.send-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.ai-chat-dialog-content .typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.ai-chat-dialog-content .typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.ai-chat-dialog-content .typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-chat-dialog-content .typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* 历史会话样式 */
.history-card {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateX(-3px);
}

.history-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.history-content {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.history-item:hover {
  background: rgba(102, 126, 234, 0.05);
  transform: translateX(4px);
}

.history-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.session-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.session-time {
  font-size: 12px;
  color: #999;
}

.history-item-preview {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
