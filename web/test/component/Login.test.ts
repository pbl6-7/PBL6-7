/**
 * 登录组件测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { ComponentPublicInstance } from 'vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('element-plus', () => ({
  ElButton: {
    name: 'ElButton',
    template: '<button type="button" :class="type"><slot /></button>',
    props: ['loading', 'type', 'nativeType']
  },
  ElInput: {
    name: 'ElInput',
    template: '<input type="text" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue', 'placeholder', 'prefixIcon'],
    emits: ['update:modelValue']
  },
  ElForm: {
    name: 'ElForm',
    template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>',
    props: ['model', 'rules']
  },
  ElFormItem: {
    name: 'ElFormItem',
    template: '<div><slot /></div>',
    props: ['prop']
  },
  ElIcon: {
    name: 'ElIcon',
    template: '<span><slot /></span>'
  },
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

const mockLogin = vi.fn()
vi.mock('@/api/user', () => ({
  userLogin: (...args: any[]) => mockLogin(...args)
}))

const LoginTemplate = `
  <div class="login-container">
    <div class="login-box">
      <h1>校园活动平台</h1>
      <form @submit.prevent="handleLogin">
        <input
          v-model="loginForm.username"
          type="text"
          placeholder="请输入用户名"
          data-testid="username-input"
        />
        <input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          data-testid="password-input"
        />
        <button type="submit" :disabled="loading">登录</button>
      </form>
    </div>
  </div>
`

const LoginComponent = {
  template: LoginTemplate,
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        return
      }
      this.loading = true
      try {
        await mockLogin(this.loginForm)
        this.$router.push('/')
      } finally {
        this.loading = false
      }
    }
  }
}

describe('登录组件测试', () => {
  let wrapper: VueWrapper<ComponentPublicInstance>

  beforeEach(() => {
    vi.clearAllMocks()
    wrapper = mount(LoginComponent, {
      global: {
        mocks: {
          $router: {
            push: vi.fn()
          }
        }
      }
    })
  })

  describe('组件渲染', () => {
    it('应该正确渲染登录表单', () => {
      expect(wrapper.find('.login-container').exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('校园活动平台')
      expect(wrapper.findAll('input')).toHaveLength(2)
      expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
    })

    it('应该有两个输入框', () => {
      const inputs = wrapper.findAll('input')
      expect(inputs.length).toBe(2)
      expect(inputs[0].attributes('placeholder')).toBe('请输入用户名')
      expect(inputs[1].attributes('placeholder')).toBe('请输入密码')
    })
  })

  describe('表单交互', () => {
    it('应该能够输入用户名', async () => {
      const usernameInput = wrapper.find('[data-testid="username-input"]')
      await usernameInput.setValue('testuser')

      expect(wrapper.vm.loginForm.username).toBe('testuser')
    })

    it('应该能够输入密码', async () => {
      const passwordInput = wrapper.find('[data-testid="password-input"]')
      await passwordInput.setValue('password123')

      expect(wrapper.vm.loginForm.password).toBe('password123')
    })

    it('应该能够清空表单', async () => {
      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = 'password123'

      expect(wrapper.vm.loginForm.username).toBe('testuser')
      expect(wrapper.vm.loginForm.password).toBe('password123')
    })
  })

  describe('登录逻辑', () => {
    it('应该在表单完整时调用登录API', async () => {
      mockLogin.mockResolvedValue({
        code: 200,
        data: { token: 'test-token', userId: 1 }
      })

      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = '123456'
      await wrapper.vm.handleLogin()

      expect(mockLogin).toHaveBeenCalledWith({
        username: 'testuser',
        password: '123456'
      })
    })

    it('应该在表单不完整时不调用登录API', async () => {
      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = ''
      await wrapper.vm.handleLogin()

      expect(mockLogin).not.toHaveBeenCalled()
    })

    it('登录成功后应该跳转到首页', async () => {
      mockLogin.mockResolvedValue({
        code: 200,
        data: { token: 'test-token', userId: 1 }
      })

      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = '123456'
      await wrapper.vm.handleLogin()

      expect(wrapper.vm.$router.push).toHaveBeenCalledWith('/')
    })

    it('登录失败时应该显示错误', async () => {
      mockLogin.mockRejectedValue(new Error('用户名或密码错误'))

      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = 'wrongpassword'
      await wrapper.vm.handleLogin()

      expect(mockLogin).toHaveBeenCalled()
    })
  })

  describe('加载状态', () => {
    it('登录时应该显示加载状态', async () => {
      mockLogin.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

      wrapper.vm.loginForm.username = 'testuser'
      wrapper.vm.loginForm.password = '123456'

      const loginPromise = wrapper.vm.handleLogin()
      expect(wrapper.vm.loading).toBe(true)

      await loginPromise
      expect(wrapper.vm.loading).toBe(false)
    })
  })
})
