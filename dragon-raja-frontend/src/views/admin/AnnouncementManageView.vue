<template>
  <div class="announcement-manage-view">
    <div class="page-header">
      <h2 class="page-title">📢 公告管理</h2>
      <el-button type="primary" @click="openCreateDialog">发布公告</el-button>
    </div>

    <div v-loading="loading">
      <div v-if="announcements.length === 0" class="empty">
        <el-empty description="暂无公告" />
      </div>
      <div v-for="item in announcements" :key="item.id" class="announcement-card">
        <div class="card-top">
          <div class="card-title">
            <el-tag v-if="item.isTop === 1" type="warning" size="small" effect="dark">置顶</el-tag>
            <span class="title-text">{{ item.title }}</span>
          </div>
          <div class="card-time">{{ formatDateTime(item.createTime) }}</div>
        </div>
        <div class="card-preview">{{ truncate(item.content, 120) }}</div>
        <div class="card-actions">
          <el-button size="small" @click="openEditDialog(item)">编辑</el-button>
          <el-button
            size="small"
            :type="item.isTop === 1 ? 'warning' : 'primary'"
            @click="handleToggleTop(item)"
          >
            {{ item.isTop === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <Pagination
      :current="currentPage"
      :total="total"
      :size="pageSize"
      @change="handlePageChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="announcementForm" :rules="rules" label-width="60px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="announcementForm.content" type="textarea" :rows="8" placeholder="公告内容" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="announcementForm.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../../api/announcement'
import { formatDateTime } from '../../utils/format'
import Pagination from '../../components/Pagination.vue'

const announcements = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const announcementForm = reactive({ title: '', content: '', isTop: 1 })
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncements({ current: currentPage.value, size: pageSize.value })
    announcements.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { /* */ }
  finally { loading.value = false }
}

function openCreateDialog() {
  isEdit.value = false; editId.value = null
  announcementForm.title = ''; announcementForm.content = ''; announcementForm.isTop = 1
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true; editId.value = row.id
  announcementForm.title = row.title; announcementForm.content = row.content; announcementForm.isTop = row.isTop
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data = { title: announcementForm.title, content: announcementForm.content, isTop: announcementForm.isTop }
      if (isEdit.value) { await updateAnnouncement(editId.value, data); ElMessage.success('修改成功') }
      else { await createAnnouncement(data); ElMessage.success('发布成功') }
      dialogVisible.value = false
      await loadAnnouncements()
    } catch { /* */ }
    finally { submitting.value = false }
  })
}

async function handleToggleTop(row) {
  try {
    await updateAnnouncement(row.id, { title: row.title, content: row.content, isTop: row.isTop === 1 ? 0 : 1 })
    ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已置顶')
    await loadAnnouncements()
  } catch { /* */ }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { await deleteAnnouncement(row.id); ElMessage.success('删除成功'); await loadAnnouncements() })
    .catch(() => {})
}

function handlePageChange(page) { currentPage.value = page; loadAnnouncements() }

onMounted(loadAnnouncements)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}
.page-title { font-size: 18px; font-weight: 700; color: var(--accent-primary); }

.announcement-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  transition: background var(--transition-fast);
}
.announcement-card:hover { background: var(--bg-hover); }

.card-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-sm);
}
.card-title { display: flex; align-items: center; gap: var(--spacing-sm); }
.title-text { font-weight: 600; color: var(--accent-secondary); font-size: 15px; }
.card-time { color: var(--text-muted); font-size: 12px; white-space: nowrap; }

.card-preview { color: var(--text-secondary); font-size: 13px; line-height: 1.6; margin-bottom: var(--spacing-sm); }

.card-actions { display: flex; gap: var(--spacing-xs); padding-top: var(--spacing-sm); border-top: 1px solid var(--border-subtle); }

.empty { padding: var(--spacing-2xl) 0; }
</style>
