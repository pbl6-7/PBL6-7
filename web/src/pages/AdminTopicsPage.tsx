import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  MessageSquare,
  Plus,
  Edit3,
  Trash2,
  Loader2,
  GraduationCap,
  X,
} from 'lucide-react';
import {
  getAllTopics,
  createTopic,
  updateTopic,
  deleteTopic,
} from '@/api/topic';
import type { TopicResponse, TopicCreateRequest, TopicUpdateRequest } from '@/api/topic';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台话题管理页面
 * 提供话题的增删改查功能，包含话题列表展示和表单编辑
 */
export default function AdminTopicsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);

  /** 话题列表数据 */
  const [topics, setTopics] = useState<TopicResponse[]>([]);
  /** 页面加载状态 */
  const [loading, setLoading] = useState(true);
  /** 表单提交中状态 */
  const [submitting, setSubmitting] = useState(false);
  /** 当前正在编辑的话题ID，null 表示新增模式 */
  const [editingId, setEditingId] = useState<number | null>(null);
  /** 表单 - 话题标题 */
  const [formTitle, setFormTitle] = useState('');
  /** 表单 - 活动ID */
  const [formActivityId, setFormActivityId] = useState('');
  /** 删除确认弹窗是否显示 */
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  /** 待删除的话题ID */
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);

  useEffect(() => {
    checkAdmin();
    loadTopics();
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
   * 加载所有话题数据
   */
  const loadTopics = async () => {
    setLoading(true);
    try {
      const res = await getAllTopics();
      setTopics(res.data.data || []);
    } catch (err) {
      console.error('加载话题失败', err);
      addToast('error', '加载话题列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 重置表单为新增模式
   */
  const resetForm = () => {
    setEditingId(null);
    setFormTitle('');
    setFormActivityId('');
  };

  /**
   * 点击编辑按钮，将话题数据填入表单并切换为编辑模式
   */
  const handleEdit = (topic: TopicResponse) => {
    setEditingId(topic.id);
    setFormTitle(topic.title);
    setFormActivityId(String(topic.activityId));
  };

  /**
   * 提交表单，根据当前模式执行创建或更新操作
   */
  const handleSubmit = async () => {
    if (!formTitle.trim()) {
      addToast('warning', '请输入话题标题');
      return;
    }
    if (formTitle.trim().length > 100) {
      addToast('warning', '话题标题不能超过100个字符');
      return;
    }
    if (!formActivityId.trim() || isNaN(Number(formActivityId))) {
      addToast('warning', '请输入有效的活动ID');
      return;
    }
    if (Number(formActivityId) <= 0 || !Number.isInteger(Number(formActivityId))) {
      addToast('warning', '活动ID必须为正整数');
      return;
    }

    setSubmitting(true);
    try {
      if (editingId !== null) {
        const data: TopicUpdateRequest = {
          title: formTitle.trim(),
        };
        await updateTopic(editingId, data);
        addToast('success', '话题更新成功');
      } else {
        const data: TopicCreateRequest = {
          title: formTitle.trim(),
          activityId: Number(formActivityId),
        };
        await createTopic(data);
        addToast('success', '话题创建成功');
      }
      resetForm();
      loadTopics();
    } catch (err) {
      console.error('保存话题失败', err);
      addToast('error', editingId !== null ? '更新话题失败，请重试' : '创建话题失败，请重试');
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
   * 确认删除话题
   */
  const confirmDelete = async () => {
    if (deleteTargetId === null) return;

    try {
      await deleteTopic(deleteTargetId);
      addToast('success', '话题已删除');
      if (editingId === deleteTargetId) {
        resetForm();
      }
      loadTopics();
    } catch (err) {
      console.error('删除话题失败', err);
      addToast('error', '删除话题失败，请重试');
    } finally {
      closeDeleteModal();
    }
  };

  /** 获取待删除话题的标题 */
  const deleteTargetTitle =
    topics.find((t) => t.id === deleteTargetId)?.title || '';

  /**
   * 格式化日期字符串为可读格式
   */
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
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
              <h1 className="text-xl font-bold text-[#4C1D95]">话题管理</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 左侧：话题列表 */}
            <div className="lg:col-span-2">
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-card border border-violet-100/50 overflow-hidden">
                {/* 列表头部 */}
                <div className="px-6 py-5 border-b border-violet-100">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <MessageSquare className="text-violet-600" size={20} />
                      <h2 className="text-lg font-semibold text-[#4C1D95]">话题列表</h2>
                    </div>
                    <span className="px-3 py-1 bg-violet-100 text-violet-700 rounded-full text-sm font-medium">
                      共 {topics.length} 个话题
                    </span>
                  </div>
                </div>

                {/* 话题列表内容 */}
                <div className="p-6">
                  {loading ? (
                    <div className="flex flex-col items-center justify-center py-16 gap-3">
                      <Loader2 className="animate-spin text-violet-600" size={32} />
                      <p className="text-gray-500">加载中...</p>
                    </div>
                  ) : topics.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-16 gap-4">
                      <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                        <MessageSquare className="text-violet-400" size={36} />
                      </div>
                      <div className="text-center">
                        <p className="text-lg font-medium text-[#4C1D95]">暂无话题</p>
                        <p className="text-sm text-gray-400 mt-1">在右侧表单中创建第一个话题</p>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {topics.map((topic) => (
                        <div
                          key={topic.id}
                          className="group relative px-5 py-4 bg-[#FAF5FF]/60 rounded-xl border border-violet-100/50 hover:border-violet-300 hover:shadow-md transition-all duration-200"
                        >
                          <div className="flex items-start justify-between">
                            {/* 话题信息 */}
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center gap-2">
                                <MessageSquare size={16} className="text-violet-500 flex-shrink-0" />
                                <p className="text-sm font-semibold text-[#4C1D95] truncate">
                                  {topic.title}
                                </p>
                              </div>
                              <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                                <span>活动ID: {topic.activityId}</span>
                                <span>创建者: {topic.creatorName || topic.creatorId}</span>
                                <span>{formatDate(topic.createdAt)}</span>
                              </div>
                            </div>
                            {/* 操作按钮，hover 时显示 */}
                            <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200 ml-3 flex-shrink-0">
                              <button
                                onClick={() => handleEdit(topic)}
                                className="p-1.5 text-violet-600 hover:bg-violet-100 rounded-lg transition-colors duration-200 cursor-pointer"
                                title="编辑话题"
                              >
                                <Edit3 size={15} />
                              </button>
                              <button
                                onClick={() => openDeleteModal(topic.id)}
                                className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors duration-200 cursor-pointer"
                                title="删除话题"
                              >
                                <Trash2 size={15} />
                              </button>
                            </div>
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
                        {editingId !== null ? '编辑话题' : '添加话题'}
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
                  {/* 标题输入框 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      话题标题 <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formTitle}
                      onChange={(e) => setFormTitle(e.target.value)}
                      placeholder="请输入话题标题"
                      maxLength={100}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm"
                    />
                  </div>

                  {/* 活动ID输入框 */}
                  <div>
                    <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                      活动ID <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="number"
                      value={formActivityId}
                      onChange={(e) => setFormActivityId(e.target.value)}
                      placeholder="请输入关联的活动ID"
                      disabled={editingId !== null}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                    />
                    {editingId !== null && (
                      <p className="text-xs text-gray-400 mt-1">编辑模式下不可修改活动ID</p>
                    )}
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
                        更新话题
                      </>
                    ) : (
                      <>
                        <Plus size={16} />
                        添加话题
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
              确定要删除话题 <span className="font-semibold text-[#4C1D95]">「{deleteTargetTitle}」</span> 吗？删除后相关数据将无法恢复。
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
