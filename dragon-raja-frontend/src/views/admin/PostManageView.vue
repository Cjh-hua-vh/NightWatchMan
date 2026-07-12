<template>
  <div class="post-manage-view">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        帖子管理
      </h2>
    </div>

    <!-- 帖子表格 -->
    <el-table :data="posts" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="标题" min-width="200">
        <template #default="{ row }">
          <span class="post-title-cell" @click="goDetail(row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="80">
        <template #default="{ row }">
          <span class="category-cell" :style="{ color: getCategoryColor(row.category) }">
            {{ row.categoryDesc || getCategoryName(row.category) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="作者" width="120" />
      <el-table-column label="浏览" width="80">
        <template #default="{ row }">
          <span>{{ row.viewCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评论" width="80">
        <template #default="{ row }">
          <span>{{ row.commentCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="warning" size="small" effect="dark">置顶</el-tag>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">
          <span class="time-cell">{{ formatDateTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" class-name="op-col">
        <template #default="{ row }">
          <div class="op-actions">
            <el-button :type="row.isTop === 1 ? 'warning' : 'primary'" size="small" @click="handleToggleTop(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button type="success" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :current="currentPage" :total="total" :size="pageSize" @change="handlePageChange" />

    <!-- 编辑帖子弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑帖子" width="650px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="60px" v-loading="editLoading">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" style="width: 100%">
            <el-option :value="1" label="闲聊" />
            <el-option :value="2" label="史料" />
            <el-option :value="3" label="委托" />
            <el-option :value="4" label="交换" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="edit-images">
            <div v-for="(img, idx) in editForm.images" :key="idx" class="edit-image-item">
              <img :src="img" class="edit-img" />
              <el-button type="danger" size="small" circle @click="removeImage(idx)" class="edit-img-del">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <el-upload :show-file-list="false" :before-upload="beforeEditUpload" :http-request="handleEditUpload"
              accept="image/*" class="edit-upload-btn">
              <el-button type="primary" :icon="Plus" circle />
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Close } from '@element-plus/icons-vue'
import { getAdminPosts, adminDeletePost, adminUpdatePost } from '../../api/admin'
import { toggleTop, getPostDetail } from '../../api/post'
import { uploadPostImage } from '../../api/file'
import { getCategoryColor, getCategoryName, formatDateTime } from '../../utils/format'
import Pagination from '../../components/Pagination.vue'

const router = useRouter()

const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const editLoading = ref(false)
const editDialogVisible = ref(false)
const editForm = ref({ id: null, title: '', content: '', category: 1, images: [] })

async function openEditDialog(row) {
  editDialogVisible.value = true
  editLoading.value = true
  editForm.value = { id: row.id, title: row.title || '', content: '加载中...', category: row.category || 1, images: [] }
  try {
    const res = await getPostDetail(row.id)
    if (res.data) {
      editForm.value.content = res.data.content || ''
      editForm.value.title = res.data.title || row.title
      editForm.value.category = res.data.category || row.category
      editForm.value.images = res.data.images || []
    }
  } catch {
    editForm.value.content = row.content || ''
  } finally {
    editLoading.value = false
  }
}

function removeImage(idx) {
  editForm.value.images.splice(idx, 1)
}

async function beforeEditUpload(file) {
  const valid = file.size < 5 * 1024 * 1024
  if (!valid) ElMessage.warning('图片大小不能超过5MB')
  return valid
}

async function handleEditUpload({ file }) {
  try {
    const res = await uploadPostImage(file)
    if (res.data) editForm.value.images.push(res.data)
  } catch { ElMessage.error('上传失败') }
}

async function handleEditSave() {
  if (!editForm.value.title.trim() || !editForm.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdatePost(editForm.value.id, editForm.value)
    ElMessage.success('编辑成功')
    editDialogVisible.value = false
    loadPosts()
  } catch { } finally { saving.value = false }
}

// 加载帖子列表
async function loadPosts() {
  loading.value = true
  try {
    const res = await getAdminPosts({ current: currentPage.value, size: pageSize.value })
    posts.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { } finally { loading.value = false }
}

function goDetail(id) { router.push(`/post/${id}`) }

function handleToggleTop(row) {
  const action = row.isTop === 1 ? '取消置顶' : '置顶'
  ElMessageBox.confirm(`确定要${action}帖子 "${row.title}" 吗？`, '操作确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await toggleTop(row.id)
    ElMessage.success(`${action}成功`)
    loadPosts()
  }).catch(() => { })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除帖子 "${row.title}" 吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await adminDeletePost(row.id)
    ElMessage.success('删除成功')
    loadPosts()
  }).catch(() => { })
}

function handlePageChange(page) { currentPage.value = page; loadPosts() }

onMounted(() => { loadPosts() })
</script>

<style scoped>
.page-header { margin-bottom: var(--spacing-lg); }

.page-title {
  display: flex; align-items: center; gap: var(--spacing-sm);
  font-size: 18px; font-weight: 700; color: var(--accent-primary);
}

.post-title-cell {
  color: var(--accent-secondary); cursor: pointer; transition: color var(--transition-fast);
}
.post-title-cell:hover { color: var(--accent-primary); text-decoration: underline; }
.category-cell { font-weight: 600; }
.text-muted { color: var(--text-muted); }
.time-cell { color: var(--text-muted); font-size: 12px; }

/* 操作列 — 紧凑布局 */
:deep(.op-col .cell) { padding: 2px 4px !important; }
.op-actions {
  display: flex; flex-wrap: wrap; gap: 2px; align-items: center; justify-content: flex-end;
}
.op-actions .el-button {
  padding: 2px 6px !important; font-size: 11px !important; min-height: 20px !important;
}

/* 操作列 — 半透明背景 */
:deep(.el-table__fixed-right) { background: transparent !important; }
:deep(.el-table__fixed-right::before) { background: transparent !important; }
:deep(.el-table__fixed-body-wrapper) { background: transparent !important; }

/* 编辑弹窗图片 */
.edit-images { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.edit-image-item { position: relative; }
.edit-img { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; }
.edit-img-del { position: absolute; top: -6px; right: -6px; padding: 2px; }
.edit-upload-btn { display: inline-flex; }

@media (max-width: 767px) {
  :deep(.op-col) { width: 100px !important; }
  :deep(.op-col .cell) { padding: 2px 4px !important; }
  .op-actions { flex-direction: column; gap: 2px; align-items: stretch; }
  .op-actions .el-button { padding: 2px 6px !important; font-size: 11px !important; min-height: 20px !important; margin: 0 !important; }
  :deep(.el-table__fixed-right) { box-shadow: -4px 0 8px rgba(0, 0, 0, 0.25); }
}
</style>
