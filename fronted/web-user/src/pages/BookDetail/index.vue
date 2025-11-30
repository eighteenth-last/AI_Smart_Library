<template>
  <div class="book-detail-page">
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
          <router-link to="/favorites" class="menu-item">我的收藏</router-link>
          <router-link to="/borrow-history" class="menu-item">借阅记录</router-link>
        </div>
        <div class="navbar-actions">
          <NavBar />
        </div>
      </div>
    </nav>

    <div class="book-detail-container" v-if="book">
    <div class="book-main-info">
      <n-grid :cols="3" :x-gap="30">
        <n-grid-item>
          <div class="book-cover-wrapper">
            <div class="book-cover">
              <img :src="getCoverUrl(book.coverUrl)" :alt="book.title" />
            </div>
          </div>
        </n-grid-item>
        <n-grid-item :span="2">
          <div class="book-details">
            <h1 class="book-title">{{ book.title }}</h1>
            
            <div class="book-rating-section">
              <div class="rating-display">
                <n-rate :value="book.averageRating" readonly allow-half size="large" color="#FFB800" />
                <span class="rating-score">{{ book.averageRating.toFixed(1) }}</span>
              </div>
              <span class="review-count">{{ book.reviewCount }} 条评价</span>
            </div>

            <div class="book-meta">
              <div class="meta-item">
                <span class="meta-label">作者</span>
                <span class="meta-value">{{ book.author.name }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">出版社</span>
                <span class="meta-value">{{ book.publisher }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">出版年份</span>
                <span class="meta-value">{{ book.publishYear }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">ISBN</span>
                <span class="meta-value">{{ book.isbn }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">分类</span>
                <n-tag type="info" size="small" round>{{ book.category.name }}</n-tag>
              </div>
              <div class="meta-item">
                <span class="meta-label">库存</span>
                <n-tag :type="book.availableStock > 0 ? 'success' : 'error'" size="medium" round>
                  {{ book.availableStock > 0 ? `可借 ${book.availableStock}/${book.totalStock}` : '暂无可借' }}
                </n-tag>
              </div>
            </div>

            <!-- 图书简介 -->
            <div class="book-description-inline">
              <h3 class="description-title">图书简介</h3>
              <p class="description-content">{{ book.description }}</p>
            </div>

            <div class="book-actions">
              <n-button 
                type="primary" 
                size="large"
                :disabled="book.availableStock === 0"
                @click="handleBorrow"
                strong
              >
                <template #icon>
                  <n-icon><book-outline /></n-icon>
                </template>
                {{ book.availableStock > 0 ? '立即借阅' : '暂无可借' }}
              </n-button>
              <n-button 
                size="large" 
                :type="isFavorited ? 'error' : 'default'"
                @click="handleFavorite"
              >
                <template #icon>
                  <n-icon><heart-outline v-if="!isFavorited" /><heart v-else /></n-icon>
                </template>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </n-button>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <div class="book-tags" v-if="book.tags && book.tags.length > 0">
      <h3>标签</h3>
      <n-space>
        <n-tag v-for="tag in book.tags" :key="tag" type="info" size="small">
          {{ tag }}
        </n-tag>
      </n-space>
    </div>

    <!-- 图书评价区域 -->
    <div class="book-reviews">
      <div class="reviews-header">
        <h3>图书评价</h3>
        <n-button v-if="userStore.isLoggedIn()" type="primary" @click="showReviewModal = true">
          <template #icon>
            <n-icon><create-outline /></n-icon>
          </template>
          写评价
        </n-button>
      </div>

      <!-- 评价统计 -->
      <div class="review-stats">
        <div class="rating-summary">
          <div class="rating-score">{{ book.averageRating.toFixed(1) }}</div>
          <n-rate :value="book.averageRating" readonly allow-half size="large" />
          <div class="rating-count">{{ book.reviewCount }}条评价</div>
        </div>
      </div>

      <!-- 评价列表 -->
      <n-spin :show="reviewsLoading">
        <div v-if="reviews.length === 0" class="empty-reviews">
          <n-empty description="暂无评价">
            <template #icon>
              <n-icon size="60" color="#d0d0d0">
                <chatbox-outline />
              </n-icon>
            </template>
          </n-empty>
        </div>
        <div v-else class="reviews-list">
          <n-card v-for="review in reviews" :key="review.reviewId" class="review-item" :bordered="false">
            <div class="review-header">
              <div class="reviewer-info">
                <n-avatar round :size="40" color="#667eea">
                  {{ review.nickname?.charAt(0) || review.username?.charAt(0) || 'U' }}
                </n-avatar>
                <div class="reviewer-detail">
                  <div class="reviewer-name">{{ review.nickname || review.username }}</div>
                  <div class="review-time">{{ formatDate(review.createdAt) }}</div>
                </div>
              </div>
              <n-rate :value="review.rating" readonly size="small" />
            </div>
            <div class="review-content">{{ review.content || '该用户未填写评价内容' }}</div>
          </n-card>
        </div>
      </n-spin>

      <!-- 分页 -->
      <div v-if="reviewPagination.itemCount > reviewPagination.pageSize" class="reviews-pagination">
        <n-pagination
          v-model:page="reviewPagination.page"
          :page-count="Math.ceil(reviewPagination.itemCount / reviewPagination.pageSize)"
          :page-size="reviewPagination.pageSize"
          @update:page="loadReviews"
        />
      </div>
    </div>

    <div class="recommendations" v-if="recommendBooks.length > 0">
      <h3>相似图书推荐</h3>
      <n-grid :cols="4" :x-gap="15" :y-gap="15">
        <n-grid-item v-for="recBook in recommendBooks" :key="recBook.bookId">
          <n-card class="recommend-card" hoverable @click="goToBookDetail(recBook.bookId)">
            <div class="recommend-cover">
              <img :src="getCoverUrl(recBook.coverUrl)" :alt="recBook.title" />
            </div>
            <div class="recommend-info">
              <h4 class="recommend-title">{{ recBook.title }}</h4>
              <p class="recommend-similarity">相似度：{{ recBook.similarityPercent }}</p>
              <p class="recommend-reason">{{ recBook.reason }}</p>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>
  </div>

  <!-- 写评价弹窗 -->
  <n-modal v-model:show="showReviewModal" preset="card" title="写评价" style="width: 600px;">
    <n-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules">
      <n-form-item label="评分" path="rating">
        <n-rate v-model:value="reviewForm.rating" allow-half size="large" />
      </n-form-item>
      <n-form-item label="评价内容" path="content">
        <n-input
          v-model:value="reviewForm.content"
          type="textarea"
          placeholder="分享你的阅读感受..."
          :rows="5"
          :maxlength="500"
          show-count
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showReviewModal = false">取消</n-button>
        <n-button type="primary" @click="submitReview" :loading="submittingReview">提交</n-button>
      </n-space>
    </template>
  </n-modal>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage, type FormInst, type FormRules } from 'naive-ui';
import { getBookDetail, getRecommendBooks, type BookDetail, type RecommendBook } from '@/api/user/books';
import { reviewAPI } from '@/api/user/review';
import { addFavorite, removeFavorite, checkFavorite } from '@/api/user/favorite';
import { borrowAPI } from '@/api/user/borrow';
import { useUserStore } from '@/store/user';
import { CreateOutline, ChatboxOutline, BookOutline, HeartOutline, Heart } from '@vicons/ionicons5';
import NavBar from '@/components/NavBar.vue';

interface Review {
  reviewId: number
  userId: number
  username: string
  nickname: string
  rating: number
  content?: string
  createdAt: string
}

const route = useRoute();
const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const loading = ref(false);
const book = ref<BookDetail | null>(null);
const recommendBooks = ref<RecommendBook[]>([]);
const isFavorited = ref(false);  // 是否已收藏

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

// 评价相关
const reviews = ref<Review[]>([]);
const reviewsLoading = ref(false);
const showReviewModal = ref(false);
const submittingReview = ref(false);
const reviewFormRef = ref<FormInst | null>(null);
const reviewForm = ref({
  rating: 5,
  content: ''
});

const reviewPagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0
});

const reviewRules: FormRules = {
  rating: [
    { required: true, type: 'number', min: 0.5, message: '请选择评分', trigger: 'change' }
  ]
};

const loadBookDetail = async () => {
  const bookId = Number(route.params.id);
  if (!bookId) {
    message.error('无效的图书ID');
    return;
  }

  loading.value = true;
  try {
    const [bookResponse, recommendResponse] = await Promise.all([
      getBookDetail(bookId),
      getRecommendBooks(bookId, 4)
    ]);
    
    book.value = bookResponse;
    recommendBooks.value = recommendResponse;
    
    // 加载评价
    await loadReviews();
    
    // 检查收藏状态（如果已登录）
    if (userStore.isLoggedIn()) {
      await checkFavoriteStatus();
    }
  } catch (error) {
    message.error('加载图书详情失败');
  } finally {
    loading.value = false;
  }
};

// 加载评价
const loadReviews = async () => {
  if (!book.value) return;
  
  reviewsLoading.value = true;
  try {
    const response = await reviewAPI.getReviews(book.value.bookId, {
      page: reviewPagination.value.page,
      size: reviewPagination.value.pageSize
    });
    
    reviews.value = response.records;
    reviewPagination.value.itemCount = response.total;
  } catch (error) {
    console.error('Load reviews error:', error);
  } finally {
    reviewsLoading.value = false;
  }
};

// 提交评价
const submitReview = async () => {
  if (!reviewFormRef.value || !book.value) return;
  
  try {
    await reviewFormRef.value.validate();
    submittingReview.value = true;
    
    await reviewAPI.createReview(book.value.bookId, {
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    });
    
    message.success('评价提交成功');
    showReviewModal.value = false;
    
    // 重置表单
    reviewForm.value = {
      rating: 5,
      content: ''
    };
    
    // 重新加载评价和图书信息
    await Promise.all([loadReviews(), loadBookDetail()]);
  } catch (error: any) {
    if (error.message) {
      message.error(error.message);
    }
  } finally {
    submittingReview.value = false;
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  if (days < 30) return `${Math.floor(days / 7)}周前`;
  
  return date.toLocaleDateString('zh-CN');
};

const handleBorrow = async () => {
  if (!book.value) return;
  
  // 检查是否登录
  if (!userStore.isLoggedIn()) {
    message.warning('请先登录');
    router.push('/login');
    return;
  }
  
  if (book.value.availableStock === 0) {
    message.warning('该图书暂无可借');
    return;
  }
  
  try {
    await borrowAPI.borrowBook(book.value.bookId);
    message.success('借阅申请已提交，请等待管理员审核');
    
    // 刷新图书信息（更新可借数量）
    await loadBookDetail();
  } catch (error: any) {
    console.error('借阅失败', error);
    message.error(error.message || '借阅申请提交失败');
  }
};

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!book.value) return;
  
  try {
    const result = await checkFavorite(book.value.bookId);
    isFavorited.value = result;
  } catch (error) {
    console.error('检查收藏状态失败', error);
  }
};

// 处理收藏操作
const handleFavorite = async () => {
  if (!book.value) return;
  
  // 检查是否登录
  if (!userStore.isLoggedIn()) {
    message.warning('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    if (isFavorited.value) {
      // 取消收藏
      await removeFavorite(book.value.bookId);
      isFavorited.value = false;
      message.success('已取消收藏');
    } else {
      // 添加收藏
      await addFavorite(book.value.bookId);
      isFavorited.value = true;
      message.success('已添加到收藏');
    }
  } catch (error: any) {
    message.error(error.message || '操作失败');
  }
};

const goToBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`);
};

onMounted(() => {
  loadBookDetail();
});
</script>

<style scoped>
.book-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏样式 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

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

.navbar-menu {
  display: flex;
  gap: 8px;
}

.menu-item {
  padding: 8px 16px;
  border-radius: 8px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
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
}

.user-btn:hover {
  background: rgba(102, 126, 234, 0.08);
}

.book-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

.book-main-info {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.book-cover-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.book-cover {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.book-cover:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.book-cover img {
  width: 100%;
  max-width: 320px;
  height: auto;
  display: block;
  border-radius: 12px;
}

.book-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.book-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #1a1a1a;
  line-height: 1.3;
  letter-spacing: 0.5px;
}

.book-rating-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
  border-radius: 12px;
  border-left: 4px solid #FFB800;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-score {
  font-size: 28px;
  font-weight: 700;
  color: #FFB800;
  line-height: 1;
}

.review-count {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.book-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  min-width: 70px;
}

.meta-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.book-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.book-actions .n-button {
  flex: 1;
  max-width: 200px;
}

.book-description-inline {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.description-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.description-content {
  font-size: 14px;
  line-height: 1.8;
  color: #555;
  margin: 0;
  text-align: justify;
}

.book-description {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.book-description h2 {
  font-size: 22px;
  margin-bottom: 15px;
  color: #333;
}

.book-description p {
  line-height: 1.8;
  color: #666;
  font-size: 16px;
}

.book-tags {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.book-tags h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

/* 评价区域样式 */
.book-reviews {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.reviews-header h3 {
  font-size: 20px;
  margin: 0;
  color: #333;
  font-weight: 600;
}

.review-stats {
  padding: 20px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 8px;
  margin-bottom: 25px;
}

.rating-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.rating-score {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  line-height: 1;
}

.rating-count {
  font-size: 14px;
  color: #999;
}

.empty-reviews {
  padding: 40px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.review-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reviewer-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-content {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  margin-top: 10px;
}

.reviews-pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.recommendations {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.recommendations h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.recommend-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.recommend-card:hover {
  transform: translateY(-5px);
}

.recommend-cover {
  width: 100%;
  height: 150px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 10px;
}

.recommend-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-title {
  font-size: 14px;
  font-weight: bold;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-similarity {
  font-size: 12px;
  color: #ff9800;
  font-weight: bold;
  margin: 4px 0;
}

.recommend-reason {
  font-size: 11px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
