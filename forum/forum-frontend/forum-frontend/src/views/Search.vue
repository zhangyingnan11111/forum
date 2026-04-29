<template>
  <div class="search">
    <el-card>
      <template #header>
        <div class="search-header">
          <h2>搜索结果</h2>
          <div class="search-info">
            找到约 {{ total }} 条结果，关键词：<strong>{{ keyword }}</strong>
          </div>
        </div>
      </template>

      <div class="search-results" v-loading="loading">
        <PostCard
            v-for="post in posts"
            :key="post.id"
            :post="post"
        />

        <el-empty v-if="!loading && posts.length === 0" description="没有找到相关帖子" />

        <div class="pagination" v-if="total > 0">
          <el-pagination
              v-model:current-page="pageNum"
              :total="total"
              :page-size="pageSize"
              layout="prev, pager, next"
              @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchPosts } from '@/api/post'
import PostCard from '@/components/PostCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const posts = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const keyword = ref('')

const fetchSearchResults = async () => {
  loading.value = true
  try {
    const res = await searchPosts({
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      posts.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  fetchSearchResults()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由参数变化
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword
    pageNum.value = 1
    fetchSearchResults()
  }
}, { immediate: true })

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    fetchSearchResults()
  }
})
</script>

<style scoped>
.search {
  max-width: 1000px;
  margin: 0 auto;
}

.search-header h2 {
  margin: 0 0 10px 0;
}

.search-info {
  color: #666;
  font-size: 14px;
}

.search-results {
  min-height: 400px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>