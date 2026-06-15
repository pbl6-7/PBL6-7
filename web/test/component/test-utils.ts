/**
 * Vue组件测试工具函数
 */
import { mount, VueWrapper } from '@vue/test-utils'
import { ComponentPublicInstance } from 'vue'

export interface MockComponent {
  name: string
  template: string
  props?: Record<string, any>
}

export const createMockRouter = () => ({
  push: jest.fn(),
  replace: jest.fn(),
  go: jest.fn(),
  back: jest.fn(),
  forward: jest.fn()
})

export const createMockStore = (state: Record<string, any> = {}) => ({
  state,
  commit: jest.fn(),
  dispatch: jest.fn(),
  getters: {},
  install: jest.fn()
})

export const waitFor = (ms: number) =>
  new Promise(resolve => setTimeout(resolve, ms))

export const triggerEvent = async (
  wrapper: VueWrapper<ComponentPublicInstance>,
  selector: string,
  event: string,
  options?: Record<string, any>
) => {
  const element = wrapper.find(selector)
  if (element.exists()) {
    await element.trigger(event, options)
  }
}

export const setInputValue = async (
  wrapper: VueWrapper<ComponentPublicInstance>,
  selector: string,
  value: string
) => {
  const input = wrapper.find(selector)
  if (input.exists()) {
    await input.setValue(value)
  }
}

export const mockElementPlus = () => {
  vi.mock('element-plus', () => ({
    ElButton: {
      name: 'ElButton',
      template: '<button><slot /></button>',
      props: ['loading', 'type', 'size']
    },
    ElInput: {
      name: 'ElInput',
      template: '<input />',
      props: ['modelValue', 'placeholder', 'type']
    },
    ElForm: {
      name: 'ElForm',
      template: '<form><slot /></form>',
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
      success: jest.fn(),
      error: jest.fn(),
      warning: jest.fn(),
      info: jest.fn()
    },
    ElCard: {
      name: 'ElCard',
      template: '<div><slot /></div>'
    },
    ElTable: {
      name: 'ElTable',
      template: '<table><slot /></table>',
      props: ['data']
    },
    ElPagination: {
      name: 'ElPagination',
      template: '<div />',
      props: ['currentPage', 'pageSize', 'total']
    }
  }))
}
