<template>
  <div class="news-view">
    <div class="page-header">
      <el-icon class="page-icon"><Document /></el-icon>
      <h2>学院新闻</h2>
    </div>
    <div v-loading="loading">
      <div v-if="newsList.length === 0 && !loading" class="empty"><el-empty description="暂无新闻" /></div>
      <div v-for="item in newsList" :key="item.id" class="news-card" @click="current = item">
        <div class="news-card-top">
          <el-tag v-if="item.isTop === 1" type="warning" size="small" effect="dark" class="top-tag">置顶</el-tag>
          <div class="news-title">{{ item.title }}</div>
        </div>
        <div class="news-card-mid">
          <div class="news-summary" v-if="item.summary">{{ item.summary }}</div>
        </div>
        <div class="news-meta">
          <span>{{ item.author }}</span>
          <span>{{ formatDateTime(item.createTime) }}</span>
        </div>
      </div>
      <Pagination v-if="total > 0" :current="currentPage" :total="total" :size="pageSize" @change="handlePageChange" />
    </div>

    <el-dialog :model-value="!!current" @update:model-value="current = null" :title="current?.title" width="700px">
      <img v-if="current?.cover" :src="current.cover" style="width:100%; border-radius: var(--radius-md); margin-bottom: var(--spacing-md)" />
      <div class="news-detail-content" v-html="current?.content"></div>
      <div class="news-detail-meta">
        <span>{{ current?.author }}</span>
        <span>{{ current?.createTime }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { getNewsList } from '../api/news'
import { formatDateTime } from '../utils/format'
import Pagination from '../components/Pagination.vue'

const loading = ref(false), newsList = ref([]), total = ref(0), currentPage = ref(1), pageSize = ref(10), current = ref(null)

async function loadNews() {
  loading.value = true
  try {
    const res = await getNewsList({ current: currentPage.value, size: pageSize.value })
    newsList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* */ }
  finally { loading.value = false }
}

function handlePageChange(p) { currentPage.value = p; loadNews() }

let newsTimer = null
onMounted(() => { loadNews(); newsTimer = setInterval(loadNews, 10000) })
onUnmounted(() => { if (newsTimer) clearInterval(newsTimer) })
</script>

<style scoped>
.news-view {
  max-width: 800px;
  margin: 0 auto;
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border: 1px solid var(--glass-light-border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
}
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

.news-card {
  background: var(--glass-light); border: 1px solid var(--glass-light-border);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border-radius: var(--radius-md); padding: var(--spacing-md); margin-bottom: var(--spacing-sm);
  cursor: pointer; transition: background var(--transition-fast);
}
.news-card:hover { background: rgba(0, 212, 255, 0.08); }
.news-card-top { display: flex; align-items: center; gap: var(--spacing-sm); margin-bottom: 6px; }
.top-tag { flex-shrink: 0; }
.news-card-mid { margin-bottom: 6px; }
.news-title { font-weight: 600; color: var(--text-primary); font-size: 15px; }
.news-summary { color: var(--text-secondary); font-size: 13px; }
.news-meta { display: flex; gap: var(--spacing-md); color: var(--text-muted); font-size: 12px; }
.news-detail-content { color: var(--text-primary); line-height: 1.8; }
.news-detail-meta { display: flex; gap: var(--spacing-lg); color: var(--text-muted); font-size: 12px; margin-top: var(--spacing-lg); padding-top: var(--spacing-md); border-top: 1px solid var(--border-color); }
.empty { padding: var(--spacing-2xl) 0; }

@media (max-width: 768px) { .news-view { max-width: 100%; padding: 0; } }
</style>
