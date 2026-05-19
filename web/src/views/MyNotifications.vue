<template>
  <div v-loading="loading" class="my-notifications-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">我的通知</span>
      </template>
    </el-page-header>

    <div v-if="notifications.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无通知" />
    </div>

    <div v-else class="notification-list">
      <el-card
        v-for="item in notifications"
        :key="item.id"
        :class="['notification-card', { 'is-read': item.isRead }]"
        @click="handleNotificationClick(item)"
      >
        <div class="card-header">
          <el-tag :type="getNotificationType(item.type)" size="small">
            {{ getNotificationTypeLabel(item.type) }}
          </el-tag>
          <span class="notification-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div class="notification-content">
          <h4 class="notification-title">{{ item.activityTitle }}</h4>
          <p class="notification-text">{{ item.content }}</p>
        </div>
        <div class="card-footer">
          <el-button
            v-if="!item.isRead"
            type="primary"
            size="small"
            @click.stop="handleMarkAsRead(item.id)"
          >
            标记已读
          </el-button>
          <el-button
            type="default"
            size="small"
            @click.stop="goToActivity(item.activityId)"
          >
            查看活动
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-if="notifications.length > 0" class="pagination-container">
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
import { getMyNotifications, markAsRead } from '@/api/notification'
import type { Notification } from '@/types/notification'
import { NotificationType } from '@/types/notification'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const notifications = ref<Notification[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await getMyNotifications(currentPage.value, pageSize.value)
    notifications.value = res.data.records
    total.value = res.data.total
  } catch {
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadNotifications()
}

const handleNotificationClick = (item: Notification) => {
  if (!item.isRead) {
    handleMarkAsRead(item.id)
  }
  goToActivity(item.activityId)
}

const handleMarkAsRead = async (notificationId: number) => {
  try {
    await markAsRead(notificationId)
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = true
    }
  } catch {
    ElMessage.error('标记已读失败')
  }
}

const goToActivity = (activityId: number) => {
  router.push(`/activity/${activityId}`)
}

const getNotificationType = (type: string) => {
  const typeMap: Record<string, any> = {
    [NotificationType.SUBSCRIPTION_STATUS]: 'info',
    [NotificationType.ACTIVITY_UPDATE]: 'warning',
    [NotificationType.ACTIVITY_START]: 'success',
    [NotificationType.ACTIVITY_END]: 'default'
  }
  return typeMap[type] || 'info'
}

const getNotificationTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    [NotificationType.SUBSCRIPTION_STATUS]: '订阅状态',
    [NotificationType.ACTIVITY_UPDATE]: '活动更新',
    [NotificationType.ACTIVITY_START]: '活动开始',
    [NotificationType.ACTIVITY_END]: '活动结束'
  }
  return labelMap[type] || '通知'
}

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.my-notifications-container {
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

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.notification-card {
  transition: all 0.3s;
}

.notification-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-card.is-read {
  opacity: 0.7;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-content {
  margin-bottom: 12px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
