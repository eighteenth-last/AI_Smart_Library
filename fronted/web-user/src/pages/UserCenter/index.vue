<template>
  <div class="user-center-container">
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
            <NavBar />
          </div>
        </div>
      </n-layout-header>

      <!-- 主要内容 -->
      <n-layout-content class="content" :style="{ paddingTop: '60px' }">
        <div class="user-center-wrapper">
          <n-grid :cols="4" :x-gap="24">
            <!-- 左侧导航菜单 -->
            <n-grid-item>
              <n-card class="nav-card">
                <n-menu
                  :value="activeMenu"
                  :options="menuOptions"
                  @update:value="handleMenuSelect"
                />
              </n-card>
            </n-grid-item>

            <!-- 右侧内容区域 -->
            <n-grid-item :span="3">
              <n-spin :show="loading">
                <!-- 个人信息卡片 -->
                <n-card v-show="activeMenu === 'profile'" class="content-card">
                <template #header>
                  <div class="card-header">
                    <n-icon size="24" color="#667eea">
                      <information-circle-outline />
                    </n-icon>
                    <span class="card-title">账号信息</span>
                  </div>
                </template>

                <!-- 头像区域 -->
                <div class="avatar-section">
                  <!-- 使用img标签显示头像 -->
                  <div class="avatar-container">
                    <img 
                      v-if="currentAvatarUrl" 
                      :src="currentAvatarUrl" 
                      class="avatar-image"
                      alt="用户头像"
                    />
                    <div v-else class="avatar-placeholder">
                      {{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) || 'U' }}
                    </div>
                  </div>
                  <n-upload
                    :show-file-list="false"
                    @before-upload="handleAvatarUpload"
                  >
                    <n-button size="small" class="upload-btn">
                      <template #icon>
                        <n-icon><camera-outline /></n-icon>
                      </template>
                      更换头像
                    </n-button>
                  </n-upload>
                  <div v-if="uploadedAvatarUrl" style="margin-top: 8px; font-size: 12px; color: #18a058;">
                    ✓ 已选择新头像，点击"保存"后生效
                  </div>
                </div>

                <n-divider />

                <n-descriptions label-placement="left" :column="2" bordered class="info-descriptions">
                  <n-descriptions-item label="用户名" :span="2">
                    <div class="info-value">
                      <n-icon size="18" color="#667eea" style="margin-right: 8px;">
                        <person-outline />
                      </n-icon>
                      {{ userInfo.username }}
                    </div>
                  </n-descriptions-item>
                  <n-descriptions-item label="昵称" :span="2">
                    <n-input
                      v-if="editing"
                      v-model:value="editForm.nickname"
                      placeholder="请输入昵称"
                    />
                    <div v-else class="info-value">
                      <n-icon size="18" color="#667eea" style="margin-right: 8px;">
                        <happy-outline />
                      </n-icon>
                      {{ userInfo.nickname || '-' }}
                    </div>
                  </n-descriptions-item>
                  <n-descriptions-item label="手机号">
                    <n-input
                      v-if="editing"
                      v-model:value="editForm.phone"
                      placeholder="请输入手机号"
                    />
                    <div v-else class="info-value">
                      <n-icon size="18" color="#667eea" style="margin-right: 8px;">
                        <call-outline />
                      </n-icon>
                      {{ userInfo.phone || '-' }}
                    </div>
                  </n-descriptions-item>
                  <n-descriptions-item label="邮箱">
                    <n-input
                      v-if="editing"
                      v-model:value="editForm.email"
                      placeholder="请输入邮箱"
                    />
                    <div v-else class="info-value">
                      <n-icon size="18" color="#667eea" style="margin-right: 8px;">
                        <mail-outline />
                      </n-icon>
                      {{ userInfo.email || '-' }}
                    </div>
                  </n-descriptions-item>
                </n-descriptions>

                <div class="action-buttons">
                  <n-button v-if="!editing" type="primary" size="large" @click="startEditing">
                    <template #icon>
                      <n-icon><create-outline /></n-icon>
                    </template>
                    编辑信息
                  </n-button>
                  <template v-else>
                    <n-button type="primary" size="large" @click="saveUserInfo">
                      <template #icon>
                        <n-icon><checkmark-outline /></n-icon>
                      </template>
                      保存
                    </n-button>
                    <n-button size="large" @click="cancelEditing">
                      <template #icon>
                        <n-icon><close-outline /></n-icon>
                      </template>
                      取消
                    </n-button>
                  </template>
                </div>
              </n-card>

              <!-- 成为作家申请表单 -->
              <n-card v-show="activeMenu === 'author'" class="content-card">
                <template #header>
                  <div class="card-header">
                    <n-icon size="24" color="#667eea">
                      <create-outline />
                    </n-icon>
                    <span class="card-title">申请成为作家</span>
                  </div>
                </template>

                <n-alert v-if="userInfo.role === 'author'" type="success" class="author-alert">
                  <template #icon>
                    <n-icon size="24">
                      <checkmark-circle-outline />
                    </n-icon>
                  </template>
                  <template #header>
                    <strong>恭喜您，已是作家身份！</strong>
                  </template>
                  您当前的角色是作家，可以开始创作和发布作品了。如有任何疑问，请联系管理员。
                </n-alert>
                <n-form v-else ref="authorFormRef" :model="authorForm" :rules="authorRules" label-placement="left" label-width="120" class="author-form">
                  <n-form-item label="真实姓名" path="name">
                    <n-input v-model:value="authorForm.name" placeholder="请输入您的真实姓名" size="large" />
                  </n-form-item>
                  <n-form-item label="国家/地区" path="country">
                    <n-input v-model:value="authorForm.country" placeholder="例如：中国" size="large" />
                  </n-form-item>
                  <n-form-item label="出生年份" path="birthYear">
                    <n-input-number
                      v-model:value="authorForm.birthYear"
                      placeholder="例如：1990"
                      :min="1900"
                      :max="new Date().getFullYear()"
                      size="large"
                      style="width: 100%;"
                    />
                  </n-form-item>
                  <n-form-item label="个人简介" path="intro">
                    <n-input
                      v-model:value="authorForm.intro"
                      type="textarea"
                      placeholder="请介绍您的写作经历、作品风格、代表作品等..."
                      :rows="6"
                      :maxlength="500"
                      show-count
                      size="large"
                    />
                  </n-form-item>
                  <n-form-item>
                    <n-button type="primary" size="large" @click="submitAuthorApplication" :loading="submittingAuthor" block>
                      <template #icon>
                        <n-icon><paper-plane-outline /></n-icon>
                      </template>
                      提交申请
                    </n-button>
                  </n-form-item>
                </n-form>
              </n-card>
              </n-spin>
            </n-grid-item>
          </n-grid>
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
import { ref, onMounted, h, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { authAPI } from '@/api/user/auth'
import request from '@/utils/request'
import { 
  NLayout, 
  NLayoutHeader, 
  NLayoutContent, 
  NLayoutFooter, 
  NButton, 
  NCard, 
  NDescriptions,
  NDescriptionsItem,
  NInput,
  NGrid,
  NGridItem,
  NIcon,
  NMenu,
  NForm,
  NFormItem,
  NInputNumber,
  NAlert,
  NUpload,
  NDivider,
  NSpin,
  type FormInst,
  type FormRules,
} from 'naive-ui'
import {
  PersonOutline,
  InformationCircleOutline,
  CreateOutline,
  CameraOutline,
  CallOutline,
  MailOutline,
  HappyOutline,
  CheckmarkOutline,
  CloseOutline,
  CheckmarkCircleOutline,
  PaperPlaneOutline,
} from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import NavBar from "@/components/NavBar.vue";

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 当前选中的菜单
const activeMenu = ref('profile')

// 菜单选项
const menuOptions = [
  {
    label: '个人信息',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(InformationCircleOutline) })
  },
  {
    label: '成为作家',
    key: 'author',
    icon: () => h(NIcon, null, { default: () => h(CreateOutline) })
  }
]

// 菜单选择处理
const handleMenuSelect = (key: string) => {
  activeMenu.value = key
}

// 用户信息
const userInfo = ref<any>({})
const loading = ref(true)
const editing = ref(false)
const editForm = ref({
  nickname: '',
  phone: '',
  email: ''
})

// 头像
const avatarUrl = ref('')
const uploadedAvatarUrl = ref('') // 临时存储上传的头像URL

// 获取头像完整URL
const getAvatarUrl = (url?: string) => {
  if (!url) return '';
  // 如果是相对路径，添加API前缀
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

// 计算当前显示的头像
const currentAvatarUrl = computed(() => {
  const url = uploadedAvatarUrl.value || avatarUrl.value;
  return getAvatarUrl(url);
});


// 头像上传
const handleAvatarUpload = async (options: { file: any }) => {
  const file = options.file.file || options.file;
  if (!file) return;

  // 验证文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }

  try {
    // 上传文件到服务器
    const formData = new FormData();
    formData.append('file', file);
    
    const data = await request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (data && data.url) {
      // 临时存储头像URL，等待点击保存时才更新到数据库
      uploadedAvatarUrl.value = data.url;
      message.success('头像上传成功，请点击"保存"按钮');
    } else {
      message.error('头像上传失败');
    }
  } catch (error: any) {
    console.error('Upload avatar error:', error);
    message.error('头像上传失败');
  }

  return false; // 阻止默认上传行为
};

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  loading.value = true
  try {
    // axios响应拦截器已经返回 data 部分，所以 response 就是用户对象
    const data = await authAPI.getProfile()
    console.log('User info loaded:', data)
    
    if (data && data.userId) {
      userInfo.value = data
      avatarUrl.value = data.profilePicture || ''
      
      // 更新全局 store
      userStore.setUser(data)
      
      // 初始化编辑表单
      editForm.value = {
        nickname: data.nickname || '',
        phone: data.phone || '',
        email: data.email || ''
      }
    } else {
      message.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('Load user info error:', error)
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const startEditing = () => {
  editing.value = true
}

const cancelEditing = () => {
  editing.value = false
  // 恢复原始值
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || ''
  }
}

const saveUserInfo = async () => {
  try {
    // 准备提交数据，如果有上传新头像，使用新头像
    const updateData = {
      nickname: editForm.value.nickname,
      phone: editForm.value.phone,
      email: editForm.value.email,
      profilePicture: uploadedAvatarUrl.value || avatarUrl.value
    };
    
    // axios拦截器已经返回 data 部分
    await authAPI.updateProfile(updateData);
    
    message.success('信息更新成功');
    editing.value = false;
    uploadedAvatarUrl.value = ''; // 清空临时头像
    
    // 重新加载用户信息
    await loadUserInfo();
    
    // 更新全局用户信息（包含所有字段）
    userStore.setUser(userInfo.value);
  } catch (error) {
    console.error('Update user info error:', error);
    message.error('更新失败');
  }
};

const goHome = () => {
  router.push('/')
}

const handleLogout = async () => {
  try {
    userStore.clearUser()
    message.success('退出成功')
    router.push('/login')
  } catch (error) {
    message.error('退出失败')
  }
}

// 作家申请表单
const authorFormRef = ref<FormInst | null>(null)
const submittingAuthor = ref(false)
const authorForm = ref({
  name: '',
  country: '',
  birthYear: null as number | null,
  intro: ''
})

const authorRules: FormRules = {
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  country: [
    { required: true, message: '请输入国家/地区', trigger: 'blur' }
  ],
  birthYear: [
    { required: true, type: 'number', message: '请输入出生年份', trigger: 'blur' }
  ],
  intro: [
    { required: true, message: '请输入个人简介', trigger: 'blur' },
    { min: 20, message: '简介至少20个字', trigger: 'blur' }
  ]
}

// 提交作家申请
const submitAuthorApplication = async () => {
  if (!authorFormRef.value) return
  
  try {
    await authorFormRef.value.validate()
    submittingAuthor.value = true
    
    // TODO: 调用API提交作家申请
    // await authorAPI.applyAuthor(authorForm.value)
    
    message.success('申请已提交，请等待审核')
    authorForm.value = {
      name: '',
      country: '',
      birthYear: null,
      intro: ''
    }
  } catch (error) {
    console.error('Submit author application error:', error)
  } finally {
    submittingAuthor.value = false
  }
}

</script>

<style scoped>
.user-center-container {
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

.user-center-wrapper {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

.nav-card {
  position: sticky;
  top: 80px;
  height: fit-content;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.nav-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.content-card {
  min-height: 400px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.content-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
}

.avatar-container {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-avatar {
  font-size: 48px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.4);
}

.upload-btn {
  border-radius: 20px;
}

.info-descriptions {
  margin: 20px 0;
}

.info-value {
  display: flex;
  align-items: center;
  font-size: 15px;
  color: #333;
}

.action-buttons {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 作家申请表单 */
.author-alert {
  margin-bottom: 24px;
}

.author-form {
  padding: 20px 0;
}

.user-info-card {
  margin-bottom: 20px;
}

.quick-links-card {
  margin-bottom: 20px;
}

.quick-link-item {
  cursor: pointer;
  text-align: center;
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