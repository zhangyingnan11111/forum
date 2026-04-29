<template>
  <div class="admin">
    <el-card>
      <template #header>
        <h2>管理后台</h2>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="users">
          <div class="admin-users">
            <div class="admin-search">
              <el-input
                  v-model="userKeyword"
                  placeholder="搜索用户"
                  style="width: 300px"
                  clearable
                  @keyup.enter="fetchUsers"
              >
                <template #append>
                  <el-button @click="fetchUsers">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>

            <el-table :data="users" v-loading="usersLoading" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="{ row }">
                  <el-select v-model="row.role" size="small" @change="updateUserRole(row)">
                    <el-option label="普通用户" value="user" />
                    <el-option label="版主" value="moderator" />
                    <el-option label="管理员" value="admin" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                      v-model="row.status"
                      :active-value="1"
                      :inactive-value="0"
                      @change="updateUserStatus(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="注册时间" width="180" />
            </el-table>

            <div class="pagination">
              <el-pagination
                  v-model:current-page="userPageNum"
                  :total="userTotal"
                  :page-size="userPageSize"
                  layout="prev, pager, next"
                  @current-change="fetchUsers"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="帖子管理" name="posts">
          <div class="admin-posts">
            <div class="admin-search">
              <el-input
                  v-model="postKeyword"
                  placeholder="搜索帖子"
                  style="width: 300px"
                  clearable
                  @keyup.enter="fetchAdminPosts"
              >
                <template #append>
                  <el-button @click="fetchAdminPosts">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>

            <el-table :data="adminPosts" v-loading="postsLoading" stripe>
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column prop="authorName" label="作者" width="120" />
              <el-table-column prop="viewCount" label="浏览" width="80" />
              <el-table-column prop="commentCount" label="评论" width="80" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="viewPost(row.id)">
                    查看
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDeletePost(row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="batch-actions" v-if="selectedPosts.length > 0">
              <el-button type="danger" @click="batchDeletePosts">
                批量删除 ({{ selectedPosts.length }})
              </el-button>
            </div>

            <div class="pagination">
              <el-pagination
                  v-model:current-page="postPageNum"
                  :total="postTotal"
                  :page-size="postPageSize"
                  layout="prev, pager, next"
                  @current-change="fetchAdminPosts"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="统计信息" name="stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">用户总数</div>
                <div class="stat-number">{{ stats.userCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">帖子总数</div>
                <div class="stat-number">{{ stats.postCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">今日新增</div>
                <div class="stat-number">{{ stats.todayNewUsers || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-title">今日帖子</div>
                <div class="stat-number">{{ stats.todayNewPosts || 0 }}</div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('users')

// 用户管理
const users = ref([])
const usersLoading = ref(false)
const userPageNum = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userKeyword = ref('')

// 帖子管理
const adminPosts = ref([])
const postsLoading = ref(false)
const postPageNum = ref(1)
const postPageSize = ref(10)
const postTotal = ref(0)
const postKeyword = ref('')
const selectedPosts = ref([])

// 统计
const stats = ref({
  userCount: 0,
  postCount: 0,
  todayNewUsers: 0,
  todayNewPosts: 0
})

// 这些API需要后端实现，这里先模拟
const fetchUsers = async () => {
  usersLoading.value = true
  try {
    // 调用后端API获取用户列表
    // const res = await getUsers(...)
    // users.value = res.data.records
    // userTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    usersLoading.value = false
  }
}

const fetchAdminPosts = async () => {
  postsLoading.value = true
  try {
    // 调用后端API获取帖子列表
    // const res = await getAdminPosts(...)
    // adminPosts.value = res.data.records
    // postTotal.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    postsLoading.value = false
  }
}

const updateUserRole = async (user) => {
  try {
    // await updateUserRoleApi(user.id, user.role)
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const updateUserStatus = async (user) => {
  try {
    // await updateUserStatusApi(user.id, user.status)
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const viewPost = (postId) => {
  router.push(`/post/${postId}`)
}

const handleDeletePost = async (postId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // await adminDeletePost(postId)
    ElMessage.success('删除成功')
    fetchAdminPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const batchDeletePosts = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedPosts.value.length} 个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // await batchDeletePostsApi(selectedPosts.value)
    ElMessage.success('批量删除成功')
    selectedPosts.value = []
    fetchAdminPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
  fetchAdminPosts()
})
</script>

<style scoped>
.admin {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-search {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.batch-actions {
  margin-top: 16px;
  text-align: right;
}

.stat-card {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}
</style>