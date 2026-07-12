<template>
  <div class="message-view">
    <div class="page-header">
      <div class="page-header-left">
        <el-icon class="page-icon"><Message /></el-icon>
        <h2>邮件</h2>
      </div>
      <div class="page-header-right">
        <el-button size="small" text @click="handleMarkAllRead" :disabled="messages.length === 0">全部已读</el-button>
        <div class="hint-text" v-if="messages.length > 0 && !batchMode">💡 长按邮件可进入批量管理模式</div>
      </div>
    </div>
    <el-tabs v-model="tab">
      <!-- 收件箱 -->
      <el-tab-pane label="收件箱" name="inbox">
        <div v-loading="loading">
          <!-- 批量操作栏 - 仅在批量模式下显示 -->
          <div class="batch-bar" v-if="batchMode && messages.length > 0">
            <span class="batch-select-all" :class="{ active: allChecked }" @click="toggleAll(!allChecked && !someChecked)">
              <span class="batch-check-icon">{{ allChecked ? '✓' : someChecked ? '–' : '' }}</span>
              <span>全选</span>
            </span>
            <span class="batch-sep"></span>
            <span class="batch-count" v-if="selectedIds.length > 0">已选 {{ selectedIds.length }} 封</span>
            <span class="batch-count" v-else>未选择</span>
            <button class="batch-cancel-btn" @click="exitBatchMode">取消</button>
            <button class="batch-del-btn" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2M10 11v6M14 11v6"/></svg>
              批量删除
            </button>
          </div>

          <div v-if="messages.length === 0" class="empty"><el-empty description="暂无邮件" /></div>
          <div v-for="msg in messages" :key="msg.id" class="msg-item" :class="{ unread: msg.isRead === 0, selected: selectedIds.includes(msg.id), 'batch-mode': batchMode }" @click="onItemClick(msg)" @touchstart="onTouchStart($event, msg)" @touchend="onTouchEnd" @mousedown="onMouseDown($event, msg)" @mouseup="onMouseUp" @mouseleave="onMouseUp">
            <el-checkbox v-if="batchMode" :model-value="selectedIds.includes(msg.id)" @click.stop @change="(v) => toggleOne(msg.id, v)" class="msg-checkbox" />
            <div class="msg-body">
              <div class="msg-header">
                <span class="msg-from">发件人：{{ msg.sender || '系统' }}</span>
                <span class="msg-time">{{ formatDateTime(msg.createTime) }}</span>
              </div>
              <div class="msg-title">{{ msg.title }}</div>
              <div class="msg-content">{{ msg.content }}</div>
              <a v-if="!batchMode" class="msg-delete-link" @click.stop.prevent="handleDelete(msg.id)">删除</a>
            </div>
            <div v-if="msg.isRead === 0" class="unread-dot"></div>
          </div>
          <Pagination v-if="total > 0" :current="currentPage" :total="total" :size="pageSize" @change="handlePageChange" />
        </div>
      </el-tab-pane>

      <!-- 发邮件 -->
      <el-tab-pane label="发邮件" name="compose">
        <div class="compose-form">
          <el-form label-width="60px">
            <el-form-item label="收件人">
              <el-select v-model="composeTo" filterable placeholder="选择好友" style="width: 100%">
                <el-option v-for="f in friendList" :key="f.friendId" :label="f.nickname || f.username" :value="f.friendId" />
              </el-select>
            </el-form-item>
            <el-form-item label="标题">
              <el-input v-model="composeTitle" placeholder="邮件标题" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input v-model="composeContent" type="textarea" :rows="6" placeholder="邮件内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="sending" @click="handleCompose">发送</el-button>
            </el-form-item>
          </el-form>

          <!-- 管理员群发 -->
          <div v-if="userStore.isAdmin" class="broadcast-section">
            <el-divider>管理员群发</el-divider>
            <el-form label-width="60px">
              <el-form-item label="标题">
                <el-input v-model="broadcastTitle" placeholder="群发邮件标题" />
              </el-form-item>
              <el-form-item label="内容">
                <el-input v-model="broadcastContent" type="textarea" :rows="4" placeholder="群发内容" />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" :loading="broadcasting" @click="handleBroadcast">向所有人发送</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-tab-pane>

      <!-- 已发送 -->
      <el-tab-pane label="已发送" name="sent">
        <div v-loading="sentLoading">
          <div v-if="sentMessages.length === 0" class="empty"><el-empty description="暂无已发送邮件" /></div>
          <div v-for="msg in sentMessages" :key="msg.id" class="msg-item">
            <div class="msg-header">
              <span class="msg-from">收件人：{{ msg.receiverName || '用户' + msg.receiverId }}</span>
              <span class="msg-time">{{ formatDateTime(msg.createTime) }}</span>
            </div>
            <div class="msg-title">{{ msg.title }}</div>
            <div class="msg-content">{{ msg.content }}</div>
            <a class="msg-delete-link" @click.stop.prevent="handleDelete(msg.id)">删除</a>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 邮件详情弹窗 -->
    <div v-if="detailMsg" class="msg-detail-overlay" @click="detailMsg = null">
      <div class="msg-detail-dialog" @click.stop>
        <div class="msg-detail-header">
          <span class="msg-detail-title">{{ detailMsg?.title }}</span>
          <span class="msg-detail-close" @click="detailMsg = null">
            <el-icon><Close /></el-icon>
          </span>
        </div>
        <div class="msg-detail-content">
          <div class="msg-detail-row">
            <span class="msg-detail-label">发件人</span>
            <span class="msg-detail-value">{{ detailMsg?.sender || '系统' }}</span>
          </div>
          <div class="msg-detail-row">
            <span class="msg-detail-label">收件人</span>
            <span class="msg-detail-value">{{ detailMsg?.receiverName || '用户' + detailMsg?.receiverId }}</span>
          </div>
          <div class="msg-detail-row">
            <span class="msg-detail-label">发送时间</span>
            <span class="msg-detail-value">{{ formatDateTime(detailMsg?.createTime) }}</span>
          </div>
          <div class="msg-detail-divider"></div>
          <div class="msg-detail-body">
            {{ detailMsg?.content }}
          </div>
        </div>
        <div class="msg-detail-footer">
          <el-button @click="detailMsg = null">关闭</el-button>
          <el-button type="danger" @click="handleDeleteDetail">删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Message } from '@element-plus/icons-vue'
import { getMessages, markAsRead, deleteMessage, getAllMessages, batchDeleteMessages, markAllAsRead } from '../api/message'
import { getFriendList } from '../api/friend'
import { useUserStore } from '../stores/user'
import { formatDateTime } from '../utils/format'
import request from '../api/request'
import Pagination from '../components/Pagination.vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const tab = ref('inbox')
const loading = ref(false), messages = ref([]), total = ref(0), currentPage = ref(1), pageSize = ref(10)

// compose
const friendList = ref([]), composeTo = ref(null), composeTitle = ref(''), composeContent = ref(''), sending = ref(false)
// broadcast
const broadcastTitle = ref(''), broadcastContent = ref(''), broadcasting = ref(false)
// sent
const sentLoading = ref(false), sentMessages = ref([])
// 批量管理
const batchMode = ref(false)
const selectedIds = ref([])
// 详情弹窗
const detailMsg = ref(null)
let longPressTimer = null
let longPressTriggered = false

const allChecked = computed(() => messages.value.length > 0 && selectedIds.value.length === messages.value.length)
const someChecked = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < messages.value.length)

async function loadInbox() {
  loading.value = true
  try {
    const res = await getAllMessages({ current: currentPage.value, size: pageSize.value })
    messages.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* */ } finally { loading.value = false }
}

async function loadSent() {
  sentLoading.value = true
  try {
    const res = await request({ url: '/message/sent', method: 'get', params: { current: 1, size: 50 } })
    sentMessages.value = res.data?.records || []
  } catch { /* */ } finally { sentLoading.value = false }
}

async function loadFriends() {
  try { const r = await getFriendList(); friendList.value = r.data || [] } catch { /* */ }
}

async function readMsg(msg) {
  if (msg.isRead === 0) { await markAsRead(msg.id); msg.isRead = 1 }
}

// 长按进入批量模式
function startLongPress(msg) {
  if (batchMode.value) return
  longPressTriggered = false
  longPressTimer = setTimeout(() => {
    longPressTriggered = true
    batchMode.value = true
    selectedIds.value = [msg.id]
    ElMessage.info('已进入批量管理模式')
  }, 600)
}
function cancelLongPress() {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}
function onTouchStart(e, msg) { startLongPress(msg) }
function onTouchEnd() { cancelLongPress() }
function onMouseDown(e, msg) { startLongPress(msg) }
function onMouseUp() { cancelLongPress() }

// 点击邮件
function onItemClick(msg) {
  if (batchMode.value) {
    // 批量模式下点击 = 切换选中
    if (selectedIds.value.includes(msg.id)) {
      selectedIds.value = selectedIds.value.filter(i => i !== msg.id)
    } else {
      selectedIds.value.push(msg.id)
    }
    if (selectedIds.value.length === 0) exitBatchMode()
  } else if (!longPressTriggered) {
    // 普通模式 = 打开详情弹窗
    readMsg(msg)
    detailMsg.value = msg
  }
}

function exitBatchMode() {
  batchMode.value = false
  selectedIds.value = []
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该邮件吗？', '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteMessage(id)
    ElMessage.success('已删除')
    loadInbox()
  } catch (e) { /* 用户取消 */ }
}

async function handleDeleteDetail() {
  if (!detailMsg.value) return
  try {
    await ElMessageBox.confirm('确定删除该邮件吗？', '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteMessage(detailMsg.value.id)
    ElMessage.success('已删除')
    detailMsg.value = null
    loadInbox()
  } catch (e) { /* 用户取消 */ }
}

function toggleAll(val) {
  selectedIds.value = val ? messages.value.map(m => m.id) : []
}
function toggleOne(id, val) {
  if (val) selectedIds.value.push(id)
  else selectedIds.value = selectedIds.value.filter(i => i !== id)
}
async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 封邮件吗？`, '批量删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await batchDeleteMessages(selectedIds.value)
    ElMessage.success(`已删除 ${selectedIds.value.length} 封邮件`)
    selectedIds.value = []
    loadInbox()
  } catch { /* */ }
}

async function handleCompose() {
  if (!composeTo.value || !composeTitle.value.trim() || !composeContent.value.trim()) {
    ElMessage.warning('请完整填写'); return
  }
  sending.value = true
  try {
    await request({ url: '/message/send', method: 'post', data: { receiverId: composeTo.value, title: composeTitle.value, content: composeContent.value } })
    ElMessage.success('发送成功')
    composeTo.value = null; composeTitle.value = ''; composeContent.value = ''
    loadSent()
  } catch { /* */ } finally { sending.value = false }
}

async function handleBroadcast() {
  if (!broadcastTitle.value.trim() || !broadcastContent.value.trim()) {
    ElMessage.warning('请填写标题和内容'); return
  }
  broadcasting.value = true
  try {
    await request({ url: '/admin/message/broadcast', method: 'post', data: { title: broadcastTitle.value, content: broadcastContent.value } })
    ElMessage.success('已向所有人发送')
    broadcastTitle.value = ''; broadcastContent.value = ''
  } catch { /* */ } finally { broadcasting.value = false }
}

function handlePageChange(p) { currentPage.value = p; loadInbox() }

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    const keepMsg = detailMsg.value
    messages.value.forEach(m => { m.isRead = 0 })
    ElMessage.success('已全部标记为已读')
    await loadInbox()
    // 保留当前打开的详情弹窗
    if (keepMsg) {
      detailMsg.value = messages.value.find(m => m.id === keepMsg.id) || keepMsg
    }
  } catch { /* */ }
}

let msgTimer = null
onMounted(async () => {
  await loadInbox()
  loadFriends()
  msgTimer = setInterval(() => { loadInbox() }, 10000)
  // 通过通知跳转来的，自动打开对应邮件详情
  const openId = route.query.open
  if (openId) {
    let msg = messages.value.find(m => m.id === Number(openId))
    if (!msg) {
      // 不在首页列表，全量拉取查找
      try {
        const res = await getAllMessages({ current: 1, size: 999 })
        const all = res.data?.records || []
        msg = all.find(m => m.id === Number(openId))
      } catch { /* ignore */ }
    }
    if (msg) {
      await readMsg(msg)
      detailMsg.value = msg
      // 清理 URL 中的 open 参数
      router.replace({ query: {} })
    }
  }
})
onUnmounted(() => { if (msgTimer) clearInterval(msgTimer) })
</script>

<style scoped>
.message-view {
  max-width: 800px;
  margin: 0 auto;
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border: 1px solid var(--glass-light-border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
}
.page-header { margin-bottom: var(--spacing-lg); display: flex; justify-content: space-between; align-items: center; }
.page-header-left { display: flex; align-items: center; gap: var(--spacing-sm); }
.page-header-right { display: flex; align-items: center; gap: var(--spacing-sm); }
.page-header h2 { font-size: 20px; color: var(--text-primary); }
.page-icon { font-size: 20px; color: var(--accent-primary); }
.hint-text { font-size: 12px; color: var(--text-muted); }

/* 批量操作栏 - 仅在批量模式下出现 */
.batch-bar {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 14px; padding: 10px 14px;
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: var(--radius-md);
  color: #fff;
  animation: slideDown 0.2s ease-out;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.batch-select-all {
  display: flex; align-items: center; gap: 6px;
  cursor: pointer; user-select: none; font-size: 13px; color: #fff;
}
.batch-select-all.active { color: #fff; }
.batch-check-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 18px; height: 18px; border-radius: 3px;
  border: 1.5px solid #fff;
  font-size: 12px; font-weight: 700; color: transparent;
  transition: all var(--transition-fast);
  background: rgba(255, 255, 255, 0.1);
}
.batch-select-all.active .batch-check-icon {
  background: #fff; color: var(--accent-primary);
}
.batch-sep { width: 1px; height: 18px; background: rgba(255, 255, 255, 0.3); }
.batch-count { font-size: 12px; margin-right: auto; }

.batch-cancel-btn {
  padding: 6px 14px; border: 1px solid #fff; background: transparent;
  color: #fff; border-radius: 6px; font-size: 12px; cursor: pointer;
  transition: background var(--transition-fast);
}
.batch-cancel-btn:hover { background: rgba(255, 255, 255, 0.15); }

.batch-del-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 12px; border: none; border-radius: 6px;
  background: #fff; color: var(--accent-primary); font-size: 12px; font-weight: 600;
  cursor: pointer; transition: opacity var(--transition-fast);
}
.batch-del-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.msg-item {
  position: relative;
  display: flex; gap: var(--spacing-sm);
  background: var(--glass-light); border: 1px solid var(--glass-light-border);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border-radius: var(--radius-md); padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm); cursor: pointer;
  transition: background var(--transition-fast);
  -webkit-user-select: none; user-select: none;
}
.msg-item:hover { background: rgba(0, 212, 255, 0.08); }
.msg-item.unread { border-left: 3px solid var(--accent-primary); }
.msg-item.selected { background: rgba(0, 212, 255, 0.15); border-color: var(--accent-primary); }
.msg-item.batch-mode { padding-left: var(--spacing-md); }

.msg-checkbox { flex-shrink: 0; margin-top: 3px; }
.msg-body { flex: 1; min-width: 0; }

.msg-header { display: flex; justify-content: space-between; margin-bottom: var(--spacing-xs); }
.msg-from { color: var(--accent-primary); font-size: 13px; }
.msg-time { color: var(--text-muted); font-size: 12px; }
.msg-title { font-weight: 600; color: var(--text-primary); font-size: 14px; margin-bottom: var(--spacing-xs); }
.msg-content { color: var(--text-secondary); font-size: 13px; }
.msg-delete-link {
  position: absolute; right: 12px; bottom: 8px;
  color: var(--danger); font-size: 12px;
  text-decoration: underline; cursor: pointer;
  opacity: 0.7; transition: opacity var(--transition-fast);
}
.msg-delete-link:hover { opacity: 1; }
.unread-dot { position: absolute; top: 8px; right: 8px; width: 8px; height: 8px; background: var(--danger); border-radius: 50%; }

.compose-form { max-width: 500px; margin-top: var(--spacing-md); }
.empty { padding: var(--spacing-2xl) 0; }

.msg-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: var(--spacing-md);
}

.msg-detail-dialog {
  position: relative;
  max-width: 500px;
  width: 100%;
  background: 
    linear-gradient(rgba(10, 14, 39, 0.50), rgba(10, 14, 39, 0.50)),
    url('/dragon-card-bg.png') center / cover no-repeat;
  border: 1px solid var(--glass-heavy-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.msg-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.msg-detail-title {
  color: var(--accent-primary);
  font-size: 15px;
  font-weight: 600;
}

.msg-detail-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.msg-detail-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--accent-primary);
}

.msg-detail-content {
  padding: var(--spacing-lg);
  max-height: 40vh;
  overflow-y: auto;
}

.msg-detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--border-color);
}

@media (max-width: 480px) {
  .message-view {
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
  
  .msg-detail-overlay {
    padding: var(--spacing-sm);
    overflow-y: auto;
  }
  
  .msg-detail-dialog {
    max-width: 100%;
    max-height: 85vh;
    overflow-y: auto;
  }
  
  .msg-detail-content {
    max-height: none;
  }
}
.msg-detail-row {
  display: flex; justify-content: space-between;
  margin-bottom: var(--spacing-sm); padding: var(--spacing-xs) 0;
}
.msg-detail-label {
  color: var(--text-muted); font-size: 13px; font-weight: 600;
}
.msg-detail-value {
  color: var(--text-primary); font-size: 13px;
}
.msg-detail-divider {
  height: 1px; background: var(--border-color);
  margin: var(--spacing-md) 0;
}
.msg-detail-body {
  color: var(--text-secondary); font-size: 14px; line-height: 1.7;
  padding: var(--spacing-sm) 0;
}

@media (max-width: 768px) { .message-view { max-width: 100%; padding: 0; } .msg-item { border-radius: 0; } .page-header { flex-direction: column; align-items: flex-start; gap: 4px; } }
</style>
