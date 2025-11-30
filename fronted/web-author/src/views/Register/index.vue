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
          <p class="brand-slogan">成为作者，分享你的知识与智慧</p>
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
            <h2 class="form-title">创建作者账号</h2>
            <p class="form-subtitle">填写信息，开启你的创作之旅</p>
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
                    <n-icon :component="UserOutlined" />
                  </template>
                </n-input>
              </n-form-item>

              <n-form-item path="nickname" class="form-col">
                <n-input 
                  v-model:value="formData.nickname" 
                  placeholder="昵称" 
                  size="large"
                >
                  <template #prefix>
                    <n-icon :component="UserOutlined" />
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
                  <n-icon :component="MailOutlined" />
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
                  <n-icon :component="PhoneOutlined" />
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
                    <n-icon :component="LockOutlined" />
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
                    <n-icon :component="LockOutlined" />
                  </template>
                </n-input>
              </n-form-item>
            </div>

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
            <router-link to="/author/login" class="footer-link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMessage, type FormInst, type FormRules } from 'naive-ui';
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined } from '@vicons/antd';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();
const message = useMessage();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const formData = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'author'
});

// 验证确认密码
const validatePasswordSame = (_rule: any, value: string) => {
  if (value !== formData.password) {
    return new Error('两次输入的密码不一致');
  }
  return true;
};

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePasswordSame, trigger: 'blur' }
  ]
};

const handleRegister = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;

    // 调用注册接口
    const response = await request.post('/auth/register', {
      username: formData.username,
      password: formData.password,
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone || undefined,
      role: formData.role
    });

    message.success('注册成功！你的作者账号已提交审核，请等待管理员审核通过后再登录。', 10);
    // 跳转到登录页
    setTimeout(() => {
      router.push('/author/login');
    }, 2000);
  } catch (error: any) {
    message.error(error.message || '注册失败');
  } finally {
    loading.value = false;
  }
};

// 从URL获取角色参数
onMounted(() => {
  const role = route.query.role as string;
  if (role) {
    formData.role = role;
  }
});
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 50%, #7dd3fc 100%);
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
  box-shadow: 0 20px 60px rgba(56, 189, 248, 0.2);
}

/* 左侧装饰区域 */
.left-section {
  flex: 1;
  background: linear-gradient(135deg, #0ea5e9 0%, #38bdf8 100%);
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
}

.brand-slogan {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
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
  margin-bottom: 12px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #0c4a6e;
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
  background: #f8fafc;
  transition: all 0.3s ease;
}

.register-form :deep(.n-input:hover) {
  border-color: #bae6fd;
  background: white;
}

.register-form :deep(.n-input.n-input--focus) {
  border-color: #0ea5e9;
  background: white;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
}

.register-form :deep(.n-input__input) {
  background: transparent !important;
}

.register-form :deep(.n-input .n-input__prefix) {
  color: #0ea5e9;
}

.register-button {
  margin-top: 12px;
  border-radius: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #0ea5e9 0%, #38bdf8 100%);
  border: none;
  transition: all 0.3s ease;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.3);
}

.form-footer {
  text-align: center;
  margin-top: 28px;
}

.footer-text {
  font-size: 14px;
  color: #64748b;
}

.footer-link {
  font-size: 14px;
  color: #0ea5e9;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: all 0.2s ease;
}

.footer-link:hover {
  color: #0284c7;
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
