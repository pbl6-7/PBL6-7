<template>
  <div v-loading="loading" class="my-subscriptions-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">我的订阅</span>
      </template>
    </el-page-header>

    <div v-if="subscriptions.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无订阅活动">
        <el-button type="primary" @click="router.push('/')">去浏览活动</el-button>
      </el-empty>
    </div>

    <div v-else class="subscription-list">
      <el-card
        v-for="item in subscriptions"
        :key="item.activityId"
        class="subscription-card"
        @click="goToActivity(item.activityId)"
      >
        <div class="card-content">
          <div class="activity-info">
            <h3 class="activity-title">{{ item.activityTitle || '活动详情' }}</h3>
            <div class="activity-meta">
              <span class="meta-item">
                <el-icon><LocationInformation /></el-icon>
                {{ item.activityLocation || '未知地点' }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(item.activityStartTime) }}
              </span>
            </div>
            <div class="subscription-time">
              订阅时间：{{ formatTime(item.createTime) }}
            </div>
          </div>
          <el-button type="primary" @click.stop="handleUnsubscribe(item)">
            取消订阅
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-if="subscriptions.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMySubscriptions, cancelSubscription } from '@/api/subscription'
import type { SubscriptionDetail } from '@/types/subscription'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, LocationInformation } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const subscriptions = ref<SubscriptionDetail[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadSubscriptions = async () => {
  loading.value = true
  try {
    const res = await getMySubscriptions()
    subscriptions.value = res.data
    total.value = res.data.length
  } catch {
    ElMessage.error('加载订阅列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadSubscriptions()
}

const handleUnsubscribe = async (item: SubscriptionDetail) => {
  try {
    await ElMessageBox.confirm('确定要取消订阅这个活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelSubscription(item.activityId)
    ElMessage.success('取消订阅成功')
    loadSubscriptions()
  } catch {
    // 用户取消
  }
}

const goToActivity = (activityId: number) => {
  router.push(`/activity/${activityId}`)
}

const formatDate = (dateStr: string | undefined | null) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadSubscriptions()
})
</script>

<style scoped>
.my-subscriptions-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.empty-state {
  margin-top: 60px;
  text-align: center;
}

.subscription-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.subscription-card {
  cursor: pointer;
  transition: all 0.3s;
}

.subscription-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-info {
  flex: 1;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.activity-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.subscription-time {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
