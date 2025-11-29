<template>
  <div class="borrow-history-container">
    <n-layout>
      <!-- 顶部导航 -->
      <n-layout-header class="header" bordered>
        <div class="header-content">
          <div class="navbar-brand" @click="goHome">
            <img src="/favicon.ico" alt="Logo" class="brand-logo" />
            <span class="brand-name">神阁慧境阁</span>
          </div>
          <div class="navbar-menu">
            <router-link to="/" class="menu-item">首页</router-link>
            <router-link to="/books" class="menu-item">图书馆</router-link>
            <router-link to="/favorites" class="menu-item">我的收藏</router-link>
            <router-link to="/borrow-history" class="menu-item active">借阅记录</router-link>
          </div>
          <div class="navbar-actions">
            <NavBar />
          </div>
        </div>
      </n-layout-header>

      <!-- 主要内容 -->
      <n-layout-content class="content">
        <div class="borrow-history-wrapper">
          <!-- 页面标题 -->
          <div class="page-header">
            <div class="header-left">
              <div class="icon-wrapper">
                <n-icon size="40" color="#667eea">
                  <book-outline />
                </n-icon>
              </div>
              <div class="header-text">
                <h1 class="page-title">我的借阅</h1>
                <p class="page-subtitle">管理您的借阅记录</p>
              </div>
            </div>
            <div class="page-stats">
              <div class="stat-item">
                <span class="stat-number">{{ borrowList.length }}</span>
                <span class="stat-label">总记录</span>
              </div>
            </div>
          </div>
          
          <!-- 筛选表单 -->
          <div class="filter-card">
            <n-form :model="searchForm" inline>
              <n-form-item label="状态">
                <n-select
                  v-model:value="searchForm.status"
                  :options="statusOptions"
                  placeholder="选择状态"
                  clearable
                  style="width: 140px;"
                  @update:value="handleStatusChange"
                />
              </n-form-item>
              <n-form-item>
                <n-button type="primary" @click="handleSearch" class="search-btn">
                  <template #icon>
                    <n-icon><search-outline /></n-icon>
                  </template>
                  搜索
                </n-button>
                <n-button @click="resetSearch" style="margin-left: 10px;" class="reset-btn">
                  <template #icon>
                    <n-icon><refresh-outline /></n-icon>
                  </template>
                  重置
                </n-button>
              </n-form-item>
            </n-form>
          </div>
          
          <n-spin :show="loading">
            <div v-if="borrowList.length === 0" class="empty-state">
              <n-empty description="您还没有借阅记录">
                <template #icon>
                  <n-icon size="80" color="#d0d0d0">
                    <book-outline />
                  </n-icon>
                </template>
              </n-empty>
            </div>
            <n-data-table
              v-else
              :columns="columns"
              :data="borrowList"
              :pagination="pagination"
              :loading="loading"
              :bordered="false"
              class="modern-table"
            />
          </n-spin>
        </div>
      </n-layout-content>

      <!-- 页脚 -->
      <n-layout-footer class="footer" bordered>
        <div class="footer-content">
          <p>神阁慧境阁 © 2025</p>
        </div>
      </n-layout-footer>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { borrowAPI } from '@/api/user/borrow'
import { BookOutline, SearchOutline, RefreshOutline } from '@vicons/ionicons5'
import NavBar from '@/components/NavBar.vue'
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NLayoutFooter, 
  NButton, 
  NSpin,
  NDataTable,
  NForm,
  NFormItem,
  NSelect,
  NEmpty,
  NIcon,
  useMessage
} from 'naive-ui'

// 消息提示
const message = useMessage()
const router = useRouter()
const userStore = useUserStore()

// 借阅记录列表
const borrowList = ref<any[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = ref({
  status: undefined as string | undefined
})

// 状态选项
const statusOptions = ref([
  { label: '已借阅', value: 'borrowed' },
  { label: '已归还', value: 'returned' },
  { label: '已逾期', value: 'overdue' }
])

// 分页
const pagination = ref({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onUpdatePage: (page: number) => {
    pagination.value.page = page
    loadBorrowHistory()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadBorrowHistory()
  }
})

// 列配置
const columns = ref([
  {
    title: '图书信息',
    key: 'bookInfo',
    render: (row: any) => {
      return h('div', [
        h('div', { style: { fontWeight: 'bold' } }, row.bookTitle),
        h('div', { style: { color: '#666', fontSize: '14px', marginTop: '4px' } }, `ID: ${row.bookId}`)
      ])
    }
  },
  {
    title: '借阅时间',
    key: 'borrowTime',
    render: (row: any) => {
      return h('div', formatDate(row.borrowTime))
    }
  },
  {
    title: '应还时间',
    key: 'dueTime',
    render: (row: any) => {
      return h('div', formatDate(row.dueTime))
    }
  },
  {
    title: '归还时间',
    key: 'returnTime',
    render: (row: any) => {
      return h('div', row.returnTime ? formatDate(row.returnTime) : '-')
    }
  },
  {
    title: '状态',
    key: 'status',
    render: (row: any) => {
      const statusMap: any = {
        'borrowed': { text: '已借阅', color: '#50d484' },
        'returned': { text: '已归还', color: '#999' },
        'overdue': { text: '已逾期', color: '#f06f00' }
      }
      const status = statusMap[row.status]
      return h('span', {
        style: {
          color: status?.color
        }
      }, status?.text || row.status)
    }
  },
  {
    title: '剩余天数',
    key: 'daysRemaining',
    render: (row: any) => {
      if (row.status === 'returned') {
        return h('span', '-')
      }
      if (row.daysRemaining !== undefined) {
        return h('span', {
          style: {
            color: row.daysRemaining < 3 ? '#f06f00' : '#666'
          }
        }, row.daysRemaining > 0 ? `${row.daysRemaining}天` : '已逾期')
      }
      return h('span', '-')
    }
  },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) => {
      return h('div', [
        h(NButton, 
          { 
            size: 'small', 
            type: 'primary', 
            style: 'margin-right: 10px;',
            onClick: () => goToBookDetail(row.bookId) 
          }, 
          { default: () => '图书详情' }
        ),
        row.status === 'borrowed' && !row.isOverdue && h(NButton, 
          { 
            size: 'small', 
            type: 'warning', 
            style: 'margin-right: 10px;',
            onClick: () => renewBook(row.recordId) 
          }, 
          { default: () => '续借' }
        ),
        row.status === 'borrowed' && h(NButton, 
          { 
            size: 'small', 
            type: 'success', 
            onClick: () => returnBook(row.recordId) 
          }, 
          { default: () => '归还' }
        )
      ].filter(Boolean))
    }
  }
])

onMounted(() => {
  loadBorrowHistory()
})

const loadBorrowHistory = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: pagination.value.page,
      size: pagination.value.pageSize
    }
    // request拦截器已经处理，直接返回的是data
    const response = await borrowAPI.getBorrowHistory(params)
    borrowList.value = response.records
    pagination.value.itemCount = response.total
  } catch (error) {
    console.error('Load borrow history error:', error)
    message.error('获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 续借图书
const renewBook = async (recordId: number) => {
  if (!window.confirm('确定要续借这本书吗？')) {
    return
  }
  
  try {
    await borrowAPI.renewBook(recordId)
    message.success('续借成功')
    loadBorrowHistory()
  } catch (error) {
    console.error('Renew book error:', error)
    message.error('续借失败')
  }
}

// 归还图书
const returnBook = async (recordId: number) => {
  if (!window.confirm('确定要归还这本书吗？')) {
    return
  }
  
  try {
    await borrowAPI.returnBook(recordId)
    message.success('归还成功')
    loadBorrowHistory()
  } catch (error) {
    console.error('Return book error:', error)
    message.error('归还失败')
  }
}

// 用户菜单选项（已移至 NavBar 组件）

// 跳转到图书详情
const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`)
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadBorrowHistory()
}

// 状态改变时自动搜索
const handleStatusChange = () => {
  pagination.value.page = 1
  loadBorrowHistory()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    status: undefined
  }
  pagination.value.page = 1
  loadBorrowHistory()
}

// 用户菜单选项（已移至 NavBar 组件）

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.borrow-history-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
}

/* 品牌区域 */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.navbar-brand:hover {
  transform: translateY(-2px);
}

.brand-logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

/* 导航菜单 */
.navbar-menu {
  display: flex;
  gap: 8px;
}

.menu-item {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.menu-item:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.menu-item.active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.12);
  font-weight: 600;
}

/* 用户操作区 */
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

.login-btn {
  padding: 6px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #667eea;
}

.login-btn:hover {
  background: rgba(102, 126, 234, 0.08);
}

.content {
  min-height: calc(100vh - 120px);
  padding-top: 80px;
}

.borrow-history-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.icon-wrapper .n-icon {
  color: white !important;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.page-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.2);
  padding: 20px 32px;
  border-radius: 12px;
  text-align: center;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  display: block;
  font-size: 36px;
  font-weight: 700;
}

.stat-label {
  display: block;
  font-size: 13px;
  opacity: 0.9;
}

/* 筛选卡片 */
.filter-card {
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.filter-card .n-form {
  margin: 0;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.reset-btn {
  transition: all 0.3s ease;
}

.reset-btn:hover {
  transform: translateY(-2px);
}

/* 表格样式 */
.modern-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.modern-table :deep(.n-data-table-th) {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.modern-table :deep(.n-data-table-td) {
  border-bottom: 1px solid #f0f0f0;
}

.modern-table :deep(.n-data-table-tr:hover) {
  background: #f8f9fa;
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 页脚 */
.footer {
  background-color: white;
  text-align: center;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 20px;
  color: #999;
  font-size: 14px;
}

.footer-content p {
  margin: 0;
}
</style>