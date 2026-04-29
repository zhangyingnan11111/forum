<template>
  <header class="header">
    <div class="header-container">
      <div class="logo" @click="router.push('/home')">
        <el-icon><ChatDotRound /></el-icon>
        <span>论坛系统</span>
      </div>

      <div class="search-bar">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子..."
            clearable
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>

        <template v-if="userStore.isLoggedIn">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button text @click="router.push('/notifications')">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>

          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar">
                {{ userStore.userInfo.username?.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="myPosts">我的帖子</el-dropdown-item>
                <el-dropdown-item command="create">发布帖子</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getUnreadCount } from '@/api/notification.js'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Search, Bell, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const unreadCount = ref(0)

const isAdmin = computed(() => userStore.userInfo.role === 'admin')

const fetchUnreadCount = async () => {
  if (userStore.isLoggedIn) {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'myPosts':
      router.push('/home?tab=myposts')
      break
    case 'create':
      router.push('/post/create')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/home')
      break
  }
}

onMounted(() => {
  fetchUnreadCount()
  // 每30秒刷新一次未读数量
  setInterval(fetchUnreadCount, 30000)
})
</script>

<style scoped>
.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.search-bar {
  flex: 1;
  max-width: 400px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-info:hover {
  background-color: #f5f5f5;
}
</style>