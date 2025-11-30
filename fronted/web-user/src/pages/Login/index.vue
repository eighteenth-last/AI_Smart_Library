<template>
  <div class="user-login-container">
    <div class="login-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="left-section">
        <div class="brand">
          <div class="brand-icon">
            <img src="/favicon.ico" alt="Logo" class="logo-image" />
          </div>
          <h1 class="brand-title">神阁慧境阁</h1>
          <p class="brand-slogan">海量图书资源，让阅读更加智能便捷</p>
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
            <h2 class="form-title">用户登录</h2>
            <p class="form-subtitle">欢迎回来，开启你的阅读之旅</p>
          </div>

          <n-form ref="formRef" :model="formData" :rules="rules" class="login-form">
            <n-form-item path="username">
              <n-input 
                v-model:value="formData.username" 
                placeholder="请输入用户名" 
                size="large"
                @keydown.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon>
                    <person-outline />
                  </n-icon>
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
                  <n-icon>
                    <lock-closed-outline />
                  </n-icon>
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
              登录
            </n-button>
          </n-form>

          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link">立即注册</router-link>
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
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5';
import { useUserStore } from '@/store/user';
import { login, authAPI } from '@/api/user/auth';

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
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
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
    
    // 1. 先登录获取token
    const response = await login(formData);
    userStore.setToken(response.token);
    
    // 2. 登录成功后立即获取完整的用户信息（包括头像）
    try {
      const userProfile = await authAPI.getProfile();
      userStore.setUserInfo({
        userId: userProfile.userId,
        username: userProfile.username,
        nickname: userProfile.nickname,
        role: userProfile.role as 'reader' | 'admin' | 'author',
        email: userProfile.email,
        phone: userProfile.phone,
        profilePicture: userProfile.profilePicture
      });
    } catch (profileError) {
      console.error('获取用户详细信息失败:', profileError);
      // 如果获取详细信息失败，仍然使用登录返回的基本信息
      userStore.setUserInfo({
        userId: response.userId,
        username: response.username,
        nickname: response.nickname,
        role: response.role as 'reader' | 'admin' | 'author',
        email: ''
      });
    }
    
    message.success('登录成功');
    router.push('/');
  } catch (error: any) {
    message.error(error.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.user-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.3);
}

/* 左侧装饰区域 */
.left-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  font-size: 80px;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  color: white;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  line-height: 1.6;
  max-width: 300px;
}

.logo-image {
  width: 96px;
  height: 96px;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.2));
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
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.card-header {
  margin-bottom: 40px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.login-form :deep(.n-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.n-input) {
  border-radius: 10px;
  border: 2px solid #e5e7eb;
  background: #f9fafb;
  transition: all 0.3s ease;
}

.login-form :deep(.n-input:hover) {
  border-color: #d1d5db;
  background: white;
}

.login-form :deep(.n-input.n-input--focus) {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.login-form :deep(.n-input__input) {
  background: transparent !important;
}

.login-form :deep(.n-input .n-input__prefix) {
  color: #667eea;
}

.login-button {
  margin-top: 12px;
  border-radius: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.form-footer {
  text-align: center;
  margin-top: 32px;
}

.footer-text {
  font-size: 14px;
  color: #64748b;
}

.footer-link {
  font-size: 14px;
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: all 0.2s ease;
}

.footer-link:hover {
  color: #5568d3;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }
  
  .left-section {
    padding: 40px 20px;
    min-height: 200px;
  }
  
  .brand-icon {
    font-size: 50px;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .right-section {
    padding: 40px 20px;
  }
}
</style>
