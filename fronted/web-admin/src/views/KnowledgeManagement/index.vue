<template>
  <div class="knowledge-management">
    <n-card title="知识库管理" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <n-space>
          <n-input 
            v-model:value="searchQuery" 
            placeholder="搜索知识库名称/描述" 
            clearable
            style="width: 300px"
          >
            <template #prefix>
              <n-icon :component="SearchOutlined" />
            </template>
          </n-input>
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
            新增知识库
          </n-button>
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
      style="width: 700px"
      :bordered="false"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="120px"
      >
        <n-form-item label="知识库名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入知识库名称" />
        </n-form-item>

        <n-form-item label="描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入知识库描述"
            :rows="4"
          />
        </n-form-item>

        <n-form-item label="向量维度" path="vectorDimension">
          <n-input-number
            v-model:value="formData.vectorDimension"
            placeholder="请输入向量维度"
            :min="1"
            :max="4096"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="索引类型" path="indexType">
          <n-select
            v-model:value="formData.indexType"
            placeholder="请选择索引类型"
            :options="indexTypeOptions"
          />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-radio :value="1">启用</n-radio>
            <n-radio :value="0">禁用</n-radio>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue';
import { useMessage, useDialog, NButton, NTag, NSpace, type DataTableColumns } from 'naive-ui';
import { 
  SearchOutlined, 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@vicons/antd';
import {
  getKnowledgeBaseList,
  createKnowledgeBase,
  updateKnowledgeBase,
  deleteKnowledgeBase,
  type KnowledgeBase,
  type KnowledgeBaseCreateDTO,
  type KnowledgeBaseUpdateDTO
} from '@/api/admin/knowledge';

const message = useMessage();
const dialog = useDialog();

// 搜索关键词
const searchQuery = ref('');

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
const modalTitle = ref('新增知识库');
const submitting = ref(false);
const formRef = ref();
const formData = reactive({
  kbId: undefined as number | undefined,
  name: '',
  description: '',
  vectorDimension: 1536,
  indexType: 'FLAT',
  status: 1
});

// 索引类型选项
const indexTypeOptions = [
  { label: 'FLAT（平面索引）', value: 'FLAT' },
  { label: 'IVF（倒排文件索引）', value: 'IVF' },
  { label: 'HNSW（分层图索引）', value: 'HNSW' }
];

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  vectorDimension: [
    { required: true, type: 'number', message: '请输入向量维度', trigger: 'blur' }
  ],
  indexType: [
    { required: true, message: '请选择索引类型', trigger: 'change' }
  ]
};

// 表格列定义
const columns: DataTableColumns<any> = [
  {
    title: 'ID',
    key: 'kbId',
    width: 80,
    align: 'center'
  },
  {
    title: '知识库名称',
    key: 'name',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '描述',
    key: 'description',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '向量维度',
    key: 'vectorDimension',
    width: 120,
    align: 'center'
  },
  {
    title: '索引类型',
    key: 'indexType',
    width: 120,
    align: 'center'
  },
  {
    title: '文档数量',
    key: 'documentCount',
    width: 120,
    align: 'center',
    render: (row) => row.documentCount || 0
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => {
      return h(
        NTag,
        {
          type: row.status === 1 ? 'success' : 'error',
          size: 'small'
        },
        {
          default: () => row.status === 1 ? '启用' : '禁用',
          icon: () => h(row.status === 1 ? CheckCircleOutlined : CloseCircleOutlined)
        }
      );
    }
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    align: 'center'
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    align: 'center',
    render: (row) => {
      return h(
        NSpace,
        { justify: 'center' },
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
                icon: () => h(EditOutlined)
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
                icon: () => h(DeleteOutlined)
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
    const response = await getKnowledgeBaseList({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchQuery.value
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
  modalTitle.value = '新增知识库';
  formData.kbId = undefined;
  formData.name = '';
  formData.description = '';
  formData.vectorDimension = 1536;
  formData.indexType = 'FLAT';
  formData.status = 1;
  showModal.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑知识库';
  formData.kbId = row.kbId;
  formData.name = row.name;
  formData.description = row.description;
  formData.vectorDimension = row.vectorDimension;
  formData.indexType = row.indexType;
  formData.status = row.status;
  showModal.value = true;
};

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除知识库“${row.name}”吗？此操作将同时删除该知识库下的所有文档！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteKnowledgeBase(row.kbId);
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

    const submitData: KnowledgeBaseCreateDTO | KnowledgeBaseUpdateDTO = {
      name: formData.name,
      description: formData.description,
      vectorDimension: formData.vectorDimension,
      indexType: formData.indexType,
      status: formData.status
    };

    if (formData.kbId) {
      await updateKnowledgeBase(formData.kbId, submitData);
      message.success('更新成功');
    } else {
      await createKnowledgeBase(submitData as KnowledgeBaseCreateDTO);
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

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.knowledge-management {
  padding: 24px;
}

.action-bar {
  margin-bottom: 20px;
}

.data-table {
  margin-top: 20px;
}

:deep(.n-data-table-th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.n-data-table-td) {
  font-size: 14px;
}
</style>
