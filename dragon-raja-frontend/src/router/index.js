import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/blood-test',
    name: 'BloodTest',
    component: () => import('../views/BloodTestView.vue'),
    meta: { title: '3E血统测试' }
  },
  {
    path: '/chat/:targetId',
    name: 'ChatDetail',
    component: () => import('../views/ChatDetailView.vue'),
    meta: { title: '私信', requiresAuth: true }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('../views/PostDetailView.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'post/create',
        name: 'PostCreate',
        component: () => import('../views/PostCreateView.vue'),
        meta: { title: '发布帖子', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { title: '个人资料', requiresAuth: true }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../views/MessageView.vue'),
        meta: { title: '邮件', requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/ChatView.vue'),
        meta: { title: '私信', requiresAuth: true }
      },
      {
        path: 'friends',
        name: 'Friends',
        component: () => import('../views/FriendsView.vue'),
        meta: { title: '联系人', requiresAuth: true }
      },
      {
        path: 'news',
        name: 'News',
        component: () => import('../views/NewsView.vue'),
        meta: { title: '学院新闻' }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('../views/UserProfileView.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'admin',
        component: () => import('../views/admin/AdminLayout.vue'),
        meta: { title: '管理后台', requiresAuth: true },
        children: [
          {
            path: 'audit',
            name: 'AdminAudit',
            component: () => import('../views/admin/AuditView.vue'),
            meta: { title: '注册审核', requiresAuth: true, requiresAudit: true }
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('../views/admin/UserManageView.vue'),
            meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
          },
          {
            path: 'posts',
            name: 'AdminPosts',
            component: () => import('../views/admin/PostManageView.vue'),
            meta: { title: '帖子管理', requiresAuth: true, requiresAdmin: true }
          },
          {
            path: 'announcements',
            name: 'AdminAnnouncements',
            component: () => import('../views/admin/AnnouncementManageView.vue'),
            meta: { title: '公告管理', requiresAuth: true, requiresAdmin: true }
          },
          {
            path: '',
            redirect: '/admin/users'
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} | 混血种社区` : '混血种社区'

  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 需要管理员的页面（ADMIN 专属）
  if (to.meta.requiresAdmin && userInfo.role !== 'ADMIN') {
    next('/')
    return
  }

  // 需要审核权限的页面（ADMIN 或 AUDITOR）
  if (to.meta.requiresAudit && userInfo.role !== 'ADMIN' && userInfo.role !== 'AUDITOR') {
    next('/')
    return
  }

  // 管理后台根路径 — AUDITOR 跳转到审核页
  if (to.path === '/admin' && userInfo.role === 'AUDITOR') {
    next('/admin/audit')
    return
  }

  // 已登录用户不能访问登录/注册页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/')
    return
  }

  next()
})

export default router
