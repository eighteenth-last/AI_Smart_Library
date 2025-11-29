<template>
  <n-layout class="author-layout" has-sider>
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <h2 v-if="!collapsed">作者工作台</h2>
        <h2 v-else>作者</h2>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeMenu"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="layout-header">
        <div class="header-left">
          <n-breadcrumb>
            <n-breadcrumb-item
              v-for="item in breadcrumb"
              :key="item.key"
              @click="handleBreadcrumbClick(item)"
            >
              {{ item.title }}
            </n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        <div class="header-right">
          <n-dropdown :options="userDropdownOptions" @select="handleUserDropdownSelect">
            <div class="user-info">
              <!-- 使用img标签显示头像 -->
              <div class="header-avatar-container">
                <img 
                  v-if="userInfo?.profilePicture" 
                  :src="getAvatarUrl(userInfo.profilePicture)" 
                  class="header-avatar-image"
                  alt="用户头像"
                  @error="handleHeaderAvatarError"
                  @load="handleHeaderAvatarLoad"
                />
                <div v-else class="header-avatar-placeholder">
                  {{ userInfo?.nickname?.[0] || 'A' }}
                </div>
              </div>
              <span class="username" v-if="userInfo">{{ userInfo.nickname }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>
     <n-layout-content class="layout-content">
        <router-view />
      </n-layout-content>
    </n-layout>

    <!-- 个人信息弹窗 -->
    <n-modal
      v-model:show="showProfileModal"
      preset="card"
      title="个人信息"
      style="width: 600px"
      :bordered="false"
      :segmented="{
        content: true,
        footer: 'soft'
      }"
    >
      <n-spin :show="profileLoading">
        <n-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-placement="left"
          label-width="100"
        >
          <n-form-item label="头像">
            <div class="avatar-upload">
              <!-- 使用img标签直接显示头像 -->
              <div class="avatar-container">
                <img 
                  v-if="avatarImageUrl" 
                  :src="avatarImageUrl" 
                  class="avatar-image"
                  alt="用户头像"
                  @error="handleImageError"
                />
                <div v-else class="avatar-placeholder">
                  {{ userInfo?.nickname?.[0]?.toUpperCase() || profileForm.username?.[0]?.toUpperCase() || 'A' }}
                </div>
              </div>
              <n-upload
                :show-file-list="false"
                @before-upload="handleAvatarUpload"
              >
                <n-button size="small" style="margin-top: 10px">
                  更改头像
                </n-button>
              </n-upload>
            </div>
          </n-form-item>

          <n-form-item label="用户名" path="username">
            <n-input
              v-model:value="profileForm.username"
              placeholder="请输入用户名"
            />
          </n-form-item>

          <n-form-item label="邮箱" path="email">
            <n-input
              v-model:value="profileForm.email"
              placeholder="请输入邮箱"
            />
          </n-form-item>

          <n-form-item label="手机号" path="phone">
            <n-input
              v-model:value="profileForm.phone"
              placeholder="请输入手机号"
            />
          </n-form-item>

          <n-form-item label="修改密码">
            <n-input
              v-model:value="profileForm.password"
              type="password"
              show-password-on="click"
              placeholder="不修改请留空"
            />
          </n-form-item>

          <n-form-item label="确认密码" v-if="profileForm.password">
            <n-input
              v-model:value="confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="请再次输入密码"
            />
          </n-form-item>
        </n-form>
      </n-spin>

      <template #footer>
        <n-space justify="end">
          <n-button @click="handleCloseModal">取消</n-button>
          <n-button type="primary" @click="handleUpdateProfile" :loading="submitting">
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, h, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMessage, NIcon, type FormInst, type FormRules, type UploadFileInfo } from 'naive-ui';
import {
  HomeOutlined,
  BookOutlined,
  QuestionCircleOutlined,
  BarChartOutlined,
  RobotOutlined,
  LogoutOutlined,
  UserOutlined
} from '@vicons/antd';
import { useUserStore } from '@/store/user';
import { logout, getUserProfile, updateProfile, type UpdateProfileDTO } from '@/api/author/auth';
import { uploadAPI } from '@/api/author/upload';

const router = useRouter();
const route = useRoute();
const message = useMessage();
const userStore = useUserStore();

const collapsed = ref(false);
const userInfo = computed(() => userStore.userInfo);
const showProfileModal = ref(false);
const profileLoading = ref(false);
const submitting = ref(false);
const profileFormRef = ref<FormInst | null>(null);
const confirmPassword = ref('');
const uploadedAvatarUrl = ref(''); // 临时存储上传的头像URL

const profileForm = reactive<UpdateProfileDTO>({
  username: '',
  email: '',
  phone: '',
  password: '',
  profilePicture: ''
});

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
  const url = uploadedAvatarUrl.value || profileForm.profilePicture;
  return url;
});

// 计算完整的头像URL（用于img标签）
const avatarImageUrl = computed(() => {
  return getAvatarUrl(currentAvatarUrl.value);
});

const menuOptions = [
  {
    label: '仪表盘',
    key: 'dashboard',
    icon: () => h(NIcon, null, { default: () => h(HomeOutlined) }),
    path: '/author/dashboard'
  },
  {
    label: '我的作品',
    key: 'my-books',
    icon: () => h(NIcon, null, { default: () => h(BookOutlined) }),
    path: '/author/my-books'
  },
  {
    label: '问答管理',
    key: 'qa',
    icon: () => h(NIcon, null, { default: () => h(QuestionCircleOutlined) }),
    path: '/author/qa'
  },
  {
    label: '数据统计',
    key: 'statistics',
    icon: () => h(NIcon, null, { default: () => h(BarChartOutlined) }),
    path: '/author/statistics'
  },
  {
    label: 'AI助手',
    key: 'ai-assistant',
    icon: () => h(NIcon, null, { default: () => h(RobotOutlined) }),
    path: '/author/ai-assistant'
  }
];

const activeMenu = computed(() => {
  const currentMenu = menuOptions.find(item => route.path.startsWith(item.path));
  return currentMenu?.key || 'dashboard';
});

const breadcrumb = computed(() => {
  const paths = route.path.split('/').filter(Boolean);
  const items = [{ key: 'home', title: '首页', path: '/author' }];
  
  if (paths.length > 1) {
    const menuItem = menuOptions.find(item => item.key === paths[1]);
    if (menuItem) {
      items.push({
        key: menuItem.key,
        title: menuItem.label,
        path: menuItem.path
      });
    }
  }
  
  return items;
});

const userDropdownOptions = [
  {
    label: '个人信息',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(UserOutlined) })
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogoutOutlined) })
  }
];

const profileRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
};

const handleMenuSelect = (key: string) => {
  const menuItem = menuOptions.find(item => item.key === key);
  if (menuItem && menuItem.path) {
    router.push(menuItem.path);
  }
};

const handleBreadcrumbClick = (item: any) => {
  if (item.path) {
    router.push(item.path);
  }
};

const handleUserDropdownSelect = async (key: string) => {
  if (key === 'logout') {
    try {
      await logout();
      userStore.logout();
      message.success('退出成功');
      router.push('/author/login');
    } catch (error) {
      message.error('退出失败');
    }
  } else if (key === 'profile') {
    await loadUserProfile();
    showProfileModal.value = true;
  }
};

// 关闭个人信息弹窗
const handleCloseModal = () => {
  showProfileModal.value = false;
};

// 加载用户信息
const loadUserProfile = async () => {
  profileLoading.value = true;
  try {
    const data = await getUserProfile();
    
    profileForm.username = data.username;
    profileForm.email = data.email;
    profileForm.phone = data.phone || '';
    profileForm.profilePicture = data.profilePicture || '';
    profileForm.password = '';
    confirmPassword.value = '';
    uploadedAvatarUrl.value = ''; // 清空临时头像
  } catch (error: any) {
    message.error(error.message || '加载用户信息失败');
  } finally {
    profileLoading.value = false;
  }
};

// 处理头像上传
const handleAvatarUpload = async (options: { file: UploadFileInfo }) => {
  const file = options.file.file;
  if (!file) return;

  // 验证文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }

  // 验证文件大小（修改为20MB）
  const isLt20M = file.size / 1024 / 1024 < 20;
  if (!isLt20M) {
    message.error('图片大小不能超过 20MB！');
    return false;
  }

  try {
    // 上传文件到服务器（后端会直接更新数据库）
    const result = await uploadAPI.uploadAvatar(file);
    
    // 更新临时头像URL用于立即显示
    uploadedAvatarUrl.value = result.url;
    
    // 更新表单数据
    profileForm.profilePicture = result.url;
    
    // 更新本地用户信息，立即在头部导航栏显示
    if (userStore.userInfo) {
      userStore.setUser({
        ...userStore.userInfo,
        profilePicture: result.url
      });
    }
    
    message.success('头像上传成功');
  } catch (error: any) {
    message.error(error.message || '头像上传失败');
  }

  return false; // 阻止默认上传行为
};

// 处理图片加载错误
const handleImageError = () => {
  message.error('图片加载失败');
};

// 处理头部头像加载错误
const handleHeaderAvatarError = () => {
  // 头像加载失败时静默处理
};

// 处理头部头像加载成功
const handleHeaderAvatarLoad = () => {
  // 头像加载成功
};

// 更新个人信息
const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value?.validate();

    // 验证密码
    if (profileForm.password) {
      if (!confirmPassword.value) {
        message.error('请输入确认密码');
        return;
      }
      if (profileForm.password !== confirmPassword.value) {
        message.error('两次输入的密码不一致');
        return;
      }
    }

    submitting.value = true;

    // 准备提交数据
    const updateData: UpdateProfileDTO = {
      username: profileForm.username,
      email: profileForm.email,
      phone: profileForm.phone,
      // 如果有上传新头像，使用新头像，否则使用原头像
      profilePicture: uploadedAvatarUrl.value || profileForm.profilePicture
    };

    // 只有当密码不为空时才提交
    if (profileForm.password) {
      updateData.password = profileForm.password;
    }

    const updatedUser = await updateProfile(updateData);
    
    // 更新本地用户信息（保留当前头像）
    userStore.setUser({
      ...userStore.userInfo!,
      username: updatedUser.username,
      email: updatedUser.email,
      phone: updatedUser.phone,
      // 使用表单中的头像，因为头像已经在上传时更新了
      profilePicture: profileForm.profilePicture || updatedUser.profilePicture
    });

    message.success('更新成功');
    showProfileModal.value = false;
    uploadedAvatarUrl.value = ''; // 清空临时头像
  } catch (error: any) {
    if (error.message) {
      message.error(error.message);
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.author-layout {
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e8e8e8;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #52c41a;
}

.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  flex: 1;
}

.header-right {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.header-avatar-container {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.header-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.layout-content {
  padding: 24px;
  height: calc(100vh - 64px);
  overflow-y: auto;
  background: #f0f2f5;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-container {
  width: 80px;
  height: 80px;
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
  font-size: 32px;
  font-weight: 500;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}
</style>
