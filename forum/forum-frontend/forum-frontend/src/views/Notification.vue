<template>
  <div class="notification">
    <el-card>
      <template #header>
        <div class="notification-header">
          <h2>我的通知</h2>
          <el-button
              v-if="unreadCount > 0"
              type="primary"
              size="small"
              @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
        </div>
      </template>

      <div class="notification-list" v-loading="loading">
        <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.isRead }"
            @click="handleNotificationClick(item)"
        >
          <div class="notification-icon">
            <el-icon :size="24">
              <ChatDotRound v-if="item.type === 'comment'" />
              <Star v-else-if="item.type === 'like'" />
              <Bell v-else />
            </el-icon>
          </div>

          <div class="notification-content">
            <div class="notification-message">
              <span class="sender">{{ item.senderName }}</span>
              {{ item.content }}
            </div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>

          <div class="notification-status">
            <el-badge v-if="!item.isRead" value="new" type="danger" />
          </div>
        </div>

        <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />

        <div class="pagination" v-if="total > 0">
          <el-pagination
              v-model:current-page="pageNum"
              :total="total"
              :page-size="pageSize"
              layout="prev, pager, next"
              @current-change="fetchNotifications"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, getUnreadCount, markAllAsRead, markAsRead } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const unreadCount = ref(0)

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      notifications.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  const res = await getUnreadCount()
  if (res.code === 200) {
    unreadCount.value = res.data
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已标记所有通知为已读')
    await fetchNotifications()
    await fetchUnreadCount()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleNotificationClick = async (notification) => {
  // 标记为已读
  if (!notification.isRead) {
    await markAsRead(notification.id)
    notification.isRead = true
    unreadCount.value--
  }

  // 跳转到目标页面
  if (notification.targetType === 'post') {
    router.push(`/post/${notification.targetId}`)
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
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped>
.notification {
  max-width: 800px;
  margin: 0 auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-header h2 {
  margin: 0;
}

.notification-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  border-radius: 50%;
  margin-right: 16px;
  color: #409eff;
}

.notification-content {
  flex: 1;
}

.notification-message {
  margin-bottom: 4px;
  color: #333;
}

.sender {
  font-weight: bold;
  color: #409eff;
  margin-right: 8px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-status {
  margin-left: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>