<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="header">
        <div class="logo">
          <el-icon><School /></el-icon>
        </div>
        <h1>校园活动平台</h1>
        <p>找回密码</p>
      </div>
      
      <div v-if="step === 1" class="step-content">
        <el-form
          ref="formRef"
          :model="step1Form"
          :rules="step1Rules"
          @submit.prevent="handleVerifyUsername"
        >
          <el-form-item prop="username">
            <el-input
              v-model="step1Form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="step-button"
              @click="handleVerifyUsername"
            >
              下一步
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="step === 2" class="step-content">
        <div class="security-question">
          <el-icon><QuestionFilled /></el-icon>
          <span>{{ securityQuestion }}</span>
        </div>
        <el-form
          ref="formRef2"
          :model="step2Form"
          :rules="step2Rules"
          @submit.prevent="handleVerifyAnswer"
        >
          <el-form-item prop="securityAnswer">
            <el-input
              v-model="step2Form.securityAnswer"
              placeholder="请输入密保答案"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="step-button"
              @click="handleVerifyAnswer"
            >
              下一步
            </el-button>
          </el-form-item>
        </el-form>
        <el-button text @click="step = 1">返回上一步</el-button>
      </div>

      <div v-else class="step-content">
        <el-form
          ref="formRef3"
          :model="step3Form"
          :rules="step3Rules"
          @submit.prevent="handleResetPassword"
        >
          <el-form-item prop="newPassword">
            <el-input
              v-model="step3Form.newPassword"
              type="password"
              placeholder="请输入新密码（6位以上）"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="step3Form.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="step-button"
              @click="handleResetPassword"
            >
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
        <el-button text @click="step = 2">返回上一步</el-button>
      </div>

      <div class="footer">
        <el-link type="primary" @click="router.push('/login')">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getSecurityQuestionByUsername, verifySecurityAnswer, resetPassword } from '@/api/user'
import { User, Lock, QuestionFilled, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const step = ref(1)
const loading = ref(false)
const username = ref('')
const securityQuestion = ref('')

const formRef = ref<FormInstance>()
const formRef2 = ref<FormInstance>()
const formRef3 = ref<FormInstance>()

const step1Form = reactive({ username: '' })
const step2Form = reactive({ securityAnswer: '' })
const step3Form = reactive({ newPassword: '', confirmPassword: '' })

const step1Rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const step2Rules: FormRules = {
  securityAnswer: [{ required: true, message: '请输入密保答案', trigger: 'blur' }]
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== step3Form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const step3Rules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleVerifyUsername = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await getSecurityQuestionByUsername(step1Form.username)
        username.value = step1Form.username
        securityQuestion.value = res.data.question
        step.value = 2
      } catch {
        ElMessage.error('获取密保问题失败，请检查用户名是否正确')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleVerifyAnswer = async () => {
  if (!formRef2.value) return
  
  await formRef2.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await verifySecurityAnswer({
          username: username.value,
          securityAnswer: step2Form.securityAnswer
        })
        step.value = 3
      } catch {
        ElMessage.error('密保答案错误')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleResetPassword = async () => {
  if (!formRef3.value) return
  
  await formRef3.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await resetPassword({
          username: username.value,
          securityAnswer: step2Form.securityAnswer,
          newPassword: step3Form.newPassword
        })
        ElMessage.success('密码重置成功，请登录')
        router.push('/login')
      } catch {
        ElMessage.error('密码重置失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.forgot-password-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.logo .el-icon {
  font-size: 36px;
  color: white;
}

.header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.header p {
  font-size: 14px;
  color: #909399;
}

.step-content {
  margin-bottom: 16px;
}

.security-question {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #606266;
}

.security-question .el-icon {
  color: #409eff;
}

.step-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.step-button:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.footer {
  text-align: center;
  margin-top: 16px;
}
</style>
