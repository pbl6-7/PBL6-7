<template>
  <div class="collections-container">
    <div class="header">
      <h2>我的收藏</h2>
    </div>

    <div v-loading="loading" class="activity-list">
      <el-empty v-if="!loading && activities.length === 0" description="您还没有收藏任何活动" />
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
            </div>
          </div>
          <div class="card-footer">
            <el-button type="danger" size="small" link @click.stop="handleCancelCollect(item.id)">
              取消收藏
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCollections, cancelCollect } from '@/api/collect'
import { getActivityDetail } from '@/api/activity'
import type { Activity } from '@/types/activity'
import { Calendar, LocationInformation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const activities = ref<Activity[]>([])

const loadCollections = async () => {
  loading.value = true
  try {
    const res = await getMyCollections()
    const activityPromises = res.data.map((item: any) => getActivityDetail(item.activityId))
    const activityResults = await Promise.all(activityPromises)
    activities.value = activityResults.map((res) => res.data)
  } catch {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleCancelCollect = async (activityId: number) => {
  try {
    await cancelCollect(activityId)
    ElMessage.success('取消收藏成功')
    loadCollections()
  } catch {
    ElMessage.error('取消收藏失败')
  }
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
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

onMounted(() => {
  loadCollections()
})
</script>

<style scoped>
.collections-container {
  padding: 20px 0;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.activity-list {
  min-height: 400px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.activity-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
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

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
