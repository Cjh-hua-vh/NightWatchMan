<template>
  <div class="friends-view">
    <div class="page-header">
      <el-icon class="page-icon"><UserFilled /></el-icon>
      <h2>联系人</h2>
    </div>
    <el-tabs v-model="tab">
      <!-- 好友列表 -->
      <el-tab-pane label="好友列表" name="list">
        <div v-if="friends.length === 0" class="empty"><el-empty description="暂无好友" /></div>
        <div class="friend-grid">
          <div v-for="f in pagedFriends" :key="f.friendId" class="friend-card">
            <div class="friend-click" @click="goProfile(f)">
              <UserCard :user="f" :size="48" @click.stop />
              <div class="friend-info">
                <div class="friend-name">{{ f.nickname || f.username }}</div>
                <div class="friend-grade" :style="{ color: getGradeColor(f.bloodlineGrade) }">{{ f.bloodlineGrade }}级</div>
                <div class="friend-faction" v-if="f.faction">{{ f.faction }}</div>
              </div>
            </div>
            <div class="friend-actions">
              <el-button size="small" @click="openChat(f)">发私信</el-button>
              <el-button size="small" type="danger" text @click="handleRemove(f)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="friend-pagination" v-if="friendTotal > friendPageSize">
          <el-pagination
            v-model:current-page="friendPage"
            :page-size="friendPageSize"
            :total="friendTotal"
            layout="prev, pager, next"
            small
            background
          />
        </div>
      </el-tab-pane>

      <!-- 添加好友 -->
      <el-tab-pane label="添加好友" name="add">
        <div class="add-friend-panel">
          <div class="search-section">
            <span class="search-label">账户名</span>
            <div class="search-input-group" :class="{ active: searchKeyword }">
              <el-icon class="search-icon"><User /></el-icon>
              <input
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="输入对方账户名"
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" :disabled="!searchKeyword.trim() || searching" :loading="searching" @click="handleSearch">
                <el-icon v-if="!searching"><Search /></el-icon>
                <el-icon v-else class="spin"><Loading /></el-icon>
                <span>{{ searching ? '搜索中' : '搜索' }}</span>
              </button>
            </div>
            <p class="search-hint">输入对方的账户名进行搜索</p>
          </div>
          <div v-if="searchResult" class="search-result">
            <div class="result-card">
              <UserCard :user="searchResult" :size="56" />
              <div class="result-info">
                <div class="result-name">{{ searchResult.nickname || searchResult.username }}
                  <span class="result-id">#{{ searchResult.id }}</span>
                </div>
                <div class="result-grade" :style="{ color: getGradeColor(searchResult.bloodlineGrade) }">{{ searchResult.bloodlineGrade }}级混血种</div>
                <div class="result-faction" v-if="searchResult.faction">{{ searchResult.faction }}</div>
              </div>
              <el-button type="primary" :loading="applying" @click="handleApply(searchResult.id)">添加好友</el-button>
            </div>
          </div>
          <div v-if="searched && !searchResult" class="empty"><el-empty description="未找到该用户" /></div>
        </div>
      </el-tab-pane>

      <!-- 好友申请 -->
      <el-tab-pane :label="'好友申请 (' + requests.length + ')'" name="requests">
        <div v-if="requests.length === 0" class="empty"><el-empty description="暂无申请" /></div>
        <div v-for="r in requests" :key="r.id" class="request-item">
          <UserCard :user="r" :size="40" @click.stop />
          <div class="request-info">
            <div class="friend-name">{{ r.nickname || r.username }}</div>
            <div class="request-signature" v-if="r.signature">{{ r.signature }}</div>
          </div>
          <div class="request-actions">
            <el-button size="small" type="primary" @click="handleAccept(r.id)">接受</el-button>
            <el-button size="small" @click="handleReject(r.id)">拒绝</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Search, Loading, UserFilled } from '@element-plus/icons-vue'
import { getFriendList, getPendingRequests, acceptFriend, rejectFriend, removeFriend } from '../api/friend'
import { getGradeColor } from '../utils/format'
import UserCard from '../components/UserCard.vue'
import request from '../api/request'

const router = useRouter()
const tab = ref('list')
const friends = ref([])
const friendPage = ref(1)
const friendPageSize = ref(8)
const pagedFriends = computed(() => {
  const start = (friendPage.value - 1) * friendPageSize.value
  return friends.value.slice(start, start + friendPageSize.value)
})
const friendTotal = computed(() => friends.value.length)
const requests = ref([])

// 搜索添加好友
const searchKeyword = ref('')
const searchResult = ref(null)
const searched = ref(false)
const searching = ref(false)
const applying = ref(false)

async function loadFriends() {
  try { const r = await getFriendList(); friends.value = r.data || [] } catch { /* */ }
}
async function loadRequests() {
  try { const r = await getPendingRequests(); requests.value = r.data || [] } catch { /* */ }
}

function goProfile(f) { router.push(`/user/${f.friendId}`) }
function openChat(f) { router.push(`/chat/${f.friendId}`) }

async function handleAccept(id) { await acceptFriend(id); ElMessage.success('已添加好友'); loadFriends(); loadRequests() }
async function handleReject(id) { await rejectFriend(id); ElMessage.success('已拒绝'); loadRequests() }
async function handleRemove(f) { await removeFriend(f.friendId); ElMessage.success('已删除好友'); loadFriends() }

// 搜索用户
async function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) { ElMessage.warning('请输入账户名'); return }
  searching.value = true
  searched.value = true
  searchResult.value = null
  try {
    const res = await request({ url: '/user/search', method: 'get', params: { keyword } })
    searchResult.value = res.data
  } catch {
    searchResult.value = null
  } finally { searching.value = false }
}

// 添加好友
async function handleApply(friendId) {
  applying.value = true
  try {
    await request({ url: '/friend/apply', method: 'post', data: { friendId } })
    ElMessage.success('好友申请已发送')
  } catch { /* */ }
  finally { applying.value = false }
}

let friendTimer = null
onMounted(() => { loadFriends(); loadRequests(); friendTimer = setInterval(() => { loadFriends(); loadRequests() }, 10000) })
onUnmounted(() => { if (friendTimer) clearInterval(friendTimer) })

// 切换标签或好友数变化时重置页码
watch(friendTotal, (n) => { if (n <= (friendPage.value - 1) * friendPageSize.value) friendPage.value = 1 })
watch(tab, () => { friendPage.value = 1 })
</script>

<style scoped>
.friends-view {
  max-width: 800px;
  margin: 0 auto;
  background: rgba(10, 14, 39, 0.08);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.04);
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

.friend-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--spacing-sm); }
.friend-card {
  display: flex; flex-direction: column;
  background: var(--glass-light); border: 1px solid var(--glass-light-border);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border-radius: var(--radius-md); padding: var(--spacing-md);
  transition: background var(--transition-fast);
}
.friend-card:hover { background: rgba(0, 212, 255, 0.08); }
.friend-click { display: flex; align-items: center; gap: var(--spacing-md); cursor: pointer; flex: 1; }
.friend-info { flex: 1; }
.friend-name { font-weight: 600; color: var(--text-primary); font-size: 14px; }
.friend-grade { font-size: 12px; font-weight: 600; }
.friend-faction { font-size: 12px; color: var(--text-secondary); }
.friend-actions { display: flex; justify-content: flex-end; gap: var(--spacing-xs); margin-top: var(--spacing-sm); padding-top: var(--spacing-sm); border-top: 1px solid var(--border-subtle); }

.friend-pagination { display: flex; justify-content: center; margin-top: var(--spacing-lg); }

.request-item {
  display: flex; align-items: center; gap: var(--spacing-md);
  background: var(--glass-light); border: 1px solid var(--glass-light-border);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border-radius: var(--radius-md); padding: var(--spacing-md); margin-bottom: var(--spacing-sm);
}
.request-info { flex: 1; }
.request-signature { color: var(--text-secondary); font-size: 12px; margin-top: 2px; }
.request-actions { display: flex; gap: var(--spacing-xs); }

/* 添加好友 */
.add-friend-panel { margin-top: var(--spacing-lg); }

.search-section {
  max-width: 480px;
  margin-bottom: var(--spacing-xl);
}
.search-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.search-input-group {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: 10px;
  padding: 4px 4px 4px 14px;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
}
.search-input-group:focus-within {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.12), 0 0 20px rgba(0, 212, 255, 0.18);
  background: var(--glass-heavy);
}
.search-input-group.active {
  border-color: rgba(0, 212, 255, 0.4);
}

.search-icon {
  font-size: 16px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  flex-shrink: 0;
}
.search-input-group:focus-within .search-icon { color: var(--accent-primary); }

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 15px;
  font-family: inherit;
  padding: 10px 12px;
  min-width: 0;
}
.search-input::placeholder {
  color: var(--text-muted);
  font-size: 14px;
}
/* 隐藏原生 number 控件箭头 */
.search-input::-webkit-outer-spin-button,
.search-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.search-input[type=number] { -moz-appearance: textfield; }

.search-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  border-radius: 7px;
  background: linear-gradient(135deg, var(--accent-primary), #4da6ff);
  color: #061327;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), opacity var(--transition-fast);
  box-shadow: 0 4px 12px rgba(0, 212, 255, 0.25);
}
.search-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(0, 212, 255, 0.4);
}
.search-btn:active:not(:disabled) { transform: translateY(0); }
.search-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: var(--bg-tertiary);
  color: var(--text-muted);
  box-shadow: none;
}
.search-btn .el-icon { font-size: 14px; }
.spin { animation: spin 0.9s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.search-hint {
  margin: 8px 4px 0;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.search-result { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.result-card {
  display: flex; align-items: center; gap: var(--spacing-lg);
  background: var(--glass-medium); border: 1px solid var(--glass-medium-border);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border-radius: var(--radius-md); padding: var(--spacing-lg);
}
.result-info { flex: 1; }
.result-name { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.result-id { font-size: 12px; color: var(--text-muted); margin-left: 6px; }
.result-grade { font-size: 14px; font-weight: 600; }
.result-faction { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }

.empty { padding: var(--spacing-2xl) 0; }

@media (max-width: 768px) {
  .friends-view { max-width: 100%; padding: 0; }
  .friend-grid { grid-template-columns: 1fr; }
  .search-section { max-width: 100%; }
  .search-input-group { flex-wrap: nowrap; }
  .search-input { min-width: 0; }
  .search-btn span { display: none; }
  .search-btn { padding: 9px 12px; }
}
</style>
