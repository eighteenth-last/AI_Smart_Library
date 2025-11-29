<template>
  <div class="favorites-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="navbar-brand" @click="router.push('/')">
          <img src="/favicon.ico" alt="Logo" class="brand-logo" />
          <span class="brand-name">神阁慧境阁</span>
        </div>
        <div class="navbar-menu">
          <router-link to="/" class="menu-item">首页</router-link>
          <router-link to="/books" class="menu-item">图书馆</router-link>
          <router-link to="/favorites" class="menu-item active">我的收藏</router-link>
          <router-link to="/borrow-history" class="menu-item">借阅记录</router-link>
        </div>
        <div class="navbar-actions">
          <NavBar />
        </div>
      </div>
    </nav>

    <!-- 页面内容 -->
    <div class="page-content">
      <div class="favorites-wrapper">
        <!-- 标题区域 -->
        <div class="page-header">
          <div class="header-left">
            <div class="icon-wrapper">
              <n-icon size="40" color="#ff6b6b">
                <heart-outline />
              </n-icon>
            </div>
            <div class="header-text">
              <h1 class="page-title">我的书架</h1>
              <p class="page-subtitle">珍藏每一本心爱的图书</p>
            </div>
          </div>
          <div class="page-stats">
            <div class="stat-item">
              <span class="stat-number">{{ total }}</span>
              <span class="stat-label">本图书</span>
            </div>
          </div>
        </div>
          
          <n-spin :show="loading" size="large">
            <div v-if="favoritesList.length === 0" class="empty-state">
              <div class="empty-content">
                <div class="empty-icon">
                  <n-icon size="120" color="#e0e0e0">
                    <book-outline />
                  </n-icon>
                </div>
                <h3 class="empty-title">书架还是空空的</h3>
                <p class="empty-text">快去图书馆发现你的下一本好书吧！</p>
                <n-button type="primary" size="large" round @click="router.push('/books')">
                  <template #icon>
                    <n-icon><book-outline /></n-icon>
                  </template>
                  去图书馆逛逛
                </n-button>
              </div>
            </div>
            <div v-else class="favorites-grid">
              <div
                v-for="favorite in favoritesList"
                :key="favorite.favId"
                class="book-item"
              >
                <div class="book-cover" @click="goToBookDetail(favorite.bookId)">
                  <n-image
                    :src="getCoverUrl(favorite.coverUrl)"
                    :fallback-src="'/default-book-cover.jpg'"
                    :alt="favorite.bookTitle"
                    object-fit="cover"
                    :preview-disabled="true"
                    class="cover-image"
                  />
                  <div class="book-overlay">
                    <n-button type="primary" size="small" ghost>
                      查看详情
                    </n-button>
                  </div>
                </div>
                <div class="book-info">
                  <h3 class="book-title" :title="favorite.bookTitle">{{ favorite.bookTitle }}</h3>
                  <p class="book-author">{{ favorite.authorName }}</p>
                  <div class="book-actions">
                    <n-button size="small" type="error" text @click="removeFavorite(favorite.bookId)">
                      <template #icon>
                        <n-icon><heart-dislike-outline /></n-icon>
                      </template>
                      取消收藏
                    </n-button>
                  </div>
                </div>
              </div>
            </div>
          </n-spin>
          
          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <n-pagination
              v-model:page="currentPage"
              :page-count="Math.ceil(total / pageSize)"
              :page-size="pageSize"
              @update-page="handlePageChange"
            />
          </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <div class="footer-content">
        <p>神阁慧境阁 © 2025</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getMyFavorites, removeFavorite as removeFavoriteAPI } from '@/api/user/favorite'
import { logout } from '@/api/user/auth'
import { BookOutline, PersonOutline, HeartDislikeOutline, HeartOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 收藏列表
const favoritesList = ref<any[]>([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 获取封面完整URL
const getCoverUrl = (url?: string) => {
  if (!url) return '/default-book-cover.jpg'
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    return baseURL + url
  }
  return url
}

onMounted(() => {
  loadFavorites()
})

const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await getMyFavorites(currentPage.value, pageSize.value)
    // request拦截器已经处理，直接返回的是data
    favoritesList.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('Load favorites error:', error)
    message.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (bookId: number) => {
  try {
    await removeFavoriteAPI(bookId)
    message.success('已取消收藏')
    loadFavorites() // 重新加载列表
  } catch (error) {
    console.error('Remove favorite error:', error)
    message.error('取消收藏失败')
  }
}

const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`)
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadFavorites()
}
</script>

<style scoped>
.favorites-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏样式 */
.navbar {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

.navbar-menu {
  display: flex;
  gap: 30px;
}

.menu-item {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  padding: 6px 14px;
  border-radius: 6px;
  transition: all 0.2s;
}

.menu-item:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.menu-item.active {
  color: #667eea;
  font-weight: 500;
  background: rgba(102, 126, 234, 0.1);
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 15px;
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

/* 页面内容 */
.page-content {
  padding-top: 60px;
  min-height: calc(100vh - 60px);
}

.favorites-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 60px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  width: 70px;
  height: 70px;
  background: white;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.15);
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
  color: #333;
  letter-spacing: 1px;
}

.page-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.page-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  background: white;
  padding: 16px 28px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: #999;
}

/* 图书网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 30px 20px;
  margin-bottom: 40px;
}

/* 图书项目 */
.book-item {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.book-cover {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  cursor: pointer;
  background: #f0f0f0;
}

.book-cover :deep(.n-image),
.cover-image {
  width: 100%;
  height: 100%;
}

.book-cover :deep(.n-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.book-item:hover .book-cover :deep(.n-image img) {
  transform: scale(1.05);
}

/* 图书遮罩层 */
.book-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.book-item:hover .book-overlay {
  opacity: 1;
}

/* 图书信息 */
.book-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 2.8em;
}

.book-author {
  font-size: 13px;
  color: #999;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-actions {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  background: white;
  border-radius: 16px;
  margin: 20px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.empty-content {
  text-align: center;
  padding: 40px;
}

.empty-icon {
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.empty-text {
  font-size: 15px;
  color: #999;
  margin: 0 0 30px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 40px;
}

.footer {
  background: white;
  border-top: 1px solid #e0e0e0;
  text-align: center;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.footer-content p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 20px;
  }
  
  .header-left {
    width: 100%;
  }
  
  .icon-wrapper {
    width: 60px;
    height: 60px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 13px;
  }
  
  .page-stats {
    width: 100%;
  }
  
  .stat-item {
    flex: 1;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 20px 15px;
  }
  
  .book-cover {
    height: 200px;
  }
  
  .empty-title {
    font-size: 20px;
  }
  
  .empty-text {
    font-size: 14px;
  }
}
</style>