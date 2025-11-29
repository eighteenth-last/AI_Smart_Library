<template>
  <div class="borrow-management">
    <n-space vertical :size="16">
      <!-- 统计卡片 -->
      <n-grid cols="1 s:2 m:4" responsive="screen" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="借阅中" :value="stats.borrowed">
              <template #suffix>
                <span class="stat-suffix">本</span>
              </template>
            </n-statistic>
            <div class="stat-icon borrowed">📚</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="已归还" :value="stats.returned">
              <template #suffix>
                <span class="stat-suffix">本</span>
              </template>
            </n-statistic>
            <div class="stat-icon returned">✅</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="逾期" :value="stats.overdue">
              <template #suffix>
                <span class="stat-suffix">本</span>
              </template>
            </n-statistic>
            <div class="stat-icon overdue">⚠️</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="总计" :value="stats.total">
              <template #suffix>
                <span class="stat-suffix">条</span>
              </template>
            </n-statistic>
            <div class="stat-icon total">📊</div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 筛选和表格 -->
      <n-card title="借阅记录" :bordered="false">
        <!-- 筛选区域 -->
        <template #header-extra>
          <n-space>
            <n-select
              v-model:value="filterStatus"
              :options="statusOptions"
              placeholder="筛选状态"
              clearable
              style="width: 140px"
              @update:value="handleSearch"
            />
            <n-input
              v-model:value="filterKeyword"
              placeholder="搜索用户名或图书名"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <n-button text @click="handleSearch">🔍</n-button>
              </template>
            </n-input>
            <n-button @click="handleReset">重置</n-button>
            <n-button type="primary" @click="loadData">刷新</n-button>
          </n-space>
        </template>

        <!-- 借阅记录表格 -->
        <n-data-table
          :columns="columns"
          :data="borrowList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BorrowRecord) => row.recordId"
          :striped="true"
          :bordered="false"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-card>
    </n-space>

    <!-- 拒绝原因弹窗 -->
    <n-modal v-model:show="showRejectModal" :mask-closable="false">
      <n-card
        style="width: 500px;"
        title="拒绝借阅申请"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <n-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules">
          <n-form-item label="拒绝原因" path="reason">
            <n-input
              v-model:value="rejectForm.reason"
              type="textarea"
              placeholder="请输入拒绝原因..."
              :autosize="{
                minRows: 3,
                maxRows: 5
              }"
            />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showRejectModal = false">取消</n-button>
            <n-button type="error" :loading="approving" @click="submitReject">确认拒绝</n-button>
          </n-space>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted, reactive } from 'vue'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NTag, NImage, NSpace, NIcon, NModal, NCard, NForm, NFormItem, NInput, useMessage, useDialog } from 'naive-ui'
import { borrowAPI, type BorrowRecord } from '@/api/admin/borrows'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const borrowList = ref<BorrowRecord[]>([])
const filterStatus = ref<string | null>(null)
const filterKeyword = ref('')

// 审批相关
const showRejectModal = ref(false)
const approving = ref(false)
const currentRecord = ref<BorrowRecord | null>(null)
const rejectFormRef = ref<FormInst | null>(null)
const rejectForm = reactive({
  reason: ''
})
const rejectRules: FormRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, message: '拒绝原因至少5个字符', trigger: 'blur' }
  ]
}

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

const stats = reactive({
  borrowed: 0,
  returned: 0,
  overdue: 0,
  total: 0
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})

const statusOptions = [
  { label: '待审批', value: 'pending' },
  { label: '借阅中', value: 'borrowed' },
  { label: '已归还', value: 'returned' },
  { label: '逾期', value: 'overdue' },
  { label: '已拒绝', value: 'rejected' }
]

// 表格列定义
const columns: DataTableColumns<BorrowRecord> = [
  {
    title: 'ID',
    key: 'recordId',
    width: 70,
    align: 'center'
  },
  {
    title: '用户',
    key: 'username',
    width: 120,
    ellipsis: {
      tooltip: true
    }
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
    title: '借阅时间',
    key: 'borrowTime',
    width: 160,
    render: (row) => row.borrowTime ? h('div', {}, [
      h('div', { style: 'font-size: 13px' }, dayjs(row.borrowTime).format('YYYY-MM-DD')),
      h('div', { style: 'font-size: 12px; color: #999' }, dayjs(row.borrowTime).format('HH:mm:ss'))
    ]) : h('span', { style: 'color: #999' }, '-')
  },
  {
    title: '应还时间',
    key: 'dueTime',
    width: 160,
    render: (row) => {
      if (!row.dueTime) return h('span', { style: 'color: #999' }, '-')
      const isOverdue = row.status === 'overdue'
      return h('div', {}, [
        h('div', {
          style: `font-size: 13px; color: ${isOverdue ? '#d03050' : 'inherit'}`
        }, dayjs(row.dueTime).format('YYYY-MM-DD')),
        h('div', {
          style: `font-size: 12px; color: ${isOverdue ? '#d03050' : '#999'}`
        }, dayjs(row.dueTime).format('HH:mm:ss'))
      ])
    }
  },
  {
    title: '归还时间',
    key: 'returnTime',
    width: 160,
    render: (row) => row.returnTime ? h('div', {}, [
      h('div', { style: 'font-size: 13px; color: #18a058' }, dayjs(row.returnTime).format('YYYY-MM-DD')),
      h('div', { style: 'font-size: 12px; color: #999' }, dayjs(row.returnTime).format('HH:mm:ss'))
    ]) : h('span', { style: 'color: #999' }, '-')
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => {
      const statusMap: Record<string, { type: any, text: string }> = {
        pending: { type: 'warning', text: '待审批' },
        borrowed: { type: 'info', text: '借阅中' },
        returned: { type: 'success', text: '已归还' },
        overdue: { type: 'error', text: '逾期' },
        rejected: { type: 'default', text: '已拒绝' }
      }
      const status = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: status.type, round: true }, { default: () => status.text })
    }
  },
  {
    title: '续借状态',
    key: 'isRenewed',
    width: 90,
    align: 'center',
    render: (row) => h(NTag, {
      type: row.isRenewed === 1 ? 'warning' : 'default',
      size: 'small',
      round: true
    }, { default: () => row.isRenewed === 1 ? '已续借' : '未续借' })
  },
  {
    title: '逾期费用',
    key: 'overdueFee',
    width: 80,
    align: 'right',
    render: (row) => row.overdueFee > 0 ? h('span', {
      style: 'color: #d03050; font-weight: 500'
    }, `¥${row.overdueFee.toFixed(2)}`) : h('span', { style: 'color: #999' }, '-')
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    align: 'center',
    fixed: 'right',
    render: (row) => {
      if (row.status === 'pending') {
        return h(NSpace, { size: 8 }, {
          default: () => [
            h(NButton, {
              type: 'success',
              size: 'small',
              onClick: () => handleApprove(row, true)
            }, { default: () => '同意' }),
            h(NButton, {
              type: 'error',
              size: 'small',
              onClick: () => handleApprove(row, false)
            }, { default: () => '拒绝' })
          ]
        })
      }
      return h('span', { style: 'color: #999' }, '-')
    }
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await borrowAPI.getList({
      status: filterStatus.value || undefined,
      page: pagination.page,
      size: pagination.pageSize
    })
    borrowList.value = data.records
    pagination.itemCount = data.total
    
    // 计算统计数据
    updateStats(data.records)
  } catch (error: any) {
    message.error(error.message || '加载借阅记录失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = (records: BorrowRecord[]) => {
  stats.borrowed = records.filter(r => r.status === 'borrowed').length
  stats.returned = records.filter(r => r.status === 'returned').length
  stats.overdue = records.filter(r => r.status === 'overdue').length
  stats.total = records.length
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterStatus.value = null
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

// 处理审批
const handleApprove = (record: BorrowRecord, approved: boolean) => {
  currentRecord.value = record
  
  if (approved) {
    // 同意借阅
    dialog.warning({
      title: '确认审批',
      content: `确认同意用户 "${record.username}" 借阅图书 "${record.bookTitle}" 吗？`,
      positiveText: '确认',
      negativeText: '取消',
      onPositiveClick: async () => {
        await doApprove(record.recordId, true)
      }
    })
  } else {
    // 拒绝借阅，打开弹窗输入原因
    rejectForm.reason = ''
    showRejectModal.value = true
  }
}

// 提交拒绝
const submitReject = async () => {
  try {
    await rejectFormRef.value?.validate()
    await doApprove(currentRecord.value!.recordId, false, rejectForm.reason)
    showRejectModal.value = false
  } catch (error) {
    // 验证失败
  }
}

// 执行审批
const doApprove = async (recordId: number, approved: boolean, rejectReason?: string) => {
  approving.value = true
  try {
    await borrowAPI.approveBorrow({
      recordId,
      approved,
      rejectReason
    })
    message.success(approved ? '审批通过' : '已拒绝借阅申请')
    loadData() // 重新加载数据
  } catch (error: any) {
    message.error(error.message || '审批失败')
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.borrow-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

/* 统计卡片样式 */
.stat-card {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
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
  font-size: 48px;
  opacity: 0.2;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  opacity: 0.3;
  transform: translateY(-50%) scale(1.1);
}

.stat-card .stat-icon.borrowed {
  filter: hue-rotate(200deg);
}

.stat-card .stat-icon.returned {
  filter: hue-rotate(100deg);
}

.stat-card .stat-icon.overdue {
  filter: hue-rotate(0deg);
}

.stat-card .stat-icon.total {
  filter: hue-rotate(40deg);
}

.stat-suffix {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}

/* 表格样式优化 */
:deep(.n-data-table) {
  font-size: 14px;
}

:deep(.n-data-table .n-data-table-th) {
  font-weight: 600;
  background: #fafafa;
}

:deep(.n-data-table .n-data-table-td) {
  padding: 12px 16px;
}

:deep(.n-data-table-striped .n-data-table-tr:nth-child(even)) {
  background: #fafafa;
}
</style>
