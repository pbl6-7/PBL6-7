<template>
  <div class="registrations-container">
    <div class="header">
      <h2>我的报名</h2>
    </div>

    <div v-loading="loading" class="registration-list">
      <el-empty v-if="!loading && registrations.length === 0" description="您还没有报名任何活动" />
      <el-table v-else :data="registrations" style="width: 100%" stripe>
        <el-table-column prop="activityTitle" label="活动名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/activity/${row.activityId}`)">
              {{ row.activityTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="registeredAt" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.registeredAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/activity/${row.activityId}`)">查看</el-button>
            <el-button
              v-if="row.status !== 'CANCELLED'"
              type="danger"
              link
              @click="handleCancel(row.activityId)"
            >
              取消报名
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadRegistrations"
        @current-change="loadRegistrations"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRegistrations, cancelRegistration } from '@/api/registration'
import type { Registration } from '@/types/registration'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const registrations = ref<Registration[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const loadRegistrations = async () => {
  loading.value = true
  try {
    const res = await getMyRegistrations(currentPage.value, pageSize.value)
    registrations.value = res.data.records
    total.value = res.data.total
  } catch {
    ElMessage.error('加载报名记录失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (activityId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelRegistration(activityId)
    ElMessage.success('取消报名成功')
    loadRegistrations()
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
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消'
  }
  return labels[status] || status
}

onMounted(() => {
  loadRegistrations()
})
</script>

<style scoped>
.registrations-container {
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

.registration-list {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  min-height: 400px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
