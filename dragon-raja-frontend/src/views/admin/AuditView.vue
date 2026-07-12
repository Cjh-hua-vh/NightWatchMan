<template>
  <div class="audit-view">
    <div class="page-header">
      <el-icon class="page-icon"><Document /></el-icon>
      <h2>注册审核</h2>
    </div>
    <div v-loading="loading">
      <div v-if="list.length === 0" class="empty">
        <el-empty description="暂无待审核用户" />
      </div>
      <div v-for="u in list" :key="u.id" class="audit-card">
        <div class="audit-info">
          <el-avatar :size="40" :src="u.avatar">{{ (u.nickname || u.username)?.charAt(0) }}</el-avatar>
          <div class="audit-detail">
            <div class="audit-name">{{ u.nickname || u.username }}
              <span class="audit-username">@{{ u.username }}</span>
            </div>
            <div class="audit-meta">
              血统：<span :style="{ color: getGradeColor(u.bloodlineGrade) }">{{ u.bloodlineGrade }}级</span>
              <span v-if="u.faction" style="margin-left:12px">派系：{{ u.faction }}</span>
              <span style="margin-left:12px">{{ formatDate(u.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="audit-actions">
          <el-button type="success" size="small" :loading="loadingId === u.id" @click="handleAudit(u.id, 1)">通过</el-button>
          <el-button type="danger" size="small" :loading="loadingId === u.id" @click="handleAudit(u.id, 2)">拒绝</el-button>
        </div>
      </div>
      <Pagination v-if="total > 0" :current="currentPage" :total="total" :size="pageSize" @change="handlePageChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import request from '../../api/request'
import { getGradeColor } from '../../utils/format'
import Pagination from '../../components/Pagination.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loadingId = ref(null)

function formatDate(t) {
  if (!t) return ''
  return t.substring(0, 10)
}

async function loadList() {
  loading.value = true
  try {
    const res = await request({ url: '/admin/users/pending', method: 'get', params: { current: currentPage.value, size: pageSize.value } })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* */ } finally { loading.value = false }
}

async function handleAudit(userId, status) {
  loadingId.value = userId
  try {
    await request({ url: `/admin/users/${userId}/audit`, method: 'put', data: { status } })
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    loadList()
  } catch { /* */ } finally { loadingId.value = null }
}

function handlePageChange(p) { currentPage.value = p; loadList() }

onMounted(loadList)
</script>

<style scoped>
.audit-view { max-width: 700px; }
.page-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}
.page-icon {
  font-size: 20px;
  color: var(--accent-primary);
}
.page-header h2 { font-size: 20px; color: var(--text-primary); font-weight: 700; }

.audit-card {
  display: flex; align-items: center; justify-content: space-between;
  background: var(--bg-secondary); border: 1px solid var(--border-color);
  border-radius: var(--radius-md); padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  transition: background var(--transition-fast);
}
.audit-card:hover { background: var(--bg-hover); }

.audit-info { display: flex; align-items: center; gap: var(--spacing-md); flex: 1; min-width: 0; }
.audit-detail { min-width: 0; }
.audit-name { font-weight: 600; color: var(--text-primary); font-size: 15px; }
.audit-username { color: var(--text-muted); font-size: 12px; margin-left: var(--spacing-xs); }
.audit-meta { color: var(--text-secondary); font-size: 12px; margin-top: 2px; }

.audit-actions { display: flex; gap: var(--spacing-xs); flex-shrink: 0; margin-left: var(--spacing-md); }

.empty { padding: var(--spacing-2xl) 0; }
</style>
