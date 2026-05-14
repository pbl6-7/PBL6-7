<template>
  <div class="my-activities-container">
    <div class="header">
      <h2>我的活动</h2>
      <el-button type="primary" @click="router.push('/publish')">
        <el-icon><Plus /></el-icon>
        发布新活动
      </el-button>
    </div>

    <div v-loading="loading" class="activity-list">
      <el-empty v-if="!loading && activities.length === 0" description="您还没有发布任何活动" />
      <el-table v-else :data="activities" style="width: 100%" stripe>
        <el-table-column prop="title" label="活动名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/activity/${row.id}`)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="活动地点" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.approvalStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.approvalStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxParticipants" label="人数" width="100">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }}/{{ row.maxParticipants }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/activity/${row.id}`)">查看</el-button>
            <el-button type="primary" link @click="router.push(`/edit-activity/${row.id}`)">编辑</el-button>
            <el-button type="primary" link @click="router.push(`/activity-registrations/${row.id}`)">报名</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyActivities, deleteActivity } from '@/api/activity'
import type { Activity } from '@/types/activity'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const activities = ref<Activity[]>([])

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getMyActivities()
    activities.value = res.data
  } catch {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteActivity(id)
    ElMessage.success('删除成功')
    loadActivities()
  } catch {
    // 用户取消
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.my-activities-container {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.activity-list {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
