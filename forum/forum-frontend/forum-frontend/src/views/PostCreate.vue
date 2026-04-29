<template>
  <div class="post-create">
    <el-card>
      <template #header>
        <h2>{{ isEdit ? '编辑帖子' : '发布新帖子' }}</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="form.title"
              placeholder="请输入标题（1-200字符）"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <div class="editor-container">
            <div class="editor-toolbar">
              <el-button-group>
                <el-button size="small" @click="insertMarkdown('**')">
                  <b>B</b>
                </el-button>
                <el-button size="small" @click="insertMarkdown('*')">
                  <i>I</i>
                </el-button>
                <el-button size="small" @click="insertMarkdown('`')">
                  <code>`</code>
                </el-button>
              </el-button-group>
            </div>
            <el-input
                v-model="form.content"
                type="textarea"
                :rows="15"
                placeholder="使用Markdown编写内容..."
            />
            <div class="preview-container">
              <h4>预览：</h4>
              <div class="preview" v-html="renderedContent"></div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, createPost, updatePost, getPostDetail } from '@/api/post'
import { marked } from 'marked'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)
const categories = ref([])

const isEdit = computed(() => route.params.id && route.path.includes('/edit'))

const form = reactive({
  title: '',
  categoryId: null,
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在1-200字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

const renderedContent = computed(() => {
  return form.content ? marked(form.content) : ''
})

const insertMarkdown = (symbol) => {
  const textarea = document.querySelector('.el-textarea__inner')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = form.content
  const selectedText = text.substring(start, end)

  let newText
  if (symbol === '**') {
    newText = text.substring(0, start) + '**' + selectedText + '**' + text.substring(end)
  } else if (symbol === '*') {
    newText = text.substring(0, start) + '*' + selectedText + '*' + text.substring(end)
  } else if (symbol === '`') {
    newText = text.substring(0, start) + '`' + selectedText + '`' + text.substring(end)
  }

  form.content = newText
}

const fetchCategories = async () => {
  const res = await getCategories()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const fetchPostDetail = async () => {
  const res = await getPostDetail(route.params.id)
  if (res.code === 200) {
    form.title = res.data.title
    form.content = res.data.content
    form.categoryId = res.data.categoryId
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePost(route.params.id, {
        title: form.title,
        content: form.content,
        categoryId: form.categoryId
      })
      ElMessage.success('更新成功')
      router.push(`/post/${route.params.id}`)
    } else {
      const res = await createPost({
        title: form.title,
        content: form.content,
        categoryId: form.categoryId
      })
      ElMessage.success('发布成功')
      router.push(`/post/${res.data}`)
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
  if (isEdit.value) {
    fetchPostDetail()
  }
})
</script>

<style scoped>
.post-create {
  max-width: 1000px;
  margin: 0 auto;
}

.editor-container {
  width: 100%;
}

.editor-toolbar {
  margin-bottom: 10px;
}

.preview-container {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.preview {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
}
</style>