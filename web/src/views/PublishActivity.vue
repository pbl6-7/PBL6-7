<template>
  <div class="publish-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">发布活动</span>
      </template>
    </el-page-header>

    <el-card class="form-card">
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
            :disabled-date="disabledStartDate"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledEndDate"
          />
        </el-form-item>

        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number
            v-model="form.maxParticipants"
            :min="1"
            :max="10000"
            placeholder="请输入最大参与人数"
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
          <el-button type="primary" :loading="loading" size="large" @click="handleSubmit">
            发布活动
          </el-button>
          <el-button size="large" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { publishActivity } from '@/api/activity'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

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
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ]
}

const disabledStartDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledEndDate = (time: Date) => {
  if (!form.startTime) return time.getTime() < Date.now() - 8.64e7
  return time.getTime() < new Date(form.startTime).getTime()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (new Date(form.endTime) <= new Date(form.startTime)) {
        ElMessage.error('结束时间必须晚于开始时间')
        return
      }
      
      loading.value = true
      try {
        await publishActivity({
          title: form.title,
          location: form.location,
          startTime: form.startTime,
          endTime: form.endTime,
          maxParticipants: form.maxParticipants,
          description: form.description
        })
        ElMessage.success('活动发布成功')
        router.push('/my-activities')
      } catch {
        ElMessage.error('活动发布失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.publish-container {
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
