<template>
  <div class="home">
    <!-- 分类导航 -->
    <div class="category-nav">
      <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :name="category.id.toString()">
        </el-tab-pane>
      </el-tabs>

      <div class="sort-buttons">
        <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
          <el-radio-button value="createTime">最新</el-radio-button>
          <el-radio-button value="viewCount">最热</el-radio-button>
          <el-radio-button value="commentCount">最多回复</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list" v-loading="loading">
      <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
          @refresh="fetchPosts"
      />

      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 快速发帖按钮 -->
    <el-button
        v-if="userStore.isLoggedIn"
        class="fab"
        type="primary"
        circle
        size="large"
        @click="router.push('/post/create')"
    >
      <el-icon><Edit /></el-icon>
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPostList, getCategories } from '@/api/post'
import PostCard from '@/components/PostCard.vue'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const posts = ref([])
const categories = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeCategory = ref('all')
const sortBy = ref('createTime')

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类失败')
  }
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      order: 'DESC'
    }

    if (activeCategory.value !== 'all') {
      params.categoryId = activeCategory.value
    }

    console.log('请求参数:', params)  // 添加日志

    const res = await getPostList(params)
    console.log('响应数据:', res)  // 添加日志

    if (res.code === 200) {
      posts.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载失败：' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  pageNum.value = 1
  fetchPosts()
}

const handleSortChange = () => {
  pageNum.value = 1
  fetchPosts()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  fetchPosts()
}

onMounted(() => {
  fetchCategories()
  fetchPosts()
})
</script>

<style scoped>
.home {
  max-width: 1000px;
  margin: 0 auto;
}

.category-nav {
  background: white;
  border-radius: 8px;
  padding: 0 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.sort-buttons {
  padding: 10px 0;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

@media (max-width: 768px) {
  .category-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .fab {
    bottom: 20px;
    right: 20px;
    width: 48px;
    height: 48px;
  }
}
</style>