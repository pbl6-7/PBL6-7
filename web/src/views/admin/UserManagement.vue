<template>
  <div class="user-management-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-select v-model="filterRole" placeholder="筛选角色" clearable size="default" @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option label="普通用户" value="user" />
          <el-option label="发布者" value="publisher" />
          <el-option label="管理员" value="admin" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或姓名"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
          style="width: 300px"
        />
      </div>
    </div>

    <el-card class="user-table-card">
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" min-width="150" />
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditRole(row)">修改角色</el-button>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改用户角色" width="500px">
      <el-form :model="currentUser" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="currentUser.realName" disabled />
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(currentUser.role)">{{ getRoleLabel(currentUser.role) }}</el-tag>
        </el-form-item>
        <el-form-item label="新角色" required>
          <el-select v-model="newRole" placeholder="请选择新角色">
            <el-option label="普通用户" value="user" />
            <el-option label="发布者" value="publisher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRole" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserList, updateUserRole, type UserItem } from '@/api/admin'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const userList = ref<UserItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchKeyword = ref('')
const filterRole = ref('')
const dialogVisible = ref(false)
const currentUser = ref<Partial<UserItem>>({})
const newRole = ref('')

const getRoleLabel = (role: string) => {
  const labels: Record<string, string> = {
    user: '普通用户',
    publisher: '发布者',
    admin: '管理员'
  }
  return labels[role] || role
}

const getRoleTagType = (role: string) => {
  const types: Record<string, any> = {
    user: 'info',
    publisher: 'warning',
    admin: 'danger'
  }
  return types[role] || 'info'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchKeyword.value || undefined,
      role: filterRole.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    userList.value = res.data.list
    total.value = res.data.total
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadUsers()
}

const handleEditRole = (user: UserItem) => {
  currentUser.value = { ...user }
  newRole.value = user.role
  dialogVisible.value = true
}

const handleSubmitRole = async () => {
  if (!newRole.value) {
    ElMessage.warning('请选择新角色')
    return
  }

  if (newRole.value === currentUser.value.role) {
    ElMessage.warning('新角色与当前角色相同，无需修改')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将用户"${currentUser.value.username}"的角色从"${getRoleLabel(currentUser.value.role!)}"修改为"${getRoleLabel(newRole.value)}"吗？`,
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitLoading.value = true
    await updateUserRole(currentUser.value.id!, newRole.value)
    ElMessage.success('角色更新成功')
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('角色更新失败')
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management-container {
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

.user-table-card {
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
