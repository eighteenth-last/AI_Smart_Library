<template>
  <div class="qa-management-container">
    <!-- 筛选区域：左上角 -->
    <div class="filter-section">
      <n-select
        v-model:value="searchForm.status"
        :options="statusOptions"
        placeholder="🔍 筛选状态"
        clearable
        style="width: 200px;"
        @update:value="handleSearch"
      />
    </div>
    
    <!-- 问答列表：筛选区下方，左对齐 -->
    <n-card :bordered="false" class="table-card">
      <n-spin :show="loading">
        <n-data-table
          :columns="columns"
          :data="qaList"
          :pagination="pagination"
          :loading="loading"
          :row-props="rowProps"
          striped
        />
      </n-spin>
    </n-card>
    
    <!-- 回复弹窗 -->
    <n-modal v-model:show="showReplyModal" :mask-closable="false">
      <n-card
        style="width: 600px;"
        title="回复问题"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <n-form :model="replyForm" :rules="replyFormRules" ref="replyFormRef">
          <n-form-item label="问题" :show-label="false" style="margin-bottom: 15px;">
            <n-card size="small" :bordered="true">
              <p><strong>用户:</strong> {{ currentQA?.username }}</p>
              <p><strong>问题:</strong> {{ currentQA?.question }}</p>
              <p><strong>提问时间:</strong> {{ formatDate(currentQA?.createdAt) }}</p>
            </n-card>
          </n-form-item>
          
          <n-form-item label="回复内容" path="answer">
            <n-input
              v-model:value="replyForm.answer"
              type="textarea"
              :autosize="{
                minRows: 4,
                maxRows: 6
              }"
              placeholder="请输入您的回复..."
            />
          </n-form-item>
          
          <n-form-item label="公开设置" path="isPublic">
            <n-switch v-model:value="replyForm.isPublic">
              <template #checked>公开</template>
              <template #unchecked>私密</template>
            </n-switch>
            <n-tooltip trigger="hover" style="max-width: 200px;">
              <template #trigger>
                <n-icon size="18" style="margin-left: 8px; color: #999;">
                  <HelpCircleOutline />
                </n-icon>
              </template>
              公开的回复将被添加到知识库中，其他用户可能看到
            </n-tooltip>
          </n-form-item>
        </n-form>
        <template #footer>
          <div style="display: flex; justify-content: flex-end;">
            <n-button @click="showReplyModal = false">取消</n-button>
            <n-button type="primary" @click="submitReply" :loading="submitting">提交回复</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>

    <!-- 查看详情弹窗 -->
    <n-modal 
      v-model:show="showDetailModal" 
      :mask-closable="true"
      preset="card"
      style="width: 700px; max-height: 85vh;"
      title="问答详情"
      :bordered="false"
      size="huge"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <n-scrollbar style="max-height: 55vh;">
        <div class="detail-content">
            <!-- 问题信息 -->
            <div class="detail-section">
              <div class="section-header">
                <span class="section-icon">👤</span>
                <span class="section-title">提问信息</span>
              </div>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">用户名：</span>
                  <span class="info-value">{{ currentQA?.username }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">提问时间：</span>
                  <span class="info-value">{{ formatDate(currentQA?.createdAt) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">状态：</span>
                  <n-tag :type="currentQA?.status === 'pending' ? 'warning' : 'success'" size="small">
                    {{ currentQA?.status === 'pending' ? '待回复' : '已回复' }}
                  </n-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">公开设置：</span>
                  <n-tag :type="currentQA?.isPublic === 1 ? 'info' : 'default'" size="small">
                    {{ currentQA?.isPublic === 1 ? '公开' : '私密' }}
                  </n-tag>
                </div>
              </div>
            </div>

            <!-- 问题内容 -->
            <div class="detail-section">
              <div class="section-header">
                <span class="section-icon">❓</span>
                <span class="section-title">问题内容</span>
              </div>
              <div class="question-box">
                {{ currentQA?.question }}
              </div>
            </div>

            <!-- 回复内容 -->
            <div class="detail-section" v-if="currentQA?.answer">
              <div class="section-header">
                <span class="section-icon">💬</span>
                <span class="section-title">回复内容</span>
              </div>
              <div class="answer-box">
                {{ currentQA?.answer }}
              </div>
              <div class="answer-time" v-if="currentQA?.answeredAt">
                <span>🕒 回复时间：{{ formatDate(currentQA?.answeredAt) }}</span>
              </div>
            </div>

            <!-- 未回复提示 -->
            <div class="detail-section" v-else>
              <n-empty description="该问题尚未回复" size="medium">
                <template #extra>
                  <n-button type="primary" @click="openReplyFromDetail">
                    立即回复
                  </n-button>
                </template>
              </n-empty>
            </div>
        </div>
      </n-scrollbar>
      
      <template #footer>
        <n-space justify="end" :size="12">
          <n-button @click="showDetailModal = false">关闭</n-button>
          <n-button v-if="currentQA?.status === 'pending'" type="primary" @click="openReplyFromDetail">
            回复问题
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { 
  NCard, 
  NForm, 
  NFormItem, 
  NInput, 
  NSelect, 
  NButton, 
  NDataTable, 
  NSpin, 
  NModal, 
  NSwitch,
  NTag,
  NEmpty,
  NScrollbar,
  NSpace,
  useMessage,
  NTooltip,
  NIcon
} from 'naive-ui'
import { HelpCircleOutline } from '@vicons/ionicons5'
import { qaAPI } from '@/api/author/qa'

// 消息提示
const message = useMessage()

// 搜索表单
const searchForm = ref({
  status: undefined as string | undefined
})

// 状态选项
const statusOptions = ref([
  { label: '待回复', value: 'pending' },
  { label: '已回复', value: 'answered' }
])

// 问答列表
const qaList = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onUpdatePage: (page: number) => {
    pagination.value.page = page
    loadQAList()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadQAList()
  }
})

// 当前操作的问答
const currentQA = ref<any>(null)

// 回复表单
const replyForm = ref({
  answer: '',
  isPublic: true
})

const replyFormRef = ref()
const showReplyModal = ref(false)
const showDetailModal = ref(false)

// 表单验证规则
const replyFormRules = {
  answer: [
    { required: true, message: '请输入回复内容', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 列配置
const columns = ref([
  {
    title: '用户',
    key: 'username',
    width: 120
  },
  {
    title: '问题',
    key: 'question',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const statusMap: any = {
        'pending': '待回复',
        'answered': '已回复'
      }
      return h('span', {
        style: {
          color: row.status === 'pending' ? '#f0a020' : '#50d484',
          fontWeight: '500'
        }
      }, statusMap[row.status] || row.status)
    }
  },
  {
    title: '公开',
    key: 'isPublic',
    width: 80,
    render: (row: any) => {
      return h('span', row.isPublic === 1 ? '是' : '否')
    }
  },
  {
    title: '提问时间',
    key: 'createdAt',
    width: 180,
    render: (row: any) => {
      return h('span', formatDate(row.createdAt))
    }
  },
  {
    title: '回复时间',
    key: 'answeredAt',
    width: 180,
    render: (row: any) => {
      return h('span', row.answeredAt ? formatDate(row.answeredAt) : '-')
    }
  }
])

onMounted(() => {
  loadQAList()
})

// 行属性：让每一行都可以点击
const rowProps = (row: any) => {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      openDetailModal(row)
    }
  }
}

// 加载问答列表
const loadQAList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.value.page,
      size: pagination.value.pageSize
    }
    
    // 如果有状态筛选，添加到参数中
    if (searchForm.value.status) {
      params.status = searchForm.value.status
    }
    
    const response = await qaAPI.getQuestions(params)
    qaList.value = response.records
    pagination.value.itemCount = response.total
  } catch (error) {
    console.error('Load QA list error:', error)
    message.error('获取问答列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.page = 1
  loadQAList()
}

// 打开回复弹窗
const openReplyModal = (qa: any) => {
  currentQA.value = qa
  replyForm.value = {
    answer: '',
    isPublic: true
  }
  showReplyModal.value = true
}

// 打开详情弹窗
const openDetailModal = (qa: any) => {
  currentQA.value = qa
  showDetailModal.value = true
}

// 从详情页打开回复弹窗
const openReplyFromDetail = () => {
  showDetailModal.value = false
  replyForm.value = {
    answer: '',
    isPublic: true
  }
  showReplyModal.value = true
}

// 提交回复
const submitReply = async () => {
  await replyFormRef.value?.validate()
  submitting.value = true
  
  try {
    const response = await qaAPI.answerQuestion({
      qaId: currentQA.value.qaId,
      answer: replyForm.value.answer,
      isPublic: replyForm.value.isPublic ? 1 : 0
    })
    
    if (response.data.code === 200) {
      message.success('回复成功')
      showReplyModal.value = false
      loadQAList()
    } else {
      message.error(response.data.msg || '回复失败')
    }
  } catch (error) {
    console.error('Submit reply error:', error)
    message.error('回复失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.qa-management-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 筛选区域：左上角 */
.filter-section {
  margin-bottom: 16px;
}

/* 表格卡片 */
.table-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

/* 表格样式优化 */
.qa-management-container :deep(.n-data-table) {
  background: transparent;
}

.qa-management-container :deep(.n-data-table-th) {
  background: #f9fafb;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 2px solid #e5e7eb;
}

.qa-management-container :deep(.n-data-table-td) {
  border-bottom: 1px solid #f3f4f6;
  padding: 16px 12px;
}

/* 表格行样式 */
.qa-management-container :deep(.n-data-table-tr) {
  transition: all 0.3s ease;
}

.qa-management-container :deep(.n-data-table-tr:hover) {
  background: linear-gradient(90deg, #f0fdf4 0%, #ffffff 100%) !important;
  transform: translateX(4px);
  box-shadow: -4px 0 0 0 #18a058, 0 2px 8px rgba(24, 160, 88, 0.1);
}

/* 详情弹窗样式 */
.detail-content {
  padding: 12px 0;
}

.detail-section {
  margin-bottom: 28px;
  animation: fadeInUp 0.4s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
  background: linear-gradient(90deg, #f9fafb 0%, transparent 100%);
  padding-left: 12px;
  border-radius: 4px;
}

.section-icon {
  font-size: 22px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f9fafb 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.info-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 600;
  min-width: 80px;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.question-box {
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 15px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.question-box::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-30%, -30%); }
}

.answer-box {
  padding: 20px 24px;
  background: linear-gradient(135deg, #18a058 0%, #0d8646 100%);
  color: white;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 15px;
  box-shadow: 0 8px 20px rgba(24, 160, 88, 0.3);
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.answer-box::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: shimmer 3s infinite;
}

.answer-time {
  padding: 12px 18px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-left: 4px solid #18a058;
  border-radius: 8px;
  font-size: 13px;
  color: #166534;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 160, 88, 0.1);
}
</style>