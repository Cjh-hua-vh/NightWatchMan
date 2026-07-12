<template>
  <div class="pagination-wrapper">
    <el-pagination
      v-model:current-page="innerCurrent"
      :page-size="size"
      :total="total"
      :background="background"
      layout="prev, pager, next"
      :prev-text="'上一页'"
      :next-text="'下一页'"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    default: 1
  },
  total: {
    type: Number,
    default: 0
  },
  size: {
    type: Number,
    default: 10
  },
  background: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['change'])

const innerCurrent = ref(props.current)

// 监听外部 current 变化
watch(
  () => props.current,
  (val) => {
    innerCurrent.value = val
  }
)

// 页码变化
function handleChange(page) {
  emit('change', page)
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-button-color: var(--text-primary);
  --el-pagination-hover-color: var(--accent-primary);
}
</style>
