<template>
  <div class="home-view">
    <!-- 公告横幅 -->
    <AnnouncementBanner />

    <!-- 三栏布局：空列｜帖子区（居中）｜在线学员+快速入口 -->
    <div class="home-layout">
      <!-- 左占位列（空，用于居中） -->
      <aside class="spacer-panel"></aside>

      <!-- 中栏：帖子列表 -->
      <main class="center-panel">
        <!-- 移动端：在线学员浮动按钮 -->
        <div class="mobile-online-btn" @click="showOnlineDialog = true">
          <el-icon><User /></el-icon>
          <span>在线学员</span>
          <el-badge :value="onlineCount" type="success" v-if="onlineCount > 0" />
        </div>

        <!-- Tab切换 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="post-tabs">
          <el-tab-pane label="最新" name="latest" />
          <el-tab-pane label="热门" name="hot" />
        </el-tabs>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子标题或内容..."
            :prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleClearSearch"
          />
          <span v-if="isSearching" class="search-info">
            搜索 "{{ searchKeyword }}" 共 {{ total }} 条结果
            <span class="search-clear-link" @click="handleClearSearch">清除</span>
          </span>
        </div>

        <!-- 分类筛选（非搜索模式显示） -->
        <div class="category-filter" v-if="!isSearching">
          <span class="filter-label">分类：</span>
          <div class="filter-tags">
            <span
              v-for="cat in categories"
              :key="cat.value"
              class="filter-tag"
              :class="{ active: selectedCategory === cat.value }"
              :style="selectedCategory === cat.value ? { color: cat.color, borderColor: cat.color } : {}"
              @click="handleCategoryChange(cat.value)"
            >
              {{ cat.label }}
            </span>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list" v-loading="loading">
          <div v-if="posts.length === 0 && !loading" class="empty-posts">
            <el-empty :description="isSearching ? '未找到匹配的帖子' : '暂无帖子'" />
          </div>
          <PostCard v-for="post in posts" :key="post.id" :post="post" :keyword="searchKeyword" />
        </div>

        <!-- 分页 -->
        <Pagination
          v-if="total > 0"
          :current="currentPage"
          :total="total"
          :size="pageSize"
          @change="handlePageChange"
        />
      </main>

      <!-- 右栏：在线学员 + 快速入口 -->
      <aside class="right-panel">
        <OnlineUsers />
        <div class="quick-entry">
          <div class="entry-header">
            <el-icon><Compass /></el-icon>
            <span>快速入口</span>
          </div>
          <div class="entry-list">
            <div class="entry-item glow-hover" @click="router.push('/post/create')" v-if="userStore.isLoggedIn">
              <el-icon><EditPen /></el-icon>
              <span>发布帖子</span>
            </div>
            <div class="entry-item glow-hover" @click="router.push('/profile')" v-if="userStore.isLoggedIn">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </div>
            <div class="entry-item glow-hover" @click="router.push('/login')" v-if="!userStore.isLoggedIn">
              <el-icon><User /></el-icon>
              <span>登录</span>
            </div>
            <div class="entry-item glow-hover" @click="router.push('/blood-test')" v-if="!userStore.isLoggedIn">
              <el-icon><CirclePlus /></el-icon>
              <span>注册</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 移动端：在线学员弹窗 -->
    <Teleport to="body">
      <div v-if="showOnlineDialog" class="online-overlay" @click.self="showOnlineDialog = false">
        <div class="online-popup">
          <div class="online-popup-header">
            <button class="online-popup-close" @click="showOnlineDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div class="online-popup-body">
            <OnlineUsers />
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { Compass, EditPen, UserFilled, User, CirclePlus, Search, Close } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getPosts, getHotPosts } from '../api/post'
import { getOnlineUsers } from '../api/user'
import AnnouncementBanner from '../components/AnnouncementBanner.vue'
import OnlineUsers from '../components/OnlineUsers.vue'
import PostCard from '../components/PostCard.vue'
import Pagination from '../components/Pagination.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('latest')
const selectedCategory = ref(0)
const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const searchKeyword = ref('')
const isSearching = computed(() => !!searchKeyword.value.trim())
const showOnlineDialog = ref(false)
const onlineCount = ref(0)

const categories = [
  { value: 0, label: '全部', color: '#00d4ff' },
  { value: 1, label: '闲聊', color: '#4da6ff' },
  { value: 2, label: '史料', color: '#b388ff' },
  { value: 3, label: '委托', color: '#ff9800' },
  { value: 4, label: '交换', color: '#66bb6a' }
]

async function loadPosts() {
  loading.value = true
  try {
    if (activeTab.value === 'hot') {
      const res = await getHotPosts()
      posts.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      const params = {
        current: currentPage.value,
        size: pageSize.value
      }
      if (selectedCategory.value > 0) {
        params.category = selectedCategory.value
      }
      if (searchKeyword.value.trim()) {
        params.keyword = searchKeyword.value.trim()
      }
      const res = await getPosts(params)
      posts.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  if (activeTab.value === 'hot') {
    searchKeyword.value = ''
  }
  router.push({ query: { ...route.query, page: undefined } })
  loadPosts()
}

function handleSearch() {
  if (!searchKeyword.value.trim()) return
  currentPage.value = 1
  activeTab.value = 'latest'
  router.push({ query: { ...route.query, page: undefined } })
  loadPosts()
}

function handleClearSearch() {
  searchKeyword.value = ''
  currentPage.value = 1
  loadPosts()
}

function handleCategoryChange(value) {
  selectedCategory.value = value
  currentPage.value = 1
  router.push({ query: { ...route.query, page: undefined } })
  if (activeTab.value === 'hot') {
    activeTab.value = 'latest'
  }
  loadPosts()
}

function handlePageChange(page) {
  currentPage.value = page
  router.push({ query: { ...route.query, page: page > 1 ? page : undefined } })
  loadPosts()
}

async function loadOnlineCount() {
  try {
    const res = await getOnlineUsers()
    onlineCount.value = (res.data.records || []).length
  } catch (error) {
    console.error('获取在线人数失败:', error)
  }
}

onMounted(async () => {
  const page = parseInt(route.query.page) || 1
  if (page > 1) currentPage.value = page
  await loadPosts()
  await loadOnlineCount()
  const savedY = sessionStorage.getItem('home_scroll_y')
  if (savedY) {
    sessionStorage.removeItem('home_scroll_y')
    nextTick(() => window.scrollTo(0, parseInt(savedY)))
  }
})

onBeforeRouteLeave((to, from, next) => {
  sessionStorage.setItem('home_scroll_y', window.scrollY.toString())
  next()
})
</script>

<style scoped>
.home-view {
  padding-bottom: var(--spacing-lg);
}

/* 三栏：左空列｜中帖区（居中）｜右在线学员+快速入口 */
.home-layout {
  display: grid;
  grid-template-columns: 240px 1fr 240px;
  gap: var(--spacing-xl);
  align-items: start;
  padding: 0 var(--spacing-sm);
}

/* 左占位列 */
.spacer-panel {
  /* 空列，仅占位使帖子区居中 */
}

/* 右栏：在线学员 + 快速入口（间距 20px） */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 80px;
}

/* 中栏 */
.center-panel {
  min-width: 0;
  background: rgba(18, 22, 48, 0.68);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-xl);
  padding: var(--spacing-md);
}

/* 移动端：在线学员浮动按钮（默认隐藏） */
.mobile-online-btn {
  display: none;
}

/* 在线学员弹窗 */
.online-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.15s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.online-popup {
  width: 90vw;
  max-width: 380px;
  max-height: 60vh;
  background: var(--glass-heavy);
  backdrop-filter: var(--glass-heavy-blur);
  -webkit-backdrop-filter: var(--glass-heavy-blur);
  border: 1px solid var(--glass-heavy-border);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.online-popup-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 12px;
  flex-shrink: 0;
}

.online-popup-close {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  transition: color var(--transition-fast);
}

.online-popup-close:hover {
  color: var(--text-primary);
}

.online-popup-body {
  overflow-y: auto;
  flex: 1;
}

/* Tab */
.post-tabs {
  margin-bottom: var(--spacing-md);
}

/* 搜索栏 */
.search-bar {
  margin-bottom: var(--spacing-md);
}

.search-input {
  max-width: 100%;
}

/* 暗色凹陷输入框 -- 比页面背景更深，像刻进去的 */
.search-input :deep(.el-input__wrapper) {
  background: #060b1c;
  border: 1px solid #141d36;
  border-radius: 8px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.4);
  padding: 4px 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #1e2a4a;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent-primary);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.4), 0 0 0 2px rgba(0, 212, 255, 0.15);
}

/* 输入文字：清晰可读的灰蓝色 */
.search-input :deep(.el-input__inner) {
  color: #8690a8;
  font-size: 14px;
  line-height: 1.5;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: #4a5570;
  font-weight: 400;
}

/* 前缀搜索图标 */
.search-input :deep(.el-input__prefix) {
  color: #4a5570;
}

.search-input :deep(.el-input__prefix-inner) {
  transition: color 0.2s ease;
}

.search-input:has(:deep(.el-input__wrapper.is-focus)) :deep(.el-input__prefix-inner) {
  color: var(--accent-primary);
}

/* 后缀按钮 */
.search-btn {
  color: #4a5570;
  background: transparent;
  border: none;
  transition: color 0.2s ease;
}

.search-btn:hover {
  color: #8690a8;
  background: rgba(0, 212, 255, 0.08);
}

.search-info {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #4a5570;
}

.search-clear-link {
  color: var(--accent-primary);
  cursor: pointer;
  margin-left: 8px;
  font-size: 12px;
}

.search-clear-link:hover {
  text-decoration: underline;
}

/* 分类筛选 */
.category-filter {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm) 0;
}

.filter-label {
  color: var(--text-secondary);
  font-size: 14px;
  flex-shrink: 0;
}

.filter-tags {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.filter-tag {
  position: relative;
  padding: 4px 12px;
  border: none;
  border-radius: 0;
  color: rgba(136, 146, 176, 0.55);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  background: transparent;
}

.filter-tag:hover {
  color: var(--text-secondary);
}

.filter-tag.active {
  color: var(--accent-primary);
  font-weight: 600;
}

.filter-tag.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 1.5px;
  background: var(--accent-primary);
  box-shadow: 0 0 6px rgba(0, 212, 255, 0.4);
}

/* 帖子列表 */
.post-list {
  min-height: 300px;
}

.empty-posts {
  padding: var(--spacing-xl) 0;
}

/* 右栏 */
.right-panel {
  position: sticky;
  top: 80px;
}

/* 响应式适配 */
@media (max-width: 1280px) {
  .home-layout {
    grid-template-columns: 200px 1fr 200px;
    gap: var(--spacing-md);
  }
}

@media (max-width: 1024px) {
  .home-layout {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .spacer-panel,
  .right-panel {
    display: none;
  }

  /* 移动端显示在线学员浮动按钮 */
  .mobile-online-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background: var(--glass-medium);
    backdrop-filter: var(--glass-medium-blur);
    -webkit-backdrop-filter: var(--glass-medium-blur);
    border: 1px solid var(--glass-medium-border);
    border-radius: 20px;
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all var(--transition-fast);
    margin-top: -8px;
    margin-bottom: var(--spacing-sm);
    float: right;
  }

  .mobile-online-btn:hover {
    border-color: var(--accent-primary);
    color: var(--accent-primary);
  }

  .mobile-online-btn .el-badge {
    margin-left: 2px;
  }
}

@media (max-width: 768px) {
  .home-layout {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .spacer-panel,
  .right-panel {
    display: none;
  }

  .center-panel {
    border-radius: var(--radius-md);
  }

  .category-filter {
    gap: var(--spacing-xs);
  }

  .filter-tag {
    padding: 3px 10px;
    font-size: 12px;
  }

  .post-tabs {
    margin-bottom: var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .category-filter {
    flex-wrap: wrap;
  }
  
  .filter-label {
    width: 100%;
    margin-bottom: var(--spacing-xs);
  }
  
  .filter-tags {
    gap: var(--spacing-xs-sm);
  }
  
  .filter-tag {
    padding: 3px 8px;
    font-size: 11px;
  }
}

/* 搜索关键词高亮（全局，v-html 注入不受 scoped 限制） */
:deep(.search-hl) {
  background: rgba(255, 215, 0, 0.25);
  color: #ffd700;
  font-weight: 600;
  padding: 1px 2px;
  border-radius: 2px;
}

/* 快速入口 */
.quick-entry {
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.entry-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--accent-primary);
  font-size: 15px;
  font-weight: 600;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border-color);
}

.entry-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.entry-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.entry-item:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  transform: translateX(4px);
}
</style>