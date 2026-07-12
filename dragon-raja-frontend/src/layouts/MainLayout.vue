<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar" :class="{ scrolled: isScrolled }">
      <div class="navbar-inner">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <img src="/logo.png" alt="Logo" class="logo-img" />
          <span class="logo-text">永不熄灭的黄金瞳</span>
        </div>

        <!-- 导航链接 -->
        <nav class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/post/create" class="nav-link" v-if="userStore.isLoggedIn">发帖</router-link>
          <router-link to="/news" class="nav-link">新闻</router-link>
          <router-link to="/friends" class="nav-link" v-if="userStore.isLoggedIn">联系人</router-link>
          <router-link to="/messages" class="nav-link" v-if="userStore.isLoggedIn">邮件</router-link>
          <router-link to="/chat" class="nav-link" v-if="userStore.isLoggedIn">私信</router-link>
          <router-link to="/admin/audit" class="nav-link" v-if="userStore.canAudit && !userStore.isAdmin">注册审核</router-link>
          <router-link to="/admin/users" class="nav-link" v-if="userStore.isAdmin">管理后台</router-link>
        </nav>

        <!-- 消息通知 -->
        <div class="user-area">
          <template v-if="userStore.isLoggedIn">
            <el-popover ref="notifPopoverRef" placement="bottom" :width="320" trigger="click" @show="onNotifShow" @hide="onNotifHide">
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                  <el-button :icon="Bell" circle class="bell-btn" />
                </el-badge>
              </template>
              <div class="notif-list" ref="notifListRef" :class="{ collapsing: collapsing }" :style="listStyle">
                <div class="notif-header">
                  <span>消息通知</span>
                  <el-button text size="small" :loading="clearing" :disabled="clearing" @click="markAllRead" v-if="messages.length > 0">清空全部</el-button>
                </div>
                <div v-if="messages.length === 0" class="notif-empty">暂无消息</div>
                <div v-for="msg in messages" :key="msg.id" class="notif-item" :class="{ unread: msg.isRead === 0, sliding: slidingIds.includes(msg.id) }" @click="readMsg(msg)">
                  <div class="notif-left">
                    <div class="notif-unread-dot" v-if="msg.isRead === 0"></div>
                    <el-avatar :size="36" :src="msg.avatar" class="notif-avatar">
                      {{ (msg.sender || '诺玛').charAt(0) }}
                    </el-avatar>
                  </div>
                  <div class="notif-body">
                    <div class="notif-head">
                      <span class="notif-sender">{{ msg.sender || '诺玛' }}</span>
                      <span class="notif-tag" :class="(msg.type || 'system').toLowerCase()">{{ typeLabel(msg.type) }}</span>
                    </div>
                    <div class="notif-content">{{ msg.content }}</div>
                    <div class="notif-meta">
                      <span class="notif-time">{{ formatRelativeTime(msg.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-popover>
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="36" :src="userStore.userInfo.avatar || defaultAvatar">
                  {{ userStore.userInfo.nickname?.charAt(0) || userStore.userInfo.username?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="post">发布帖子</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            <el-button @click="router.push('/blood-test')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>混血种的乐园 © 2026 | 卡塞尔学院</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getNotifications, getUnreadCount, markAsRead, markNotificationsRead } from '../api/message'
import { formatRelativeTime } from '../utils/format'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = ''

const messages = ref([])
const unreadCount = ref(0)
const badgeDismissed = ref(false)
const lastKnownCount = ref(0)
const slidingIds = ref([])
const clearing = ref(false)
const collapsing = ref(false)
const listStyle = ref({})
const isScrolled = ref(false)
const notifListRef = ref(null)
const notifPopoverRef = ref(null)
let pollTimer = null
let scrollTimer = null

async function loadMessages() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getNotifications({ current: 1, size: 5 })
    messages.value = res.data?.records || []
  } catch { /* ignore */ }
}

async function loadUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    const count = res.data?.count || 0
    // 如果已消除红点且没有新通知，保持0
    if (badgeDismissed.value && count <= lastKnownCount.value) {
      unreadCount.value = 0
    } else {
      unreadCount.value = count
      if (count > lastKnownCount.value) badgeDismissed.value = false
    }
  } catch { /* ignore */ }
}

async function goPost(postId, msgId) {
  await markAsRead(msgId)
  loadUnread()
  loadMessages()
  router.push(`/post/${postId}`)
}

async function markAllRead() {
  if (clearing.value || messages.value.length === 0) return
  clearing.value = true
  const allList = [...messages.value]
  const el = notifListRef.value
  if (el) {
    listStyle.value = { minHeight: el.offsetHeight + 'px' }
  }
  await markNotificationsRead()
  unreadCount.value = 0
  badgeDismissed.value = false
  lastKnownCount.value = 0
  allList.forEach((msg, i) => {
    setTimeout(() => slidingIds.value.push(msg.id), i * 80)
  })
  const slideMs = allList.length * 80 + 350
  setTimeout(() => {
    messages.value = []
    slidingIds.value = []
    // 阶段2：容器收缩动画
    collapsing.value = true
    listStyle.value = { minHeight: '0px' }
    setTimeout(() => {
      listStyle.value = {}
      collapsing.value = false
      clearing.value = false
    }, 400)
  }, slideMs)
}

async function readMsg(msg) {
  if (msg.isRead === 0) { await markAsRead(msg.id); msg.isRead = 1 }
  if (msg.type === 'PRIVATE') {
    router.push(msg.senderId ? `/chat?user=${msg.senderId}` : '/chat')
  } else if (msg.type === 'REPLY' && msg.postId) {
    const query = msg.commentId ? `?comment=${msg.commentId}` : ''
    router.push(`/post/${msg.postId}${query}`)
  } else if (msg.type === 'SYSTEM') {
    router.push(`/messages?open=${msg.id}`)
  } else {
    router.push(`/messages?open=${msg.id}`)
  }
  notifPopoverRef.value?.hide()
}

function startPoll() {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(() => { loadUnread(); loadMessages() }, 10000)
}
function stopPoll() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

async function onNotifShow() {
  stopPoll()
  lastKnownCount.value = unreadCount.value
  unreadCount.value = 0
  badgeDismissed.value = true
  loadMessages()
}
function onNotifHide() { startPoll() }

function handleScroll() {
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => {
    isScrolled.value = window.scrollY > 50
  }, 100)
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadMessages()
    loadUnread()
    startPoll()
  }
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => { 
  if (pollTimer) clearInterval(pollTimer)
  if (scrollTimer) clearTimeout(scrollTimer)
  window.removeEventListener('scroll', handleScroll)
})

function typeLabel(t) { return {'REPLY':'回复','PRIVATE':'私信','SYSTEM':'系统','EMAIL':'邮件'}[t] || '' }

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'post':
      router.push('/post/create')
      break
    case 'logout':
      handleLogout()
      break
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消操作，不做处理
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* 导航栏 — heavy (55%) 最实，确保导航清晰 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--glass-heavy);
  backdrop-filter: var(--glass-heavy-blur);
  -webkit-backdrop-filter: var(--glass-heavy-blur);
  border-bottom: 1px solid var(--glass-heavy-border);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
  transition: all var(--transition-normal);
}

.navbar.scrolled {
  background: rgba(10, 14, 39, 0.70);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.5),
    0 0 16px rgba(0, 212, 255, 0.06);
}

.navbar-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.logo:hover {
  text-shadow: 0 0 10px var(--accent-glow);
}

.logo-icon { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
.logo-img { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; }

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-primary);
  text-shadow: 0 0 10px var(--accent-glow);
  letter-spacing: 2px;
  white-space: nowrap;
}

/* 导航链接 */
.nav-links {
  display: flex;
  gap: var(--spacing-lg);
  flex: 1;
  margin-left: var(--spacing-xl);
}

.nav-link {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  position: relative;
}

.nav-link:hover {
  color: var(--accent-primary);
}

.nav-link.router-link-exact-active {
  color: var(--accent-primary);
}

.nav-link.router-link-exact-active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--accent-primary);
  box-shadow: 0 0 8px var(--accent-glow);
}

/* 用户区域 */
.bell-btn { color: var(--text-secondary); transition: color var(--transition-fast); width: 40px; height: 40px; font-size: 18px; margin-right: 4px; }
.bell-btn:hover { color: var(--accent-primary); }

.user-area :deep(.el-badge.el-tooltip__trigger) {
  display: flex;
  left: 15px;
}

.notif-list { max-height: 360px; overflow-y: auto; transition: min-height 0.4s cubic-bezier(0.4, 0, 0.2, 1); }

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px 8px;
  margin-bottom: 4px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.notif-empty {
  text-align: center;
  padding: var(--spacing-xl) 0;
  color: var(--text-muted);
  font-size: 13px;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: 
    background 0.2s ease,
    transform 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    max-height 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    padding-top 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    padding-bottom 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    margin-bottom 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  max-height: 200px;
  opacity: 1;
  transform: translateX(0);
}

.notif-item:not(:last-child) {
  border-bottom: 1px solid var(--border-color);
}

.notif-item:hover {
  background: var(--bg-hover);
}

.notif-item.unread {
  background: rgba(0, 212, 255, 0.04);
}

.notif-item.unread .notif-content {
  color: var(--text-primary);
  font-weight: 500;
}

/* 左侧：未读点 + 头像 */
.notif-left {
  position: relative;
  flex-shrink: 0;
  margin-top: 2px;
}

.notif-unread-dot {
  position: absolute;
  top: -2px;
  left: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-primary);
  box-shadow: 0 0 6px var(--accent-glow);
  z-index: 2;
}

.notif-avatar {
  border: 1px solid var(--border-color);
}

/* 右侧内容体 */
.notif-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.notif-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.notif-sender {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notif-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.notif-tag.private, .notif-tag.email {
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.12);
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.notif-tag.reply {
  color: #b388ff;
  background: rgba(179, 136, 255, 0.12);
  border: 1px solid rgba(179, 136, 255, 0.2);
}

.notif-tag.system {
  color: #ff9800;
  background: rgba(255, 152, 0, 0.12);
  border: 1px solid rgba(255, 152, 0, 0.2);
}

.notif-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.notif-item.unread .notif-content {
  -webkit-line-clamp: 3;
}

.notif-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 1px;
}

.notif-time {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.notif-item.sliding {
  transform: translateX(120%) scale(0.92);
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-bottom: 0;
  overflow: hidden;
  background: rgba(0, 212, 255, 0.08);
  pointer-events: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.user-info:hover {
  background-color: var(--bg-hover);
}

.username {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
}

/* 内容区 */
.content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

@media (max-width: 768px) {
  .navbar-inner { 
    flex-wrap: wrap; 
    height: auto; 
    padding: var(--spacing-sm) var(--spacing-md); 
    gap: var(--spacing-xs); 
  }
  .logo { flex: 0 0 auto; }
  .logo-text { display: none; }
  .logo-img { width: 28px; height: 28px; }
  .nav-links { flex: 1; display: flex; gap: 4px; overflow: visible; margin-left: var(--spacing-sm); flex-wrap: nowrap; }
  .nav-link { font-size: 12px; padding: 4px 8px; white-space: nowrap; }
  .user-area { width: 100%; justify-content: flex-end; }
  .content { padding: var(--spacing-md); }
  .username { display: none; }
  .bell-btn { width: 36px; height: 36px; font-size: 16px; }
  .user-info .el-avatar { width: 34px; height: 34px; }
}

@media (max-width: 480px) {
  .navbar-inner { 
    padding: var(--spacing-xs-sm) var(--spacing-md); 
  }
  .content { 
    padding: var(--spacing-md); 
    width: 100%; 
    max-width: 100%; 
    margin: 0; 
  }
}

@media (prefers-reduced-motion: reduce) {
  .notif-item {
    transition: none !important;
  }
  .notif-item.sliding {
    transform: none;
    opacity: 0;
    max-height: 0;
    padding-top: 0;
    padding-bottom: 0;
    margin-bottom: 0;
    overflow: hidden;
  }
}

/* 页脚 — medium (35%) */
.footer {
  text-align: center;
  padding: var(--spacing-lg);
  color: var(--text-muted);
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border-top: 1px solid var(--glass-medium-border);
  font-size: 13px;
}
</style>