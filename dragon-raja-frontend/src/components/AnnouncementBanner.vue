<template>
  <div class="announcement-banner" v-if="announcement">
    <div class="banner-icon">
      <el-icon><Bell /></el-icon>
    </div>
    <div class="banner-content">
      <div class="banner-title">{{ announcement.title }}</div>
      <div class="banner-scroll">
        <div class="scroll-text" ref="scrollTextRef">
          {{ announcement.content }}
          <span class="scroll-repeat">{{ announcement.content }}</span>
        </div>
      </div>
    </div>
    <div class="banner-time">{{ formatRelativeTime(announcement.createTime) }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getLatestAnnouncement } from '../api/announcement'
import { formatRelativeTime } from '../utils/format'

const announcement = ref(null)

async function loadLatestAnnouncement() {
  try {
    const res = await getLatestAnnouncement()
    announcement.value = res.data || null
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

onMounted(() => {
  loadLatestAnnouncement()
})
</script>

<style scoped>
.announcement-banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: rgba(10, 14, 39, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: var(--radius-xl);
  padding: var(--spacing-md-lg) var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.04);
  overflow: hidden;
  position: relative;
}

.announcement-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.3), transparent);
}

.announcement-banner::after {
  content: '';
  position: absolute;
  top: 50%;
  left: -100%;
  width: 100%;
  height: 100%;
  transform: translateY(-50%);
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.04), transparent);
  animation: lightSweep 8s ease-in-out infinite;
}

@keyframes lightSweep {
  0%, 100% { left: -100%; }
  50% { left: 100%; }
}

.banner-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  color: rgba(0, 212, 255, 0.85);
  font-size: 18px;
  animation: bell-ring 4s ease-in-out infinite;
  border: 1px solid rgba(0, 212, 255, 0.18);
}

@keyframes bell-ring {
  0%, 100% { transform: rotate(0deg); }
  8% { transform: rotate(-10deg); }
  16% { transform: rotate(10deg); }
  24% { transform: rotate(-6deg); }
  32% { transform: rotate(6deg); }
  40% { transform: rotate(0deg); }
}

.banner-content {
  flex: 1;
  min-width: 0;
}

.banner-title {
  color: rgba(0, 212, 255, 0.9);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.banner-scroll {
  overflow: hidden;
  white-space: nowrap;
}

.scroll-text {
  display: inline-block;
  animation: scroll-left 25s linear infinite;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.scroll-repeat {
  margin-left: 80px;
}

@keyframes scroll-left {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.banner-time {
  flex-shrink: 0;
  color: var(--text-muted);
  font-size: 12px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .announcement-banner {
    padding: var(--spacing-sm-md) var(--spacing-md);
    border-radius: var(--radius-lg);
    gap: var(--spacing-sm);
  }
  
  .banner-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
  
  .banner-title {
    font-size: 13px;
  }
  
  .scroll-text {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .banner-time {
    display: none;
  }
}
</style>