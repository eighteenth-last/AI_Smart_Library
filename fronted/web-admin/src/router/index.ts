import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 扩展路由元信息类型
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: string[]
  }
}

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/admin/login'
  },
  {
    path: '/admin',
    name: 'Admin',
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, roles: ['admin'] },
    component: () => import('../views/Layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/Dashboard/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/UserManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'books',
        name: 'BookManagement',
        component: () => import('../views/BookManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('../views/CategoryManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'borrows',
        name: 'BorrowManagement',
        component: () => import('../views/BorrowManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'reviews',
        name: 'ReviewManagement',
        component: () => import('../views/ReviewManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'notifications',
        name: 'NotificationManagement',
        component: () => import('../views/NotificationManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'author-review',
        name: 'AuthorReview',
        component: () => import('../views/AuthorReview/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'knowledge',
        name: 'KnowledgeManagement',
        component: () => import('../views/KnowledgeManagement/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'knowledge-qa',
        name: 'KnowledgeQA',
        component: () => import('../views/KnowledgeQA/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'token-stats',
        name: 'TokenStats',
        component: () => import('../views/TokenStats/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('../views/AiAssistant/index.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/Login/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  if (to.meta.requiresAuth && !token) {
    next('/admin/login')
  } else if (to.meta.roles && Array.isArray(to.meta.roles) && !to.meta.roles.includes(userInfo.role)) {
    // 如果用户角色不符合要求，重定向到登录页
    next('/admin/login')
  } else {
    next()
  }
})

export default router