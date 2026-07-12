<template>
  <div class="online-users">
    <div class="panel-header">
      <span class="title">
        <el-icon><User /></el-icon>
        在线学员
      </span>
      <el-badge :value="users.length" type="primary" />
    </div>

    <div class="users-list" v-loading="loading">
      <div v-if="users.length === 0 && !loading" class="empty-text">
        暂无在线学员
      </div>
      <div v-for="user in users" :key="user.id" class="user-item">
        <UserCard :user="user" :size="32" show-name />
        <span class="online-dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import { getOnlineUsers } from '../api/user'
import { getGradeColor } from '../utils/format'
import UserCard from './UserCard.vue'

const users = ref([])
const loading = ref(false)

// 加载在线用户
async function loadOnlineUsers() {
  loading.value = true
  try {
    const res = await getOnlineUsers()
    users.value = res.data.records || []
  } catch (error) {
    console.error('获取在线用户失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOnlineUsers()
})
</script>

<style scoped>
.online-users {
  background: rgba(15, 24, 48, 0.35) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  border: 1px solid rgba(255, 255, 255, 0.10) !important;
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--accent-primary);
  font-size: 15px;
  font-weight: 600;
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  max-height: 480px;
  overflow-y: auto;
  padding-right: var(--spacing-xs);
}

.empty-text {
  text-align: center;
  color: var(--text-muted);
  padding: var(--spacing-xl) 0;
  font-size: 13px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm-md);
  padding: var(--spacing-xs-s) var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
  cursor: pointer;
}

.user-item:hover {
  background-color: var(--bg-hover);
}

.online-dot {
  width: 8px;
  height: 8px;
  background-color: var(--success);
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 4px var(--success);
}

.user-item :deep(.trigger-avatar) {
  box-shadow: 0 0 0 2px rgba(74, 222, 128, 0.25), 0 0 8px rgba(74, 222, 128, 0.15);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  flex: 1;
  min-width: 0;
}

.user-name {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-tags {
  display: flex;
  gap: var(--spacing-xs);
}

.grade-tag {
  padding: 0 4px;
  border: 1px solid;
  border-radius: 2px;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
}

.faction-tag {
  color: var(--text-secondary);
  font-size: 11px;
}
</style>
