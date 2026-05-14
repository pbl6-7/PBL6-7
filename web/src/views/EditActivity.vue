<template>
  <div class="edit-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">编辑活动</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading" class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="activity-form"
      >
        <el-form-item label="活动名称" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入活动名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动地点" prop="location">
          <el-input
            v-model="form.location"
            placeholder="请输入活动地点"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number
            v-model="form.maxParticipants"
            :min="1"
            :max="10000"
          />
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入活动描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" size="large" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button size="large" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getActivityDetail, updateActivity } from '@/api/activity'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(true)
const submitting = ref(false)

const form = reactive({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: 100,
  description: ''
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 50, message: '活动名称长度为2-50个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

const loadActivity = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getActivityDetail(id)
    Object.assign(form, {
      title: res.data.title,
      location: res.data.location,
      startTime: res.data.startTime,
      endTime: res.data.endTime,
      maxParticipants: res.data.maxParticipants,
      description: res.data.description
    })
  } catch {
    ElMessage.error('加载活动信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (new Date(form.endTime) <= new Date(form.startTime)) {
        ElMessage.error('结束时间必须晚于开始时间')
        return
      }
      
      submitting.value = true
      try {
        const id = Number(route.params.id)
        await updateActivity(id, {
          title: form.title,
          location: form.location,
          startTime: form.startTime,
          endTime: form.endTime,
          maxParticipants: form.maxParticipants,
          description: form.description
        })
        ElMessage.success('活动更新成功')
        router.push('/my-activities')
      } catch {
        ElMessage.error('活动更新失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadActivity()
})
</script>

<style scoped>
.edit-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.form-card {
  border-radius: 12px;
  margin-top: 20px;
}

.activity-form {
  max-width: 600px;
  padding: 20px 0;
}

.activity-form :deep(.el-input-number) {
  width: 200px;
}
</style>
