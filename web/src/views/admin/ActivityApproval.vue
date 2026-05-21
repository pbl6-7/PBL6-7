<template>
  <div class="activity-approval-container">
    <div class="page-header">
      <h2>活动审核</h2>
      <div class="header-actions">
        <el-select v-model="filterStatus" placeholder="审核状态" clearable size="default" @change="loadActivities">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索活动"
          :prefix-icon="Search"
          clearable
          @input="loadActivities"
          style="width: 300px"
        />
      </div>
    </div>

    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总活动数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.pending || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card approved">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card rejected">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.rejected || 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="activity-table-card">
      <el-table :data="activityList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布者" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="活动地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="approvalStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ getApprovalStatusLabel(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleApprove(row)" v-if="row.approvalStatus === 'PENDING'">通过</el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)" v-if="row.approvalStatus === 'PENDING'">拒绝</el-button>
            <el-button type="primary" link size="small" @click="goToDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadActivities"
          @current-change="loadActivities"
        />
      </div>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="活动名称">
          <span>{{ currentActivity?.title }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReject" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getActivitiesByApprovalStatus,
  approveActivity,
  rejectActivity,
  getApprovalStatistics,
  type UserItem
} from '@/api/admin'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Activity } from '@/types/activity'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const activityList = ref<Activity[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchKeyword = ref('')
const filterStatus = ref('pending')
const statistics = ref<any>({})

const rejectDialogVisible = ref(false)
const currentActivity = ref<Activity | null>(null)
const rejectForm = ref({
  reason: ''
})

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

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getActivitiesByApprovalStatus(filterStatus.value || '')
    activityList.value = res.data
    total.value = res.data.length
  } catch {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await getApprovalStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleApprove = async (activity: Activity) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过活动"${activity.title}"的审核吗？`,
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    await approveActivity(activity.id)
    ElMessage.success('活动审核通过')
    loadActivities()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = (activity: Activity) => {
  currentActivity.value = activity
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

const handleSubmitReject = async () => {
  if (!currentActivity.value) return

  try {
    await ElMessageBox.confirm(
      `确定要拒绝活动"${currentActivity.value.title}"吗？`,
      '确认拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitLoading.value = true
    await rejectActivity(currentActivity.value.id, rejectForm.value.reason)
    ElMessage.success('活动已拒绝')
    rejectDialogVisible.value = false
    loadActivities()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/activity/${id}`)
}

onMounted(() => {
  loadActivities()
  loadStatistics()
})
</script>

<style scoped>
.activity-approval-container {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  text-align: center;
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

.stat-card.pending .stat-value {
  color: #e6a23c;
}

.stat-card.approved .stat-value {
  color: #67c23a;
}

.stat-card.rejected .stat-value {
  color: #f56c6c;
}

.activity-table-card {
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
