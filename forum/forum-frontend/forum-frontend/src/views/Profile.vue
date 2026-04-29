<template>
  <div class="profile">
    <el-row :gutter="20">
      <!-- 侧边栏 -->
      <el-col :span="6">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0) }}
            </el-avatar>
            <h3>{{ userInfo.username }}</h3>
            <p class="role">
              <el-tag :type="roleType" size="small">{{ roleText }}</el-tag>
            </p>
          </div>

          <el-divider />

          <div class="stats">
            <div class="stat-item">
              <div class="stat-value">{{ postCount }}</div>
              <div class="stat-label">帖子</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ commentCount }}</div>
              <div class="stat-label">评论</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 主要内容 -->
      <el-col :span="18">
        <el-card>
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="个人资料" name="info">
              <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="form.username" />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" />
                </el-form-item>

                <el-form-item label="头像URL">
                  <el-input v-model="form.avatar" placeholder="请输入头像图片URL" />
                </el-form-item>

                <el-form-item label="个人简介">
                  <el-input
                      v-model="form.intro"
                      type="textarea"
                      :rows="3"
                      maxlength="200"
                      show-word-limit
                      placeholder="介绍一下自己吧"
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="handleUpdate" :loading="updating">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="我的帖子" name="posts">
              <div class="my-posts" v-loading="postsLoading">
                <div v-if="myPosts.length === 0 && !postsLoading" class="empty-posts">
                  <el-empty description="暂无帖子">
                    <el-button type="primary" @click="router.push('/post/create')">
                      发布第一篇帖子
                    </el-button>
                  </el-empty>
                </div>
                <PostCard
                    v-for="post in myPosts"
                    :key="post.id"
                    :post="post"
                    @click="goToPost(post.id)"
                />

                <div class="pagination" v-if="postsTotal > 0">
                  <el-pagination
                      v-model:current-page="postsPageNum"
                      :total="postsTotal"
                      :page-size="postsPageSize"
                      layout="prev, pager, next"
                      @current-change="fetchMyPosts"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { getMyPosts } from '@/api/post'
import PostCard from '@/components/PostCard.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const updating = ref(false)
const activeTab = ref('info')
const postCount = ref(0)
const commentCount = ref(0)

// 我的帖子
const myPosts = ref([])
const postsLoading = ref(false)
const postsPageNum = ref(1)
const postsPageSize = ref(10)
const postsTotal = ref(0)

const userInfo = computed(() => userStore.userInfo)

const roleType = computed(() => {
  const role = userInfo.value.role
  if (role === 'admin') return 'danger'
  if (role === 'moderator') return 'warning'
  return 'info'
})

const roleText = computed(() => {
  const role = userInfo.value.role
  if (role === 'admin') return '管理员'
  if (role === 'moderator') return '版主'
  return '普通用户'
})

const form = reactive({
  username: '',
  email: '',
  avatar: '',
  intro: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      form.username = res.data.username
      form.email = res.data.email
      form.avatar = res.data.avatar || ''
      form.intro = res.data.intro || ''
      postCount.value = res.data.postCount || 0
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const fetchMyPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getMyPosts({
      pageNum: postsPageNum.value,
      pageSize: postsPageSize.value
    })
    console.log('我的帖子响应:', res)

    if (res.code === 200) {
      myPosts.value = res.data.records || []
      postsTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('获取我的帖子失败:', error)
    ElMessage.error('加载失败：' + (error.message || '网络错误'))
  } finally {
    postsLoading.value = false
  }
}

const goToPost = (postId) => {
  console.log('跳转到帖子:', postId)
  router.push(`/post/${postId}`)
}

const handleTabChange = (tabName) => {
  if (tabName === 'posts') {
    fetchMyPosts()
  }
}

const handleUpdate = async () => {
  await formRef.value.validate()

  updating.value = true
  try {
    const success = await userStore.updateProfile({
      username: form.username,
      email: form.email,
      avatar: form.avatar,
      intro: form.intro
    })

    if (success) {
      ElMessage.success('更新成功')
      await fetchUserInfo()
    } else {
      ElMessage.error('更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
  // 如果 URL 参数指定了 tab，则切换到对应标签
  if (router.currentRoute.value.query.tab === 'myposts') {
    activeTab.value = 'posts'
    fetchMyPosts()
  }
})
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  text-align: center;
}

.avatar-section {
  padding: 20px 0;
}

.role {
  margin-top: 10px;
}

.stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.my-posts {
  min-height: 300px;
}

.empty-posts {
  text-align: center;
  padding: 40px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>