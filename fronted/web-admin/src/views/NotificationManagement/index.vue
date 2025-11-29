<template>
  <div class="notification-management">
    <n-space vertical :size="16">
      <!-- 统计卡片 -->
      <n-grid cols="1 s:2 m:4" responsive="screen" :x-gap="16" :y-gap="16">
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="总通知" :value="stats.total">
              <template #suffix>
                <span class="stat-suffix">条</span>
              </template>
            </n-statistic>
            <div class="stat-icon">📢</div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card unread">
            <n-statistic label="未读" :value="stats.unread" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="系统公告" :value="stats.systemAnnouncement" />
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic label="逾期提醒" :value="stats.overdue" />
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 通知列表 -->
      <n-card title="通知管理" :bordered="false">
        <template #header-extra>
          <n-space>
            <n-select
              v-model:value="filterType"
              :options="typeOptions"
              placeholder="筛选类型"
              clearable
              style="width: 150px"
              @update:value="handleSearch"
            />
            <n-select
              v-model:value="filterIsRead"
              :options="readOptions"
              placeholder="筛选状态"
              clearable
              style="width: 120px"
              @update:value="handleSearch"
            />
            <n-button @click="handleReset">重置</n-button>
            <n-button type="primary" @click="showSendDialog">发送通知</n-button>
            <n-button type="info" @click="loadData">刷新</n-button>
          </n-space>
        </template>

        <n-data-table
          :columns="columns"
          :data="notificationList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: Notification) => row.notificationId"
          :striped="true"
          :bordered="false"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-card>
    </n-space>

    <!-- 发送通知对话框 -->
    <n-modal
      v-model:show="showDialog"
      title="发送通知"
      preset="dialog"
      :positive-text="'发送'"
      :negative-text="'取消'"
      @positive-click="handleSubmit"
      style="width: 600px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="100"
        style="margin-top: 20px"
      >
        <n-form-item label="接收对象" path="userId">
          <n-radio-group v-model:value="sendToAll">
            <n-space>
              <n-radio :value="true">全体用户</n-radio>
              <n-radio :value="false">指定用户</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item v-if="!sendToAll" label="用户ID" path="userId">
          <n-input-number
            v-model:value="formData.userId"
            placeholder="请输入用户ID"
            :min="1"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="通知类型" path="type">
          <n-select
            v-model:value="formData.type"
            :options="typeOptions"
            placeholder="选择通知类型"
          />
        </n-form-item>
        <n-form-item label="通知标题" path="title">
          <n-input
            v-model:value="formData.title"
            placeholder="请输入通知标题"
            maxlength="100"
            show-count
          />
        </n-form-item>
        <n-form-item label="通知内容" path="content">
          <n-input
            v-model:value="formData.content"
            type="textarea"
            placeholder="请输入通知内容"
            :rows="5"
            maxlength="500"
            show-count
          />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted, reactive } from 'vue'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NTag, NSpace, useMessage, useDialog } from 'naive-ui'
import { notificationAPI, type Notification, type NotificationStats, type CreateNotificationDTO } from '@/api/admin/notifications'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const notificationList = ref<Notification[]>([])
const filterType = ref<string | null>(null)
const filterIsRead = ref<number | null>(null)
const showDialog = ref(false)
const formRef = ref<FormInst | null>(null)
const sendToAll = ref(true)

const stats = reactive<NotificationStats>({
  total: 0,
  unread: 0,
  systemAnnouncement: 0,
  borrowDue: 0,
  overdue: 0
})

const formData = ref<CreateNotificationDTO>({
  userId: undefined,
  type: 'system_announcement',
  title: '',
  content: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const typeOptions = [
  { label: '系统公告', value: 'system_announcement' },
  { label: '借阅到期提醒', value: 'borrow_due' },
  { label: '逾期提醒', value: 'overdue' }
]

const readOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 }
]

const rules: FormRules = {
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ]
}

// 表格列定义
const columns: DataTableColumns<Notification> = [
  {
    title: 'ID',
    key: 'notificationId',
    width: 70,
    align: 'center'
  },
  {
    title: '用户',
    key: 'username',
    width: 120
  },
  {
    title: '类型',
    key: 'typeName',
    width: 120,
    render: (row) => {
      const typeMap: Record<string, any> = {
        'system_announcement': 'info',
        'borrow_due': 'warning',
        'overdue': 'error'
      }
      return h(NTag, {
        type: typeMap[row.type] || 'default',
        round: true
      }, { default: () => row.typeName })
    }
  },
  {
    title: '标题',
    key: 'title',
    minWidth: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '内容',
    key: 'content',
    minWidth: 250,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'isRead',
    width: 90,
    align: 'center',
    render: (row) => h(NTag, {
      type: row.isRead === 1 ? 'success' : 'warning',
      size: 'small',
      round: true
    }, { default: () => row.isRead === 1 ? '已读' : '未读' })
  },
  {
    title: '发送时间',
    key: 'createdAt',
    width: 180,
    render: (row) => dayjs(row.createdAt).format('YYYY-MM-DD HH:mm')
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    align: 'center',
    render: (row) => h(NButton, {
      size: 'small',
      type: 'error',
      onClick: () => handleDelete(row)
    }, { default: () => '删除' })
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await notificationAPI.getList({
      type: filterType.value || undefined,
      isRead: filterIsRead.value ?? undefined,
      page: pagination.page,
      size: pagination.pageSize
    })
    notificationList.value = data.records
    pagination.itemCount = data.total
  } catch (error: any) {
    message.error(error.message || '加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const data = await notificationAPI.getStats()
    Object.assign(stats, data)
  } catch (error: any) {
    message.error(error.message || '加载统计数据失败')
  }
}

// 显示发送对话框
const showSendDialog = () => {
  formData.value = {
    userId: undefined,
    type: 'system_announcement',
    title: '',
    content: ''
  }
  sendToAll.value = true
  showDialog.value = true
}

// 发送通知
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    const submitData = { ...formData.value }
    if (sendToAll.value) {
      submitData.userId = null
    }
    await notificationAPI.sendNotification(submitData)
    message.success(sendToAll.value ? '通知已群发' : '通知发送成功')
    showDialog.value = false
    await loadData()
    await loadStats()
  } catch (error: any) {
    if (error.message) {
      message.error(error.message)
    }
    throw error
  }
}

// 删除通知
const handleDelete = (row: Notification) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除给"${row.username}"的通知「${row.title}」吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await notificationAPI.deleteNotification(row.notificationId)
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
  filterType.value = null
  filterIsRead.value = null
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
.notification-management {
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

.stat-card.unread {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
}

.stat-suffix {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}
</style>
