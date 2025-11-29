<template>
  <div class="author-dashboard">
    <div class="dashboard-header">
      <h1 class="page-title">作者工作台</h1>
      <p class="page-subtitle">查看您的作品数据统计</p>
    </div>

    <n-spin :show="loading">
      <!-- 数据卡片区域 -->
      <n-grid :cols="4" :x-gap="20" :y-gap="20" class="stats-cards">
        <n-grid-item>
          <n-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-icon book-icon">
                <n-icon size="36" color="#ffffff">
                  <book-outline />
                </n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">作品总数</div>
                <div class="stat-value">{{ stats.totalBooks }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-icon borrow-icon">
                <n-icon size="36" color="#ffffff">
                  <swap-horizontal-outline />
                </n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">总借阅量</div>
                <div class="stat-value">{{ stats.totalBorrows }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-icon rating-icon">
                <n-icon size="36" color="#ffffff">
                  <star-outline />
                </n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">平均评分</div>
                <div class="stat-value">{{ stats.averageRating.toFixed(1) }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-icon question-icon">
                <n-icon size="36" color="#ffffff">
                  <help-circle-outline />
                </n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">待回答问题</div>
                <div class="stat-value">{{ stats.pendingQuestions }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 图表区域 -->
      <n-grid :cols="2" :x-gap="20" :y-gap="20" class="charts-section">
        <!-- 系统通知 -->
        <n-grid-item>
          <n-card title="系统通知" class="chart-card">
            <div class="notification-list">
              <div 
                v-for="notification in notifications" 
                :key="notification.notificationId"
                class="notification-item"
                :class="{ 'unread': notification.isRead === 0 }"
              >
                <div class="notification-icon">
                  <n-icon size="20" :color="getNotificationColor(notification.type)">
                    <notifications-outline v-if="notification.type === 'system_announcement'" />
                    <time-outline v-else-if="notification.type === 'borrow_due'" />
                    <warning-outline v-else />
                  </n-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title">{{ notification.title }}</span>
                    <n-tag :type="getNotificationTagType(notification.type)" size="small">
                      {{ notification.typeName }}
                    </n-tag>
                  </div>
                  <div class="notification-text">{{ notification.content }}</div>
                  <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
                </div>
              </div>
              <n-empty 
                v-if="notifications.length === 0" 
                description="暂无通知"
                style="margin: 40px 0"
              />
            </div>
          </n-card>
        </n-grid-item>

        <!-- 作品借阅排行TOP5 -->
        <n-grid-item>
          <n-card title="作品借阅排行 TOP5" class="chart-card">
            <div class="top-books-list">
              <div 
                v-for="(book, index) in stats.topBooks" 
                :key="book.bookId"
                class="book-item"
              >
                <div class="book-rank" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="book-info">
                  <div class="book-title">{{ book.title }}</div>
                  <div class="book-stats">
                    <span class="borrow-count">{{ book.borrowCount }} 次借阅</span>
                    <n-rate 
                      :value="book.averageRating" 
                      readonly 
                      size="small" 
                      :count="5"
                      allow-half
                    />
                    <span class="rating-score">{{ book.averageRating.toFixed(1) }}</span>
                  </div>
                </div>
              </div>
              <n-empty 
                v-if="stats.topBooks.length === 0" 
                description="暂无数据"
                style="margin: 40px 0"
              />
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useMessage } from 'naive-ui';
import { BookOutline, SwapHorizontalOutline, StarOutline, HelpCircleOutline, NotificationsOutline, TimeOutline, WarningOutline } from '@vicons/ionicons5';
import { getAuthorStats, type AuthorStats } from '@/api/author/stats';
import request from '@/utils/request';

const message = useMessage();
const loading = ref(false);

interface Notification {
  notificationId: number;
  title: string;
  content: string;
  type: string;
  typeName: string;
  isRead: number;
  createdAt: string;
}

const notifications = ref<Notification[]>([]);

const stats = ref<AuthorStats>({
  totalBooks: 0,
  totalBorrows: 0,
  averageRating: 0,
  totalReviews: 0,
  pendingQuestions: 0,
  topBooks: [],
  monthlyTrends: []
});

// 加载数据
const loadStats = async () => {
  loading.value = true;
  try {
    const data = await getAuthorStats();
    // 确保所有字段都有默认值
    stats.value = {
      totalBooks: data.totalBooks || 0,
      totalBorrows: data.totalBorrows || 0,
      averageRating: data.averageRating || 0,
      totalReviews: data.totalReviews || 0,
      pendingQuestions: data.pendingQuestions || 0,
      topBooks: data.topBooks || [],
      monthlyTrends: data.monthlyTrends || []
    };
    
    // 加载系统通知
    await loadNotifications();
  } catch (error: any) {
    message.error('加载统计数据失败：' + (error.message || '未知错误'));
    console.error('加载统计数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载系统通知
const loadNotifications = async () => {
  try {
    const response = await request.get('/notifications/announcements', {
      params: {
        page: 1,
        size: 5
      }
    });
    
    notifications.value = response.records || [];
  } catch (error: any) {
    console.error('加载系统通知失败:', error);
  }
};

// 获取通知颜色
const getNotificationColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'system_announcement': '#667eea',
    'borrow_due': '#fd9644',
    'overdue': '#fc5c65'
  };
  return colorMap[type] || '#667eea';
};

// 获取通知标签类型
const getNotificationTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    'system_announcement': 'info',
    'borrow_due': 'warning',
    'overdue': 'error'
  };
  return typeMap[type] || 'info';
};

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit' 
  });
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.author-dashboard {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 24px;
}

.dashboard-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
  transition: all 0.2s ease;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.stat-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.book-icon {
  background: #3b82f6;
}

.borrow-icon {
  background: #10b981;
}

.rating-icon {
  background: #f59e0b;
}

.favorite-icon {
  background: #ec4899;
}

.question-icon {
  background: #ef4444;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

/* 图表区域 */
.charts-section {
  margin-top: 20px;
}

.chart-card {
  height: 400px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

/* 系统通知列表 */
.notification-list {
  padding: 8px 0;
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
}

.notification-item.unread {
  background: #eff6ff;
  border-left-color: #3b82f6;
}

.notification-item:hover {
  background: #f3f4f6;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.notification-text {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
}

/* 作品排行列表 */
.top-books-list {
  padding: 8px 0;
}

.book-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f9fafb;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.book-item:hover {
  background: #f3f4f6;
}

.book-rank {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.rank-1 {
  background: #f59e0b;
}

.rank-2 {
  background: #9ca3af;
}

.rank-3 {
  background: #ea580c;
}

.rank-4, .rank-5 {
  background: #3b82f6;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.borrow-count {
  color: #10b981;
  font-weight: 600;
}

.rating-score {
  color: #f59e0b;
  font-weight: 600;
  margin-left: -8px;
}
</style>
