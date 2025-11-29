<template>
  <div class="dashboard-container">
    <div class="stats-section">
      <n-grid :cols="4" :x-gap="20" :y-gap="20">
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6f7ff; color: #1890ff">
                <n-icon size="30"><user-outlined /></n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">用户总数</div>
                <div class="stat-value">{{ stats.totalUsers.toLocaleString() }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f6ffed; color: #52c41a">
                <n-icon size="30"><book-outlined /></n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">图书总数</div>
                <div class="stat-value">{{ stats.totalBooks.toLocaleString() }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff7e6; color: #fa8c16">
                <n-icon size="30"><swap-outlined /></n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">借阅总数</div>
                <div class="stat-value">{{ stats.totalBorrows.toLocaleString() }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff1f0; color: #f5222d">
                <n-icon size="30"><exclamation-circle-outlined /></n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">逾期数量</div>
                <div class="stat-value">{{ stats.overdueCount.toLocaleString() }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <div class="charts-section">
      <n-grid :cols="2" :x-gap="20" :y-gap="20">
        <n-grid-item>
          <n-card title="借阅趋势" size="medium">
            <div ref="borrowTrendChart" style="height: 300px"></div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card title="热门图书 TOP10" size="medium">
            <div class="hot-books-list">
              <div class="hot-book-item" v-for="(book, index) in hotBooks" :key="book.bookId">
                <div class="book-rank">{{ index + 1 }}</div>
                <div class="book-info">
                  <div class="book-title">{{ book.title }}</div>
                  <div class="book-author">{{ book.authorName }}</div>
                </div>
                <div class="book-count">{{ book.borrowCount }} 次</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <div class="recent-section">
      <n-card title="最近操作" size="medium">
        <n-timeline v-if="recentActivities.length > 0">
          <n-timeline-item
            v-for="item in recentActivities"
            :key="item.id"
            :type="item.type"
            :title="item.title"
            :content="item.content"
            :time="item.time"
          />
        </n-timeline>
        <n-empty v-else description="暂无最近操作记录" />
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useMessage } from 'naive-ui';
import * as echarts from 'echarts';
import {
  UserOutlined,
  BookOutlined,
  SwapOutlined,
  ExclamationCircleOutlined
} from '@vicons/antd';
import { dashboardAPI } from '@/api/admin/dashboard';

const message = useMessage();
const borrowTrendChart = ref<HTMLElement>();
const loading = ref(false);

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalBooks: 0,
  totalBorrows: 0,
  overdueCount: 0
});

const hotBooks = ref<any[]>([]);

const recentActivities = ref<any[]>([]);

const borrowTrendData = ref<any[]>([]);

// 加载统计数据
const loadOverviewData = async () => {
  try {
    const data = await dashboardAPI.getOverview();
    stats.value = {
      totalUsers: data.totalUsers || 0,
      totalBooks: data.totalBooks || 0,
      totalBorrows: data.totalBorrows || 0,
      overdueCount: data.overdueCount || 0
    };
  } catch (error) {
    console.error('加载统计数据失败', error);
    message.error('加载统计数据失败');
  }
};

// 加载热门图书
const loadHotBooks = async () => {
  try {
    const data = await dashboardAPI.getHotBooks(10);
    hotBooks.value = data || [];
  } catch (error) {
    console.error('加载热门图书失败', error);
    message.error('加载热门图书失败');
  }
};

// 加载借阅趋势数据
const loadBorrowTrends = async () => {
  try {
    const data = await dashboardAPI.getBorrowTrends('month', 12);
    borrowTrendData.value = data || [];
    initBorrowTrendChart();
  } catch (error) {
    console.error('加载借阅趋势失败', error);
    message.error('加载借阅趋势失败');
  }
};

// 加载最近操作
const loadRecentActivities = async () => {
  try {
    const data = await dashboardAPI.getRecentActivities(10);
    recentActivities.value = data || [];
  } catch (error) {
    console.error('加载最近操作失败', error);
    message.error('加载最近操作失败');
  }
};

const initBorrowTrendChart = () => {
  if (!borrowTrendChart.value) return;
  
  const chart = echarts.init(borrowTrendChart.value);
  
  // 从 API 数据中提取
  const periods = borrowTrendData.value.map(item => item.period);
  const borrowCounts = borrowTrendData.value.map(item => item.borrowCount);
  const returnCounts = borrowTrendData.value.map(item => item.returnCount);
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['借阅量', '归还量']
    },
    xAxis: {
      type: 'category',
      data: periods.length > 0 ? periods : ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '借阅量',
        type: 'line',
        data: borrowCounts,
        smooth: true,
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '归还量',
        type: 'line',
        data: returnCounts,
        smooth: true,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  };
  
  chart.setOption(option);
};

onMounted(async () => {
  loading.value = true;
  try {
    await Promise.all([
      loadOverviewData(),
      loadHotBooks(),
      loadBorrowTrends(),
      loadRecentActivities()
    ]);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stats-section {
  margin-bottom: 30px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.charts-section {
  margin-bottom: 30px;
}

.hot-books-list {
  max-height: 300px;
  overflow-y: auto;
}

.hot-book-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.hot-book-item:last-child {
  border-bottom: none;
}

.book-rank {
  width: 30px;
  height: 30px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.book-author {
  font-size: 12px;
  color: #666;
}

.book-count {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.recent-section {
  margin-bottom: 30px;
}
</style>
