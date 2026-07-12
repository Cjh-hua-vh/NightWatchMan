<template>
  <router-view />
</template>

<script setup>
import { onUnmounted, watch } from 'vue'
import { useUserStore } from './stores/user'
import { heartbeat } from './api/user'

const userStore = useUserStore()
let timer = null

function startHeartbeat() {
  stopHeartbeat()
  timer = setInterval(() => {
    heartbeat().catch(() => {})  // 静默失败，不影响用户体验
  }, 30000)  // 每 30 秒
}

function stopHeartbeat() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

watch(() => userStore.isLoggedIn, (val) => {
  if (val) startHeartbeat()
  else stopHeartbeat()
}, { immediate: true })

onUnmounted(() => stopHeartbeat())
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}
</style>
