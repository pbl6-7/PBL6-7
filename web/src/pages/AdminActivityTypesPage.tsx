import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Layers,
  Plus,
  Edit3,
  Trash2,
  Loader2,
  GraduationCap,
  X,
  FileText,
} from 'lucide-react';
import {
  getAllActivityTypes,
  createActivityType,
  updateActivityType,
  deleteActivityType,
} from '@/api/activityType';
import type { ActivityTypeResponse, ActivityTypeCreateRequest } from '@/api/activityType';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台活动类型管理页面
 * 提供活动类型的列表展示、新增、编辑和删除功能
 */
export default function AdminActivityTypesPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);

  /** 活动类型列表 */
  const [types, setTypes] = useState<ActivityTypeResponse[]>([]);
  /** 页面加载状态 */
  const [loading, setLoading] = useState(true);
  /** 表单提交中状态 */
  const [submitting, setSubmitting] = useState(false);
  /** 当前编辑的活动类型ID，null 表示新增模式 */
  const [editingId, setEditingId] = useState<number | null>(null);
  /** 表单数据 */
  const [formData, setFormData] = useState<ActivityTypeCreateRequest>({
    name: '',
    description: '',
    icon: '',
  });
  /** 删除确认弹窗是否可见 */
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  /** 待删除的活动类型ID */
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    checkAdmin();
    loadTypes();
  }, []);

  /**
   * 检查当前用户是否为管理员
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
   * 加载所有活动类型数据
   */
  const loadTypes = async () => {
    setLoading(true);
    try {
      const res = await getAllActivityTypes();
      setTypes(res.data.data || []);
    } catch (err) {
      console.error('加载活动类型失败', err);
      addToast('error', '加载活动类型列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 处理表单提交（新增或更新）
   */
  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      addToast('warning', '请输入类型名称');
      return;
    }
    if (formData.name.trim().length > 50) {
      addToast('warning', '类型名称不能超过50个字符');
      return;
    }
    if (formData.description && formData.description.length > 500) {
      addToast('warning', '类型描述不能超过500个字符');
      return;
    }
    if (formData.icon && formData.icon.length > 50) {
      addToast('warning', '图标名称不能超过50个字符');
      return;
    }

    setSubmitting(true);
    try {
      if (editingId !== null) {
        await updateActivityType(editingId, formData);
        addToast('success', '活动类型已更新');
      } else {
        await createActivityType(formData);
        addToast('success', '活动类型已创建');
      }
      resetForm();
      loadTypes();
    } catch (err) {
      console.error('保存活动类型失败', err);
      addToast('error', editingId !== null ? '更新活动类型失败' : '创建活动类型失败');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 进入编辑模式，预填表单数据
   */
  const handleEdit = (type: ActivityTypeResponse) => {
    setEditingId(type.id);
    setFormData({
      name: type.name,
      description: type.description || '',
      icon: type.icon || '',
    });
  };

  /**
   * 打开删除确认弹窗
   */
  const openDeleteModal = (id: number) => {
    setDeletingId(id);
    setDeleteModalOpen(true);
  };

  /**
   * 确认删除活动类型
   */
  const confirmDelete = async () => {
    if (deletingId === null) return;

    try {
      await deleteActivityType(deletingId);
      addToast('success', '活动类型已删除');
      // 若正在编辑被删除的类型，重置表单
      if (editingId === deletingId) {
        resetForm();
      }
      loadTypes();
    } catch (err) {
      console.error('删除活动类型失败', err);
      addToast('error', '删除活动类型失败');
    } finally {
      setDeleteModalOpen(false);
      setDeletingId(null);
    }
  };

  /**
   * 重置表单为新增模式
   */
  const resetForm = () => {
    setEditingId(null);
    setFormData({ name: '', description: '', icon: '' });
  };

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
              <h1 className="text-xl font-bold text-[#4C1D95]">活动类型管理</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 左侧：类型列表 */}
            <div className="lg:col-span-2">
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-card border border-violet-100/50 overflow-hidden">
                <div className="px-6 py-5 border-b border-violet-100">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Layers className="text-violet-600" size={20} />
                      <h2 className="text-lg font-semibold text-[#4C1D95]">类型列表</h2>
                    </div>
                    <span className="px-3 py-1 bg-violet-100 text-violet-700 rounded-full text-sm font-medium">
                      {types.length} 个类型
                    </span>
                  </div>
                </div>

                {loading ? (
                  <div className="flex flex-col items-center justify-center py-16 gap-3">
                    <Loader2 className="animate-spin text-violet-600" size={32} />
                    <p className="text-gray-500">加载中...</p>
                  </div>
                ) : types.length === 0 ? (
                  /* 空状态展示 */
                  <div className="flex flex-col items-center justify-center py-16 gap-4">
                    <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                      <Layers className="text-violet-400" size={36} />
                    </div>
                    <div className="text-center">
                      <p className="text-lg font-medium text-[#4C1D95]">暂无活动类型</p>
                      <p className="text-sm text-gray-400 mt-1">在右侧表单中添加第一个活动类型</p>
                    </div>
                  </div>
                ) : (
                  <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {types.map((type) => (
                      <div
                        key={type.id}
                        className="group relative bg-white/80 backdrop-blur-sm rounded-xl border border-violet-100/60 p-4 hover:shadow-lg hover:border-violet-300 transition-all duration-300"
                      >
                        {/* 卡片内容 */}
                        <div className="flex items-start gap-3">
                          <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-violet-400 to-purple-500 rounded-lg flex items-center justify-center shadow-sm">
                            <Layers size={18} className="text-white" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h3 className="text-sm font-semibold text-[#4C1D95] truncate">
                              {type.name}
                            </h3>
                            {type.description && (
                              <p className="text-xs text-gray-500 mt-1 line-clamp-2">
                                {type.description}
                              </p>
                            )}
                            {type.icon && (
                              <div className="flex items-center gap-1 mt-2">
                                <FileText size={12} className="text-violet-400" />
                                <span className="text-xs text-violet-500">{type.icon}</span>
                              </div>
                            )}
                          </div>
                        </div>

                        {/* hover 显示的操作按钮 */}
                        <div className="absolute top-3 right-3 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                          <button
                            onClick={() => handleEdit(type)}
                            className="p-1.5 rounded-lg bg-violet-100 text-violet-600 hover:bg-violet-200 transition-colors duration-200 cursor-pointer"
                            title="编辑"
                          >
                            <Edit3 size={14} />
                          </button>
                          <button
                            onClick={() => openDeleteModal(type.id)}
                            className="p-1.5 rounded-lg bg-red-100 text-red-600 hover:bg-red-200 transition-colors duration-200 cursor-pointer"
                            title="删除"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* 右侧：添加/编辑表单 */}
            <div className="lg:col-span-1">
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-card border border-violet-100/50 sticky top-24">
                <div className="px-6 py-5 border-b border-violet-100">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Plus className="text-violet-600" size={20} />
                      <h2 className="text-lg font-semibold text-[#4C1D95]">
                        {editingId !== null ? '编辑类型' : '添加类型'}
                      </h2>
                    </div>
                    {editingId !== null && (
                      <button
                        onClick={resetForm}
                        className="p-1.5 rounded-lg bg-violet-100 text-violet-600 hover:bg-violet-200 transition-colors duration-200 cursor-pointer"
                        title="取消编辑"
                      >
                        <X size={16} />
                      </button>
                    )}
                  </div>
                </div>

                <div className="p-6 space-y-5">
                  {/* 名称输入框 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      类型名称 <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      placeholder="请输入类型名称"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      maxLength={50}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm"
                    />
                  </div>

                  {/* 描述文本域 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      类型描述
                    </label>
                    <textarea
                      placeholder="请输入类型描述"
                      rows={3}
                      value={formData.description || ''}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      maxLength={500}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm resize-none"
                    />
                  </div>

                  {/* 图标名称输入框 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      图标名称
                    </label>
                    <input
                      type="text"
                      placeholder="例如: Layers, Calendar"
                      value={formData.icon || ''}
                      onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
                      maxLength={50}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm"
                    />
                  </div>

                  {/* 提交按钮 */}
                  <button
                    onClick={handleSubmit}
                    disabled={submitting}
                    className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-violet-600 text-white rounded-xl font-medium hover:bg-violet-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer shadow-md"
                  >
                    {submitting ? (
                      <>
                        <Loader2 size={18} className="animate-spin" />
                        提交中...
                      </>
                    ) : editingId !== null ? (
                      <>
                        <Edit3 size={18} />
                        更新类型
                      </>
                    ) : (
                      <>
                        <Plus size={18} />
                        添加类型
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
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl border border-violet-100 p-6 w-full max-w-md mx-4">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                <Trash2 size={18} className="text-red-600" />
              </div>
              <h3 className="text-lg font-semibold text-[#4C1D95]">确认删除</h3>
            </div>
            <p className="text-sm text-gray-600 mb-6">
              确定要删除该活动类型吗？此操作不可撤销。
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                onClick={() => {
                  setDeleteModalOpen(false);
                  setDeletingId(null);
                }}
                className="px-5 py-2.5 bg-violet-100 text-[#4C1D95] rounded-xl text-sm font-medium hover:bg-violet-200 transition-colors duration-200 cursor-pointer"
              >
                取消
              </button>
              <button
                onClick={confirmDelete}
                className="px-5 py-2.5 bg-red-600 text-white rounded-xl text-sm font-medium hover:bg-red-700 transition-colors duration-200 cursor-pointer"
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
