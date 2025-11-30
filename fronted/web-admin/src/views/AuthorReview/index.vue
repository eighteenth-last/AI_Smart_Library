<template>
  <div class="author-review-container">
    <n-card title="作者审核" :bordered="false">
      <template #header-extra>
        <n-space>
          <n-button type="primary" @click="loadAuthors">
            <template #icon>
              <n-icon><ReloadOutlined /></n-icon>
            </template>
            刷新
          </n-button>
        </n-space>
      </template>

      <!-- 筛选器 -->
      <n-space class="filter-section" style="margin-bottom: 16px;">
        <n-select
          v-model:value="filterStatus"
          :options="statusOptions"
          placeholder="筛选状态"
          clearable
          style="width: 150px;"
          @update:value="handleFilter"
        />
      </n-space>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="authorList"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
      />
    </n-card>

    <!-- 审核弹窗 -->
    <n-modal
      v-model:show="showReviewModal"
      preset="card"
      :title="getModalTitle()"
      style="width: 700px;"
      :bordered="false"
      :segmented="{
        content: true,
        footer: 'soft'
      }"
    >
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px;">
          <n-icon size="24" color="#667eea">
            <EyeOutlined />
          </n-icon>
          <span style="font-size: 18px; font-weight: 600;">{{ getModalTitle() }}</span>
        </div>
      </template>

      <n-spin :show="!currentAuthor">
        <n-descriptions 
          v-if="currentAuthor" 
          :column="2" 
          bordered 
          label-placement="left"
          :label-style="{ fontWeight: '600', background: '#f5f7fa', width: '120px' }"
          :content-style="{ background: '#ffffff' }"
        >
          <n-descriptions-item label="申请人" :span="2">
            <div style="display: flex; align-items: center; gap: 8px;">
              <n-icon size="18" color="#667eea">
                <UserOutlined />
              </n-icon>
              <n-tag type="info">{{ currentAuthor.username || '-' }}</n-tag>
            </div>
          </n-descriptions-item>
          
          <n-descriptions-item label="作者名称" :span="2">
            <div style="display: flex; align-items: center; gap: 8px;">
              <n-icon size="18" color="#f59e0b">
                <StarOutlined />
              </n-icon>
              <strong style="font-size: 16px; color: #333;">{{ currentAuthor.name }}</strong>
            </div>
          </n-descriptions-item>
          
          <n-descriptions-item label="国家">
            <div style="display: flex; align-items: center; gap: 6px;">
              <n-icon size="16" color="#10b981">
                <GlobalOutlined />
              </n-icon>
              {{ currentAuthor.country || '-' }}
            </div>
          </n-descriptions-item>
          
          <n-descriptions-item label="出生年份">
            <div style="display: flex; align-items: center; gap: 6px;">
              <n-icon size="16" color="#3b82f6">
                <CalendarOutlined />
              </n-icon>
              {{ currentAuthor.birthYear || '-' }}
            </div>
          </n-descriptions-item>
          
          <n-descriptions-item label="个人简介" :span="2">
            <n-card size="small" :bordered="false" style="background: #f9fafb; margin-top: 4px;">
              <div style="white-space: pre-wrap; line-height: 1.8; color: #555; max-height: 200px; overflow-y: auto;">
                {{ currentAuthor.intro || '-' }}
              </div>
            </n-card>
          </n-descriptions-item>
          
          <n-descriptions-item label="申请时间" :span="2">
            <div style="display: flex; align-items: center; gap: 6px;">
              <n-icon size="16" color="#8b5cf6">
                <ClockCircleOutlined />
              </n-icon>
              <span style="color: #666;">{{ formatDate(currentAuthor.createdAt) }}</span>
            </div>
          </n-descriptions-item>
        </n-descriptions>
      </n-spin>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">

          <!-- 只有待审核状态才显示审核按钮 -->
          <template v-if="currentAuthor && currentAuthor.authStatus === 0">
            <n-button 
              type="error" 
              @click="handleReject" 
              :loading="submitting"
              size="large"
              ghost
            >
              <template #icon>
                <n-icon><CloseOutlined /></n-icon>
              </template>
              拒绝申请
            </n-button>
            <n-button 
              type="success" 
              @click="handleApprove" 
              :loading="submitting"
              size="large"
            >
              <template #icon>
                <n-icon><CheckOutlined /></n-icon>
              </template>
              通过审核
            </n-button>
          </template>
          
          <!-- 已审核状态显示审核结果 -->
          <template v-else-if="currentAuthor">
            <n-alert 
              v-if="currentAuthor.authStatus === 1" 
              type="success" 
              style="flex: 1;"
            >
              <template #icon>
                <n-icon><CheckOutlined /></n-icon>
              </template>
              该作者已通过审核
            </n-alert>
            <n-alert 
              v-else-if="currentAuthor.authStatus === 2" 
              type="error" 
              style="flex: 1;"
            >
              <template #icon>
                <n-icon><CloseOutlined /></n-icon>
              </template>
              该作者申请已被拒绝
            </n-alert>
          </template>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { NButton, NTag, NIcon, NSpace, NAlert, useMessage, type DataTableColumns } from 'naive-ui'
import { 
  CheckOutlined, 
  CloseOutlined, 
  EyeOutlined, 
  ReloadOutlined,
  UserOutlined,
  StarOutlined,
  GlobalOutlined,
  CalendarOutlined,
  ClockCircleOutlined
} from '@vicons/antd'
import { authorAPI, type Author } from '@/api/admin/author'

const message = useMessage()

// 状态
const loading = ref(false)
const submitting = ref(false)
const authorList = ref<Author[]>([])
const showReviewModal = ref(false)
const currentAuthor = ref<Author | null>(null)
const filterStatus = ref<number | null>(null)

// 状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 }
]

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onChange: (page: number) => {
    pagination.value.page = page
    loadAuthors()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadAuthors()
  }
})

// 表格列
const columns: DataTableColumns = [
  {
    title: 'ID',
    key: 'authorId',
    width: 80
  },
  {
    title: '作者名称',
    key: 'name',
    width: 150
  },
  {
    title: '申请用户',
    key: 'username',
    width: 120,
    render: (row: any) => row.username || '-'
  },
  {
    title: '国家',
    key: 'country',
    width: 100,
    render: (row: any) => row.country || '-'
  },
  {
    title: '出生年份',
    key: 'birthYear',
    width: 100,
    render: (row: any) => row.birthYear || '-'
  },
  {
    title: '简介',
    key: 'intro',
    ellipsis: {
      tooltip: true
    },
    render: (row: any) => row.intro?.substring(0, 50) + '...' || '-'
  },
  {
    title: '状态',
    key: 'authStatus',
    width: 100,
    render: (row: any) => {
      const statusMap: Record<number, { type: any, text: string }> = {
        0: { type: 'warning', text: '待审核' },
        1: { type: 'success', text: '已通过' },
        2: { type: 'error', text: '已拒绝' }
      }
      const status = statusMap[row.authStatus] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  {
    title: '申请时间',
    key: 'createdAt',
    width: 180,
    render: (row: any) => formatDate(row.createdAt)
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (row: any) => {
      return h(NSpace, {}, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              text: true,
              onClick: () => handleViewDetail(row)
            },
            {
              icon: () => h(NIcon, {}, { default: () => h(EyeOutlined) }),
              default: () => '查看'
            }
          )
        ]
      })
    }
  }
]

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 获取弹窗标题
const getModalTitle = () => {
  if (!currentAuthor.value) return '作者详情'
  
  const statusMap: Record<number, string> = {
    0: '审核作者申请',
    1: '作者详情（已通过）',
    2: '作者详情（已拒绝）'
  }
  
  return statusMap[currentAuthor.value.authStatus] || '作者详情'
}

// 加载作者列表
const loadAuthors = async () => {
  loading.value = true
  try {
    const response = await authorAPI.getAuthorsForAdmin({
      page: pagination.value.page,
      size: pagination.value.pageSize,
      authStatus: filterStatus.value ?? undefined
    })
    
    authorList.value = response.records
    pagination.value.itemCount = response.total
  } catch (error) {
    console.error('Load authors error:', error)
    message.error('加载作者列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  pagination.value.page = 1
  loadAuthors()
}

// 查看详情
const handleViewDetail = (author: any) => {
  currentAuthor.value = author
  showReviewModal.value = true
}

// 通过审核
const handleApprove = async () => {
  if (!currentAuthor.value) return
  
  submitting.value = true
  try {
    await authorAPI.reviewAuthor(currentAuthor.value.authorId, 1)
    
    message.success('审核通过')
    showReviewModal.value = false
    loadAuthors()
  } catch (error) {
    console.error('Approve error:', error)
    message.error('审核失败')
  } finally {
    submitting.value = false
  }
}

// 拒绝审核
const handleReject = async () => {
  if (!currentAuthor.value) return
  
  submitting.value = true
  try {
    await authorAPI.reviewAuthor(currentAuthor.value.authorId, 2)
    
    message.success('已拒绝申请')
    showReviewModal.value = false
    loadAuthors()
  } catch (error) {
    console.error('Reject error:', error)
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadAuthors()
})
</script>

<style scoped>
.author-review-container {
  padding: 24px;
}

.filter-section {
  margin-bottom: 16px;
}
</style>
