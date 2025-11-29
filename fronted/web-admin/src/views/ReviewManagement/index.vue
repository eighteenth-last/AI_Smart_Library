<template>
  <div class="review-management">
    <n-space vertical :size="16">
      <!-- 统计卡片 -->
      <n-grid cols="1 s:2 m:4 l:6" responsive="screen" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="总评价" :value="stats.total">
              <template #suffix>
                <span class="stat-suffix">条</span>
              </template>
            </n-statistic>
            <div class="stat-icon">💬</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card star-5">
            <n-statistic label="5★" :value="stats.fiveStar" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card star-4">
            <n-statistic label="4★" :value="stats.fourStar" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card star-3">
            <n-statistic label="3★" :value="stats.threeStar" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card star-2">
            <n-statistic label="2★" :value="stats.twoStar" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card star-1">
            <n-statistic label="1★" :value="stats.oneStar" />
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 评价列表 -->
      <n-card title="评价管理" :bordered="false">
        <template #header-extra>
          <n-space>
            <n-select
              v-model:value="filterRating"
              :options="ratingOptions"
              placeholder="筛选评分"
              clearable
              style="width: 120px"
              @update:value="handleSearch"
            />
            <n-input
              v-model:value="filterKeyword"
              placeholder="搜索用户名或图书名"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <n-button @click="handleReset">重置</n-button>
            <n-button type="primary" @click="loadData">刷新</n-button>
          </n-space>
        </template>

        <n-data-table
          :columns="columns"
          :data="reviewList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: Review) => row.reviewId"
          :striped="true"
          :bordered="false"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted, reactive } from 'vue'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NTag, NImage, NSpace, NRate, useMessage, useDialog } from 'naive-ui'
import { reviewAPI, type Review, type ReviewStats } from '@/api/admin/reviews'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const reviewList = ref<Review[]>([])
const filterRating = ref<number | null>(null)
const filterKeyword = ref('')

// 获取封面完整URL
const getCoverUrl = (url?: string) => {
  if (!url) return '/book-placeholder.png';
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

const stats = reactive<ReviewStats>({
  total: 0,
  fiveStar: 0,
  fourStar: 0,
  threeStar: 0,
  twoStar: 0,
  oneStar: 0,
  avgRating: 0
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const ratingOptions = [
  { label: '5星', value: 5 },
  { label: '4星', value: 4 },
  { label: '3星', value: 3 },
  { label: '2星', value: 2 },
  { label: '1星', value: 1 }
]

// 表格列定义
const columns: DataTableColumns<Review> = [
  {
    title: 'ID',
    key: 'reviewId',
    width: 70,
    align: 'center'
  },
  {
    title: '用户',
    key: 'username',
    width: 120
  },
  {
    title: '图书信息',
    key: 'bookTitle',
    minWidth: 250,
    render: (row) => {
      return h(NSpace, { align: 'center', size: 12 }, {
        default: () => [
          h(NImage, {
            width: 50,
            height: 70,
            src: getCoverUrl(row.coverUrl),
            fallbackSrc: '/book-placeholder.png',
            style: 'border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1)',
            objectFit: 'cover'
          }),
          h('div', { style: 'flex: 1; min-width: 0' }, [
            h('div', {
              style: 'font-weight: 500; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap'
            }, row.bookTitle),
            h('div', {
              style: 'font-size: 12px; color: #999; margin-top: 4px'
            }, `ID: ${row.bookId}`)
          ])
        ]
      })
    }
  },
  {
    title: '评分',
    key: 'rating',
    width: 150,
    align: 'center',
    render: (row) => h(NRate, {
      value: row.rating,
      readonly: true,
      size: 'small'
    })
  },
  {
    title: '评价内容',
    key: 'content',
    minWidth: 300,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '评价时间',
    key: 'createdAt',
    width: 180,
    render: (row) => dayjs(row.createdAt).format('YYYY-MM-DD HH:mm')
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    align: 'center',
    render: (row) => h(NSpace, { justify: 'center' }, {
      default: () => [
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row)
        }, { default: () => '删除' })
      ]
    })
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await reviewAPI.getList({
      rating: filterRating.value || undefined,
      page: pagination.page,
      size: pagination.pageSize
    })
    reviewList.value = data.records
    pagination.itemCount = data.total
  } catch (error: any) {
    message.error(error.message || '加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const data = await reviewAPI.getStats()
    Object.assign(stats, data)
  } catch (error: any) {
    message.error(error.message || '加载统计数据失败')
  }
}

// 删除评价
const handleDelete = (row: Review) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户"${row.username}"对《${row.bookTitle}》的评价吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await reviewAPI.deleteReview(row.reviewId)
        message.success('删除成功')
        await loadData()
        await loadStats()
      } catch (error: any) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterRating.value = null
  filterKeyword.value = ''
  pagination.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.review-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.stat-card {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card .stat-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  opacity: 0.2;
}

.stat-card.star-5 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
}

.stat-card.star-4 {
  background: linear-gradient(135deg, #4ade80 0%, #86efac 100%);
}

.stat-card.star-3 {
  background: linear-gradient(135deg, #60a5fa 0%, #93c5fd 100%);
}

.stat-card.star-2 {
  background: linear-gradient(135deg, #fb923c 0%, #fdba74 100%);
}

.stat-card.star-1 {
  background: linear-gradient(135deg, #f87171 0%, #fca5a5 100%);
}

.stat-suffix {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}
</style>
