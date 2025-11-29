<template>
  <n-config-provider :theme="theme">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <router-view />
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { NConfigProvider, NMessageProvider, NNotificationProvider, NDialogProvider } from 'naive-ui';

const userStore = useUserStore();
const theme = ref(null); // 使用浅色主题

onMounted(() => {
  // 检查本地存储中的用户信息，恢复登录状态
  const userInfo = localStorage.getItem('userInfo');
  if (userInfo) {
    userStore.setUser(JSON.parse(userInfo));
  }
});
</script>