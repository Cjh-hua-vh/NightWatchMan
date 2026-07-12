<template>
  <div class="admin-layout">
    <div class="admin-container">
      <!-- 左侧菜单 -->
      <aside class="admin-sidebar">
        <div class="sidebar-header">
          <el-icon><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu :default-active="activeMenu" router>
          <!-- 审核员只有注册审核 -->
          <template v-if="userStore.canAudit && !userStore.isAdmin">
            <el-menu-item index="/admin/audit">
              <el-icon><User /></el-icon>
              <span>注册审核</span>
            </el-menu-item>
          </template>
          <!-- 管理员完整菜单 -->
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/posts">
              <el-icon><Document /></el-icon>
              <span>帖子管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/announcements">
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/audit">
              <el-icon><Select /></el-icon>
              <span>注册审核</span>
            </el-menu-item>
          </template>
        </el-menu>
        <div class="sidebar-footer">
          <router-link to="/" class="back-home">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </router-link>
        </div>
      </aside>

      <!-- 右侧内容 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Setting, User, Document, Bell, HomeFilled, Select } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const activeMenu = computed(() => route.path)
const userStore = useUserStore()
</script>

<style scoped>
.admin-layout {
  min-height: calc(100vh - 64px - 60px);
}

.admin-container {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: var(--spacing-lg);
  min-height: 100%;
}

/* 左侧菜单 — heavy (55%) 导航类必须清晰 */
.admin-sidebar {
  background: var(--glass-heavy);
  backdrop-filter: var(--glass-heavy-blur);
  -webkit-backdrop-filter: var(--glass-heavy-blur);
  border: 1px solid var(--glass-heavy-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  position: sticky;
  top: 80px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--accent-primary);
  font-size: 16px;
  font-weight: 700;
  padding: var(--spacing-sm) var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  text-shadow: 0 0 8px var(--accent-glow);
}

.sidebar-footer {
  margin-top: auto;
  padding-top: var(--spacing-md);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.back-home {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: 14px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.back-home:hover {
  color: var(--accent-primary);
  background-color: var(--bg-hover);
}

/* 右侧内容 */
.admin-content {
  min-width: 0;
  overflow-x: auto;
}

@media (max-width: 767px) {
  .admin-container { grid-template-columns: 1fr; gap: 0; }
  .admin-sidebar {
    position: static;
    flex-direction: row;
    border-radius: 0;
    border-left: none;
    border-right: none;
    padding: 4px;
    overflow-x: auto;
  }
  .sidebar-header { display: none; }
  .admin-sidebar :deep(.el-menu) { display: flex; flex-direction: row; flex: 1; }
  .admin-sidebar :deep(.el-menu-item) { padding: 0 10px; flex: 1; justify-content: center; font-size: 12px; }
  .sidebar-footer { display: none; }
}
</style>
