/**
 * 格式化日期时间
 * @param {string|Date} date - 日期字符串或Date对象
 * @param {string} format - 格式模板，默认 'yyyy-MM-dd HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateTime(date, format = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期（仅年月日）
 * @param {string|Date} date - 日期
 * @returns {string} yyyy-MM-dd
 */
export function formatDate(date) {
  return formatDateTime(date, 'yyyy-MM-dd')
}

/**
 * 相对时间格式化（如"3分钟前"、"2小时前"）
 * @param {string|Date} date - 日期
 * @returns {string} 相对时间描述
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const now = Date.now()
  const diff = now - d.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return formatDate(date)
}

/**
 * 获取血统等级对应的颜色
 * @param {string} grade - 血统等级 D/C/B/A/S
 * @returns {string} 颜色值
 */
export function getGradeColor(grade) {
  const colorMap = {
    S: '#ffd700',
    A: '#b388ff',
    B: '#4da6ff',
    C: '#66bb6a',
    D: '#8892b0'
  }
  return colorMap[grade] || '#8892b0'
}

/**
 * 获取帖子分类对应的颜色
 * @param {number} category - 分类编号 1-4
 * @returns {string} 颜色值
 */
export function getCategoryColor(category) {
  const colorMap = {
    1: '#4da6ff',
    2: '#b388ff',
    3: '#ff9800',
    4: '#66bb6a'
  }
  return colorMap[category] || '#4da6ff'
}

/**
 * 获取帖子分类名称
 * @param {number} category - 分类编号 1-4
 * @returns {string} 分类名称
 */
export function getCategoryName(category) {
  const nameMap = {
    1: '闲聊',
    2: '史料',
    3: '委托',
    4: '交换'
  }
  return nameMap[category] || '未知'
}

/**
 * 获取用户状态名称
 * @param {number} status - 状态码 0-待审核 1-正常 2-封禁
 * @returns {string} 状态名称
 */
export function getUserStatusName(status) {
  const statusMap = {
    0: '待审核',
    1: '正常',
    2: '封禁'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取用户状态颜色
 * @param {number} status - 状态码
 * @returns {string} 颜色值
 */
export function getUserStatusColor(status) {
  const colorMap = {
    0: '#ff9800',
    1: '#66bb6a',
    2: '#f44336'
  }
  return colorMap[status] || '#8892b0'
}


/**
 * 高亮文本中的关键词（用于搜索结果展示）
 * @param {string} text - 原始文本
 * @param {string} keyword - 要匹配的关键词
 * @returns {string} 包含 <mark> 标签的 HTML 字符串
 */
export function highlightText(text, keyword) {
  if (!text || !keyword || !keyword.trim()) return text || ''
  const escaped = keyword.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return String(text).replace(regex, '<mark class="search-hl">$1</mark>')
}

/**
 * 获取图片完整URL（相对路径自动拼接后端地址）
 */
export function getImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    // 开发模式：把 localhost 绝对路径转为相对路径，避免手机/公网访问时 localhost 指向本机
    if (import.meta.env.DEV && (path.includes('localhost') || path.includes('127.0.0.1'))) {
      try {
        const url = new URL(path)
        return url.pathname
      } catch { return path }
    }
    return path
  }
  // 开发模式(Vite dev server)使用相对路径，让 Vite proxy 统一处理 /uploads 转发
  if (import.meta.env.DEV) {
    return path
  }
  // 生产模式：根据访问域名拼接后端地址
  const publicBackendUrl =
    typeof window !== 'undefined' && window.location.hostname.includes('cpolar.top')
      ? 'https://6563181d.r5.cpolar.top'
      : ''
  const backendUrl = publicBackendUrl || import.meta.env.VITE_BACKEND_URL || ''
  if (path.startsWith('/uploads/')) return backendUrl ? backendUrl + path : path
  if (path.startsWith('/api/')) return path
  return path
}
