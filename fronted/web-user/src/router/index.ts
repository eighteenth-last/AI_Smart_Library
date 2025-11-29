import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/Home/index.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/Login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/Register/index.vue')
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('../pages/Books/index.vue')
  },
  {
    path: '/book/:id',
    name: 'BookDetail',
    component: () => import('../pages/BookDetail/index.vue'),
    props: true
  },
  {
    path: '/user-center',
    name: 'UserCenter',
    component: () => import('../pages/UserCenter/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../pages/Favorites/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reviews',
    name: 'Reviews',
    component: () => import('../pages/Reviews/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/borrow-history',
    name: 'BorrowHistory',
    component: () => import('../pages/BorrowHistory/index.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router