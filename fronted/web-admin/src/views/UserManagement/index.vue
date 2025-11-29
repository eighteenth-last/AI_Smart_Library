<template>
  <div class="user-management">
    <div class="search-section">
      <n-card>
        <n-form inline :model="searchForm" label-placement="left" label-width="auto">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="用户名/昵称" clearable />
          </n-form-item>
          <n-form-item label="角色">
            <n-select 
              v-model:value="searchForm.role" 
              placeholder="选择角色" 
              clearable 
              style="width: 150px"
              :options="[
                { label: '读者', value: 'reader' },
                { label: '作者', value: 'author' },
                { label: '管理员', value: 'admin' }
              ]"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select 
              v-model:value="searchForm.status" 
              placeholder="选择状态" 
              clearable 
              style="width: 150px"
              :options="[
                { label: '正常', value: 1 },
                { label: '禁用', value: 0 }
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
          <n-button type="primary" @click="handleAdd">新增用户</n-button>
        </div>
        <n-data-table
          :columns="columns"
          :data="userList"
          :loading="loading"
          :pagination="pagination"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-card>
    </div>

    <n-modal v-model:show="showModal" :title="modalTitle" preset="card" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80px">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="!!formData.userId" />
        </n-form-item>
        <n-form-item label="密码" path="password" v-if="!formData.userId">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="角色" path="role">
          <n-select 
            v-model:value="formData.role" 
            placeholder="选择角色"
            :options="[
              { label: '读者', value: 'reader' },
              { label: '作者', value: 'author' },
              { label: '管理员', value: 'admin' }
            ]"
          />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-radio :value="1">正常</n-radio>
            <n-radio :value="0">禁用</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h } from 'vue';
import { useMessage, useDialog, NTag, NSpace, NButton } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { getUserList, createUser, updateUser, deleteUser } from '@/api/admin/users';

const message = useMessage();
const dialog = useDialog();

const loading = ref(false);
const userList = ref([]);
const showModal = ref(false);
const modalTitle = ref('新增用户');
const submitLoading = ref(false);

const searchForm = reactive({
  keyword: '',
  role: null,
  status: null,
  page: 1,
  size: 10
});

const formRef = ref();
const formData = reactive({
  userId: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'reader',
  status: 1
});

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
};

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
});

const createColumns = (): DataTableColumns => [
  { title: '用户ID', key: 'userId', width: 80 },
  { title: '用户名', key: 'username', width: 120 },
  { title: '昵称', key: 'nickname', width: 120 },
  {
    title: '角色',
    key: 'role',
    width: 100,
    render: (row: any) => {
      const roleMap: Record<string, string> = {
        reader: '读者',
        author: '作者',
        admin: '管理员'
      };
      const roleColor: Record<string, 'default' | 'info' | 'error'> = {
        reader: 'default',
        author: 'info',
        admin: 'error'
      };
      return h(NTag, { type: roleColor[row.role] as any }, { default: () => roleMap[row.role] || row.role });
    }
  },
  { title: '邮箱', key: 'email', width: 180 },
  { title: '手机号', key: 'phone', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) => {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error' }, {
        default: () => row.status === 1 ? '正常' : '禁用'
      });
    }
  },
  { title: '创建时间', key: 'createdAt', width: 160 },
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
    console.log('开始加载用户列表...', searchForm);
    const response = await getUserList(searchForm);
    console.log('接口返回数据:', response);
    userList.value = response.records || [];
    pagination.itemCount = response.total || 0;
    console.log('用户列表赋值完成:', userList.value);
  } catch (error) {
    console.error('加载用户列表失败:', error);
    message.error('加载用户列表失败');
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
  searchForm.role = null;
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
  modalTitle.value = '新增用户';
  Object.assign(formData, {
    userId: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    role: 'reader',
    status: 1
  });
  showModal.value = true;
};

const handleEdit = (row: any) => {
  modalTitle.value = '编辑用户';
  Object.assign(formData, {
    userId: row.userId,
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    role: row.role,
    status: row.status
  });
  showModal.value = true;
};

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户 "${row.username}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(row.userId);
        message.success('删除成功');
        loadData();
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;
    
    if (formData.userId) {
      const { password, ...updateData } = formData;
      await updateUser(formData.userId, updateData);
      message.success('更新成功');
    } else {
      await createUser(formData);
      message.success('添加成功');
    }
    
    showModal.value = false;
    loadData();
  } catch (error) {
    message.error(formData.userId ? '更新失败' : '添加失败');
  } finally {
    submitLoading.value = false;
  }
};

loadData();
</script>

<style scoped>
.user-management {
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
</style>
