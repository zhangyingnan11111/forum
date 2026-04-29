<template>
  <div class="post-detail" v-loading="loading">
    <div v-if="post" class="post-card">
      <!-- 帖子头部 -->
      <div class="post-header">
        <h1 class="post-title">
          <el-tag v-if="post.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="post.isEssence" type="warning" size="small" effect="dark">精华</el-tag>
          {{ post.title }}
        </h1>

        <div class="post-meta">
          <div class="author-info">
            <el-avatar :size="40" :src="post.authorAvatar">
              {{ post.authorName?.charAt(0) }}
            </el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ post.authorName }}</span>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>

          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          </div>
        </div>
      </div>

      <!-- 帖子内容 -->
      <div class="post-content" v-html="renderedContent"></div>

      <!-- 帖子操作 -->
      <div class="post-actions">
        <el-button
            :type="isLiked ? 'primary' : 'default'"
            @click="handleLike"
        >
          <el-icon><Star /></el-icon>
          {{ isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
        </el-button>

        <el-button v-if="isOwner" @click="handleEdit">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>

        <el-button v-if="isOwner" type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon> 删除
        </el-button>

        <el-button v-if="hasManagePermission" type="warning" @click="handleTop">
          <el-icon><Top /></el-icon>
          {{ post.isTop ? '取消置顶' : '置顶' }}
        </el-button>

        <el-button v-if="hasManagePermission" type="success" @click="handleEssence">
          <el-icon><Medal /></el-icon>
          {{ post.isEssence ? '取消精华' : '设为精华' }}
        </el-button>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="comments-section">
      <h3>评论 ({{ comments.length }})</h3>

      <!-- 发表评论 -->
      <div class="comment-input" v-if="userStore.isLoggedIn">
        <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
        />
        <div class="comment-actions">
          <el-button type="primary" @click="submitComment" :loading="submitting">
            发表评论
          </el-button>
        </div>
      </div>

      <div v-else class="login-tip">
        <el-alert
            title="请登录后发表评论"
            type="info"
            :closable="false"
            show-icon
        />
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            @delete="fetchComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPostDetail, deletePost, topPost, essencePost } from '@/api/post'
import { getComments, createComment, deleteComment } from '@/api/comment'
import { like, unlike, getLikeStatus } from '@/api/like'
import CommentItem from '@/components/CommentItem.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Star, Edit, Delete, Top, Medal } from '@element-plus/icons-vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const isLiked = ref(false)

const postId = computed(() => route.params.id)
const isOwner = computed(() => post.value && userStore.userInfo.userId === post.value.authorId)
const hasManagePermission = computed(() =>
    userStore.isAdmin || userStore.isModerator
)

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error(err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

const renderedContent = computed(() => {
  return post.value ? marked(post.value.content) : ''
})

const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(postId.value)
    if (res.code === 200) {
      post.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载失败')
    router.push('/home')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  const res = await getComments(postId.value)
  if (res.code === 200) {
    comments.value = res.data
  }
}

const fetchLikeStatus = async () => {
  if (userStore.isLoggedIn) {
    const res = await getLikeStatus('post', postId.value)
    if (res.code === 200) {
      isLiked.value = res.data.isLiked
    }
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (isLiked.value) {
      await unlike('post', postId.value)
      post.value.likeCount--
      isLiked.value = false
      ElMessage.success('取消点赞')
    } else {
      await like('post', postId.value)
      post.value.likeCount++
      isLiked.value = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      content: commentContent.value,
      postId: postId.value
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    await fetchComments()
    // 刷新帖子详情以更新评论数
    await fetchPostDetail()
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const handleEdit = () => {
  router.push(`/post/edit/${postId.value}`)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deletePost(postId.value)
    ElMessage.success('删除成功')
    router.push('/home')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleTop = async () => {
  try {
    await topPost(postId.value, !post.value.isTop)
    ElMessage.success(post.value.isTop ? '已取消置顶' : '置顶成功')
    post.value.isTop = !post.value.isTop
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleEssence = async () => {
  try {
    await essencePost(postId.value, !post.value.isEssence)
    ElMessage.success(post.value.isEssence ? '已取消精华' : '加精成功')
    post.value.isEssence = !post.value.isEssence
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchPostDetail()
  fetchComments()
  fetchLikeStatus()
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
}

.post-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.post-title {
  font-size: 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  color: #409eff;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-stats {
  display: flex;
  gap: 16px;
  color: #666;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-content {
  line-height: 1.8;
  font-size: 16px;
  margin-bottom: 24px;
  min-height: 300px;
}

.post-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comments-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.comments-section h3 {
  margin-bottom: 20px;
}

.comment-input {
  margin-bottom: 24px;
}

.comment-actions {
  margin-top: 12px;
  text-align: right;
}

.login-tip {
  margin-bottom: 20px;
}

.comment-list {
  margin-top: 20px;
}
</style>