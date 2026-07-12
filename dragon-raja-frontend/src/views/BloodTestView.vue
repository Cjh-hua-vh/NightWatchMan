<template>
  <div class="blood-test-view">
    <ParticleBackground />

    <div class="test-card">
      <div v-if="stage === 'start'" class="start-stage">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <h1>3E 血统测试</h1>
        <p class="subtitle">混血种入学资格测试</p>
        <el-button type="primary" size="large" class="start-btn" @click="stage = 'exam'">
          开始测试
        </el-button>
      </div>

      <div v-else-if="stage === 'exam'" class="exam-stage">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: ((currentQ + 1) / questions.length * 100) + '%' }"></div>
        </div>
        <span class="progress-text">{{ currentQ + 1 }} / {{ questions.length }}</span>

        <div class="question-card" v-if="questions[currentQ]">
          <div class="q-category">{{ questions[currentQ].category }}</div>
          <h2 class="q-title">{{ questions[currentQ].question }}</h2>
          <div class="q-options">
            <div
              v-for="opt in ['A', 'B', 'C', 'D']"
              :key="opt"
              class="q-option"
              :class="{ selected: answers[questions[currentQ].id] === opt }"
              @click="selectAnswer(opt)"
            >
              <span class="opt-letter">{{ opt }}</span>
              <span class="opt-text">{{ questions[currentQ]['option' + opt] }}</span>
            </div>
          </div>
        </div>

        <div class="exam-actions">
          <el-button v-if="currentQ > 0" @click="currentQ--">上一题</el-button>
          <el-button v-if="currentQ < questions.length - 1" type="primary" :disabled="!answers[questions[currentQ]?.id]" @click="currentQ++">下一题</el-button>
          <el-button v-else type="primary" :disabled="Object.keys(answers).length < questions.length" :loading="submitting" @click="handleSubmit">提交</el-button>
        </div>
      </div>

      <div v-else-if="stage === 'result'" class="result-stage">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <h1>血统鉴定完成</h1>
        <div class="result-card">
          <div class="result-grade" :style="{ color: getGradeColor(result.grade) }">
            {{ result.grade }} 级混血种
          </div>
          <div class="result-detail" v-if="result.yanling">言灵：<strong>{{ result.yanling }}</strong></div>
          <div class="result-detail" v-if="result.bloodType">血型：{{ result.bloodType }}</div>
        </div>
        <el-button type="primary" size="large" @click="goRegister">确认，前往注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamQuestions, submitExam, skipTest } from '../api/blood-test'
import { getGradeColor } from '../utils/format'
import ParticleBackground from '../components/ParticleBackground.vue'

const router = useRouter()
const stage = ref('start')
const questions = ref([])
const currentQ = ref(0)
const answers = ref({})
const submitting = ref(false)
const result = ref({})

function goRegister() {
  const r = result.value
  router.push({ path: '/register', query: { grade: r.grade, yanling: r.yanling || '' } })
}

function onKeyDown(e) {
  if (stage.value === 'start' && (e.key === '0' || e.keyCode === 48)) {
    e.preventDefault()
    handleSkip()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeyDown)
  loadQuestions()
})
onUnmounted(() => window.removeEventListener('keydown', onKeyDown))

async function loadQuestions() {
  try { const res = await getExamQuestions(); questions.value = res.data || [] }
  catch { ElMessage.error('加载题目失败') }
}

function selectAnswer(opt) {
  const qid = questions.value[currentQ.value]?.id
  if (qid) answers.value[qid] = opt
}

async function handleSubmit() {
  submitting.value = true
  try {
    const res = await submitExam({ answers: answers.value })
    result.value = res.data
    stage.value = 'result'
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}

async function handleSkip() {
  try {
    const res = await skipTest()
    result.value = res.data
    stage.value = 'result'
  } catch { ElMessage.error('跳过失败') }
}
</script>

<style scoped>
.blood-test-view {
  position: relative; min-height: 100vh; display: flex;
  align-items: center; justify-content: center;
  background: var(--bg-primary); padding: var(--spacing-lg);
}
.test-card {
  position: relative; z-index: 10; width: 520px; max-width: 100%;
  background: var(--bg-secondary); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: var(--spacing-xl); box-shadow: var(--shadow-md);
}
.logo-img { width: 56px; height: 56px; border-radius: 50%; object-fit: cover; display: block; margin: 0 auto var(--spacing-md); }
.start-stage { text-align: center; padding: var(--spacing-lg) 0; }
h1 { font-size: 22px; font-weight: 700; color: var(--accent-primary); margin-bottom: var(--spacing-xs); }
.subtitle { color: var(--text-secondary); font-size: 14px; margin-bottom: var(--spacing-xl); }
.start-btn { min-width: 200px; }
.progress-bar { height: 3px; background: var(--border-color); border-radius: 2px; margin-bottom: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--accent-primary); border-radius: 2px; transition: width var(--transition-fast); }
.progress-text { font-size: 12px; color: var(--text-muted); }
.question-card { margin: var(--spacing-lg) 0; }
.q-category { display: inline-block; padding: 2px 8px; background: var(--accent-ghost); color: var(--accent-primary); border-radius: var(--radius-sm); font-size: 12px; font-weight: 600; margin-bottom: var(--spacing-sm); }
.q-title { font-size: 16px; font-weight: 600; color: var(--text-primary); line-height: 1.6; margin-bottom: var(--spacing-lg); }
.q-options { display: flex; flex-direction: column; gap: var(--spacing-sm); }
.q-option { display: flex; align-items: center; gap: var(--spacing-md); padding: var(--spacing-sm) var(--spacing-md); background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-md); cursor: pointer; transition: all var(--transition-fast); }
.q-option:hover { border-color: var(--accent-primary); background: var(--bg-hover); }
.q-option.selected { border-color: var(--accent-primary); background: var(--accent-ghost); }
.opt-letter { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: var(--border-color); color: var(--text-secondary); font-weight: 700; font-size: 13px; flex-shrink: 0; }
.q-option.selected .opt-letter { background: var(--accent-primary); color: #0c0c0c; }
.opt-text { font-size: 14px; color: var(--text-primary); }
.exam-actions { display: flex; justify-content: center; gap: var(--spacing-md); margin-top: var(--spacing-lg); }
.result-stage { text-align: center; padding: var(--spacing-lg) 0; }
.result-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: var(--spacing-lg); margin: var(--spacing-lg) 0; }
.result-grade { font-size: 36px; font-weight: 900; margin-bottom: var(--spacing-md); }
.result-detail { color: var(--text-primary); font-size: 15px; margin-bottom: var(--spacing-xs); }
.result-detail strong { color: var(--accent-primary); }
</style>
