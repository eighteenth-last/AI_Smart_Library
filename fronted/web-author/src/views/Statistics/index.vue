<template>
  <div class="statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">📊</div>
          <div class="header-info">
            <h2 class="header-title">数据统计</h2>
            <p class="header-subtitle">查看作品数据与借阅趋势</p>
          </div>
        </div>
        <div class="header-right">
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            placeholder="选择日期范围"
            clearable
            @update:value="handleDateChange"
            style="width: 280px;"
          />
          <n-button type="primary" @click="loadStatistics" :loading="loading">
            <template #icon>
              <n-icon><SearchOutline /></n-icon>
            </template>
            刷新数据
          </n-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片网格 -->
    <div class="stats-container">
      <n-grid :cols="4" :x-gap="20" :y-gap="20">
        <!-- 作品总数 -->
        <n-grid-item>
          <div class="stat-card books">
            <div class="stat-background">
              <div class="stat-icon">📚</div>
            </div>
            <div class="stat-content">
              <div class="stat-label">作品总数</div>
              <div class="stat-value">{{ statistics.totalBooks }}</div>
              <div class="stat-trend">+0% 较上月</div>
            </div>
          </div>
        </n-grid-item>

        <!-- 总借阅次数 -->
        <n-grid-item>
          <div class="stat-card borrows">
            <div class="stat-background">
              <div class="stat-icon">📖</div>
            </div>
            <div class="stat-content">
              <div class="stat-label">总借阅次数</div>
              <div class="stat-value">{{ statistics.totalBorrows }}</div>
              <div class="stat-trend positive">+12% 较上月</div>
            </div>
          </div>
        </n-grid-item>

        <!-- 总评价数 -->
        <n-grid-item>
          <div class="stat-card reviews">
            <div class="stat-background">
              <div class="stat-icon">⭐</div>
            </div>
            <div class="stat-content">
              <div class="stat-label">总评价数</div>
              <div class="stat-value">{{ statistics.totalReviews }}</div>
              <div class="stat-trend positive">+8% 较上月</div>
            </div>
          </div>
        </n-grid-item>

        <!-- 总收藏数 -->
        <n-grid-item>
          <div class="stat-card favorites">
            <div class="stat-background">
              <div class="stat-icon">❤️</div>
            </div>
            <div class="stat-content">
              <div class="stat-label">总收藏数</div>
              <div class="stat-value">{{ statistics.totalFavorites }}</div>
              <div class="stat-trend positive">+5% 较上月</div>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 详细统计表格 -->
    <div class="table-container">
      <n-card :bordered="false" class="table-card">
        <template #header>
          <div class="table-header">
            <div class="table-title">
              <n-icon size="20" color="#667eea">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M3,3H11V11H3V3M13,3H21V11H13V3M3,13H11V21H3V13M13,13H21V21H13V13Z"/>
                </svg>
              </n-icon>
              <span>作品数据统计</span>
            </div>
          </div>
        </template>
        <n-spin :show="loading">
          <n-data-table
            :columns="columns"
            :data="bookStats"
            :pagination="pagination"
            :loading="loading"
            :bordered="false"
            striped
          />
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import {
  NCard,
  NGrid,
  NGridItem,
  NDatePicker,
  NButton,
  NSpace,
  NDataTable,
  NSpin,
  NIcon,
  useMessage
} from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getAuthorStats, type AuthorStats } from '@/api/author/stats'
import { getAuthorBooks } from '@/api/author/books'

const message = useMessage()

// 统计数据
const statistics = ref<AuthorStats>({
  totalBooks: 0,
  totalBorrows: 0,
  totalReviews: 0,
  totalFavorites: 0,
  averageRating: 0,
  pendingQuestions: 0,
  topBooks: [],
  monthlyTrends: []
})

// 日期范围
const dateRange = ref<[number, number] | null>(null)

// 表格数据
const bookStats = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage: (page: number) => {
    pagination.value.page = page
    loadBookStats()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadBookStats()
  }
})

// 表格列配置
const columns = ref([
  {
    title: '书名',
    key: 'title',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: 'ISBN',
    key: 'isbn',
    width: 150
  },
  {
    title: '借阅次数',
    key: 'borrowCount',
    width: 120,
    sorter: (a: any, b: any) => a.borrowCount - b.borrowCount
  },
  {
    title: '平均评分',
    key: 'averageRating',
    width: 120,
    render: (row: any) => {
      return h('span', row.averageRating ? row.averageRating.toFixed(1) : '-')
    }
  },
  {
    title: '总库存',
    key: 'totalStock',
    width: 100
  },
  {
    title: '剩余库存',
    key: 'availableStock',
    width: 100
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const statusMap: Record<number, string> = {
        0: '下架',
        1: '上架'
      }
      return h('span', {
        style: {
          color: row.status === 1 ? '#10b981' : '#ef4444',
          fontWeight: '500'
        }
      }, statusMap[row.status] || '未知')
    }
  }
])

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    // 调用后端API获取统计数据
    const stats = await getAuthorStats()
    statistics.value = stats
    
    // 加载作品统计列表
    await loadBookStats()
    
    message.success('统计数据加载成功')
  } catch (error: any) {
    console.error('Load statistics error:', error)
    message.error(error.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 加载作品统计列表
const loadBookStats = async () => {
  try {
    // 调用后端API获取作品列表
    const books = await getAuthorBooks()
    bookStats.value = books
    pagination.value.itemCount = books.length
  } catch (error: any) {
    console.error('Load book stats error:', error)
    message.error(error.message || '加载作品统计失败')
  }
}

// 日期范围变化
const handleDateChange = () => {
  loadStatistics()
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px 40px;
  margin-bottom: 24px;
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
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
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
  gap: 12px;
}

/* 统计卡片容器 */
.stats-container {
  padding: 0 24px 24px 24px;
}

/* 统计卡片 */
.stat-card {
  position: relative;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  cursor: pointer;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0;
  transition: opacity 0.4s;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, transparent 100%);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-card:hover::before {
  opacity: 1;
}

/* 卡片颜色 */
.stat-card.books {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.borrows {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card.reviews {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card.favorites {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

/* 背景图标 */
.stat-background {
  position: absolute;
  right: -20px;
  top: -20px;
  opacity: 0.15;
}

.stat-background .stat-icon {
  font-size: 120px;
  transform: rotate(-15deg);
}

/* 内容区域 */
.stat-content {
  position: relative;
  z-index: 1;
  flex: 1;
}

.stat-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.stat-trend {
  font-size: 13px;
  opacity: 0.8;
  font-weight: 500;
}

.stat-trend.positive {
  color: rgba(255, 255, 255, 0.95);
}

/* 表格容器 */
.table-container {
  padding: 0 24px 24px 24px;
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* 表格样式 */
.statistics-container :deep(.n-data-table) {
  background: transparent;
}

.statistics-container :deep(.n-data-table-th) {
  background: #f9fafb;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 12px;
  border-bottom: 2px solid #e5e7eb;
}

.statistics-container :deep(.n-data-table-td) {
  border-bottom: 1px solid #f3f4f6;
  padding: 16px 12px;
  color: #1f2937;
}

.statistics-container :deep(.n-data-table-tr:hover) {
  background: linear-gradient(90deg, #f0fdf4 0%, #ffffff 100%) !important;
}
</style>
