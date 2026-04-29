<template>
  <div class="comment-item">
    <div class="comment-header">
      <div class="comment-author">
        <el-avatar :size="32" :src="comment.authorAvatar">
          {{ comment.authorName?.charAt(0) }}
        </el-avatar>
        <div class="author-info">
          <span class="author-name">{{ comment.authorName }}</span>
          <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
        </div>
      </div>

      <div class="comment-actions">
        <el-button
            text
            size="small"
            :type="comment.isLiked ? 'primary' : 'default'"
            @click="handleLike"
        >
          <el-icon><Star /></el-icon>
          {{ comment.likeCount }}
        </el-button>

        <el-button
            v-if="isOwner"
            text
            size="small"
            type="danger"
            @click="handleDelete"
        >
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <div class="comment-content">{{ comment.content }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { like, unlike } from '@/api/like.js'
import { deleteComment } from '@/api/comment.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['delete'])

const userStore = useUserStore()

const isOwner = computed(() =>
    userStore.userInfo.userId === props.comment.authorId ||
    userStore.isAdmin
)

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (props.comment.isLiked) {
      await unlike('comment', props.comment.id)
      props.comment.likeCount--
      props.comment.isLiked = false
    } else {
      await like('comment', props.comment.id)
      props.comment.likeCount++
      props.comment.isLiked = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(props.comment.id)
    ElMessage.success('删除成功')
    emit('delete')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
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
</script>

<style scoped>
.comment-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  color: #409eff;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-content {
  padding-left: 44px;
  line-height: 1.6;
  color: #333;
}
</style>