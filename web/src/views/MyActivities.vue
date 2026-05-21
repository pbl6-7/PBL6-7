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
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/activity/${row.id}`)">查看</el-button>
            <el-button type="primary" link @click="router.push(`/edit-activity/${row.id}`)">编辑</el-button>
            <el-button type="primary" link @click="router.push(`/activity-registrations/${row.id}`)">报名</el-button>
            <el-button type="success" link @click="openPhotoManager(row.id, row.title)">
              <el-icon><Image /></el-icon>相册
            </el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <Teleport to="body">
      <el-dialog 
        v-model="showPhotoModal" 
        :title="`相册管理 - ${currentActivityTitle}`" 
        width="80%"
        @close="closePhotoModal"
      >
        <div class="photo-manager">
          <div class="upload-section">
            <el-button 
              type="primary" 
              :loading="uploadLoading"
              @click="document.getElementById('photo-upload')?.click()"
            >
              <el-icon><Plus /></el-icon>
              上传照片
            </el-button>
            <input 
              id="photo-upload"
              type="file" 
              multiple 
              accept="image/*" 
              style="display: none"
              @change="handlePhotoUpload"
            />
          </div>
          <div v-if="photos.length === 0" class="no-photos">
            暂无照片，请上传活动照片
          </div>
          <div v-else class="photo-grid">
            <div v-for="photo in photos" :key="photo.id" class="photo-item">
              <img :src="photo.photoUrl" :alt="photo.photoName" />
              <div class="photo-actions">
                <el-button type="danger" size="small" icon="Trash" @click="handlePhotoDelete(photo.id)" />
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyActivities, deleteActivity } from '@/api/activity'
import { getActivityPhotos, uploadPhoto, deletePhoto } from '@/api/photo'
import type { Activity } from '@/types/activity'
import type { ActivityPhoto } from '@/types/activity'
import { Plus, Image } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const activities = ref<Activity[]>([])

const showPhotoModal = ref(false)
const currentActivityId = ref<number>(0)
const currentActivityTitle = ref('')
const photos = ref<ActivityPhoto[]>([])
const uploadLoading = ref(false)

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

const openPhotoManager = async (activityId: number, title: string) => {
  currentActivityId.value = activityId
  currentActivityTitle.value = title
  showPhotoModal.value = true
  await loadPhotos(activityId)
}

const loadPhotos = async (activityId: number) => {
  try {
    const res = await getActivityPhotos(activityId)
    photos.value = res.data
  } catch {
    ElMessage.error('加载相册失败')
  }
}

const handlePhotoUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  uploadLoading.value = true
  try {
    for (const file of Array.from(files)) {
      await uploadPhoto(currentActivityId.value, file)
    }
    ElMessage.success('照片上传成功')
    await loadPhotos(currentActivityId.value)
  } catch {
    ElMessage.error('照片上传失败')
  } finally {
    uploadLoading.value = false
    target.value = ''
  }
}

const handlePhotoDelete = async (photoId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这张照片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePhoto(photoId)
    ElMessage.success('照片删除成功')
    photos.value = photos.value.filter(p => p.id !== photoId)
  } catch {
    // 用户取消
  }
}

const closePhotoModal = () => {
  showPhotoModal.value = false
  currentActivityId.value = 0
  currentActivityTitle.value = ''
  photos.value = []
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

.photo-manager {
  padding: 10px 0;
}

.upload-section {
  margin-bottom: 20px;
}

.no-photos {
  text-align: center;
  padding: 60px;
  color: #909399;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
}

.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}
</style>
