<template>
  <div class="post-card" @click="handleClick">
    <div class="post-header">
      <div class="post-title">
        <el-tag v-if="post.isTop" type="danger" size="small" effect="dark" class="tag">置顶</el-tag>
        <el-tag v-if="post.isEssence" type="warning" size="small" effect="dark" class="tag">精华</el-tag>
        <span>{{ post.title }}</span>
      </div>
    </div>

    <div class="post-summary">{{ post.summary || post.content?.substring(0, 200) }}</div>

    <div class="post-footer">
      <div class="post-author">
        <el-avatar :size="24" :src="post.authorAvatar">
          {{ post.authorName?.charAt(0) }}
        </el-avatar>
        <span>{{ post.authorName }}</span>
      </div>

      <div class="post-stats">
        <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
        <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
        <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
      </div>

      <div class="post-time">
        {{ formatTime(post.createTime) }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'refresh'])
const router = useRouter()

const handleClick = () => {
  console.log('点击帖子:', props.post.id)
  if (props.post.id) {
    router.push(`/post/${props.post.id}`)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / 86400000)

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}
</script>

<style scoped>
.post-card {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.post-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.post-header {
  margin-bottom: 12px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.post-title .tag {
  font-size: 12px;
}

.post-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.post-stats {
  display: flex;
  gap: 12px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>