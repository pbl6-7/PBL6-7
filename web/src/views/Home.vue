<template>
  <div class="home-container">
    <div class="search-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索活动..."
          size="large"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @input="handleSearchInput"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <div v-if="showSuggestions && suggestions.length > 0" class="suggestions">
          <div
            v-for="item in suggestions"
            :key="item"
            class="suggestion-item"
            @click="handleSuggestionClick(item)"
          >
            <el-icon><Search /></el-icon>
            {{ item }}
          </div>
        </div>
      </div>
      <div class="hot-searches">
        <span class="label">热门搜索：</span>
        <el-tag
          v-for="item in hotSearches"
          :key="item"
          class="hot-tag"
          @click="handleHotSearch(item)"
        >
          {{ item }}
        </el-tag>
      </div>
    </div>

    <div class="filter-section">
      <el-select
        v-model="filterStatus"
        placeholder="活动状态"
        clearable
        size="default"
        @change="loadActivities"
      >
        <el-option label="全部" value="" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-date-picker
        v-model="filterDateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        size="default"
        value-format="YYYY-MM-DD"
        @change="loadActivities"
      />
    </div>

    <div v-loading="loading" class="activity-list">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动" />
      <div v-else class="activity-grid">
        <el-card
          v-for="item in activities"
          :key="item.id"
          class="activity-card"
          :body-style="{ padding: '0px' }"
          shadow="hover"
          @click="goToDetail(item.id)"
        >
          <div class="card-header">
            <el-tag :type="getStatusType(item.status)">{{ getStatusLabel(item.status) }}</el-tag>
            <el-tag v-if="item.approvalStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="item.approvalStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </div>
          <div class="card-body">
            <h3 class="activity-title">{{ item.title }}</h3>
            <div class="activity-info">
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.startTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ item.location }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ item.publisherName }}</span>
              </div>
            </div>
            <div class="activity-description">{{ item.description }}</div>
          </div>
          <div class="card-footer">
            <span class="participants">
              <el-icon><UserFilled /></el-icon>
              {{ item.currentParticipants || 0 }}/{{ item.maxParticipants }}人
            </span>
            <el-button type="primary" size="small" link>查看详情</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 12, 16, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadActivities"
        @current-change="loadActivities"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityList } from '@/api/activity'
import { getSearchSuggestions, getHotSearches } from '@/api/search'
import type { Activity } from '@/types/activity'
import { Search, Calendar, LocationInformation, User, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const activities = ref<Activity[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const searchKeyword = ref('')
const filterStatus = ref('')
const filterDateRange = ref<string[]>([])

const suggestions = ref<string[]>([])
const hotSearches = ref<string[]>([])
const showSuggestions = ref(false)

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getActivityList({
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined,
      startDate: filterDateRange.value?.[0],
      endDate: filterDateRange.value?.[1],
      page: currentPage.value,
      size: pageSize.value
    })
    activities.value = res.data.records
    total.value = res.data.total
  } catch {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const loadHotSearches = async () => {
  try {
    const res = await getHotSearches()
    hotSearches.value = res.data
  } catch (error) {
    console.error('加载热门搜索失败', error)
  }
}

const handleSearch = () => {
  showSuggestions.value = false
  currentPage.value = 1
  loadActivities()
}

const handleSearchInput = async () => {
  if (searchKeyword.value.length > 0) {
    try {
      const res = await getSearchSuggestions(searchKeyword.value)
      suggestions.value = res.data.suggestions
      showSuggestions.value = suggestions.value.length > 0
    } catch {
      suggestions.value = []
    }
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
}

const handleSuggestionClick = (item: string) => {
  searchKeyword.value = item
  showSuggestions.value = false
  handleSearch()
}

const handleHotSearch = (item: string) => {
  searchKeyword.value = item
  handleSearch()
}

const goToDetail = (id: number) => {
  router.push(`/activity/${id}`)
}

const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    PUBLISHED: 'success',
    DRAFT: 'info',
    CANCELLED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PUBLISHED: '已发布',
    DRAFT: '草稿',
    CANCELLED: '已取消'
  }
  return labels[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadActivities()
  loadHotSearches()
  
  document.addEventListener('click', () => {
    showSuggestions.value = false
  })
})
</script>

<style scoped>
.home-container {
  padding: 20px 0;
}

.search-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.search-box {
  position: relative;
  max-width: 600px;
  margin: 0 auto 16px;
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 100;
  margin-top: 4px;
  overflow: hidden;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.suggestion-item:hover {
  background: #f5f7fa;
}

.hot-searches {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-searches .label {
  font-size: 14px;
  color: #909399;
}

.hot-tag {
  cursor: pointer;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.activity-list {
  min-height: 400px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.activity-card {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
}

.card-body {
  padding: 16px;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.info-item .el-icon {
  color: #909399;
}

.activity-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.participants {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
