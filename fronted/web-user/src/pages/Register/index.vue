<template>
  <div class="register-container">
    <n-card class="register-card" title="用户注册" size="large">
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" size="large">
            <template #prefix>
              <n-icon><person-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="password">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码" size="large">
            <template #prefix>
              <n-icon><lock-closed-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="confirmPassword">
          <n-input v-model:value="formData.confirmPassword" type="password" placeholder="请确认密码" size="large">
            <template #prefix>
              <n-icon><lock-closed-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" size="large">
            <template #prefix>
              <n-icon><mail-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称（选填）" size="large">
            <template #prefix>
              <n-icon><happy-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号（选填）" size="large">
            <template #prefix>
              <n-icon><call-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="role">
          <n-radio-group v-model:value="formData.role" name="role">
            <n-space>
              <n-radio value="reader">读者</n-radio>
              <n-radio value="author">作者</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item>
          <n-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width: 100%">
            注册
          </n-button>
        </n-form-item>
      </n-form>
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </n-card>
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
  role: 'reader'
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
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
};

const handleRegister = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;
    
    const { confirmPassword, ...registerData } = formData;
    const response = await register(registerData);
    
    userStore.setToken(response.token);
    message.success('注册成功');
    
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
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 450px;
  padding: 20px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}
</style>
