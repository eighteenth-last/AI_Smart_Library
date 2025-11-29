<template>
  <div class="login-container">
    <n-card class="login-card" title="用户登录" size="large">
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" size="large">
            <template #prefix>
              <n-icon><person-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="password">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码" size="large" @keydown.enter="handleLogin">
            <template #prefix>
              <n-icon><lock-closed-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item>
          <n-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </n-button>
        </n-form-item>
      </n-form>
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5';
import { useUserStore } from '@/store/user';
import { login } from '@/api/user/auth';

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
    
    const response = await login(formData);
    userStore.setToken(response.token);
    userStore.setUserInfo({
      userId: response.userId,
      username: response.username,
      nickname: response.nickname,
      role: response.role as 'reader' | 'admin' | 'author',
      email: ''
    });
    
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}
</style>
