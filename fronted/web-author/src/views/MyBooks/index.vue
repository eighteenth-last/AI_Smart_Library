<template>
  <div class="my-books">
    <div class="search-section">
      <n-card>
        <n-form inline :model="searchForm" label-placement="left" label-width="auto">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="书名/ISBN" clearable />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">搜索</n-button>
            <n-button @click="handleReset" style="margin-left: 8px">重置</n-button>
          </n-form-item>
        </n-form>
      </n-card>
    </div>

    <div class="table-section">
      <n-card>
        <div class="table-header">
          <n-button type="primary" @click="handleAdd">新增作品</n-button>
        </div>
        <n-data-table
          :columns="columns"
          :data="bookList"
          :loading="loading"
          :pagination="pagination"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-card>
    </div>

    <!-- 新增/编辑作品弹窗 -->
    <n-modal 
      v-model:show="showModal" 
      :title="modalTitle" 
      preset="card" 
      style="width: 900px;"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <n-scrollbar style="max-height: 60vh; padding-right: 12px;">
        <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="90px">
          <!-- 基本信息分组 -->
          <n-divider title-placement="left" style="margin-top: 0;">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📚 基本信息
            </n-text>
          </n-divider>
          
          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="ISBN" path="isbn">
                <n-input 
                  v-model:value="formData.isbn" 
                  placeholder="请输入图书ISBN编号" 
                  :disabled="isEdit"
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="书名" path="title">
                <n-input 
                  v-model:value="formData.title" 
                  placeholder="请输入图书名称" 
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="分类" path="categoryId">
                <n-select 
                  v-model:value="formData.categoryId" 
                  placeholder="选择图书分类" 
                  :options="categoryOptions"
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="出版社" path="publisher">
                <n-input 
                  v-model:value="formData.publisher" 
                  placeholder="请输入出版社名称" 
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="出版年份" path="publishYear">
                <n-input-number 
                  v-model:value="formData.publishYear" 
                  placeholder="如：2023" 
                  style="width: 100%" 
                  :min="1900" 
                  :max="2100"
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="馆藏总量" path="totalStock">
                <n-input-number 
                  v-model:value="formData.totalStock" 
                  placeholder="请输入库存数量" 
                  min="1" 
                  style="width: 100%"
                  size="large"
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <!-- 封面图片 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              🎨 封面图片
            </n-text>
          </n-divider>
          
          <div class="cover-section">
            <div class="cover-main">
              <div class="cover-preview-container">
                <div v-if="getCoverUrl(formData.coverUrl)" class="has-cover">
                  <img :src="getCoverUrl(formData.coverUrl)" alt="封面预览" class="preview-image" />
                  <div class="cover-overlay">
                    <n-upload
                      :show-file-list="false"
                      @before-upload="handleCoverUpload"
                    >
                      <n-button type="primary" size="medium" ghost>
                        <template #icon>
                          <span style="font-size: 18px;">📷</span>
                        </template>
                        更换封面
                      </n-button>
                    </n-upload>
                  </div>
                </div>
                <div v-else class="no-cover">
                  <div class="empty-icon">📖</div>
                  <div class="empty-text">暂无封面</div>
                  <n-upload
                    :show-file-list="false"
                    @before-upload="handleCoverUpload"
                  >
                    <n-button type="primary" size="medium">
                      <template #icon>
                        <span style="font-size: 18px;">⬆️</span>
                      </template>
                      上传封面
                    </n-button>
                  </n-upload>
                </div>
              </div>
            </div>
            <div class="cover-tips-panel">
              <div class="tips-header">
                <span style="font-size: 16px;">💡</span>
                <span style="font-weight: 600; color: #374151;">上传要求</span>
              </div>
              <div class="tips-list">
                <div class="tip-item">
                  <span class="tip-dot">•</span>
                  <span>建议尺寸：600×800 像素</span>
                </div>
                <div class="tip-item">
                  <span class="tip-dot">•</span>
                  <span>支持格式：JPG、PNG</span>
                </div>
                <div class="tip-item">
                  <span class="tip-dot">•</span>
                  <span>文件大小：不超过 20MB</span>
                </div>
              </div>
              <div v-if="uploadedCoverUrl" class="upload-success-tip">
                <span style="font-size: 18px;">✅</span>
                <span>封面上传成功！</span>
              </div>
            </div>
          </div>

          <!-- 详细信息 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📝 详细信息
            </n-text>
          </n-divider>

          <n-form-item label="图书简介" path="description">
            <n-input 
              v-model:value="formData.description" 
              type="textarea" 
              placeholder="请输入图书简介，介绍图书的主要内容、特色、作者背景等..." 
              :rows="6"
              :maxlength="500"
              show-count
              size="large"
            />
          </n-form-item>

          <n-form-item label="图书标签" path="tagIds">
            <n-select 
              v-model:value="formData.tagIds" 
              multiple 
              placeholder="选择标签（可多选，最多3个）" 
              :options="tagOptions"
              :max-tag-count="3"
              size="large"
            />
          </n-form-item>
        </n-form>
      </n-scrollbar>
      
      <template #footer>
        <n-space justify="end" :size="16">
          <n-button @click="showModal = false" size="large" style="min-width: 120px;">
            <template #icon>
              <span style="font-size: 18px;">✕</span>
            </template>
            取消
          </n-button>
          <n-button 
            type="primary" 
            @click="handleSubmit" 
            :loading="submitLoading" 
            size="large"
            style="min-width: 120px;"
          >
            <template #icon>
              <span style="font-size: 18px;">✓</span>
            </template>
            {{ isEdit ? '保存修改' : '立即添加' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 查看详情弹窗 -->
    <n-modal v-model:show="showDetailModal" title="作品详情" preset="card" style="width: 900px">
      <n-spin :show="detailLoading">
        <n-tabs type="line" animated>
          <!-- 基本信息 -->
          <n-tab-pane name="basic" tab="基本信息">
            <div class="detail-basic">
              <div class="detail-cover">
                <img 
                  v-if="bookDetail.coverUrl" 
                  :src="getCoverUrl(bookDetail.coverUrl)" 
                  alt="封面" 
                  class="detail-cover-image"
                  @error="(e) => (e.target as HTMLImageElement).src = '/default-book-cover.jpg'"
                />
                <div v-else class="detail-cover-placeholder">暂无封面</div>
              </div>
              <div class="detail-info">
                <h2 class="detail-title">{{ bookDetail.title || '未知书名' }}</h2>
                <n-space vertical size="small">
                  <div><strong>ISBN:</strong> {{ bookDetail.isbn || '-' }}</div>
                  <div><strong>出版社:</strong> {{ bookDetail.publisher || '-' }}</div>
                  <div><strong>出版年份:</strong> {{ bookDetail.publishYear || '-' }}</div>
                  <div><strong>馆藏总量:</strong> {{ bookDetail.totalStock || 0 }}</div>
                  <div><strong>平均评分:</strong> 
                    <n-rate :value="bookDetail.averageRating || 0" readonly allow-half size="small" />
                    <span style="margin-left: 8px;">{{ (bookDetail.averageRating || 0).toFixed(1) }}</span>
                  </div>
                  <div><strong>简介:</strong></div>
                  <div class="detail-description">{{ bookDetail.description || '暂无简介' }}</div>
                </n-space>
              </div>
            </div>
          </n-tab-pane>

          <!-- 借阅情况 -->
          <n-tab-pane name="borrow" tab="借阅情况">
            <div class="detail-stats">
              <n-statistic label="总借阅次数" :value="borrowStats.totalBorrows" />
              <n-statistic label="当前借出" :value="borrowStats.currentBorrowed" />
              <n-statistic label="剩余库存" :value="borrowStats.availableStock" />
            </div>
            <n-divider />
            <h3>最近借阅记录</h3>
            <n-data-table
              :columns="borrowColumns"
              :data="borrowList"
              :pagination="false"
              size="small"
              max-height="300"
            />
          </n-tab-pane>

          <!-- 评价情况 -->
          <n-tab-pane name="review" tab="评价情况">
            <div class="detail-stats">
              <n-statistic label="评价总数" :value="reviewStats.totalReviews" />
              <n-statistic label="平均评分" :value="reviewStats.averageRating?.toFixed(1)" />
              <n-statistic label="5星评价" :value="reviewStats.fiveStarCount" />
            </div>
            <n-divider />
            <h3>最近评价</h3>
            <div class="review-list">
              <div v-for="review in reviewList" :key="review.reviewId" class="review-item">
                <div class="review-header">
                  <span class="review-user">{{ review.username || '匿名用户' }}</span>
                  <n-rate :value="review.rating || 0" readonly size="small" />
                </div>
                <div class="review-content">{{ review.content || '暂无评价内容' }}</div>
                <div class="review-time">{{ formatDate(review.createdAt) }}</div>
              </div>
              <n-empty v-if="reviewList.length === 0" description="暂无评价" />
            </div>
          </n-tab-pane>

          <!-- 收藏情况 -->
          <n-tab-pane name="favorite" tab="收藏情况">
            <div class="detail-stats">
              <n-statistic label="收藏总数" :value="favoriteStats.totalFavorites" />
              <n-statistic label="本周新增" :value="favoriteStats.weeklyNew" />
              <n-statistic label="本月新增" :value="favoriteStats.monthlyNew" />
            </div>
            <n-divider />
            <h3>最近收藏用户</h3>
            <n-data-table
              :columns="favoriteColumns"
              :data="favoriteList"
              :pagination="false"
              size="small"
              max-height="300"
            />
          </n-tab-pane>
        </n-tabs>
      </n-spin>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage, NImage, NTag, NSpace, NButton, NRate, type UploadFileInfo } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { getAuthorBooks, type AuthorBook } from '@/api/author/books';
import request from '@/utils/request';

const router = useRouter();
const message = useMessage();

const loading = ref(false);
const bookList = ref<AuthorBook[]>([]);
const showModal = ref(false);
const modalTitle = ref('新增作品');
const submitLoading = ref(false);
const uploadedCoverUrl = ref('');
const isEdit = ref(false);

// 详情相关
const showDetailModal = ref(false);
const detailLoading = ref(false);
const bookDetail = ref<any>({});
const borrowStats = ref({ totalBorrows: 0, currentBorrowed: 0, availableStock: 0 });
const borrowList = ref<any[]>([]);
const reviewStats = ref({ totalReviews: 0, averageRating: 0, fiveStarCount: 0 });
const reviewList = ref<any[]>([]);
const favoriteStats = ref({ totalFavorites: 0, weeklyNew: 0, monthlyNew: 0 });
const favoriteList = ref<any[]>([]);

const categoryOptions = ref([]);
const tagOptions = ref([]);

const searchForm = reactive({
  keyword: '',
  page: 1,
  size: 10
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
});

const formRef = ref();
const formData = reactive({
  bookId: null as number | null,
  isbn: '',
  title: '',
  categoryId: null as number | null,
  publisher: '',
  publishYear: new Date().getFullYear(),
  coverUrl: '',
  description: '',
  totalStock: 10,
  tagIds: [] as number[]
});

const formRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, type: 'number', message: '请选择分类', trigger: 'change' }],
  totalStock: [{ required: true, type: 'number', message: '请输入馆藏总量', trigger: 'blur' }]
};

const createColumns = (): DataTableColumns => [
  {
    title: '封面',
    key: 'coverUrl',
    width: 80,
    render: (row: any) => {
      return h(NImage, {
        src: getCoverUrl(row.coverUrl) || '/default-book-cover.jpg',
        width: 60,
        height: 80,
        objectFit: 'cover',
        fallbackSrc: '/default-book-cover.jpg'
      });
    }
  },
  { title: '书名', key: 'title', width: 150, ellipsis: { tooltip: true } },
  { title: 'ISBN', key: 'isbn', width: 120 },
  { title: '出版社', key: 'publisher', width: 120 },
  { title: '出版年份', key: 'publishYear', width: 90 },
  { title: '馆藏', key: 'totalStock', width: 80 },
  { title: '借阅次数', key: 'borrowCount', width: 90 },
  {
    title: '评分',
    key: 'averageRating',
    width: 120,
    render: (row: any) => {
      return h(NSpace, { size: 4, align: 'center' }, {
        default: () => [
          h(NRate, { value: row.averageRating, readonly: true, allowHalf: true, size: 'small' }),
          h('span', { style: 'font-size: 12px; color: #ff9800;' }, row.averageRating.toFixed(1))
        ]
      });
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) => {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default' }, {
        default: () => row.status === 1 ? '在售' : '下架'
      });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row: any) => {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleView(row) }, { default: () => '查看' }),
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' })
        ]
      });
    }
  }
];

const columns = createColumns();

// 借阅记录表格列
const borrowColumns = [
  { title: '借阅人', key: 'username', width: 120 },
  { 
    title: '借阅日期', 
    key: 'borrowTime',
    width: 150,
    render: (row: any) => formatDate(row.borrowTime)
  },
  { 
    title: '应还日期', 
    key: 'dueTime',
    width: 150,
    render: (row: any) => formatDate(row.dueTime)
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render: (row: any) => {
      const statusMap: Record<string, { type: any; text: string }> = {
        'borrowed': { type: 'warning', text: '借阅中' },
        'returned': { type: 'success', text: '已归还' },
        'overdue': { type: 'error', text: '已逾期' }
      };
      const statusInfo = statusMap[row.status] || { type: 'default', text: '未知' };
      return h(NTag, { type: statusInfo.type }, { default: () => statusInfo.text });
    }
  }
];

// 收藏用户表格列
const favoriteColumns = [
  { title: '用户名', key: 'username', width: 150 },
  { 
    title: '收藏时间', 
    key: 'createdAt',
    width: 180,
    render: (row: any) => formatDate(row.createdAt)
  },
  { 
    title: '用户角色', 
    key: 'role',
    width: 100,
    render: (row: any) => {
      const roleMap: Record<string, string> = {
        'reader': '读者',
        'author': '作者',
        'admin': '管理员'
      };
      return h(NTag, { size: 'small' }, { default: () => roleMap[row.role] || '未知' });
    }
  }
];

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getAuthorBooks(searchForm.keyword);
    bookList.value = data;
    pagination.itemCount = data.length;
  } catch (error: any) {
    message.error('加载作品列表失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  searchForm.page = 1;
  pagination.page = 1;
  loadData();
};

const handleReset = () => {
  searchForm.keyword = '';
  handleSearch();
};

const handlePageChange = (page: number) => {
  searchForm.page = page;
  pagination.page = page;
  loadData();
};

const handlePageSizeChange = (pageSize: number) => {
  searchForm.size = pageSize;
  pagination.pageSize = pageSize;
  handleSearch();
};

const handleAdd = () => {
  modalTitle.value = '新增作品';
  isEdit.value = false;
  Object.assign(formData, {
    bookId: null,
    isbn: '',
    title: '',
    categoryId: null,
    publisher: '',
    publishYear: new Date().getFullYear(),
    coverUrl: '',
    description: '',
    totalStock: 10,
    tagIds: []
  });
  uploadedCoverUrl.value = '';
  showModal.value = true;
};

const handleView = async (row: any) => {
  showDetailModal.value = true;
  detailLoading.value = true;
  
  // 初始化数据 - 使用book表的字段
  bookDetail.value = { ...row };
  // 借阅统计数据
  borrowStats.value = { 
    totalBorrows: row.borrowCount || 0,  // 使用book表的borrow_count字段
    currentBorrowed: 0,  // 待计算
    availableStock: row.availableStock || 0  // 使用book表的available_stock字段
  };
  borrowList.value = [];
  reviewStats.value = { totalReviews: 0, averageRating: row.averageRating || 0, fiveStarCount: 0 };
  reviewList.value = [];
  favoriteStats.value = { totalFavorites: 0, weeklyNew: 0, monthlyNew: 0 };
  favoriteList.value = [];
  
  try {
    // 并行获取所有数据
    const [borrowRes, reviewRes, favoriteRes] = await Promise.allSettled([
      // 获取借阅情况 - 使用公开接口
      request.get('/borrow/books/' + row.bookId + '/records', { 
        params: { 
          page: 1,
          size: 10
        } 
      }),
      // 获取评价情况 - 使用公开接口
      request.get('/books/' + row.bookId + '/reviews', { 
        params: { 
          page: 1,
          size: 10
        } 
      }),
      // 获取收藏情况
      request.get('/books/' + row.bookId + '/favorites', { 
        params: { 
          page: 1,
          size: 10
        } 
      })
    ]);
    
    // 处理借阅数据
    if (borrowRes.status === 'fulfilled' && borrowRes.value.data.code === 200) {
      const borrowData = borrowRes.value.data.data;
      const records = borrowData.records || [];
      // 计算当前借出数量（status字段：borrowed/returned/overdue）
      const currentBorrowed = records.filter((r: any) => r.status === 'borrowed' || r.status === 'overdue').length;
      borrowStats.value = {
        totalBorrows: row.borrowCount || 0,  // 总借阅次数用book表的borrow_count
        currentBorrowed: currentBorrowed,
        availableStock: row.availableStock || 0  // 可用库存用book表的available_stock
      };
      // 格式化借阅记录（不需要手动格式化，后端已返回格式化的时间）
      borrowList.value = records;
    }
    
    // 处理评价数据
    if (reviewRes.status === 'fulfilled' && reviewRes.value.data.code === 200) {
      const reviewData = reviewRes.value.data.data;
      const reviews = reviewData.records || [];
      // 计算统计数据
      const fiveStarCount = reviews.filter((r: any) => r.rating === 5).length;
      reviewStats.value = {
        totalReviews: reviewData.total || reviews.length,
        averageRating: row.averageRating || 0,  // 使用book表的average_rating
        fiveStarCount: fiveStarCount
      };
      reviewList.value = reviews;
    }
    
    // 处理收藏数据
    if (favoriteRes.status === 'fulfilled' && favoriteRes.value.data.code === 200) {
      const favoriteData = favoriteRes.value.data.data;
      // PageResult结构：{ records, total, page, size }
      const favorites = favoriteData.records || [];
      
      // 计算时间范围
      const now = new Date();
      const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      
      const weeklyNew = favorites.filter((f: any) => 
        new Date(f.createdAt) > weekAgo
      ).length;
      const monthlyNew = favorites.filter((f: any) => 
        new Date(f.createdAt) > monthAgo
      ).length;
      
      favoriteStats.value = {
        totalFavorites: favoriteData.total || 0,
        weeklyNew: weeklyNew,
        monthlyNew: monthlyNew
      };
      favoriteList.value = favorites;
    }
  } catch (error: any) {
    message.error('加载详情失败：' + (error.message || '未知错误'));
  } finally {
    detailLoading.value = false;
  }
};

const handleEdit = async (row: any) => {
  modalTitle.value = '编辑作品';
  isEdit.value = true;
  
  try {
    const response = await request.get(`/admin/books/${row.bookId}`);
    if (response.data.code === 200) {
      const book = response.data.data;
      Object.assign(formData, {
        bookId: book.bookId,
        isbn: book.isbn,
        title: book.title,
        categoryId: book.categoryId,
        publisher: book.publisher,
        publishYear: book.publishYear,
        coverUrl: book.coverUrl,
        description: book.description,
        totalStock: book.totalStock,
        tagIds: book.tagIds || []
      });
      uploadedCoverUrl.value = '';
      showModal.value = true;
    }
  } catch (error: any) {
    message.error('加载作品信息失败：' + (error.message || '未知错误'));
  }
};

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await request.get('/categories/list');
    if (response.data.code === 200) {
      const flattenCategories = (categories: any[]): any[] => {
        let result: any[] = [];
        categories.forEach((cat: any) => {
          result.push({ label: cat.name, value: cat.categoryId });
          if (cat.children && cat.children.length > 0) {
            result = result.concat(flattenCategories(cat.children));
          }
        });
        return result;
      };
      categoryOptions.value = flattenCategories(response.data.data);
    }
  } catch (error) {
    console.error('加载分类失败:', error);
  }
};

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await request.get('/tags/list');
    if (response.data.code === 200) {
      tagOptions.value = response.data.data.map((tag: any) => ({
        label: tag.name,
        value: tag.tagId
      }));
    }
  } catch (error) {
    console.error('加载标签失败:', error);
  }
};

// 处理封面上传
const handleCoverUpload = async (options: { file: UploadFileInfo }) => {
  const file = options.file.file;
  if (!file) return;

  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }

  const isLt20M = file.size / 1024 / 1024 < 20;
  if (!isLt20M) {
    message.error('图片大小不能超过 20MB！');
    return false;
  }

  try {
    const uploadFormData = new FormData();
    uploadFormData.append('file', file);
    
    const response = await request.post('/file/upload/book-cover', uploadFormData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (response.data.code === 200) {
      uploadedCoverUrl.value = response.data.data.url;
      formData.coverUrl = response.data.data.url;
      message.success('封面上传成功');
    }
  } catch (error: any) {
    console.error('封面上传失败:', error);
    message.error('封面上传失败');
  }

  return false;
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;
    
    const finalCoverUrl = uploadedCoverUrl.value || formData.coverUrl;
    
    const submitData = {
      isbn: formData.isbn,
      title: formData.title,
      categoryId: formData.categoryId,
      publisher: formData.publisher,
      publishYear: formData.publishYear,
      coverUrl: finalCoverUrl,
      description: formData.description,
      totalStock: formData.totalStock,
      tagIds: formData.tagIds
    };
    
    let response;
    if (isEdit.value && formData.bookId) {
      // 编辑模式
      response = await request.put(`/admin/books/${formData.bookId}`, submitData);
    } else {
      // 新增模式
      // 获取当前登录用户信息
      const userInfoStr = localStorage.getItem('userInfo');
      const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {};
      
      response = await request.post('/admin/books', {
        ...submitData,
        authorId: userInfo.userId
      });
    }
    
    if (response.data.code === 200) {
      message.success(isEdit.value ? '编辑成功' : '添加成功');
      showModal.value = false;
      uploadedCoverUrl.value = '';
      loadData();
    } else {
      message.error(response.data.msg || (isEdit.value ? '编辑失败' : '添加失败'));
    }
  } catch (error: any) {
    message.error(isEdit.value ? '编辑失败' : '添加失败');
  } finally {
    submitLoading.value = false;
  }
};

// 获取封面完整URL
const getCoverUrl = (url?: string) => {
  if (!url) return '';
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadData();
  loadCategories();
  loadTags();
});
</script>

<style scoped>
.my-books {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 24px;
}

.search-section {
  margin-bottom: 20px;
}

.search-section :deep(.n-card) {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  transition: box-shadow 0.2s ease;
}

.search-section :deep(.n-card:hover) {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
}

.table-section :deep(.n-card) {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.table-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.n-data-table-th) {
  background: #f9fafb;
  color: #374151;
  font-weight: 600;
}

:deep(.n-data-table-tr:hover .n-data-table-td) {
  background: #f9fafb;
}

.cover-upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* 封面上传区域样式优化 */
.cover-section {
  display: flex;
  gap: 32px;
  padding: 12px 0;
}

.cover-main {
  flex-shrink: 0;
}

.cover-preview-container {
  width: 200px;
  height: 280px;
  position: relative;
}

.has-cover {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.has-cover:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.has-cover:hover .cover-overlay {
  opacity: 1;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(4px);
}

.no-cover {
  width: 100%;
  height: 100%;
  border: 3px dashed #d1d5db;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  transition: all 0.3s ease;
}

.no-cover:hover {
  border-color: #18a058;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  transform: scale(1.02);
}

.empty-icon {
  font-size: 64px;
  opacity: 0.5;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-text {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.cover-tips-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.tip-dot {
  color: #18a058;
  font-weight: bold;
  font-size: 16px;
}

.upload-success-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 1px solid #86efac;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #166534;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  color: #9ca3af;
  font-size: 13px;
}

/* 分割线样式优化 */
.my-books :deep(.n-divider) {
  margin: 24px 0 20px 0;
}

.my-books :deep(.n-divider:first-child) {
  margin-top: 0;
}

.my-books :deep(.n-divider .n-divider__title) {
  font-weight: 600;
}

.my-books :deep(.n-divider .n-divider__line) {
  background-color: #e5e7eb;
}

/* 表单样式优化 */
.my-books :deep(.n-form-item-label) {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.my-books :deep(.n-input),
.my-books :deep(.n-input-number),
.my-books :deep(.n-select),
.my-books :deep(.n-input__input) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.my-books :deep(.n-input:hover),
.my-books :deep(.n-input-number:hover),
.my-books :deep(.n-select:hover) {
  border-color: #18a058;
}

.my-books :deep(.n-input:focus-within),
.my-books :deep(.n-input-number:focus-within),
.my-books :deep(.n-select:focus-within) {
  border-color: #18a058;
  box-shadow: 0 0 0 3px rgba(24, 160, 88, 0.1);
}

.my-books :deep(.n-input--textarea .n-input__textarea) {
  border-radius: 8px;
}

/* 滚动条样式 */
.my-books :deep(.n-scrollbar-rail) {
  right: 2px;
}

.my-books :deep(.n-scrollbar-rail__scrollbar) {
  background-color: #d1d5db;
  border-radius: 4px;
}

.my-books :deep(.n-scrollbar-rail__scrollbar:hover) {
  background-color: #9ca3af;
}

/* 详情弹窗样式 */
.detail-basic {
  display: flex;
  gap: 24px;
  padding: 16px 0;
}

.detail-cover {
  flex-shrink: 0;
}

.detail-cover-image {
  width: 180px;
  height: 240px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.detail-cover-placeholder {
  width: 180px;
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #9ca3af;
  font-size: 14px;
}

.detail-info {
  flex: 1;
}

.detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.detail-description {
  color: #6b7280;
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  margin-top: 8px;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.review-list {
  max-height: 400px;
  overflow-y: auto;
}

.review-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: 6px;
  margin-bottom: 12px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 600;
  color: #1f2937;
}

.review-content {
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
