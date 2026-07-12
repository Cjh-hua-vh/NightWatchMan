<template>
  <div class="user-manage-view">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><User /></el-icon>
        用户管理
      </h2>
    </div>

    <!-- 用户表格 -->
    <el-table :data="users" v-loading="loading" style="width: 100%" class="user-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="用户名" width="100">
        <template #default="{ row }">
          <span class="user-cell">{{ row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="100" />
      <el-table-column label="血统" width="60">
        <template #default="{ row }">
          <span class="grade-cell" :style="{ color: getGradeColor(row.bloodlineGrade) }">
            {{ row.bloodlineGrade }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="faction" label="派系" width="80" />
      <el-table-column label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small" effect="dark">
            {{ getUserStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="warning" size="small" effect="dark">管理员</el-tag>
          <el-tag v-else-if="row.role === 'AUDITOR'" type="warning" size="small" effect="plain">审核员</el-tag>
          <el-tag v-else type="info" size="small" effect="dark">用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="150">
        <template #default="{ row }">
          <span class="time-cell">{{ formatDateTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" class-name="op-col">
        <template #default="{ row }">
          <div class="op-actions">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, 2)">拒绝</el-button>
            </template>
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleBan(row)" v-if="row.status === 1">封禁</el-button>
            <el-button type="success" size="small" @click="handleBan(row)" v-if="row.status === 2">解封</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :current="currentPage"
      :total="total"
      :size="pageSize"
      @change="handlePageChange"
    />

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="头像">
          <el-upload :show-file-list="false" :before-upload="beforeEditAvatarUpload" :http-request="handleEditAvatarUpload" accept="image/*">
            <img v-if="editForm.avatar" :src="editForm.avatar" style="width:48px;height:48px;border-radius:50%;object-fit:cover;cursor:pointer" />
            <el-button v-else size="small">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="个性签名"><el-input v-model="editForm.signature" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="血统等级">
          <el-select v-model="editForm.bloodlineGrade">
            <el-option label="S级 - 纯血龙裔" value="S" />
            <el-option label="A级 - 高阶混血" value="A" />
            <el-option label="B级 - 中阶混血" value="B" />
            <el-option label="C级 - 初阶混血" value="C" />
            <el-option label="D级 - 觉醒者" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="言灵"><el-input v-model="editForm.yanling" /></el-form-item>
        <el-form-item label="派系">
          <el-select v-model="editForm.faction" clearable>
            <el-option label="狮心会" value="狮心会" />
            <el-option label="学生会" value="学生会" />
            <el-option label="执行部" value="执行部" />
            <el-option label="教授" value="教授" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUsers, auditUser, banUser, updateUser, deleteUser } from '../../api/admin'
import { uploadAvatar } from '../../api/file'
import { getGradeColor, getUserStatusName, formatDateTime } from '../../utils/format'
import Pagination from '../../components/Pagination.vue'

const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const editDialogVisible = ref(false)
const editForm = ref({ id: null, nickname: '', avatar: '', signature: '', bloodlineGrade: 'D', faction: '', yanling: '' })

function openEditDialog(row) {
  editForm.value = {
    id: row.id,
    nickname: row.nickname || '',
    avatar: row.avatar || '',
    signature: row.signature || '',
    bloodlineGrade: row.bloodlineGrade || 'D',
    faction: row.faction || '',
    yanling: row.yanling || ''
  }
  editDialogVisible.value = true
}

function beforeEditAvatarUpload(file) {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 2) { ElMessage.error('图片不能超过2MB'); return false }
  return true
}
async function handleEditAvatarUpload(options) {
  try {
    const res = await uploadAvatar(options.file)
    editForm.value.avatar = res.data
    ElMessage.success('上传成功')
  } catch { ElMessage.error('上传失败') }
}

async function handleEditSave() {
  try {
    await updateUser(editForm.value.id, editForm.value)
    ElMessage.success('已更新用户信息')
    editDialogVisible.value = false
    loadUsers()
  } catch { /* */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.nickname || row.username}」吗？该用户的帖子和评论将一并删除。`, '危险操作', { type: 'error', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' })
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    loadUsers()
  } catch { /* */ }
}

// 获取状态标签类型
function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 加载用户列表
async function loadUsers() {
  loading.value = true
  try {
    const res = await getUsers({
      current: currentPage.value,
      size: pageSize.value
    })
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 审核用户
function handleAudit(row, status) {
  const action = status === 1 ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 的注册申请吗？`, '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await auditUser(row.id, { status })
        ElMessage.success(`已${action}`)
        await loadUsers()
      } catch (error) {
        console.error('审核失败:', error)
      }
    })
    .catch(() => {})
}

// 封禁/解封用户
function handleBan(row) {
  const action = row.status === 1 ? '封禁' : '解封'
  ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗？`, '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await banUser(row.id)
        ElMessage.success(`已${action}`)
        await loadUsers()
      } catch (error) {
        console.error('操作失败:', error)
      }
    })
    .catch(() => {})
}

// 分页变化
function handlePageChange(page) {
  currentPage.value = page
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.page-header {
  margin-bottom: var(--spacing-lg);
}

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-info-text { display: flex; flex-direction: column; }
.user-name { font-weight: 600; color: var(--text-primary); font-size: 14px; }
.user-id { font-size: 11px; color: var(--text-muted); }
.time-text { font-size: 12px; color: var(--text-secondary); }
.text-muted { color: var(--text-muted); font-size: 13px; }
.page-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-primary);
}

.user-cell {
  color: var(--accent-secondary);
  font-weight: 500;
}

.grade-cell {
  font-weight: 700;
  font-size: 15px;
  display: inline-block;
  min-width: 16px;
  text-align: center;
}

.time-cell {
  color: var(--text-muted);
  font-size: 12px;
}

/* 表格外层 — 由 el-table 自身处理滚动与布局 */
.user-table {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

/* 操作列 — 紧凑布局、按钮变小 */
:deep(.op-col .cell) {
  padding: 2px 4px !important;
}
.op-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  align-items: center;
  justify-content: flex-end;
}
.op-actions .el-button {
  padding: 2px 6px !important;
  font-size: 11px !important;
  min-height: 20px !important;
}

/* 操作列 — 半透明背景 */
:deep(.el-table__fixed-right) { background: transparent !important; }
:deep(.el-table__fixed-right::before) { background: transparent !important; }
:deep(.el-table__fixed-body-wrapper) { background: transparent !important; }

/* ============ 移动端适配 ============ */
@media (max-width: 767px) {
  .user-table {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }
  /* 信息列字号缩小、单元格 padding 减少 */
  :deep(.el-table .cell) {
    padding: 0 6px !important;
    font-size: 12px;
  }
  :deep(.el-table th .cell) {
    font-size: 11px;
    padding: 0 6px !important;
  }
  /* 操作列更紧凑 */
  :deep(.op-col) {
    width: 100px !important;
  }
  :deep(.op-col .cell) {
    padding: 2px 4px !important;
  }
  .op-actions {
    flex-direction: column;
    gap: 2px;
    align-items: stretch;
  }
  .op-actions .el-button {
    padding: 2px 6px !important;
    font-size: 11px !important;
    min-height: 20px !important;
    margin: 0 !important;
  }
  /* 固定列阴影 — 暗示其与左半边可滑动 */
  :deep(.el-table__fixed-right) {
    box-shadow: -4px 0 8px rgba(0, 0, 0, 0.25);
  }
}
</style>
