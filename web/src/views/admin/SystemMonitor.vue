<template>
  <div class="system-monitor-container">
    <div class="page-header">
      <h2>系统监控</h2>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activities">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ systemStatus.totalActivities || 0 }}</div>
              <div class="stat-label">总活动数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ systemStatus.pendingActivities || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon registrations">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ systemStatus.totalRegistrations || 0 }}</div>
              <div class="stat-label">总报名数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ systemStatus.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="metrics-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">本周新增活动</div>
            <div class="metric-value">{{ metrics.activitiesLast7Days || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">本周新增报名</div>
            <div class="metric-value">{{ metrics.registrationsLast7Days || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">本周新注册用户</div>
            <div class="metric-value">{{ metrics.newUsersLast7Days || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="recent-data">
      <el-col :span="12">
        <el-card shadow="hover" class="recent-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-table :data="recentActivities" v-loading="loading" stripe max-height="400">
            <el-table-column prop="title" label="活动名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="approvalStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getApprovalStatusType(row.approvalStatus)">
                  {{ getApprovalStatusLabel(row.approvalStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="recent-card">
          <template #header>
            <div class="card-header">
              <span>最近用户</span>
            </div>
          </template>
          <el-table :data="recentUsers" v-loading="loading" stripe max-height="400">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Calendar, Clock, User, Avatar, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const systemStatus = ref<any>({})
const metrics = ref<any>({})
const recentActivities = ref<any[]>([])
const recentUsers = ref<any[]>([])

const getApprovalStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return labels[status] || status
}

const getApprovalStatusType = (status: string) => {
  const types: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

const getRoleLabel = (role: string) => {
  const labels: Record<string, string> = {
    user: '普通用户',
    publisher: '发布者',
    admin: '管理员'
  }
  return labels[role] || role
}

const getRoleTagType = (role: string) => {
  const types: Record<string, any> = {
    user: 'info',
    publisher: 'warning',
    admin: 'danger'
  }
  return types[role] || 'info'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const loadSystemStatus = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/monitor/status')
    systemStatus.value = res.data
  } catch (error) {
    console.error('加载系统状态失败', error)
  }
}

const loadMetrics = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/monitor/metrics')
    metrics.value = res.data
  } catch (error) {
    console.error('加载系统指标失败', error)
  }
}

const loadRecentActivities = async () => {
  try {
    const res = await request.get<any, { data: any[] }>('/admin/monitor/recent-activities')
    recentActivities.value = res.data
  } catch (error) {
    console.error('加载最近活动失败', error)
  }
}

const loadRecentUsers = async () => {
  try {
    const res = await request.get<any, { data: any[] }>('/admin/monitor/recent-users')
    recentUsers.value = res.data
  } catch (error) {
    console.error('加载最近用户失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadSystemStatus(),
      loadMetrics(),
      loadRecentActivities(),
      loadRecentUsers()
    ])
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-monitor-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.status-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.activities {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.registrations {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.users {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.metrics-cards {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 8px;
}

.metric-content {
  text-align: center;
  padding: 20px 0;
}

.metric-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.metric-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}

.recent-data {
  margin-bottom: 20px;
}

.recent-card {
  border-radius: 8px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
