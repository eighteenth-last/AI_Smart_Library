import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/author/login'
  },
  {
    path: '/author',
    name: 'Author',
    redirect: '/author/dashboard',
    meta: { requiresAuth: true, roles: ['author'] },
    component: () => import('../views/Layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'AuthorDashboard',
        component: () => import('../views/Dashboard/index.vue'),
        meta: { requiresAuth: true, roles: ['author'] }
      },
      {
        path: 'my-books',
        name: 'MyBooks',
        component: () => import('../views/MyBooks/index.vue'),
        meta: { requiresAuth: true, roles: ['author'] }
      },
      {
        path: 'qa',
        name: 'QAManagement',
        component: () => import('../views/QAManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['author'] }
      },
      {
        path: 'statistics',
        name: 'AuthorStatistics',
        component: () => import('../views/Statistics/index.vue'),
        meta: { requiresAuth: true, roles: ['author'] }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('../views/AiAssistant/index.vue'),
        meta: { requiresAuth: true, roles: ['author'] }
      }
    ]
  },
  {
    path: '/author/login',
    name: 'AuthorLogin',
    component: () => import('../views/Login/index.vue')
  },
  {
    path: '/register',
    name: 'AuthorRegister',
    component: () => import('../views/Register/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}
  
  // 如果是登录页，直接放行
  if (to.path === '/author/login' || to.path === '/register') {
    next()
    return
  }
  
  // 需要认证的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      // 没有token，跳转登录
      next('/author/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      if (!userInfo.role || !to.meta.roles.includes(userInfo.role)) {
        // 角色不匹配，跳转登录
        next('/author/login')
        return
      }
    }
  }
  
  next()
})

export default router