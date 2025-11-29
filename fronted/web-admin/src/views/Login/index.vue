<template>
  <div class="admin-login-container">
    <div class="login-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="left-section">
        <div class="brand">
          <div class="brand-icon">
            <img src="/favicon.ico" alt="Logo" class="logo-image" />
          </div>
          <h1 class="brand-title">神阁慧境阁</h1>
          <p class="brand-slogan">管理后台系统</p>
          <p class="brand-desc">高效管理 · 智能运营 · 数据驱动</p>
        </div>
        <div class="decoration">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="right-section">
        <div class="login-card">
          <div class="card-header">
            <h2 class="form-title">管理员登录</h2>
            <p class="form-subtitle">欢迎回来，请登录您的管理员账号</p>
          </div>

          <n-form ref="formRef" :model="formData" :rules="rules" class="login-form">
            <n-form-item path="username">
              <n-input 
                v-model:value="formData.username" 
                placeholder="请输入管理员账号" 
                size="large"
                @keydown.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon :component="UserOutlined" />
                </template>
              </n-input>
            </n-form-item>
            
            <n-form-item path="password">
              <n-input 
                v-model:value="formData.password" 
                type="password" 
                placeholder="请输入密码" 
                size="large"
                show-password-on="click"
                @keydown.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon :component="LockOutlined" />
                </template>
              </n-input>
            </n-form-item>

            <n-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              @click="handleLogin" 
              block
              class="login-button"
            >
              <template #icon>
                <n-icon :component="LoginOutlined" />
              </template>
              登录
            </n-button>
          </n-form>

          <div class="form-footer">
            <n-icon :component="SafetyOutlined" size="14" class="footer-icon" />
            <span class="footer-text">仅限管理员账号登录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { 
  UserOutlined, 
  LockOutlined, 
  BookOutlined, 
  LoginOutlined,
  SafetyOutlined 
} from '@vicons/antd';
import { useUserStore } from '@/store/user';
import { login, getUserProfile } from '@/api/admin/auth';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const formRef = ref();
const loading = ref(false);

const formData = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;
    
    const response = await login(formData);
    
    if (response.role !== 'admin') {
      message.error('非管理员账号无法登录后台');
      loading.value = false;
      return;
    }
    
    // 先设置基本用户信息和 token，这样后续请求才能带上 token
    userStore.setUser({
      userId: response.userId,
      username: response.username,
      nickname: response.nickname,
      role: response.role,
      token: response.token
    });
    
    // 现在可以安全地获取完整用户信息（请求会带上 token）
    try {
      const userProfile = await getUserProfile();
      
      // 更新完整的用户信息
      userStore.setUser({
        userId: response.userId,
        username: response.username,
        nickname: response.nickname,
        role: response.role,
        email: userProfile.email,
        phone: userProfile.phone,
        profilePicture: userProfile.profilePicture,
        token: response.token
      });
    } catch (error) {
      // 如果获取用户详情失败，不影响登录，使用基本信息
      console.error('获取用户详情失败:', error);
    }
    
    message.success('登录成功');
    router.push('/admin/dashboard');
  } catch (error: any) {
    message.error(error.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8eaf6 0%, #c5cae9 50%, #9fa8da 100%);
  padding: 20px;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(103, 58, 183, 0.2);
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧装饰区域 */
.left-section {
  flex: 1;
  background: linear-gradient(135deg, #673ab7 0%, #9575cd 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.brand {
  text-align: center;
  z-index: 2;
  position: relative;
}

.brand-icon {
  margin-bottom: 32px;
  animation: float 3s ease-in-out infinite;
}

.logo-image {
  width: 96px;
  height: 96px;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.2));
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  color: white;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 12px 0;
  font-weight: 500;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  line-height: 1.6;
}

.decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float-circle 20s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 7s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 14s;
}

@keyframes float-circle {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(30px, -30px); }
  66% { transform: translate(-20px, 20px); }
}

/* 右侧表单区域 */
.right-section {
  flex: 1;
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.card-header {
  margin-bottom: 40px;
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  color: #4a148c;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #757575;
  margin: 0;
}

.login-form :deep(.n-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.n-input) {
  border-radius: 10px;
  border: 2px solid #e8eaf6;
  background: white;
  transition: all 0.3s ease;
}

.login-form :deep(.n-input:hover) {
  border-color: #c5cae9;
}

.login-form :deep(.n-input.n-input--focus) {
  border-color: #673ab7;
  background: white;
  box-shadow: 0 0 0 4px rgba(103, 58, 183, 0.1);
}

.login-form :deep(.n-input__input) {
  background: transparent !important;
}

.login-form :deep(.n-input .n-input__prefix) {
  color: #673ab7;
}

.login-button {
  margin-top: 12px;
  border-radius: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #673ab7 0%, #9575cd 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(103, 58, 183, 0.35);
}

.login-button:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-icon {
  color: #9e9e9e;
}

.footer-text {
  font-size: 13px;
  color: #757575;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }
  
  .left-section {
    padding: 40px 20px;
    min-height: 250px;
  }
  
  .logo-image {
    width: 64px;
    height: 64px;
  }
  
  .brand-title {
    font-size: 32px;
  }
  
  .brand-slogan {
    font-size: 16px;
  }
  
  .right-section {
    padding: 40px 20px;
  }
  
  .form-title {
    font-size: 24px;
  }
}
</style>
