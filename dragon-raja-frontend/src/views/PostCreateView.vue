<template>
  <div class="post-create-view">
    <div class="create-card glow-hover">
      <!-- 头部 -->
      <div class="create-header">
        <h2 class="title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</h2>
        <p class="subtitle">分享你的见闻与思考</p>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="postForm" :rules="rules" label-width="80px" class="create-form">
        <!-- 分类选择器 -->
        <el-form-item label="分类" prop="category">
          <div class="category-selector">
            <div
              v-for="cat in categories"
              :key="cat.value"
              class="category-option"
              :class="{ active: postForm.category === cat.value }"
              :style="postForm.category === cat.value ? {
                color: cat.color,
                borderColor: cat.color,
                backgroundColor: cat.color + '15',
                boxShadow: `0 0 10px ${cat.color}40`
              } : {}"
              @click="postForm.category = cat.value"
            >
              <span class="cat-dot" :style="{ backgroundColor: cat.color }"></span>
              <span>{{ cat.label }}</span>
            </div>
          </div>
        </el-form-item>

        <!-- 标题输入 -->
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="postForm.title"
            placeholder="请输入帖子标题（最多200字）"
            maxlength="200"
            show-word-limit
            size="large"
          />
        </el-form-item>

        <!-- 内容输入 -->
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入帖子内容..."
            maxlength="5000"
            show-word-limit
            resize="vertical"
          />
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="图片">
          <div class="image-upload-area">
            <div
              v-for="(img, index) in postForm.images"
              :key="index"
              class="image-item"
            >
              <img :src="img" :alt="`图片${index + 1}`" class="uploaded-image" />
              <button class="remove-image" @click="removeImage(index)">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="upload-trigger" @click="triggerUpload">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span class="upload-text">添加图片</span>
            </div>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              multiple
              class="hidden-input"
              @change="handleFileSelect"
            />
          </div>
          <p class="image-hint">支持 JPG/PNG/GIF/WebP 格式，最多上传9张</p>
        </el-form-item>

        <!-- 按钮区 -->
        <el-form-item>
          <div class="form-actions">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '发布帖子' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'
import { createPost, updatePost, getPostDetail } from '../api/post'
import { uploadPostImage } from '../api/file'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const fileInputRef = ref(null)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const uploadLoading = ref(false)

const postForm = reactive({
  category: 1,
  title: '',
  content: '',
  images: []
})

const categories = [
  { value: 1, label: '闲聊', color: '#4da6ff' },
  { value: 2, label: '史料', color: '#b388ff' },
  { value: 3, label: '委托', color: '#ff9800' },
  { value: 4, label: '交换', color: '#66bb6a' }
]

const rules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 200, message: '标题最长200字', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 加载编辑数据
async function loadEditData() {
  if (route.query.id) {
    isEdit.value = true
    editId.value = route.query.id
    try {
      const res = await getPostDetail(editId.value)
      postForm.category = res.data.category
      postForm.title = res.data.title
      postForm.content = res.data.content
      postForm.images = res.data.images || []
    } catch (error) {
      console.error('加载帖子数据失败:', error)
    }
  }
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleFileSelect(event) {
  const files = event.target.files
  if (!files || files.length === 0) return

  const maxImages = 9
  const remaining = maxImages - postForm.images.length
  if (files.length > remaining) {
    ElMessage.warning(`最多只能上传${maxImages}张图片`)
    return
  }

  uploadLoading.value = true
  try {
    for (const file of files) {
      const res = await uploadPostImage(file)
      if (res.data) {
        postForm.images.push(res.data)
      }
    }
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
    console.error('上传失败:', error)
  } finally {
    uploadLoading.value = false
    event.target.value = ''
  }
}

function removeImage(index) {
  postForm.images.splice(index, 1)
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const postData = {
        title: postForm.title,
        content: postForm.content,
        category: postForm.category,
        images: postForm.images
      }

      if (isEdit.value) {
        await updatePost(editId.value, postData)
        ElMessage.success('修改成功')
        router.push(`/post/${editId.value}`)
      } else {
        await createPost(postData)
        ElMessage.success('发布成功')
        router.push('/')
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadEditData()
})
</script>

<style scoped>
.post-create-view {
  max-width: 800px;
  margin: 0 auto;
}

.create-card {
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border: 1px solid var(--glass-light-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}

/* 头部 */
.create-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-primary);
  text-shadow: 0 0 10px var(--accent-glow);
  margin-bottom: var(--spacing-xs);
}

.subtitle {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 分类选择器 */
.category-selector {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.category-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.category-option:hover {
  border-color: var(--accent-secondary);
  color: var(--accent-secondary);
}

.category-option.active {
  font-weight: 600;
}

.cat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
}

.remove-image:hover {
  background: rgba(255, 0, 0, 0.8);
}

.upload-trigger {
  width: 100px;
  height: 100px;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: all var(--transition-fast);
  background: rgba(0, 212, 255, 0.03);
}

.upload-trigger:hover {
  border-color: var(--accent-primary);
  background: rgba(0, 212, 255, 0.08);
}

.upload-icon {
  font-size: 24px;
  color: var(--text-muted);
}

.upload-text {
  font-size: 12px;
  color: var(--text-muted);
}

.hidden-input {
  display: none;
}

.image-hint {
  margin-top: var(--spacing-xs);
  font-size: 12px;
  color: var(--text-muted);
}

/* 按钮区 */
.form-actions {
  display: flex;
  gap: var(--spacing-sm);
  width: 100%;
  justify-content: center;
  align-items: center;
}

.form-actions .el-button {
  min-width: 140px;
}

@media (max-width: 767px) {
  .post-create-view { padding: 0 var(--spacing-sm); }
  .create-card { border-radius: 0; border-left: none; border-right: none; padding: var(--spacing-md); }
  /* 移动端：label 顶置到上方，表单内容居中 */
  .create-form :deep(.el-form-item) { flex-direction: column; align-items: stretch; }
  .create-form :deep(.el-form-item__label) { width: auto !important; text-align: left; padding: 0 0 6px 0; }
  .create-form :deep(.el-form-item__content) { margin-left: 0 !important; }
  /* 分类选项居中换行 */
  .category-selector { justify-content: center; }
  .category-option { font-size: 13px; padding: 6px 10px; }
  /* 标题和内容宽度 100% */
  .create-form :deep(.el-input), .create-form :deep(.el-textarea) { width: 100%; }
  /* 按钮 */
  .form-actions { flex-direction: column; gap: 10px; align-items: center; }
  .form-actions .el-button { width: 100% !important; max-width: 320px; display: block; }
}
</style>
