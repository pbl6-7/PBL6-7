import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

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
        path: 'activity-registrations/:id',
        name: 'ActivityRegistrations',
        component: () => import('@/views/ActivityRegistrations.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
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
  } else {
    next()
  }
})

export default router
