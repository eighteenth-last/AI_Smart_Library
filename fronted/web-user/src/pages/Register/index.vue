<template>
  <div class="register-container">
    <div class="register-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="left-section">
        <div class="brand">

          <div class="brand-icon">
            <img src="/favicon.ico" alt="Logo" class="logo-image" />
          </div>
          <h1 class="brand-title">神阁慧境阁</h1>
          <p class="brand-slogan">加入我们，开启智慧阅读新体验</p>
        </div>
        <div class="decoration">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="right-section">
        <div class="register-card">
          <div class="card-header">
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">填写信息，开启你的阅读之旅</p>
          </div>

          <n-form ref="formRef" :model="formData" :rules="rules" class="register-form">
            <div class="form-row">
              <n-form-item path="username" class="form-col">
                <n-input 
                  v-model:value="formData.username" 
                  placeholder="用户名 (3-50字符)" 
                  size="large"
                >
                  <template #prefix>
                    <n-icon>
                      <person-outline />
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>

              <n-form-item path="nickname" class="form-col">
                <n-input 
                  v-model:value="formData.nickname" 
                  placeholder="昵称 (可选)" 
                  size="large"
                >
                  <template #prefix>
                    <n-icon>
                      <happy-outline />
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>
            </div>

            <n-form-item path="email">
              <n-input 
                v-model:value="formData.email" 
                placeholder="邮箱" 
                size="large"
              >
                <template #prefix>
                  <n-icon>
                    <mail-outline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="phone">
              <n-input 
                v-model:value="formData.phone" 
                placeholder="手机号 (可选)" 
                size="large"
              >
                <template #prefix>
                  <n-icon>
                    <call-outline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>

            <div class="form-row">
              <n-form-item path="password" class="form-col">
                <n-input 
                  v-model:value="formData.password" 
                  type="password" 
                  placeholder="密码 (6-20字符)" 
                  size="large"
                  show-password-on="click"
                >
                  <template #prefix>
                    <n-icon>
                      <lock-closed-outline />
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>

              <n-form-item path="confirmPassword" class="form-col">
                <n-input 
                  v-model:value="formData.confirmPassword" 
                  type="password" 
                  placeholder="确认密码" 
                  size="large"
                  show-password-on="click"
                >
                  <template #prefix>
                    <n-icon>
                      <lock-closed-outline />
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>
            </div>

            <!-- 用户端注册默认为读者，不显示角色选择 -->
            <n-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              @click="handleRegister" 
              block
              class="register-button"
            >
              注册
            </n-button>
          </n-form>

          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">立即登录</router-link>
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
import { PersonOutline, LockClosedOutline, MailOutline, HappyOutline, CallOutline } from '@vicons/ionicons5';
import { useUserStore } from '@/store/user';
import { register } from '@/api/user/auth';

const router = useRouter();
const message = useMessage();
const userStore = useUserStore();

const formRef = ref();
const loading = ref(false);

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  nickname: '',
  phone: '',
  role: 'reader' as 'reader' | 'author'
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string) => {
        return value === formData.password;
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
};

const handleRegister = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;
    
    const { confirmPassword, ...registerData } = formData;
    const response = await register(registerData);
    
    userStore.setToken(response.token);
    message.success('注册成功！欢迎加入神阁慧境阁');
    router.push('/');
  } catch (error: any) {
    message.error(error.message || '注册失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.logo-image {
  width: 96px;
  height: 96px;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.2));
}

.register-wrapper {
  display: flex;
  width: 100%;
  max-width: 1100px;
  min-height: 650px;
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
  padding: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
}

.register-card {
  width: 100%;
  max-width: 480px;
}

.card-header {
  margin-bottom: 24px;
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

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-col {
  flex: 1;
}

.register-form :deep(.n-input) {
  border-radius: 10px;

  background: #f9fafb;
  transition: all 0.3s ease;
}

.register-form :deep(.n-input:hover) {
  border-color: #d1d5db;
  background: white;
}

.register-form :deep(.n-input.n-input--focus) {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.register-form :deep(.n-input__input) {
  background: transparent !important;
}

.register-form :deep(.n-input .n-input__prefix) {
  color: #667eea;
}

.register-form :deep(.n-radio) {
  padding: 0 16px;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.register-form :deep(.n-radio:hover) {
  border-color: #d1d5db;
  background: white;
}

.register-form :deep(.n-radio.n-radio--checked) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.register-button {
  margin-top: 12px;
  border-radius: 10px;
  height: 40px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.form-footer {
  text-align: center;
  margin-top: 15px;
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
  .register-wrapper {
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
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
