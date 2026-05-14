<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <el-icon><School /></el-icon>
        </div>
        <h1>校园活动平台</h1>
        <p>注册新账号</p>
      </div>
      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            size="large"
            :prefix-icon="UserFilled"
          />
        </el-form-item>
        <el-form-item prop="contact">
          <el-input
            v-model="registerForm.contact"
            placeholder="请输入联系方式"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6位以上）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="securityQuestionId">
          <el-select
            v-model="registerForm.securityQuestionId"
            placeholder="请选择密保问题"
            size="large"
            class="full-width"
          >
            <el-option
              v-for="item in securityQuestions"
              :key="item.id"
              :label="item.question"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="securityAnswer">
          <el-input
            v-model="registerForm.securityAnswer"
            placeholder="请输入密保答案"
            size="large"
            :prefix-icon="QuestionFilled"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userRegister, getSecurityQuestions } from '@/api/user'
import type { SecurityQuestion } from '@/types/user'
import { User, Lock, UserFilled, Phone, QuestionFilled, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const securityQuestions = ref<SecurityQuestion[]>([])

const registerForm = reactive({
  username: '',
  realName: '',
  contact: '',
  password: '',
  confirmPassword: '',
  securityQuestionId: 0,
  securityAnswer: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  securityQuestionId: [
    { required: true, message: '请选择密保问题', trigger: 'change' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ]
}

const loadSecurityQuestions = async () => {
  try {
    const res = await getSecurityQuestions()
    securityQuestions.value = res.data
  } catch (error) {
    console.error('获取密保问题失败', error)
  }
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userRegister({
          username: registerForm.username,
          password: registerForm.password,
          realName: registerForm.realName,
          contact: registerForm.contact,
          securityQuestionId: registerForm.securityQuestionId,
          securityAnswer: registerForm.securityAnswer
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch {
        ElMessage.error('注册失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadSecurityQuestions()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
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

.register-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.register-header p {
  font-size: 14px;
  color: #909399;
}

.register-form {
  margin-bottom: 24px;
}

.register-form :deep(.el-input__wrapper),
.register-form :deep(.el-select__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
}

.register-form :deep(.el-input__inner),
.register-form :deep(.el-select__placeholder) {
  font-size: 15px;
}

.full-width {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.register-button:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-footer .el-link {
  font-size: 14px;
}
</style>
