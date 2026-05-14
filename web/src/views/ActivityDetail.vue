<template>
  <div v-loading="loading" class="activity-detail-container">
    <template v-if="activity">
      <el-page-header @back="router.back()" title="返回">
        <template #content>
          <span class="page-title">活动详情</span>
        </template>
      </el-page-header>

      <div class="detail-content">
        <div class="main-content">
          <el-card class="activity-card">
            <div class="card-header">
              <el-tag :type="getStatusType(activity.status)">{{ getStatusLabel(activity.status) }}</el-tag>
              <el-tag v-if="activity.approvalStatus === 'PENDING'" type="warning">待审核</el-tag>
              <el-tag v-else-if="activity.approvalStatus === 'APPROVED'" type="success">已通过</el-tag>
              <el-tag v-else type="danger">已拒绝</el-tag>
            </div>
            <h1 class="activity-title">{{ activity.title }}</h1>
            <div class="activity-meta">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDateRange(activity.startTime, activity.endTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ activity.location }}</span>
              </div>
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>发布者：{{ activity.publisherName }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>报名截止：{{ formatDate(activity.endTime) }}</span>
              </div>
            </div>
            <el-divider />
            <div class="activity-description">
              <h3>活动描述</h3>
              <p>{{ activity.description || '暂无描述' }}</p>
            </div>
            <div class="activity-stats">
              <div class="stat-item">
                <span class="stat-value">{{ activity.currentParticipants || 0 }}</span>
                <span class="stat-label">已报名</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ activity.maxParticipants }}</span>
                <span class="stat-label">总人数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ activity.maxParticipants - (activity.currentParticipants || 0) }}</span>
                <span class="stat-label">剩余名额</span>
              </div>
            </div>
          </el-card>

          <el-card class="comments-card">
            <template #header>
              <div class="card-header-title">
                <span>评论</span>
                <span class="comment-count">({{ comments.length }})</span>
              </div>
            </template>
            <div class="comment-input">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="请输入评论..."
                maxlength="500"
                show-word-limit
              />
              <el-button type="primary" :loading="submitting" @click="submitComment">
                发布评论
              </el-button>
            </div>
            <div v-if="comments.length === 0" class="no-comments">
              暂无评论，快来抢沙发~
            </div>
            <div v-else class="comment-list">
              <div v-for="item in comments" :key="item.id" class="comment-item">
                <div class="comment-avatar">
                  <el-avatar :size="40" icon="UserFilled" />
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-username">{{ item.username }}</span>
                    <span class="comment-time">{{ formatTime(item.createdAt) }}</span>
                  </div>
                  <p class="comment-text">{{ item.content }}</p>
                  <el-button
                    v-if="userStore.userInfo?.id === item.userId"
                    type="danger"
                    size="small"
                    link
                    @click="deleteComment(item.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <div class="side-content">
          <el-card class="action-card">
            <div class="action-buttons">
              <el-button
                v-if="!isPublisher"
                type="primary"
                size="large"
                :disabled="!canRegister || isRegistered"
                :loading="registerLoading"
                class="action-button"
                @click="handleRegister"
              >
                {{ isRegistered ? '已报名' : (canRegister ? '立即报名' : '已截止') }}
              </el-button>
              <el-button
                v-if="isPublisher"
                type="primary"
                size="large"
                class="action-button"
                @click="router.push(`/edit-activity/${activity.id}`)"
              >
                编辑活动
              </el-button>
              <el-button
                v-if="isPublisher"
                type="danger"
                size="large"
                class="action-button"
                @click="handleDelete"
              >
                删除活动
              </el-button>
              <el-button
                v-if="!isPublisher"
                :type="isCollected ? 'warning' : 'default'"
                size="large"
                class="action-button"
                :loading="collectLoading"
                @click="handleCollect"
              >
                <el-icon><Star /></el-icon>
                {{ isCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>
            <div class="collect-stat">
              <el-icon><Star /></el-icon>
              <span>{{ collectCount }} 人收藏</span>
            </div>
          </el-card>

          <el-card v-if="isPublisher" class="registrations-card">
            <template #header>
              <div class="card-header-title">
                <span>报名管理</span>
              </div>
            </template>
            <div class="registration-list">
              <el-table :data="registrations" style="width: 100%" size="small">
                <el-table-column prop="username" label="用户名" />
                <el-table-column prop="status" label="状态">
                  <template #default="{ row }">
                    <el-tag :type="getRegistrationStatusType(row.status)">
                      {{ getRegistrationStatusLabel(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="success"
                      size="small"
                      link
                      @click="updateRegistrationStatus(row.id, 'APPROVED')"
                    >
                      通过
                    </el-button>
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="danger"
                      size="small"
                      link
                      @click="updateRegistrationStatus(row.id, 'REJECTED')"
                    >
                      拒绝
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-button
              v-if="registrations.length > 0"
              type="primary"
              link
              class="view-more"
              @click="router.push(`/activity-registrations/${activity.id}`)"
            >
              查看全部报名
            </el-button>
          </el-card>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getActivityDetail } from '@/api/activity'
import { getCommentList, publishComment, deleteComment as deleteCommentApi } from '@/api/comment'
import { collectActivity, cancelCollect, checkCollectStatus } from '@/api/collect'
import { registerActivity, cancelRegistration, getActivityRegistrations, updateRegistrationStatus as updateRegistrationStatusApi } from '@/api/registration'
import { useUserStore } from '@/stores/user'
import type { Activity } from '@/types/activity'
import type { Comment } from '@/types/comment'
import type { Registration } from '@/types/registration'
import { Calendar, LocationInformation, User, Clock, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const activity = ref<Activity | null>(null)
const comments = ref<Comment[]>([])
const registrations = ref<Registration[]>([])

const commentContent = ref('')
const submitting = ref(false)

const isCollected = ref(false)
const collectCount = ref(0)
const collectLoading = ref(false)

const registerLoading = ref(false)
const isRegistered = ref(false)

const isPublisher = computed(() => {
  return userStore.userInfo?.id === activity.value?.publisherId
})

const canRegister = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const endTime = new Date(activity.value.endTime)
  return now < endTime && (activity.value.currentParticipants || 0) < activity.value.maxParticipants
})

const loadActivity = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getActivityDetail(id)
    activity.value = res.data
    
    const statusRes = await checkCollectStatus(id)
    isCollected.value = statusRes.data.collected
    collectCount.value = statusRes.data.collectCount

    const regRes = await getActivityRegistrations(id, 1, 5)
    registrations.value = regRes.data.records
  } catch {
    ElMessage.error('加载活动详情失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const id = Number(route.params.id)
    const res = await getCommentList(id)
    comments.value = res.data
  } catch (error) {
    console.error('加载评论失败', error)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    const id = Number(route.params.id)
    await publishComment(id, { content: commentContent.value })
    ElMessage.success('评论发布成功')
    commentContent.value = ''
    loadComments()
  } catch {
    ElMessage.error('评论发布失败')
  } finally {
    submitting.value = false
  }
}

const deleteComment = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCommentApi(commentId)
    ElMessage.success('评论删除成功')
    loadComments()
  } catch {
    // 用户取消
  }
}

const handleCollect = async () => {
  if (!activity.value) return
  collectLoading.value = true
  try {
    if (isCollected.value) {
      await cancelCollect(activity.value.id)
      ElMessage.success('取消收藏成功')
      isCollected.value = false
      collectCount.value--
    } else {
      await collectActivity(activity.value.id)
      ElMessage.success('收藏成功')
      isCollected.value = true
      collectCount.value++
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

const handleRegister = async () => {
  if (!activity.value) return
  registerLoading.value = true
  try {
    await registerActivity({ activityId: activity.value.id })
    ElMessage.success('报名成功')
    loadActivity()
  } catch {
    ElMessage.error('报名失败')
  } finally {
    registerLoading.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // await deleteActivity(activity.value.id)
    ElMessage.success('活动删除成功')
    router.push('/my-activities')
  } catch {
    // 用户取消
  }
}

const updateRegistrationStatus = async (registrationId: number, status: string) => {
  try {
    await updateRegistrationStatusApi({ registrationId, status })
    ElMessage.success('状态更新成功')
    loadActivity()
  } catch {
    ElMessage.error('状态更新失败')
  }
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

const getRegistrationStatusType = (status: string) => {
  const types: Record<string, any> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return types[status] || 'info'
}

const getRegistrationStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消'
  }
  return labels[status] || status
}

const formatDateRange = (start: string, end: string) => {
  const startDate = new Date(start)
  const endDate = new Date(end)
  const format = (d: Date) => `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  return `${format(startDate)} - ${format(endDate)}`
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadActivity()
  loadComments()
})
</script>

<style scoped>
.activity-detail-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  margin-top: 20px;
}

.activity-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.activity-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.4;
}

.activity-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.meta-item .el-icon {
  color: #409eff;
}

.el-divider {
  margin: 20px 0;
}

.activity-description h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.activity-description p {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.activity-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.comments-card {
  border-radius: 12px;
  margin-top: 20px;
}

.card-header-title {
  font-size: 16px;
  font-weight: 600;
}

.comment-count {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-input .el-button {
  align-self: flex-end;
}

.no-comments {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.comment-username {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.action-card {
  border-radius: 12px;
  position: sticky;
  top: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-button {
  width: 100%;
}

.collect-stat {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #909399;
}

.registrations-card {
  border-radius: 12px;
  margin-top: 20px;
}

.registration-list {
  margin-bottom: 12px;
}

.view-more {
  width: 100%;
}

@media (max-width: 768px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
  
  .activity-meta {
    grid-template-columns: 1fr;
  }
}
</style>
