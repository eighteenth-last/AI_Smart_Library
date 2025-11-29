<template>
  <div class="book-management">
    <div class="search-section">
      <n-card>
        <n-form inline :model="searchForm" label-placement="left" label-width="auto">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="书名/作者/ISBN" clearable />
          </n-form-item>
          <n-form-item label="分类">
            <n-select 
              v-model:value="searchForm.categoryId" 
              placeholder="选择分类" 
              clearable 
              style="width: 150px" 
              :options="categoryOptions"
              :loading="!categoryOptions || categoryOptions.length === 0"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select 
              v-model:value="searchForm.status" 
              placeholder="选择状态" 
              clearable 
              style="width: 150px"
              :options="[
                { label: '在售', value: 1 },
                { label: '下架', value: 0 }
              ]"
            />
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
          <n-button type="primary" @click="handleAdd">新增图书</n-button>
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
        <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="110px">
          <!-- 基本信息 -->
          <n-divider title-placement="left" style="margin-top: 0;">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📚 基本信息
            </n-text>
          </n-divider>
          
          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="ISBN" path="isbn">
                <n-input v-model:value="formData.isbn" placeholder="请输入ISBN" />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="书名" path="title">
                <n-input v-model:value="formData.title" placeholder="请输入书名" />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="作者" path="authorId">
                <n-select 
                  v-model:value="formData.authorId" 
                  placeholder="选择作者" 
                  :options="authorOptions" 
                  filterable 
                />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="分类" path="categoryId">
                <n-select 
                  v-model:value="formData.categoryId" 
                  placeholder="选择分类" 
                  :options="categoryOptions" 
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <!-- 出版信息 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📖 出版信息
            </n-text>
          </n-divider>

          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="出版社" path="publisher">
                <n-input v-model:value="formData.publisher" placeholder="请输入出版社" />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="出版年份" path="publishYear">
                <n-input-number 
                  v-model:value="formData.publishYear" 
                  placeholder="请输入出版年份" 
                  :min="1900"
                  :max="new Date().getFullYear() + 5"
                  style="width: 100%" 
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <!-- 封面图片 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              🖼️ 封面图片
            </n-text>
          </n-divider>

          <n-form-item>
            <div class="cover-upload-container">
              <div class="cover-preview-box">
                <div v-if="getCoverUrl(formData.coverUrl)" class="cover-image-wrapper">
                  <img :src="getCoverUrl(formData.coverUrl)" alt="封面预览" class="cover-image" />
                  <div class="cover-mask">
                    <n-button text @click="handleRemoveCover" style="color: white;">
                      <template #icon>
                        <n-icon size="20">
                          <svg viewBox="0 0 24 24">
                            <path fill="currentColor" d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
                          </svg>
                        </n-icon>
                      </template>
                      删除
                    </n-button>
                  </div>
                </div>
                <div v-else class="cover-placeholder">
                  <n-icon size="40" color="#d0d0d0">
                    <svg viewBox="0 0 24 24">
                      <path fill="currentColor" d="M13,9H18.5L13,3.5V9M6,2H14L20,8V20A2,2 0 0,1 18,22H6C4.89,22 4,21.1 4,20V4C4,2.89 4.89,2 6,2M6,20H15L18,20V12L14,16L12,14L6,20M8,9A2,2 0 0,0 6,11A2,2 0 0,0 8,13A2,2 0 0,0 10,11A2,2 0 0,0 8,9Z"/>
                    </svg>
                  </n-icon>
                  <n-text style="margin-top: 8px; color: #a0a0a0; font-size: 13px;">暂无封面</n-text>
                </div>
              </div>
              <div class="cover-upload-actions">
                <n-upload
                  :show-file-list="false"
                  @before-upload="handleCoverUpload"
                >
                  <n-button type="primary" secondary>
                    <template #icon>
                      <n-icon>
                        <svg viewBox="0 0 24 24">
                          <path fill="currentColor" d="M9,16V10H5L12,3L19,10H15V16H9M5,20V18H19V20H5Z"/>
                        </svg>
                      </n-icon>
                    </template>
                    {{ formData.coverUrl ? '更换封面' : '上传封面' }}
                  </n-button>
                </n-upload>
                <div v-if="uploadedCoverUrl" class="upload-success-tip">
                  <n-icon size="16" color="#18a058">
                    <svg viewBox="0 0 24 24">
                      <path fill="currentColor" d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M11,16.5L18,9.5L16.59,8.09L11,13.67L7.91,10.59L6.5,12L11,16.5Z"/>
                    </svg>
                  </n-icon>
                  <n-text style="color: #18a058; font-size: 13px; margin-left: 4px;">封面已上传</n-text>
                </div>
                <n-text depth="3" style="font-size: 12px; margin-top: 8px;">
                  支持 JPG、PNG 格式，建议尺寸 600x800px
                </n-text>
              </div>
            </div>
          </n-form-item>

          <!-- 图书详情 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📝 图书详情
            </n-text>
          </n-divider>

          <n-form-item label="简介" path="description">
            <n-input 
              v-model:value="formData.description" 
              type="textarea" 
              placeholder="请输入图书简介" 
              :rows="4" 
              :maxlength="500"
              show-count
            />
          </n-form-item>

          <!-- 库存与标签 -->
          <n-divider title-placement="left">
            <n-text style="font-size: 15px; font-weight: 600; color: #18a058;">
              📦 库存与标签
            </n-text>
          </n-divider>

          <n-grid :cols="2" :x-gap="20">
            <n-grid-item>
              <n-form-item label="馆藏总量" path="totalStock">
                <n-input-number 
                  v-model:value="formData.totalStock" 
                  placeholder="请输入馆藏总量" 
                  :min="1" 
                  :max="10000"
                  style="width: 100%" 
                >
                  <template #suffix>
                    <n-text depth="3">本</n-text>
                  </template>
                </n-input-number>
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="标签" path="tagIds">
                <n-select 
                  v-model:value="formData.tagIds" 
                  multiple 
                  placeholder="选择标签" 
                  :options="tagOptions" 
                  max-tag-count="responsive"
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>
        </n-form>
      </n-scrollbar>
      
      <template #footer>
        <n-space justify="end" :size="16">
          <n-button @click="showModal = false" size="large">
            <template #icon>
              <n-icon>
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                </svg>
              </n-icon>
            </template>
            取消
          </n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitLoading" size="large">
            <template #icon>
              <n-icon>
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                </svg>
              </n-icon>
            </template>
            {{ modalTitle === '新增图书' ? '立即创建' : '保存修改' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import { useMessage, useDialog, NTag, NSpace, NButton, NImage, type UploadFileInfo } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { bookAPI } from '@/api/admin/books';
import { categoryAPI } from '@/api/admin/category';
import { authorAPI } from '@/api/admin/author';
import { tagAPI } from '@/api/admin/tags';
import { uploadAPI } from '@/api/admin/upload';

const message = useMessage();
const dialog = useDialog();

const loading = ref(false);
const bookList = ref([]);
const showModal = ref(false);
const modalTitle = ref('新增图书');
const submitLoading = ref(false);
const uploadedCoverUrl = ref(''); // 临时存储上传的封面URL

const categoryOptions = ref<any[]>([
  // 添加一个默认测试项，确保下拉框正常工作
  { label: '加载中...', value: null, disabled: true }
]);
const authorOptions = ref([]);
const tagOptions = ref([]);

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: null,
  page: 1,
  size: 10
});

const formRef = ref();
const formData = reactive({
  bookId: null,
  isbn: '',
  title: '',
  authorId: null,
  categoryId: null,
  publisher: '',
  publishYear: new Date().getFullYear(),
  coverUrl: '',
  description: '',
  totalStock: 10,
  tagIds: []
});

const formRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  authorId: [{ required: true, type: 'number', message: '请选择作者', trigger: 'change' }],
  categoryId: [{ required: true, type: 'number', message: '请选择分类', trigger: 'change' }],
  totalStock: [{ required: true, type: 'number', message: '请输入馆藏总量', trigger: 'blur' }]
};

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
});

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
  { title: '作者', key: 'authorName', width: 100 },
  { title: 'ISBN', key: 'isbn', width: 120 },
  { title: '出版社', key: 'publisher', width: 120 },
  { title: '出版年份', key: 'publishYear', width: 90 },
  { title: '馆藏', key: 'totalStock', width: 80 },
  { title: '可借', key: 'availableStock', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) => {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error' }, {
        default: () => row.status === 1 ? '在售' : '下架'
      });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row: any) => {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
        ]
      });
    }
  }
];

const columns = createColumns();

const loadData = async () => {
  loading.value = true;
  try {
    const data = await bookAPI.getList({
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId,
      page: searchForm.page,
      size: searchForm.size
    });
    
    bookList.value = data.records;
    pagination.itemCount = data.total;
  } catch (error: any) {
    console.error('加载图书列表失败:', error);
    message.error('加载图书列表失败');
  } finally {
    loading.value = false;
  }
};

const loadCategories = async () => {
  try {
    const data = await categoryAPI.getList();
    console.log('分类原始数据:', data);
    
    if (!data || (Array.isArray(data) && data.length === 0)) {
      console.warn('分类数据为空');
      categoryOptions.value = [];
      return;
    }
    
    // 将树形结构扁平化为选项列表
    const flattenCategories = (categories: any[]): any[] => {
      let result: any[] = [];
      categories.forEach(cat => {
        result.push({ label: cat.name, value: cat.categoryId });
        // 检查 children 是否存在且为数组
        if (cat.children && Array.isArray(cat.children) && cat.children.length > 0) {
          result = result.concat(flattenCategories(cat.children));
        }
      });
      return result;
    };
    
    categoryOptions.value = flattenCategories(data);
    console.log('分类选项:', categoryOptions.value);
    
    // 如果选项为空，给出提示
    if (categoryOptions.value.length === 0) {
      console.warn('扁平化后的分类选项为空');
    }
  } catch (error) {
    console.error('加载分类失败:', error);
    message.error('加载分类失败');
    categoryOptions.value = [];
  }
};

const loadAuthors = async () => {
  try {
    const data = await authorAPI.getList({ page: 1, size: 1000 });
    authorOptions.value = data.records.map((author: any) => ({
      label: author.name,
      value: author.authorId
    }));
  } catch (error) {
    console.error('加载作者失败:', error);
    message.error('加载作者失败');
  }
};

const loadTags = async () => {
  try {
    const data = await tagAPI.getList();
    tagOptions.value = data.map((tag: any) => ({
      label: tag.name,
      value: tag.tagId
    }));
  } catch (error) {
    console.error('加载标签失败:', error);
    message.error('加载标签失败');
  }
};

const handleSearch = () => {
  searchForm.page = 1;
  pagination.page = 1;
  loadData();
};

const handleReset = () => {
  searchForm.keyword = '';
  searchForm.categoryId = null;
  searchForm.status = null;
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
  modalTitle.value = '新增图书';
  Object.assign(formData, {
    bookId: null,
    isbn: '',
    title: '',
    authorId: null,
    categoryId: null,
    publisher: '',
    publishYear: new Date().getFullYear(),
    coverUrl: '',
    description: '',
    totalStock: 10,
    tagIds: []
  });
  uploadedCoverUrl.value = ''; // 清空临时封面
  showModal.value = true;
};

const handleEdit = (row: any) => {
  modalTitle.value = '编辑图书';
  Object.assign(formData, {
    bookId: row.bookId,
    isbn: row.isbn,
    title: row.title,
    authorId: row.authorId,
    categoryId: row.categoryId,
    publisher: row.publisher,
    publishYear: row.publishYear,
    coverUrl: row.coverUrl,
    description: row.description,
    totalStock: row.totalStock,
    tagIds: row.tagIds || []
  });
  uploadedCoverUrl.value = ''; // 清空临时封面
  showModal.value = true;
};

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除图书 "${row.title}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await bookAPI.deleteBook(row.bookId);
        message.success('删除成功');
        loadData();
      } catch (error: any) {
        console.error('删除失败:', error);
        message.error('删除失败');
      }
    }
  });
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;
    
    //  如果有上传新封面，使用新封面URL
    const finalCoverUrl = uploadedCoverUrl.value || formData.coverUrl;
    
    const submitData = {
      isbn: formData.isbn,
      title: formData.title,
      authorId: formData.authorId,
      categoryId: formData.categoryId,
      publisher: formData.publisher,
      publishYear: formData.publishYear,
      coverUrl: finalCoverUrl,
      description: formData.description,
      totalStock: formData.totalStock,
      tagIds: formData.tagIds
    };
    
    console.log('提交的图书数据:', submitData);
    console.log('封面URL:', finalCoverUrl);
    
    if (formData.bookId) {
      await bookAPI.updateBook(formData.bookId, submitData);
      message.success('更新成功');
    } else {
      await bookAPI.createBook(submitData);
      message.success('添加成功');
    }
    
    showModal.value = false;
    uploadedCoverUrl.value = ''; // 清空临时封面
    loadData();
  } catch (error: any) {
    console.error('提交失败:', error);
    message.error(formData.bookId ? '更新失败' : '添加失败');
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

// 处理封面上传
const handleCoverUpload = async (options: { file: UploadFileInfo }) => {
  const file = options.file.file;
  if (!file) return;

  // 验证文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }

  // 验证文件大小
  const isLt20M = file.size / 1024 / 1024 < 20;
  if (!isLt20M) {
    message.error('图片大小不能超过 20MB！');
    return false;
  }

  try {
    const result = await uploadAPI.uploadBookCover(file);
    console.log('上传返回结果:', result);
    uploadedCoverUrl.value = result.url;
    formData.coverUrl = result.url; // 同时更新formData
    console.log('uploadedCoverUrl:', uploadedCoverUrl.value);
    console.log('formData.coverUrl:', formData.coverUrl);
    message.success('封面上传成功');
  } catch (error: any) {
    console.error('封面上传失败:', error);
    message.error(error.message || '封面上传失败');
  }

  return false;
};

// 删除封面
const handleRemoveCover = () => {
  dialog.warning({
    title: '删除封面',
    content: '确定要删除封面吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      formData.coverUrl = '';
      uploadedCoverUrl.value = '';
      message.success('封面已删除');
    }
  });
};

onMounted(() => {
  loadData();
  loadCategories();
  loadAuthors();
  loadTags();
});
</script>

<style scoped>
.book-management {
  padding: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.table-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 封面上传区域 */
.cover-upload-container {
  display: flex;
  gap: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f9fafb 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.cover-preview-box {
  flex-shrink: 0;
}

.cover-image-wrapper {
  width: 180px;
  height: 240px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.cover-image-wrapper:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.cover-image-wrapper:hover .cover-mask {
  opacity: 1;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-mask {
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
  transition: opacity 0.3s ease;
}

.cover-placeholder {
  width: 180px;
  height: 240px;
  border-radius: 8px;
  border: 2px dashed #d0d0d0;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.cover-upload-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.upload-success-tip {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #bbf7d0;
}
</style>
