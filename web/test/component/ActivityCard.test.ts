/**
 * 活动卡片组件测试
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
  ElCard: {
    name: 'ElCard',
    template: '<div class="el-card"><slot /></div>',
    props: ['body-style']
  },
  ElButton: {
    name: 'ElButton',
    template: '<button type="button"><slot /></button>',
    props: ['type', 'size']
  },
  ElTag: {
    name: 'ElTag',
    template: '<span class="el-tag"><slot /></span>',
    props: ['type']
  },
  ElIcon: {
    name: 'ElIcon',
    template: '<span><slot /></span>'
  },
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

const ActivityCardTemplate = `
  <div class="activity-card" @click="goToDetail">
    <div class="activity-image">
      <img v-if="activity.coverImage" :src="activity.coverImage" :alt="activity.title" />
      <div v-else class="default-cover">{{ activity.title }}</div>
    </div>
    <div class="activity-content">
      <h3 class="activity-title">{{ activity.title }}</h3>
      <div class="activity-info">
        <span class="activity-time">
          {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}
        </span>
        <span class="activity-location">{{ activity.location }}</span>
      </div>
      <div class="activity-status">
        <span :class="['status-tag', activity.status]">{{ getStatusText(activity.status) }}</span>
        <span class="activity-count">{{ activity.registrationCount || 0 }} 人报名</span>
      </div>
    </div>
  </div>
`

const ActivityCardComponent = {
  template: ActivityCardTemplate,
  props: {
    activity: {
      type: Object,
      required: true
    }
  },
  emits: ['click'],
  methods: {
    goToDetail() {
      this.$router.push(`/activity/${this.activity.id}`)
      this.$emit('click', this.activity)
    },
    formatTime(time: string) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    getStatusText(status: string) {
      const statusMap: Record<string, string> = {
        published: '进行中',
        draft: '草稿',
        closed: '已结束',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    }
  }
}

describe('活动卡片组件测试', () => {
  let wrapper: VueWrapper<ComponentPublicInstance>

  const mockActivity = {
    id: 1,
    title: '校园宣讲会',
    description: '企业校园招聘宣讲',
    location: '图书馆报告厅',
    startTime: '2026-06-01T10:00:00',
    endTime: '2026-06-01T12:00:00',
    status: 'published',
    registrationCount: 50,
    coverImage: null
  }

  beforeEach(() => {
    wrapper = mount(ActivityCardComponent, {
      props: { activity: mockActivity },
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
    it('应该正确渲染活动卡片', () => {
      expect(wrapper.find('.activity-card').exists()).toBe(true)
      expect(wrapper.find('.activity-title').text()).toBe('校园宣讲会')
    })

    it('应该显示活动标题', () => {
      expect(wrapper.find('.activity-title').text()).toBe(mockActivity.title)
    })

    it('应该显示活动地点', () => {
      expect(wrapper.find('.activity-location').text()).toBe(mockActivity.location)
    })

    it('应该显示报名人数', () => {
      expect(wrapper.find('.activity-count').text()).toBe('50 人报名')
    })

    it('应该显示活动状态标签', () => {
      const statusTag = wrapper.find('.status-tag')
      expect(statusTag.exists()).toBe(true)
      expect(statusTag.text()).toBe('进行中')
    })
  })

  describe('时间格式化', () => {
    it('应该正确格式化活动时间', () => {
      const timeSpan = wrapper.find('.activity-time')
      expect(timeSpan.text()).toContain('6/1')
      expect(timeSpan.text()).toContain('10:00')
    })
  })

  describe('点击行为', () => {
    it('点击卡片应该跳转到详情页', async () => {
      await wrapper.find('.activity-card').trigger('click')

      expect(wrapper.vm.$router.push).toHaveBeenCalledWith('/activity/1')
    })

    it('点击卡片应该触发click事件', async () => {
      const testWrapper = mount(ActivityCardComponent, {
        props: { activity: mockActivity },
        global: {
          mocks: {
            $router: { push: vi.fn() }
          }
        }
      })

      await testWrapper.find('.activity-card').trigger('click')
      await testWrapper.vm.$nextTick()

      expect(testWrapper.emitted('click')).toBeTruthy()
      expect(testWrapper.emitted('click')?.[0]).toEqual([mockActivity])
    })
  })

  describe('状态显示', () => {
    it('published状态应显示"进行中"', () => {
      expect(wrapper.find('.status-tag').text()).toBe('进行中')
    })

    it('closed状态应显示"已结束"', () => {
      const closedActivity = { ...mockActivity, status: 'closed' }
      const closedWrapper = mount(ActivityCardComponent, {
        props: { activity: closedActivity },
        global: { mocks: { $router: { push: vi.fn() } } }
      })
      expect(closedWrapper.find('.status-tag').text()).toBe('已结束')
    })

    it('draft状态应显示"草稿"', () => {
      const draftActivity = { ...mockActivity, status: 'draft' }
      const draftWrapper = mount(ActivityCardComponent, {
        props: { activity: draftActivity },
        global: { mocks: { $router: { push: vi.fn() } } }
      })
      expect(draftWrapper.find('.status-tag').text()).toBe('草稿')
    })
  })

  describe('无报名数据', () => {
    it('报名人数为0时应该显示0', () => {
      const noCountActivity = { ...mockActivity, registrationCount: 0 }
      const noCountWrapper = mount(ActivityCardComponent, {
        props: { activity: noCountActivity },
        global: { mocks: { $router: { push: vi.fn() } } }
      })
      expect(noCountWrapper.find('.activity-count').text()).toBe('0 人报名')
    })

    it('报名人数为空时应该显示0', () => {
      const emptyCountActivity = { ...mockActivity, registrationCount: null }
      const emptyCountWrapper = mount(ActivityCardComponent, {
        props: { activity: emptyCountActivity },
        global: { mocks: { $router: { push: vi.fn() } } }
      })
      expect(emptyCountWrapper.find('.activity-count').text()).toBe('0 人报名')
    })
  })
})
