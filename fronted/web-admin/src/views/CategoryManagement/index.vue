<template>
  <div class="category-management">
    <n-card title="分类管理">
      <template #header-extra>
        <n-button type="primary" @click="showAddDialog">
          新增分类
        </n-button>
      </template>

      <n-space vertical :size="20">
        <!-- 搜索区域 -->
        <n-space>
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索分类名称"
            clearable
            @clear="loadData"
            style="width: 250px"
          />
          <n-button type="primary" @click="loadData">搜索</n-button>
        </n-space>

        <!-- 分类树表格 -->
        <n-data-table
          :columns="columns"
          :data="categoryList"
          :loading="loading"
          :pagination="false"
          :row-key="(row: Category) => row.categoryId"
          default-expand-all
        />
      </n-space>
    </n-card>

    <!-- 新增/编辑分类对话框 -->
    <n-modal
      v-model:show="showDialog"
      :title="dialogTitle"
      preset="dialog"
      :positive-text="'确定'"
      :negative-text="'取消'"
      @positive-click="handleSubmit"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="80"
        style="margin-top: 20px"
      >
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="父分类">
          <n-tree-select
            v-model:value="formData.parentId"
            :options="parentCategoryOptions"
            placeholder="选择父分类（不选则为顶级分类）"
            clearable
            key-field="categoryId"
            label-field="name"
            children-field="children"
          />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { NButton, NSpace, NTag, useMessage, useDialog } from 'naive-ui'
import { categoryAPI } from '@/api/admin/category'

interface Category {
  categoryId: number
  name: string
  parentId: number | null
  level: number
  children?: Category[]
}

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const searchKeyword = ref('')
const categoryList = ref<Category[]>([])
const showDialog = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref<FormInst | null>(null)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const formData = ref({
  name: '',
  parentId: null as number | null
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 父分类选项（扁平化的分类列表）
const parentCategoryOptions = ref<Category[]>([])

// 表格列定义
const columns: DataTableColumns<Category> = [
  {
    title: 'ID',
    key: 'categoryId',
    width: 80
  },
  {
    title: '分类名称',
    key: 'name',
    minWidth: 200
  },
  {
    title: '层级',
    key: 'level',
    width: 100,
    render: (row) => {
      const typeMap: Record<number, any> = {
        1: 'success',
        2: 'info',
        3: 'warning'
      }
      return h(NTag, { type: typeMap[row.level] as any }, { default: () => `第${row.level}级` })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              onClick: () => handleEdit(row)
            },
            { default: () => '编辑' }
          ),
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => handleDelete(row)
            },
            { default: () => '删除' }
          )
        ]
      })
    }
  }
]

// 加载分类数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await categoryAPI.getList()
    categoryList.value = filterCategories(data)
    parentCategoryOptions.value = data
  } catch (error: any) {
    message.error(error.message || '加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤分类（根据搜索关键词）
const filterCategories = (categories: Category[]): Category[] => {
  if (!searchKeyword.value) return categories
  
  const filtered: Category[] = []
  for (const category of categories) {
    if (category.name.includes(searchKeyword.value)) {
      filtered.push(category)
    } else if (category.children && category.children.length > 0) {
      const filteredChildren = filterCategories(category.children)
      if (filteredChildren.length > 0) {
        filtered.push({ ...category, children: filteredChildren })
      }
    }
  }
  return filtered
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  formData.value = {
    name: '',
    parentId: null
  }
  showDialog.value = true
}

// 编辑分类
const handleEdit = (row: Category) => {
  isEdit.value = true
  editId.value = row.categoryId
  dialogTitle.value = '编辑分类'
  formData.value = {
    name: row.name,
    parentId: row.parentId
  }
  showDialog.value = true
}

// 删除分类
const handleDelete = (row: Category) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除分类"${row.name}"吗？如果有子分类将无法删除。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await categoryAPI.deleteCategory(row.categoryId)
        message.success('删除成功')
        await loadData()
      } catch (error: any) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    if (isEdit.value && editId.value) {
      await categoryAPI.updateCategory(editId.value, formData.value)
      message.success('更新成功')
    } else {
      await categoryAPI.createCategory(formData.value)
      message.success('添加成功')
    }
    showDialog.value = false
    await loadData()
  } catch (error: any) {
    if (error.message) {
      message.error(error.message)
    }
    throw error // 阻止对话框关闭
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}
</style>
