import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/Layout.vue'),
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/ActivityDetail.vue')
      },
      {
        path: 'publish',
        name: 'PublishActivity',
        component: () => import('@/views/PublishActivity.vue')
      },
      {
        path: 'edit-activity/:id',
        name: 'EditActivity',
        component: () => import('@/views/EditActivity.vue')
      },
      {
        path: 'my-activities',
        name: 'MyActivities',
        component: () => import('@/views/MyActivities.vue')
      },
      {
        path: 'my-collections',
        name: 'MyCollections',
        component: () => import('@/views/MyCollections.vue')
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/MyRegistrations.vue')
      },
      {
        path: 'my-subscriptions',
        name: 'MySubscriptions',
        component: () => import('@/views/MySubscriptions.vue')
      },
      {
        path: 'my-notifications',
        name: 'MyNotifications',
        component: () => import('@/views/MyNotifications.vue')
      },
      {
        path: 'activity-registrations/:id',
        name: 'ActivityRegistrations',
        component: () => import('@/views/ActivityRegistrations.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/activities/approval',
        name: 'AdminActivityApproval',
        component: () => import('@/views/admin/ActivityApproval.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/monitor',
        name: 'AdminSystemMonitor',
        component: () => import('@/views/admin/SystemMonitor.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/statistics',
        name: 'AdminDataStatistics',
        component: () => import('@/views/admin/DataStatistics.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/register', '/forgot-password']

  if (!token && !publicPages.includes(to.path)) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    const userStore = useUserStore()
    if (userStore.userInfo?.role !== 'admin') {
      next('/home')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
