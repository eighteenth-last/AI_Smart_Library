<template>
  <div class="books-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="navbar-brand" @click="router.push('/')">
          <img src="/favicon.ico" alt="Logo" class="brand-logo" />
          <span class="brand-name">神阁慧境阁</span>
        </div>
        <div class="navbar-menu">
          <router-link to="/" class="menu-item">首页</router-link>
          <router-link to="/books" class="menu-item active">图书馆</router-link>
          <router-link to="/favorites" class="menu-item">我的收藏</router-link>
          <router-link to="/borrow-history" class="menu-item">借阅记录</router-link>
        </div>
        <div class="navbar-actions">
          <NavBar />
        </div>
      </div>
    </nav>

    <!-- 顶部Hero区域 -->
    <div class="books-hero">
      <div class="hero-content">
        <h1 class="hero-title">📚 图书馆</h1>
        <p class="hero-subtitle">发现好书，开启阅读之旅</p>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <n-input-group>
          <n-input 
            v-model:value="searchKeyword" 
            placeholder="搜索图书（书名/作者/ISBN）" 
            size="large" 
            style="flex: 1" 
            @keydown.enter="handleSearch" 
          />
          <n-button type="primary" size="large" @click="handleSearch">
            <template #icon>
              <n-icon><search-outline /></n-icon>
            </template>
            搜索
          </n-button>
        </n-input-group>
      </div>

      <div class="filter-box">
        <n-space>
          <n-select 
            v-model:value="selectedCategory" 
            placeholder="选择分类" 
            clearable 
            style="width: 200px" 
            :options="categoryOptions" 
            @update:value="handleFilter" 
          />
        </n-space>
      </div>
    </div>

    <div class="books-grid">
      <n-grid :cols="4" :x-gap="20" :y-gap="20" responsive="screen">
        <n-grid-item v-for="book in bookList" :key="book.bookId">
          <n-card class="book-card" hoverable @click="goToBookDetail(book.bookId)">
            <div class="book-cover">
              <n-image
                :src="getCoverUrl(book.coverUrl)"
                :fallback-src="'/default-book-cover.jpg'"
                :alt="book.title"
                object-fit="cover"
                :preview-disabled="true"
              />
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">作者：{{ book.authorName }}</p>
              <p class="book-category">分类：{{ book.categoryName }}</p>
              <div class="book-rating">
                <n-rate :value="book.averageRating" readonly allow-half size="small" />
                <span class="rating-text">{{ book.averageRating.toFixed(1) }}（{{ book.reviewCount }}条评价）</span>
              </div>
              <div class="book-stock">
                <n-tag :type="book.availableStock > 0 ? 'success' : 'error'" size="small">
                  {{ book.availableStock > 0 ? `可借 ${book.availableStock}/${book.totalStock}` : '暂无可借' }}
                </n-tag>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <div class="pagination" v-if="total > 0">
      <n-pagination v-model:page="page" :page-size="size" :item-count="total" @update:page="handlePageChange" />
    </div>

    <n-empty v-if="bookList.length === 0 && !loading" description="暂无图书" style="margin-top: 100px" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { SearchOutline, PersonOutline, LogOutOutline } from '@vicons/ionicons5';
import { getBookList, getCategories, type Book, type BookListResponse } from '@/api/user/books';
import { useUserStore } from '@/store/user';
import { logout } from '@/api/user/auth';
import NavBar from '@/components/NavBar.vue';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const loading = ref(false);
const bookList = ref<Book[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(12);
const searchKeyword = ref('');
const selectedCategory = ref(null);

const categoryOptions = ref([]);

// 获取封面完整URL
const getCoverUrl = (url?: string) => {
  if (!url) return '/default-book-cover.jpg';
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

const queryParams = reactive({
  keyword: '',
  categoryId: null as number | null,
  page: 1,
  size: 12
});

const loadBooks = async () => {
  loading.value = true;
  try {
    const response: BookListResponse = await getBookList(queryParams);
    bookList.value = response.records;
    total.value = response.total;
  } catch (error) {
    message.error('加载图书失败');
  } finally {
    loading.value = false;
  }
};

const loadCategories = async () => {
  try {
    const response = await getCategories();
    categoryOptions.value = response.data.map((cat: any) => ({
      label: cat.name,
      value: cat.categoryId
    }));
  } catch (error) {
    console.error('加载分类失败', error);
  }
};

const handleSearch = () => {
  queryParams.keyword = searchKeyword.value;
  queryParams.page = 1;
  page.value = 1;
  loadBooks();
};

const handleFilter = () => {
  queryParams.categoryId = selectedCategory.value;
  queryParams.page = 1;
  page.value = 1;
  loadBooks();
};

const handlePageChange = (newPage: number) => {
  queryParams.page = newPage;
  page.value = newPage;
  loadBooks();
};

const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`);
};

onMounted(() => {
  loadBooks();
  loadCategories();
});
</script>

<style scoped>
.books-container {
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

/* Hero区域 */
.books-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 20px;
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 18px;
  margin: 0;
  opacity: 0.92;
  font-weight: 400;
  letter-spacing: 1px;
}

/* 搜索筛选区域 */
.search-filter-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-20px);
  position: relative;
  z-index: 10;
}

.search-box {
  padding: 24px 24px 16px;
}

.filter-box {
  padding: 0 24px 24px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

/* 图书网格 */
.books-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  margin-bottom: 40px;
}

.book-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.book-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.book-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 10px;
  background: #f0f0f0;
}

.book-cover :deep(.n-image) {
  width: 100%;
  height: 100%;
}

.book-cover :deep(.n-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  padding: 10px 0;
}

.book-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author,
.book-category {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}

.book-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
}

.rating-text {
  font-size: 12px;
  color: #999;
}

.book-stock {
  margin-top: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .search-filter-section {
    transform: translateY(0);
    margin-top: -10px;
  }
  
  .search-box,
  .filter-box {
    padding: 16px;
  }
}
</style>
