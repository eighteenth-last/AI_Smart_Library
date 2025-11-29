<template>
  <div class="reviews-container">
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
            <router-link to="/borrow-history" class="menu-item">借阅记录</router-link>
          </div>
          <div class="navbar-actions">
            <n-button v-if="!userStore.isLoggedIn()" text @click="router.push('/login')" class="login-btn">
              登录
            </n-button>
            <n-dropdown v-else :options="userMenuOptions" @select="handleUserMenuSelect">
              <n-button text class="user-btn">
                <n-icon size="20"><person-outline /></n-icon>
                {{ userStore.userInfo?.username || '用户' }}
              </n-button>
            </n-dropdown>
          </div>
        </div>
      </n-layout-header>

      <!-- 主要内容 -->
      <n-layout-content class="content" :style="{ paddingTop: '60px' }">
        <div class="reviews-wrapper">
          <h2>我的评价</h2>
          
          <n-spin :show="loading">
            <div v-if="reviewsList.length === 0" class="empty-state">
              <n-empty description="您还没有发表任何评价" />
            </div>
            <div v-else>
              <n-card 
                v-for="review in reviewsList" 
                :key="review.reviewId" 
                class="review-card"
              >
                <div class="review-header">
                  <h3 @click="goToBookDetail(review.bookId)" class="book-title">
                    {{ review.bookTitle }}
                  </h3>
                  <n-rate :value="review.rating" readonly allow-half />
                </div>
                <div class="review-content">
                  {{ review.content || '此用户没有填写评价内容' }}
                </div>
                <div class="review-footer">
                  <span class="review-time">{{ formatDate(review.createdAt) }}</span>
                  <n-button 
                    size="small" 
                    type="primary" 
                    @click="goToBookDetail(review.bookId)"
                  >
                    查看图书
                  </n-button>
                </div>
              </n-card>
            </div>
          </n-spin>
          
          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <n-pagination
              v-model:page="currentPage"
              :page-count="Math.ceil(total / pageSize)"
              :page-size="pageSize"
              @update-page="handlePageChange"
              show-size-picker
              :page-sizes="[10, 20, 50]"
              @update-page-size="handlePageSizeChange"
            />
          </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { BookOutline, PersonOutline } from '@vicons/ionicons5'
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NLayoutFooter, 
  NButton, 
  NCard, 
  NSpin,
  NRate,
  NPagination,
  NEmpty,
  NIcon,
  NDropdown
} from 'naive-ui'
import { useMessage } from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 评价列表
const reviewsList = ref<any[]>([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadReviews()
})

// 这里模拟加载评价数据，实际项目中应该通过API获取用户的评价
const loadReviews = async () => {
  loading.value = true
  try {
    // 模拟数据加载，实际项目中应替换为真实API调用
    // const response = await reviewAPI.getUserReviews({
    //   page: currentPage.value,
    //   size: pageSize.value
    // })
    // if (response.data.code === 200) {
    //   reviewsList.value = response.data.data.records
    //   total.value = response.data.data.total
    // } else {
    //   window.$message?.error(response.data.msg || '获取评价列表失败')
    // }
    
    // 使用模拟数据演示
    setTimeout(() => {
      reviewsList.value = [
        {
          reviewId: 1,
          bookId: 1,
          bookTitle: '三体',
          rating: 5,
          content: '真正的硬科幻杰作！宇宙的宏大与人性的复杂交织在一起，令人震撼。',
          createdAt: '2025-01-15T10:30:00'
        },
        {
          reviewId: 2,
          bookId: 4,
          bookTitle: '哈利·波特与魔法石',
          rating: 4,
          content: '经典的奇幻作品，魔法世界的构建非常出色，是很好的入门读物。',
          createdAt: '2025-01-10T15:20:00'
        },
        {
          reviewId: 3,
          bookId: 7,
          bookTitle: '1984',
          rating: 5,
          content: '反乌托邦的经典之作，对现实社会的警醒意义深远。',
          createdAt: '2024-12-20T09:15:00'
        }
      ]
      total.value = 3
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('Load reviews error:', error)
    message.error('获取评价列表失败')
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`)
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadReviews()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadReviews()
}

const goToUserCenter = () => {
  router.push('/user-center')
}

const goToBooks = () => {
  router.push('/books')
}

const goToAIChat = () => {
  router.push('/ai-chat')
}

const goHome = () => {
  router.push('/')
}

const handleLogout = () => {
  userStore.clearUser()
  message.success('退出成功')
  router.push('/login')
}

// 用户菜单选项
const userMenuOptions = [
  {
    label: '个人中心',
    key: 'profile'
  },
  {
    label: '退出登录',
    key: 'logout'
  }
]

const handleUserMenuSelect = async (key: string) => {
  if (key === 'profile') {
    goToUserCenter()
  } else if (key === 'logout') {
    handleLogout()
  }
}
</script>

<style scoped>
.reviews-container {
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
}

.reviews-wrapper {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.review-card {
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.book-title {
  cursor: pointer;
  color: #18a0fc;
  margin: 0;
}

.book-title:hover {
  text-decoration: underline;
}

.review-content {
  margin-bottom: 15px;
  line-height: 1.6;
  color: #333;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 14px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.footer {
  background-color: #f5f5f5;
  text-align: center;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>