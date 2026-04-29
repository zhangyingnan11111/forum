import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/home'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/post/create',
        name: 'PostCreate',
        component: () => import('@/views/PostCreate.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/notifications',
        name: 'Notification',
        component: () => import('@/views/Notification.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.role && userInfo.role !== to.meta.role) {
        next('/home')
    } else {
        next()
    }
})

export default router