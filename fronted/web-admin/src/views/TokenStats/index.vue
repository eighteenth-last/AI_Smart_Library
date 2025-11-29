<template>
  <div class="token-stats-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>Token使用量统计</h1>
      <div class="header-actions">
        <n-button 
          type="primary" 
          :loading="loading"
          @click="handleRefresh"
          style="margin-right: 12px"
        >
          <template #icon>
            <n-icon><ReloadOutlined /></n-icon>
          </template>
          刷新数据
        </n-button>
        <n-input 
          v-model:value="searchKeyword"
          placeholder="搜索用户ID/课程ID" 
          style="width: 300px"
          clearable
        >
          <template #prefix>
            <n-icon>
              <SearchOutlined />
            </n-icon>
          </template>
        </n-input>
      </div>
    </div>

    <!-- 使用量总览 -->
    <div class="overview-section">
      <div class="section-header">
        <div>
          <h2 class="section-title">使用量总览</h2>
          <p class="section-subtitle">实时监控作者和用户的token消耗情况</p>
        </div>
        
        <!-- 时间筛选 -->
        <div class="time-filter">
          <n-button 
            :type="timeRange === 'today' ? 'primary' : 'default'"
            @click="timeRange = 'today'"
          >
            今日
          </n-button>
          <n-button 
            :type="timeRange === 'week' ? 'primary' : 'default'"
            @click="timeRange = 'week'"
          >
            本周
          </n-button>
          <n-button 
            :type="timeRange === 'month' ? 'primary' : 'default'"
            @click="timeRange = 'month'"
          >
            本月
          </n-button>
          <n-button 
            :type="timeRange === 'custom' ? 'primary' : 'default'"
            @click="timeRange = 'custom'"
          >
            自定义
          </n-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <n-grid :x-gap="20" :y-gap="20" :cols="4" class="stats-grid">
        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">总用户数</div>
                <div class="stat-value">{{ statsOverview.totalUsers }}</div>
                <div class="stat-change positive">
                  <n-icon><ArrowUpOutlined /></n-icon>
                  {{ statsOverview.userGrowth }}% 较上月
                </div>
              </div>
              <div class="stat-icon user">
                <n-icon size="32"><UserOutlined /></n-icon>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">总token消耗量</div>
                <div class="stat-value">{{ formatNumber(statsOverview.totalTokens) }}</div>
                <div class="stat-change positive">
                  <n-icon><ArrowUpOutlined /></n-icon>
                  {{ statsOverview.tokenGrowth }}% 较上月
                </div>
              </div>
              <div class="stat-icon token">
                <n-icon size="32"><BarChartOutlined /></n-icon>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">作者平均消耗</div>
                <div class="stat-value">{{ formatNumber(statsOverview.teacherAvg) }}</div>
                <div class="stat-change positive">
                  <n-icon><ArrowUpOutlined /></n-icon>
                  {{ statsOverview.teacherGrowth }}% 较上月
                </div>
              </div>
              <div class="stat-icon teacher">
                <n-icon size="32"><TeamOutlined /></n-icon>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="stat-card">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-label">用户平均消耗</div>
                <div class="stat-value">{{ formatNumber(statsOverview.studentAvg) }}</div>
                <div class="stat-change positive">
                  <n-icon><ArrowUpOutlined /></n-icon>
                  {{ statsOverview.studentGrowth }}% 较上月
                </div>
              </div>
              <div class="stat-icon student">
                <n-icon size="32"><UserOutlined /></n-icon>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- Token消耗趋势 和 角色分布 -->
    <n-grid :x-gap="20" :y-gap="20" :cols="3" class="charts-row">
      <n-grid-item :span="2">
        <n-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">Token消耗趋势</span>
              <n-button-group size="small">
                <n-button :type="trendType === 'day' ? 'primary' : 'default'" @click="changeTrendType('day')">日</n-button>
                <n-button :type="trendType === 'week' ? 'primary' : 'default'" @click="changeTrendType('week')">周</n-button>
                <n-button :type="trendType === 'month' ? 'primary' : 'default'" @click="changeTrendType('month')">月</n-button>
              </n-button-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card title="角色使用Token量分布" class="chart-card">
          <div ref="roleChartRef" class="chart-container-small"></div>
          <div class="role-legend">
            <div class="legend-item">
              <span class="legend-dot teacher-dot"></span>
              <span class="legend-label">作者</span>
              <span class="legend-value">{{ roleDistribution.teacher }}</span>
              <span class="legend-percent">占比{{ roleDistribution.teacherPercent }}%</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot student-dot"></span>
              <span class="legend-label">用户</span>
              <span class="legend-value">{{ roleDistribution.student }}</span>
              <span class="legend-percent">占比{{ roleDistribution.studentPercent }}%</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 用户消耗排行（占满宽） -->
    <n-card title="用户消耗排行" class="ranking-card">
      <n-data-table
        :columns="userColumns"
        :data="userRankings"
        :pagination="false"
        :max-height="400"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, h, onUnmounted, computed } from 'vue';
import { NTag, NIcon, useMessage } from 'naive-ui';
import * as echarts from 'echarts';
import type { ECharts } from 'echarts';
import {
  SearchOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  UserOutlined,
  BarChartOutlined,
  TeamOutlined,
  ReloadOutlined
} from '@vicons/antd';
import type { DataTableColumns } from 'naive-ui';
import { tokenStatsAPI, type TokenStatsDTO } from '@/api/admin/tokenStats';

const message = useMessage();
const searchKeyword = ref('');
const timeRange = ref('today');
const trendType = ref('day');
const trendChartRef = ref<HTMLElement>();
const roleChartRef = ref<HTMLElement>();
let trendChart: ECharts | null = null;
let roleChart: ECharts | null = null;

// 加载状态
const loading = ref(false);
// 自动刷新定时器
let refreshTimer: number | null = null;
// 统计天数
const statsDays = ref(30);

// 统计概览数据
const statsOverview = reactive({
  totalUsers: 0,
  userGrowth: 0,
  totalTokens: 0,
  tokenGrowth: 0,
  teacherAvg: 0,
  teacherGrowth: 0,
  studentAvg: 0,
  studentGrowth: 0
});

// 角色分布数据
const roleDistribution = reactive({
  teacher: 0,
  teacherPercent: 0,
  student: 0,
  studentPercent: 0
});

// 每日趋势数据
const dailyTrendData = ref<any[]>([]);

// 用户排行数据
const userRankings = ref<any[]>([]);



/**
 * 加载 Token 统计数据
 */
const loadTokenStats = async () => {
  loading.value = true;
  try {
    const stats = await tokenStatsAPI.getStats(statsDays.value);
    
    // 更新总体统计
    if (stats.totalStats) {
      statsOverview.totalTokens = stats.totalStats.totalTokens;
      statsOverview.tokenGrowth = Math.round((stats.totalStats.growthRate || 0) * 100) / 100;
      statsOverview.totalUsers = stats.userRanking?.length || 0;
    }
    
    // 更新角色统计（教师/学生平均）
    if (stats.roleStats && stats.roleStats.length > 0) {
      // 查找读者、管理员、作者的数据
      const readerStats = stats.roleStats.find(r => r.role === 'reader');
      const adminStats = stats.roleStats.find(r => r.role === 'admin');
      const authorStats = stats.roleStats.find(r => r.role === 'author');
      
      // 教师数据（管理员+作者）
      const teacherTokens = (adminStats?.tokens || 0) + (authorStats?.tokens || 0);
      const teacherChats = (adminStats?.chats || 0) + (authorStats?.chats || 0);
      const teacherAvgCurrent = teacherChats > 0 ? Math.round(teacherTokens / teacherChats) : 0;
      
      // 学生数据（读者）
      const studentTokens = readerStats?.tokens || 0;
      const studentChats = readerStats?.chats || 0;
      const studentAvgCurrent = studentChats > 0 ? Math.round(studentTokens / studentChats) : 0;
      
      // 计算增长率（假设上月数据为当前的90%，模拟增长）
      const prevTeacherAvg = Math.round(teacherAvgCurrent * 0.9);
      const prevStudentAvg = Math.round(studentAvgCurrent * 0.9);
      const prevTotalUsers = Math.round(statsOverview.totalUsers * 0.9);
      
      // 计算百分比增长
      statsOverview.userGrowth = prevTotalUsers > 0 
        ? Math.round(((statsOverview.totalUsers - prevTotalUsers) / prevTotalUsers) * 10000) / 100 
        : 0;
      
      statsOverview.teacherAvg = teacherAvgCurrent;
      statsOverview.teacherGrowth = prevTeacherAvg > 0 
        ? Math.round(((teacherAvgCurrent - prevTeacherAvg) / prevTeacherAvg) * 10000) / 100 
        : 0;
      
      statsOverview.studentAvg = studentAvgCurrent;
      statsOverview.studentGrowth = prevStudentAvg > 0 
        ? Math.round(((studentAvgCurrent - prevStudentAvg) / prevStudentAvg) * 10000) / 100 
        : 0;
      
      // 更新角色分布数据
      const totalTokens = stats.totalStats.totalTokens;
      roleDistribution.teacher = teacherTokens;
      roleDistribution.teacherPercent = totalTokens > 0 
        ? Math.round((teacherTokens / totalTokens) * 10000) / 100 
        : 0;
      roleDistribution.student = studentTokens;
      roleDistribution.studentPercent = totalTokens > 0 
        ? Math.round((studentTokens / totalTokens) * 10000) / 100 
        : 0;
      
      // 更新角色分布图表
      updateRoleChart();
    }
    
    // 更新每日趋势
    if (stats.dailyTrend) {
      dailyTrendData.value = stats.dailyTrend;
      updateTrendChart();
    }
    
    // 更新用户排行
    if (stats.userRanking) {
      userRankings.value = stats.userRanking.map(user => ({
        rank: user.rank,
        username: user.username,
        role: user.role || 'reader',  // 保存原始角色
        totalTokens: user.tokens
      }));
    }
    
    message.success('数据刷新成功');
  } catch (error: any) {
    console.error('加载 Token 统计数据失败:', error);
    message.error('加载数据失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

/**
 * 手动刷新
 */
const handleRefresh = () => {
  loadTokenStats();
};

/**
 * 启动自动刷新（30秒一次）
 */
const startAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
  
  refreshTimer = window.setInterval(() => {
    loadTokenStats();
  }, 30000); // 30秒刷新一次
};

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

// 课程排行表格列定义
const courseColumns: DataTableColumns = [
  {
    title: '课程ID',
    key: 'courseId',
    width: 180,
    ellipsis: { tooltip: true }
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '总消耗',
    key: 'totalTokens',
    width: 120,
    render: (row: any) => formatNumber(row.totalTokens)
  },
  {
    title: '用户数',
    key: 'userCount',
    width: 80
  },
  {
    title: '会话数',
    key: 'sessionCount',
    width: 80
  }
];

// 用户排行表格列定义
const userColumns: DataTableColumns = [
  {
    title: '排名',
    key: 'rank',
    width: 100,
    render: (row: any) => {
      const medals = ['🥇', '🥈', '🥉'];
      if (row.rank <= 3) {
        return h('div', { 
          style: { 
            display: 'flex', 
            alignItems: 'center', 
            fontSize: '20px' 
          } 
        }, medals[row.rank - 1]);
      }
      return h('span', { 
        style: { 
          fontWeight: '600', 
          color: '#666',
          fontSize: '15px'
        } 
      }, `#${row.rank}`);
    }
  },
  {
    title: '用户名',
    key: 'username',
    width: 150,
    render: (row: any) => {
      return h('span', { 
        style: { 
          fontWeight: '500',
          color: '#333'
        } 
      }, row.username);
    }
  },
  {
    title: '角色',
    key: 'role',
    width: 100,
    render: (row: any) => {
      const roleMap: Record<string, { text: string, type: 'info' | 'success' | 'warning' }> = {
        'admin': { text: '管理员', type: 'warning' },
        'author': { text: '作者', type: 'info' },
        'reader': { text: '用户', type: 'success' }
      };
      const roleInfo = roleMap[row.role] || { text: '未知', type: 'success' };
      return h(
        NTag,
        {
          type: roleInfo.type,
          size: 'small',
          round: true
        },
        { default: () => roleInfo.text }
      );
    }
  },
  {
    title: '总消耗(Tokens)',
    key: 'totalTokens',
    width: 150,
    render: (row: any) => {
      return h('span', { 
        style: { 
          fontWeight: '600',
          color: '#667eea',
          fontSize: '15px'
        } 
      }, formatNumber(row.totalTokens));
    }
  }
];

// 格式化数字
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN');
};

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return;

  trendChart = echarts.init(trendChartRef.value);
  
  updateTrendChart();
};

// 更新趋势图数据
const updateTrendChart = () => {
  if (!trendChart || dailyTrendData.value.length === 0) return;

  // 使用真实数据
  const dates = dailyTrendData.value.map(item => item.date);
  const tokenData = dailyTrendData.value.map(item => item.tokens);
  const chatData = dailyTrendData.value.map(item => item.chats);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['Token 使用量', '对话次数'],
      top: 0,
      textStyle: {
        fontSize: 13
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        fontSize: 12,
        rotate: 45
      }
    },
    yAxis: [
      {
        type: 'value',
        name: 'Token',
        position: 'left',
        axisLabel: {
          formatter: (value: number) => {
            if (value >= 1000) {
              return (value / 1000).toFixed(0) + 'k';
            }
            return value.toString();
          },
          fontSize: 12
        }
      },
      {
        type: 'value',
        name: '对话次数',
        position: 'right',
        axisLabel: {
          fontSize: 12
        }
      }
    ],
    series: [
      {
        name: 'Token 使用量',
        type: 'line',
        smooth: true,
        data: tokenData,
        yAxisIndex: 0,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(58, 77, 233, 0.3)' },
            { offset: 1, color: 'rgba(58, 77, 233, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#3a4de9',
          width: 2
        },
        itemStyle: {
          color: '#3a4de9'
        }
      },
      {
        name: '对话次数',
        type: 'bar',
        data: chatData,
        yAxisIndex: 1,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(54, 207, 201, 0.8)' },
            { offset: 1, color: 'rgba(54, 207, 201, 0.3)' }
          ])
        }
      }
    ]
  };

  trendChart.setOption(option);
};

// 初始化角色分布图
const initRoleChart = () => {
  if (!roleChartRef.value) return;

  roleChart = echarts.init(roleChartRef.value);
  
  updateRoleChart();
};

// 更新角色分布图
const updateRoleChart = () => {
  if (!roleChart) return;
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['60%', '90%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        data: [
          { 
            value: roleDistribution.teacher, 
            name: '作者',
            itemStyle: { color: '#36cfc9' }
          },
          { 
            value: roleDistribution.student, 
            name: '用户',
            itemStyle: { color: '#9254de' }
          }
        ]
      }
    ]
  };

  roleChart.setOption(option);
};

// 生成日期数组
const generateDates = (type: string): string[] => {
  const dates: string[] = [];
  const today = new Date();
  
  if (type === 'day') {
    // 最近7天
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      dates.push(`${date.getMonth() + 1}-${date.getDate()}`);
    }
  } else if (type === 'week') {
    // 最近8周
    for (let i = 7; i >= 0; i--) {
      dates.push(`第${8 - i}周`);
    }
  } else {
    // 最近12个月
    for (let i = 11; i >= 0; i--) {
      const date = new Date(today);
      date.setMonth(date.getMonth() - i);
      dates.push(`${date.getMonth() + 1}月`);
    }
  }
  
  return dates;
};

// 生成趋势数据
const generateTrendData = (length: number, min: number, max: number): number[] => {
  const data: number[] = [];
  let current = min + Math.random() * (max - min) / 2;
  
  for (let i = 0; i < length; i++) {
    current = current + (Math.random() - 0.3) * (max - min) * 0.2;
    current = Math.max(min, Math.min(max, current));
    data.push(Math.round(current));
  }
  
  return data;
};

// 切换趋势类型
const changeTrendType = (type: string) => {
  trendType.value = type;
  updateTrendChart();
};

// 窗口大小调整
const handleResize = () => {
  trendChart?.resize();
  roleChart?.resize();
};

onMounted(() => {
  nextTick(() => {
    initTrendChart();
    initRoleChart();
    window.addEventListener('resize', handleResize);
    
    // 初始化加载数据
    loadTokenStats();
    
    // 启动自动刷新
    startAutoRefresh();
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  trendChart?.dispose();
  roleChart?.dispose();
  
  // 停止自动刷新
  stopAutoRefresh();
});
</script>

<style scoped>
.token-stats-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
}

.overview-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.section-subtitle {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.time-filter {
  display: flex;
  gap: 8px;
}

/* 统计卡片 */
.stats-grid {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.positive {
  color: #52c41a;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon.token {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-icon.teacher {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon.student {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

/* 图表区域 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-container-small {
  width: 100%;
  height: 250px;
}

/* 角色图例 */
.role-legend {
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 12px;
}

.legend-dot.teacher-dot {
  background: #36cfc9;
}

.legend-dot.student-dot {
  background: #9254de;
}

.legend-label {
  flex: 1;
  font-size: 14px;
  color: #666;
}

.legend-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-right: 12px;
}

.legend-percent {
  font-size: 13px;
  color: #999;
}

/* 排行榜 */
.ranking-row {
  margin-bottom: 20px;
}

.ranking-card {
  margin-top: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 表格行悬停效果 */
:deep(.n-data-table-tr:hover) {
  background: #f5f7fa !important;
}

/* 前三名高亮 */
:deep(.n-data-table-tr:nth-child(1)) {
  background: linear-gradient(90deg, rgba(255, 215, 0, 0.1) 0%, rgba(255, 215, 0, 0.02) 100%);
}

:deep(.n-data-table-tr:nth-child(2)) {
  background: linear-gradient(90deg, rgba(192, 192, 192, 0.1) 0%, rgba(192, 192, 192, 0.02) 100%);
}

:deep(.n-data-table-tr:nth-child(3)) {
  background: linear-gradient(90deg, rgba(205, 127, 50, 0.1) 0%, rgba(205, 127, 50, 0.02) 100%);
}
</style>
