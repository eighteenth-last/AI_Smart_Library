<template>
  <div class="knowledge-qa-management">
    <n-card title="知识库问答对管理" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <n-space>
          <n-input 
            v-model:value="searchQuery" 
            placeholder="搜索问题/答案" 
            clearable
            style="width: 300px"
          >
            <template #prefix>
              <n-icon :component="SearchOutlined" />
            </template>
          </n-input>
          
          <n-select
            v-model:value="selectedCategory"
            placeholder="选择分类"
            clearable
            style="width: 150px"
            :options="categoryOptions"
          />
          
          <n-button type="primary" @click="handleSearch">
            <template #icon>
              <n-icon :component="SearchOutlined" />
            </template>
            搜索
          </n-button>
          
          <n-button type="success" @click="handleAdd">
            <template #icon>
              <n-icon :component="PlusOutlined" />
            </template>
            新增问答
          </n-button>
          
          <n-upload
            :custom-request="handleImport"
            :show-file-list="false"
            accept=".xlsx,.xls,.csv"
          >
            <n-button type="info">
              <template #icon>
                <n-icon :component="UploadOutlined" />
              </template>
              批量导入
            </n-button>
          </n-upload>
          
          <n-dropdown :options="exportOptions" @select="handleExport">
            <n-button>
              <template #icon>
                <n-icon :component="DownloadOutlined" />
              </template>
              导出数据
            </n-button>
          </n-dropdown>
        </n-space>
      </div>

      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="dataList"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        class="data-table"
      />
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 800px"
      :bordered="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="问题" path="question">
          <n-input
            v-model:value="formData.question"
            type="textarea"
            placeholder="请输入问题"
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="答案" path="answer">
          <n-input
            v-model:value="formData.answer"
            type="textarea"
            placeholder="请输入答案"
            :rows="5"
          />
        </n-form-item>

        <n-form-item label="分类" path="category">
          <n-input v-model:value="formData.category" placeholder="请输入分类" />
        </n-form-item>

        <n-form-item label="来源" path="source">
          <n-input v-model:value="formData.source" placeholder="请输入来源" />
        </n-form-item>

        <n-form-item label="是否公开" path="isPublic">
          <n-radio-group v-model:value="formData.isPublic">
            <n-radio :value="1">公开</n-radio>
            <n-radio :value="0">私有</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 导入结果弹窗 -->
    <n-modal
      v-model:show="showImportResult"
      title="导入结果"
      preset="card"
      style="width: 700px"
    >
      <n-space vertical>
        <n-alert v-if="importResult" :type="importResult.failedCount > 0 ? 'warning' : 'success'">
          <template #header>
            导入完成
          </template>
          总计: {{ importResult.totalCount }} 条 | 
          成功: {{ importResult.successCount }} 条 | 
          失败: {{ importResult.failedCount }} 条
        </n-alert>

        <n-collapse v-if="importResult && importResult.errorDetails && importResult.errorDetails.length > 0">
          <n-collapse-item title="错误详情" name="errors">
            <n-list bordered>
              <n-list-item v-for="(error, index) in importResult.errorDetails" :key="index">
                <n-thing>
                  <template #header>
                    第 {{ error.rowNumber }} 行
                  </template>
                  <template #description>
                    问题: {{ error.question || '(空)' }}
                  </template>
                  <n-text type="error">{{ error.errorMessage }}</n-text>
                </n-thing>
              </n-list-item>
            </n-list>
          </n-collapse-item>
        </n-collapse>
      </n-space>

      <template #footer>
        <n-space justify="end">
          <n-button type="primary" @click="showImportResult = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import { useMessage, useDialog, NButton, NTag, NSpace, type DataTableColumns, type UploadCustomRequestOptions } from 'naive-ui';
import { 
  SearchOutlined, 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined
} from '@vicons/antd';
import request from '@/utils/request';
import { importKnowledge, exportToExcel, exportToCSV, type ImportResultVO } from '@/api/admin/knowledge';

const message = useMessage();
const dialog = useDialog();

// 搜索条件
const searchQuery = ref('');
const selectedCategory = ref<string | null>(null);

// 分类选项
const categoryOptions = [
  { label: '图书馆业务', value: '图书馆业务' },
  { label: '作者问答', value: '作者问答' },
  { label: '图书推荐', value: '图书推荐' }
];

// 表格数据
const loading = ref(false);
const dataList = ref<any[]>([]);
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page: number) => {
    pagination.page = page;
    loadData();
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    loadData();
  }
});

// 弹窗相关
const showModal = ref(false);
const modalTitle = ref('新增问答');
const submitting = ref(false);
const formRef = ref();
const formData = reactive({
  kbId: undefined as number | undefined,
  question: '',
  answer: '',
  category: '',
  source: 'system',
  isPublic: 1
});

// 表单验证规则
const formRules = {
  question: [
    { required: true, message: '请输入问题', trigger: 'blur' },
    { max: 500, message: '问题长度不能超过 500 字符', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '请输入答案', trigger: 'blur' },
    { max: 5000, message: '答案长度不能超过 5000 字符', trigger: 'blur' }
  ]
};

// 导入结果
const showImportResult = ref(false);
const importResult = ref<ImportResultVO | null>(null);

// 导出选项
const exportOptions = [
  { label: '导出为 Excel', key: 'excel', icon: () => h('span', '📊') },
  { label: '导出为 CSV', key: 'csv', icon: () => h('span', '📄') }
];

// 表格列定义
const columns: DataTableColumns<any> = [
  {
    title: 'ID',
    key: 'kbId',
    width: 80,
    align: 'center'
  },
  {
    title: '问题',
    key: 'question',
    width: 250,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '答案',
    key: 'answer',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '分类',
    key: 'category',
    width: 120,
    align: 'center'
  },
  {
    title: '来源',
    key: 'source',
    width: 100,
    align: 'center'
  },
  {
    title: '是否公开',
    key: 'isPublic',
    width: 100,
    align: 'center',
    render: (row) => {
      return h(
        NTag,
        {
          type: row.isPublic === 1 ? 'success' : 'warning',
          size: 'small'
        },
        {
          default: () => row.isPublic === 1 ? '公开' : '私有'
        }
      );
    }
  },
  {
    title: '命中次数',
    key: 'hitCount',
    width: 100,
    align: 'center',
    render: (row) => row.hitCount || 0
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    align: 'center',
    fixed: 'right' as const,
    render: (row) => {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                onClick: () => handleEdit(row)
              },
              {
                default: () => '编辑',
                icon: () => h('span', { style: { marginRight: '4px' } }, '✏️')
              }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                onClick: () => handleDelete(row)
              },
              {
                default: () => '删除',
                icon: () => h('span', { style: { marginRight: '4px' } }, '🗑️')
              }
            )
          ]
        }
      );
    }
  }
];

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const response = await request.get('/ai/admin/knowledge', {
      params: {
        category: selectedCategory.value || undefined,
        page: pagination.page,
        size: pagination.pageSize
      }
    });
    
    dataList.value = response.records;
    pagination.itemCount = response.total;
  } catch (error: any) {
    message.error(error.message || '加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

// 新增
const handleAdd = () => {
  modalTitle.value = '新增问答';
  formData.kbId = undefined;
  formData.question = '';
  formData.answer = '';
  formData.category = '';
  formData.source = 'system';
  formData.isPublic = 1;
  showModal.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑问答';
  formData.kbId = row.kbId;
  formData.question = row.question;
  formData.answer = row.answer;
  formData.category = row.category || '';
  formData.source = row.source || 'system';
  formData.isPublic = row.isPublic;
  showModal.value = true;
};

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除问题"${row.question}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await request.delete(`/ai/admin/knowledge/${row.kbId}`);
        message.success('删除成功');
        loadData();
      } catch (error: any) {
        message.error(error.message || '删除失败');
      }
    }
  });
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitting.value = true;

    const submitData = {
      question: formData.question,
      answer: formData.answer,
      category: formData.category,
      source: formData.source,
      isPublic: formData.isPublic
    };

    if (formData.kbId) {
      await request.put(`/ai/admin/knowledge/${formData.kbId}`, submitData);
      message.success('更新成功');
    } else {
      await request.post('/ai/admin/knowledge', submitData);
      message.success('创建成功');
    }

    showModal.value = false;
    loadData();
  } catch (error: any) {
    if (error.message) {
      message.error(error.message);
    }
  } finally {
    submitting.value = false;
  }
};

// 导入
const handleImport = async (options: UploadCustomRequestOptions) => {
  const { file } = options;
  
  try {
    message.loading('正在导入...', { duration: 0 });
    const result = await importKnowledge(file.file as File);
    message.destroyAll();
    
    importResult.value = result;
    showImportResult.value = true;
    
    if (result.successCount > 0) {
      loadData(); // 刷新列表
    }
  } catch (error: any) {
    message.destroyAll();
    message.error(error.message || '导入失败');
  }
};

// 导出
const handleExport = async (key: string) => {
  try {
    if (key === 'excel') {
      message.loading('正在导出 Excel...', { duration: 0 });
      await exportToExcel(selectedCategory.value || undefined);
      message.destroyAll();
      message.success('导出成功！');
    } else if (key === 'csv') {
      message.loading('正在导出 CSV...', { duration: 0 });
      await exportToCSV(selectedCategory.value || undefined);
      message.destroyAll();
      message.success('导出成功！');
    }
  } catch (error: any) {
    message.destroyAll();
    message.error(error.message || '导出失败');
  }
};

// 初始化
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.knowledge-qa-management {
  padding: 20px;
}

.action-bar {
  margin-bottom: 20px;
}

.data-table {
  margin-top: 20px;
}
</style>
