<template>
  <div class="data-statistics-container">
    <div class="page-header">
      <h2>数据统计</h2>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="20" class="statistics-section">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>活动统计</span>
            </div>
          </template>
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-value">{{ activityStats.total || 0 }}</div>
              <div class="stat-label">总活动数</div>
            </div>
            <div class="stat-item pending">
              <div class="stat-value">{{ activityStats.pending || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
            <div class="stat-item approved">
              <div class="stat-value">{{ activityStats.approved || 0 }}</div>
              <div class="stat-label">已通过</div>
            </div>
            <div class="stat-item rejected">
              <div class="stat-value">{{ activityStats.rejected || 0 }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>用户统计</span>
            </div>
          </template>
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-value">{{ userStats.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <div class="stat-item admins">
              <div class="stat-value">{{ userStats.admins || 0 }}</div>
              <div class="stat-label">管理员</div>
            </div>
            <div class="stat-item publishers">
              <div class="stat-value">{{ userStats.publishers || 0 }}</div>
              <div class="stat-label">发布者</div>
            </div>
            <div class="stat-item users">
              <div class="stat-value">{{ userStats.regularUsers || 0 }}</div>
              <div class="stat-label">普通用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="statistics-section">
      <el-col :span="24">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>报名统计</span>
            </div>
          </template>
          <div class="stat-grid horizontal">
            <div class="stat-item">
              <div class="stat-value">{{ registrationStats.totalRegistrations || 0 }}</div>
              <div class="stat-label">总报名数</div>
            </div>
            <div class="stat-item week">
              <div class="stat-value">{{ registrationStats.registrations7Days || 0 }}</div>
              <div class="stat-label">本周报名</div>
            </div>
            <div class="stat-item month">
              <div class="stat-value">{{ registrationStats.registrations30Days || 0 }}</div>
              <div class="stat-label">本月报名</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="statistics-section">
      <el-col :span="24">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>本周概览</span>
            </div>
          </template>
          <div class="overview-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="overview-item">
                  <div class="overview-icon activities">
                    <el-icon><Calendar /></el-icon>
                  </div>
                  <div class="overview-info">
                    <div class="overview-value">{{ dailyStats.activitiesThisWeek || 0 }}</div>
                    <div class="overview-label">本周新增活动</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="overview-item">
                  <div class="overview-icon registrations">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="overview-info">
                    <div class="overview-value">{{ dailyStats.registrationsThisWeek || 0 }}</div>
                    <div class="overview-label">本周报名数</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="overview-item">
                  <div class="overview-icon users">
                    <el-icon><Avatar /></el-icon>
                  </div>
                  <div class="overview-info">
                    <div class="overview-value">{{ dailyStats.newUsersThisWeek || 0 }}</div>
                    <div class="overview-label">本周新用户</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Calendar, User, Avatar, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const activityStats = ref<any>({})
const userStats = ref<any>({})
const registrationStats = ref<any>({})
const dailyStats = ref<any>({})

const loadActivityStatistics = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/statistics/activities')
    activityStats.value = res.data
  } catch (error) {
    console.error('加载活动统计失败', error)
  }
}

const loadUserStatistics = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/statistics/users')
    userStats.value = res.data
  } catch (error) {
    console.error('加载用户统计失败', error)
  }
}

const loadRegistrationStatistics = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/statistics/registrations')
    registrationStats.value = res.data
  } catch (error) {
    console.error('加载报名统计失败', error)
  }
}

const loadDailyStatistics = async () => {
  try {
    const res = await request.get<any, { data: any }>('/admin/statistics/daily')
    dailyStats.value = res.data
  } catch (error) {
    console.error('加载每日统计失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadActivityStatistics(),
      loadUserStatistics(),
      loadRegistrationStatistics(),
      loadDailyStatistics()
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
.data-statistics-container {
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

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-grid.horizontal {
  grid-template-columns: repeat(3, 1fr);
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-item.pending .stat-value {
  color: #e6a23c;
}

.stat-item.approved .stat-value {
  color: #67c23a;
}

.stat-item.rejected .stat-value {
  color: #f56c6c;
}

.stat-item.admins .stat-value {
  color: #f56c6c;
}

.stat-item.publishers .stat-value {
  color: #e6a23c;
}

.stat-item.users .stat-value {
  color: #409eff;
}

.stat-item.week .stat-value {
  color: #67c23a;
}

.stat-item.month .stat-value {
  color: #409eff;
}

.overview-content {
  padding: 20px 0;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.overview-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.overview-icon.activities {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.overview-icon.registrations {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.overview-icon.users {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #909399;
}
</style>
