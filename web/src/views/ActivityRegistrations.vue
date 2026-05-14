<template>
  <div class="activity-registrations-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">报名管理</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading" class="content-card">
      <el-table :data="registrations" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" />
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
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="updateStatus(row.id, 'APPROVED')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="updateStatus(row.id, 'REJECTED')"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              type="warning"
              size="small"
              @click="updateStatus(row.id, 'CANCELLED')"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getActivityRegistrations, updateRegistrationStatus } from '@/api/registration'
import type { Registration } from '@/types/registration'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const registrations = ref<Registration[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const loadRegistrations = async () => {
  loading.value = true
  try {
    const activityId = Number(route.params.id)
    const res = await getActivityRegistrations(activityId, currentPage.value, pageSize.value)
    registrations.value = res.data.records
    total.value = res.data.total
  } catch {
    ElMessage.error('加载报名列表失败')
  } finally {
    loading.value = false
  }
}

const updateStatus = async (registrationId: number, status: string) => {
  try {
    await updateRegistrationStatus({ registrationId, status })
    ElMessage.success('状态更新成功')
    loadRegistrations()
  } catch {
    ElMessage.error('状态更新失败')
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
.activity-registrations-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.content-card {
  border-radius: 12px;
  margin-top: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
