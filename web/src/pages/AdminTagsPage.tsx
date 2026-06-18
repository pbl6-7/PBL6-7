import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Tag,
  Plus,
  Edit3,
  Trash2,
  Loader2,
  GraduationCap,
  X,
  Palette,
} from 'lucide-react';
import { getAllTags, createTag, updateTag, deleteTag } from '@/api/tag';
import type { TagResponse, TagCreateRequest } from '@/api/tag';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台标签管理页面
 * 提供标签的增删改查功能，包含标签列表展示和表单编辑
 */
export default function AdminTagsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);

  /** 标签列表数据 */
  const [tags, setTags] = useState<TagResponse[]>([]);
  /** 页面加载状态 */
  const [loading, setLoading] = useState(true);
  /** 表单提交中状态 */
  const [submitting, setSubmitting] = useState(false);
  /** 当前正在编辑的标签ID，null 表示新增模式 */
  const [editingId, setEditingId] = useState<number | null>(null);
  /** 表单 - 标签名称 */
  const [formName, setFormName] = useState('');
  /** 表单 - 标签颜色 */
  const [formColor, setFormColor] = useState('#8B5CF6');
  /** 表单 - 标签类型 */
  const [formType, setFormType] = useState('GENERAL');
  /** 删除确认弹窗是否显示 */
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  /** 待删除的标签ID */
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);

  useEffect(() => {
    checkAdmin();
    loadTags();
  }, []);

  /**
   * 检查当前用户是否为管理员，非管理员跳转至首页
   */
  const checkAdmin = () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      navigate('/login');
      return;
    }
    const user = JSON.parse(userStr);
    if (user.role !== 'ADMIN') {
      navigate('/');
    }
  };

  /**
   * 加载所有标签数据
   */
  const loadTags = async () => {
    setLoading(true);
    try {
      const res = await getAllTags();
      setTags(res.data.data || []);
    } catch (err) {
      console.error('加载标签失败', err);
      addToast('error', '加载标签列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 重置表单为新增模式
   */
  const resetForm = () => {
    setEditingId(null);
    setFormName('');
    setFormColor('#8B5CF6');
    setFormType('GENERAL');
  };

  /**
   * 点击编辑按钮，将标签数据填入表单并切换为编辑模式
   */
  const handleEdit = (tag: TagResponse) => {
    setEditingId(tag.id);
    setFormName(tag.name);
    setFormColor(tag.color || '#8B5CF6');
    setFormType(tag.type || 'GENERAL');
  };

  /**
   * 提交表单，根据当前模式执行创建或更新操作
   */
  const handleSubmit = async () => {
    if (!formName.trim()) {
      addToast('warning', '请输入标签名称');
      return;
    }
    if (formName.trim().length > 30) {
      addToast('warning', '标签名称不能超过30个字符');
      return;
    }
    if (!/^#[0-9A-Fa-f]{6}$/.test(formColor)) {
      addToast('warning', '颜色格式不正确，请使用 #RRGGBB 格式');
      return;
    }

    const data: TagCreateRequest = {
      name: formName.trim(),
      color: formColor,
      type: formType,
    };

    setSubmitting(true);
    try {
      if (editingId !== null) {
        await updateTag(editingId, data);
        addToast('success', '标签更新成功');
      } else {
        await createTag(data);
        addToast('success', '标签创建成功');
      }
      resetForm();
      loadTags();
    } catch (err) {
      console.error('保存标签失败', err);
      addToast('error', editingId !== null ? '更新标签失败，请重试' : '创建标签失败，请重试');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 打开删除确认弹窗
   */
  const openDeleteModal = (id: number) => {
    setDeleteTargetId(id);
    setDeleteModalOpen(true);
  };

  /**
   * 关闭删除确认弹窗
   */
  const closeDeleteModal = () => {
    setDeleteTargetId(null);
    setDeleteModalOpen(false);
  };

  /**
   * 确认删除标签
   */
  const confirmDelete = async () => {
    if (deleteTargetId === null) return;

    try {
      await deleteTag(deleteTargetId);
      addToast('success', '标签已删除');
      // 如果正在编辑被删除的标签，重置表单
      if (editingId === deleteTargetId) {
        resetForm();
      }
      loadTags();
    } catch (err) {
      console.error('删除标签失败', err);
      addToast('error', '删除标签失败，请重试');
    } finally {
      closeDeleteModal();
    }
  };

  /** 获取待删除标签的名称 */
  const deleteTargetName =
    tags.find((t) => t.id === deleteTargetId)?.name || '';

  /** 标签类型选项 */
  const typeOptions = [
    { value: 'GENERAL', label: '通用' },
    { value: 'CATEGORY', label: '分类' },
    { value: 'SKILL', label: '技能' },
    { value: 'TOPIC', label: '主题' },
  ];

  return (
    <div className="min-h-screen bg-[#FAF5FF] flex">
      <Toast />
      <AdminSidebar />

      {/* 主内容区 */}
      <div className="flex-1 flex flex-col">
        {/* 顶部导航栏 */}
        <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-violet-100 shadow-sm">
          <div className="flex items-center justify-between px-6 h-16">
            <div className="flex items-center gap-3">
              <GraduationCap size={24} className="text-violet-600" />
              <h1 className="text-xl font-bold text-[#4C1D95]">标签管理</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 左侧：标签列表 */}
            <div className="lg:col-span-2">
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-card border border-violet-100/50 overflow-hidden">
                {/* 列表头部 */}
                <div className="px-6 py-5 border-b border-violet-100">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Tag className="text-violet-600" size={20} />
                      <h2 className="text-lg font-semibold text-[#4C1D95]">标签列表</h2>
                    </div>
                    <span className="px-3 py-1 bg-violet-100 text-violet-700 rounded-full text-sm font-medium">
                      共 {tags.length} 个标签
                    </span>
                  </div>
                </div>

                {/* 标签列表内容 */}
                <div className="p-6">
                  {loading ? (
                    <div className="flex flex-col items-center justify-center py-16 gap-3">
                      <Loader2 className="animate-spin text-violet-600" size={32} />
                      <p className="text-gray-500">加载中...</p>
                    </div>
                  ) : tags.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-16 gap-4">
                      <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                        <Tag className="text-violet-400" size={36} />
                      </div>
                      <div className="text-center">
                        <p className="text-lg font-medium text-[#4C1D95]">暂无标签</p>
                        <p className="text-sm text-gray-400 mt-1">在右侧表单中创建第一个标签</p>
                      </div>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      {tags.map((tag) => (
                        <div
                          key={tag.id}
                          className="group relative flex items-center gap-3 px-4 py-3 bg-[#FAF5FF]/60 rounded-xl border border-violet-100/50 hover:border-violet-300 hover:shadow-md transition-all duration-200"
                        >
                          {/* 颜色圆点 */}
                          <div
                            className="flex-shrink-0 w-4 h-4 rounded-full shadow-sm ring-2 ring-white"
                            style={{ backgroundColor: tag.color || '#8B5CF6' }}
                          />
                          {/* 标签信息 */}
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-semibold text-[#4C1D95] truncate">
                              {tag.name}
                            </p>
                            <p className="text-xs text-gray-400 mt-0.5">
                              {typeOptions.find((o) => o.value === tag.type)?.label || tag.type || '通用'}
                            </p>
                          </div>
                          {/* 操作按钮，hover 时显示 */}
                          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                            <button
                              onClick={() => handleEdit(tag)}
                              className="p-1.5 text-violet-600 hover:bg-violet-100 rounded-lg transition-colors duration-200 cursor-pointer"
                              title="编辑标签"
                            >
                              <Edit3 size={15} />
                            </button>
                            <button
                              onClick={() => openDeleteModal(tag.id)}
                              className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors duration-200 cursor-pointer"
                              title="删除标签"
                            >
                              <Trash2 size={15} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* 右侧：添加/编辑表单 */}
            <div className="lg:col-span-1">
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-card border border-violet-100/50 sticky top-24">
                {/* 表单头部 */}
                <div className="px-6 py-5 border-b border-violet-100">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      {editingId !== null ? (
                        <Edit3 className="text-violet-600" size={20} />
                      ) : (
                        <Plus className="text-violet-600" size={20} />
                      )}
                      <h2 className="text-lg font-semibold text-[#4C1D95]">
                        {editingId !== null ? '编辑标签' : '添加标签'}
                      </h2>
                    </div>
                    {editingId !== null && (
                      <button
                        onClick={resetForm}
                        className="p-1.5 text-gray-400 hover:text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer"
                        title="取消编辑"
                      >
                        <X size={16} />
                      </button>
                    )}
                  </div>
                </div>

                {/* 表单内容 */}
                <div className="p-6 space-y-5">
                  {/* 名称输入框 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      标签名称 <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="请输入标签名称"
                      maxLength={30}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm"
                    />
                  </div>

                  {/* 颜色选择器 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      <div className="flex items-center gap-2">
                        <Palette size={14} />
                        标签颜色
                      </div>
                    </label>
                    <div className="flex items-center gap-3">
                      <input
                        type="color"
                        value={formColor}
                        onChange={(e) => setFormColor(e.target.value)}
                        className="w-12 h-12 rounded-xl border-2 border-violet-200 cursor-pointer p-1 bg-[#FAF5FF]"
                      />
                      <input
                        type="text"
                        value={formColor}
                        onChange={(e) => setFormColor(e.target.value)}
                        className="flex-1 px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 text-sm font-mono"
                      />
                      {/* 颜色预览圆点 */}
                      <div
                        className="w-8 h-8 rounded-full shadow-sm ring-2 ring-white flex-shrink-0"
                        style={{ backgroundColor: formColor }}
                      />
                    </div>
                  </div>

                  {/* 类型下拉 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      标签类型
                    </label>
                    <select
                      value={formType}
                      onChange={(e) => setFormType(e.target.value)}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 text-sm cursor-pointer"
                    >
                      {typeOptions.map((opt) => (
                        <option key={opt.value} value={opt.value}>
                          {opt.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* 提交按钮 */}
                  <button
                    onClick={handleSubmit}
                    disabled={submitting}
                    className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-medium text-sm transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer shadow-md hover:shadow-lg"
                  >
                    {submitting ? (
                      <>
                        <Loader2 size={16} className="animate-spin" />
                        处理中...
                      </>
                    ) : editingId !== null ? (
                      <>
                        <Edit3 size={16} />
                        更新标签
                      </>
                    ) : (
                      <>
                        <Plus size={16} />
                        添加标签
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>

      {/* 删除确认弹窗 */}
      {deleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          {/* 遮罩层 */}
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            onClick={closeDeleteModal}
          />
          {/* 弹窗内容 */}
          <div className="relative bg-white rounded-2xl shadow-2xl border border-violet-100 p-6 w-full max-w-md mx-4 animate-in">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Trash2 className="text-red-500" size={20} />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-[#4C1D95]">确认删除</h3>
                <p className="text-sm text-gray-500 mt-0.5">此操作不可撤销</p>
              </div>
            </div>
            <p className="text-sm text-gray-600 mb-6">
              确定要删除标签 <span className="font-semibold text-[#4C1D95]">「{deleteTargetName}」</span> 吗？删除后相关数据将无法恢复。
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                onClick={closeDeleteModal}
                className="px-5 py-2.5 bg-violet-50 text-[#4C1D95] hover:bg-violet-100 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer"
              >
                取消
              </button>
              <button
                onClick={confirmDelete}
                className="px-5 py-2.5 bg-red-500 hover:bg-red-600 text-white rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer shadow-md"
              >
                确认删除
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
