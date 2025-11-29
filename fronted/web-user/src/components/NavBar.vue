<template>
  <div class="navbar-actions">
    <n-button v-if="!userStore.isLoggedIn()" text @click="router.push('/login')" class="login-btn">
      登录
    </n-button>
    <template v-else>
      <!-- 消息通知按钮 -->
      <n-popover trigger="hover" placement="bottom" :show-arrow="false" v-if="unreadCount > 0">
        <template #trigger>
          <n-badge :value="unreadCount" :max="99" class="notification-badge" :processing="true">
            <n-button text class="notification-btn" @click="showNotificationModal = true">
              <n-icon size="20">
                <notifications-outline />
              </n-icon>
            </n-button>
          </n-badge>
        </template>
        <div style="max-width: 200px;">
          <n-text strong>您有 {{ unreadCount }} 条未读消息</n-text>
        </div>
      </n-popover>
      <n-badge v-else :value="unreadCount" :max="99" class="notification-badge">
        <n-button text class="notification-btn" @click="showNotificationModal = true">
          <n-icon size="20">
            <notifications-outline />
          </n-icon>
        </n-button>
      </n-badge>
      
      <!-- 用户菜单 -->
      <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
        <n-button text class="user-btn">
          <!-- 显示头像 -->
          <n-avatar
              v-if="currentAvatarUrl"
              :size="28"
              :src="currentAvatarUrl"
              style="margin-right: 8px;"
          />
          <n-icon v-else size="20" style="margin-right: 4px;"><person-outline /></n-icon>
          {{ userStore.userInfo?.username || '用户' }}
        </n-button>
      </n-dropdown>
    </template>

    <!-- 消息通知弹窗 -->
    <n-modal
      v-model:show="showNotificationModal"
      preset="card"
      title="消息通知"
      style="width: 600px; max-height: 70vh;"
      :bordered="false"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <template #header-extra>
        <n-button text @click="handleMarkAllAsRead" :disabled="unreadCount === 0" type="primary">
          全部已读
        </n-button>
      </template>

      <n-scrollbar style="max-height: 50vh;">
        <n-empty v-if="notifications.length === 0" description="暂无消息" />
        <n-list v-else hoverable clickable>
          <n-list-item
            v-for="item in notifications"
            :key="item.notificationId"
            @click="handleNotificationClick(item)"
          >
            <template #prefix>
              <n-icon size="24" :color="getNotificationColor(item.type)">
                <component :is="getNotificationIcon(item.type)" />
              </n-icon>
            </template>
            <div class="notification-item">
              <div class="notification-header">
                <n-text strong :class="{ 'unread': item.isRead === 0 }">
                  {{ item.title }}
                </n-text>
                <n-tag
                  size="small"
                  :type="getNotificationTagType(item.type)"
                  style="margin-left: 8px;"
                >
                  {{ getNotificationTypeLabel(item.type) }}
                </n-tag>
              </div>
              <n-text depth="3" class="notification-content">
                {{ item.content }}
              </n-text>
              <n-text depth="3" class="notification-time">
                {{ formatTime(item.createdAt) }}
              </n-text>
            </div>
            <template #suffix>
              <n-badge v-if="item.isRead === 0" dot processing />
            </template>
          </n-list-item>
        </n-list>
      </n-scrollbar>

      <template #footer>
        <div class="notification-footer">
          <n-pagination
            v-model:page="pagination.page"
            :page-count="pagination.pageCount"
            :page-size="pagination.pageSize"
            @update:page="loadNotifications"
            simple
          />
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { NButton, NDropdown, NAvatar, NIcon, NModal, NScrollbar, NList, NListItem, NText, NTag, NBadge, NEmpty, NPagination, NPopover, useMessage } from 'naive-ui'
import { PersonOutline, NotificationsOutline, TimeOutline, AlertCircleOutline, InformationCircleOutline } from '@vicons/ionicons5'
import { notificationAPI } from '@/api/user/notification'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

// 获取头像完整URL
const getAvatarUrl = (url?: string) => {
  if (!url) return '';
  if (url.startsWith('/')) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return baseURL + url;
  }
  return url;
};

// 计算当前头像URL
const currentAvatarUrl = computed(() => {
  return getAvatarUrl(userStore.userInfo?.profilePicture);
});

// 消息通知相关
const showNotificationModal = ref(false)
const notifications = ref<any[]>([])
const unreadCount = ref(0)
const pagination = ref({
  page: 1,
  pageSize: 10,
  pageCount: 1
})

// 用户菜单选项
const userMenuOptions = [
  {
    label: '个人中心',
    key: 'profile'
  },
  {
    label: '退出登录',
    key: 'logout'
  }
]

const handleUserMenuSelect = async (key: string) => {
  if (key === 'profile') {
    router.push('/user-center')
  } else if (key === 'logout') {
    try {
      userStore.clearUser()
      message.success('退出成功')
      router.push('/login')
    } catch (error) {
      message.error('退出失败')
    }
  }
}

// 加载通知列表
const loadNotifications = async () => {
  if (!userStore.isLoggedIn()) return
  
  try {
    const response = await notificationAPI.getList({
      page: pagination.value.page,
      size: pagination.value.pageSize
    })
    
    // 响应拦截器已经返回了 data，所以直接访问属性
    notifications.value = response.records || []
    unreadCount.value = response.unreadCount || 0
    pagination.value.pageCount = Math.ceil((response.total || 0) / pagination.value.pageSize)
  } catch (error) {
    console.error('加载通知失败:', error)
    // 发生错误时重置数据
    notifications.value = []
    unreadCount.value = 0
  }
}

// 点击通知项
const handleNotificationClick = async (item: any) => {
  if (item.isRead === 0) {
    try {
      await notificationAPI.markAsRead(item.notificationId)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

// 全部标记已读
const handleMarkAllAsRead = async () => {
  try {
    await notificationAPI.markAllAsRead()
    notifications.value.forEach(item => item.isRead = 1)
    unreadCount.value = 0
    message.success('已全部标记为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
    message.error('操作失败')
  }
}

// 获取通知图标
const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'borrow_due':
      return TimeOutline
    case 'overdue':
      return AlertCircleOutline
    case 'system_announcement':
      return InformationCircleOutline
    default:
      return NotificationsOutline
  }
}

// 获取通知颜色
const getNotificationColor = (type: string) => {
  switch (type) {
    case 'borrow_due':
      return '#f0a020'
    case 'overdue':
      return '#d03050'
    case 'system_announcement':
      return '#2080f0'
    default:
      return '#18a058'
  }
}

// 获取通知标签类型
const getNotificationTagType = (type: string) => {
  switch (type) {
    case 'borrow_due':
      return 'warning'
    case 'overdue':
      return 'error'
    case 'system_announcement':
      return 'info'
    default:
      return 'default'
  }
}

// 获取通知类型标签
const getNotificationTypeLabel = (type: string) => {
  switch (type) {
    case 'borrow_due':
      return '到期提醒'
    case 'overdue':
      return '逾期提醒'
    case 'system_announcement':
      return '系统公告'
    default:
      return '通知'
  }
}

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn(), (isLoggedIn) => {
  if (isLoggedIn) {
    loadNotifications()
  } else {
    notifications.value = []
    unreadCount.value = 0
  }
})

// 监听弹窗打开，刷新数据
watch(showNotificationModal, (show) => {
  if (show) {
    pagination.value.page = 1
    loadNotifications()
  }
})

// 组件挂载时加载通知
onMounted(() => {
  if (userStore.isLoggedIn()) {
    loadNotifications()
    // 每60秒自动刷新一次
    setInterval(() => {
      if (userStore.isLoggedIn()) {
        loadNotifications()
      }
    }, 60000)
  }
})
</script>

<style scoped>
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-badge {
  display: inline-flex;
}

.notification-btn {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #333;
  transition: all 0.3s ease;
  padding: 4px 8px;
}

.notification-btn:hover {
  color: #667eea;
}

.user-btn {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #333;
  transition: all 0.3s ease;
}

.user-btn:hover {
  color: #667eea;
}

.login-btn {
  font-size: 14px;
  color: #667eea;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-btn:hover {
  color: #5568d3;
}

.notification-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.notification-header {
  display: flex;
  align-items: center;
}

.notification-header .unread {
  font-weight: 600;
}

.notification-content {
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
}

.notification-footer {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
