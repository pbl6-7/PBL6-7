import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Calendar,
  CheckCircle,
  XCircle,
  Clock,
  Eye,
  Loader2,
  GraduationCap,
  MapPin,
  Users,
  LayoutGrid,
  List,
  Trash2,
  FileEdit,
  PlayCircle,
  Ban,
  Flag,
} from 'lucide-react';
import { getActivitiesByApprovalStatus, getActivitiesByStatus, auditActivity, deleteActivityAdmin } from '@/api/admin';
import type { Activity } from '@/types/activity';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/** 筛选维度类型：审核状态或活动状态 */
type FilterDimension = 'approval' | 'status';

/**
 * 管理后台活动管理页面
 * 提供活动列表展示、筛选（审核状态/活动状态）、审核通过/拒绝、删除功能
 */
export default function AdminActivitiesPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterDimension, setFilterDimension] = useState<FilterDimension>('approval');
  const [statusFilter, setStatusFilter] = useState('all');
  const [processing, setProcessing] = useState<number | null>(null);
  const [rejectModal, setRejectModal] = useState<{ id: number; title: string } | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ id: number; title: string } | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  useEffect(() => {
    checkAdmin();
  }, []);

  useEffect(() => {
    loadActivities();
  }, [statusFilter, filterDimension]);

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
   * 加载活动列表数据
   */
  const loadActivities = async () => {
    setLoading(true);
    try {
      if (filterDimension === 'approval') {
        if (statusFilter === 'all') {
          const [pending, approved, rejected] = await Promise.all([
            getActivitiesByApprovalStatus('pending'),
            getActivitiesByApprovalStatus('approved'),
            getActivitiesByApprovalStatus('rejected'),
          ]);
          setActivities([
            ...(pending.data.data || []),
            ...(approved.data.data || []),
            ...(rejected.data.data || []),
          ]);
        } else {
          const res = await getActivitiesByApprovalStatus(statusFilter);
          setActivities(res.data.data || []);
        }
      } else {
        if (statusFilter === 'all') {
          const [draft, published, cancelled, ended] = await Promise.all([
            getActivitiesByStatus('draft'),
            getActivitiesByStatus('published'),
            getActivitiesByStatus('cancelled'),
            getActivitiesByStatus('ended'),
          ]);
          setActivities([
            ...(draft.data.data || []),
            ...(published.data.data || []),
            ...(cancelled.data.data || []),
            ...(ended.data.data || []),
          ]);
        } else {
          const res = await getActivitiesByStatus(statusFilter);
          setActivities(res.data.data || []);
        }
      }
    } catch (err) {
      console.error('加载活动失败', err);
      addToast('error', '加载活动列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 切换筛选维度时重置筛选条件
   */
  const handleDimensionChange = (dim: FilterDimension) => {
    setFilterDimension(dim);
    setStatusFilter('all');
  };

  /**
   * 打开拒绝原因弹窗
   */
  const openRejectModal = (activity: Activity) => {
    setRejectModal({ id: activity.id, title: activity.title });
    setRejectReason('');
  };

  /**
   * 审核活动（通过或拒绝）
   */
  const handleAudit = async (id: number, approved: boolean, reason?: string) => {
    setProcessing(id);
    try {
      await auditActivity(id, approved, reason);
      addToast('success', approved ? '活动已通过审核' : '活动已拒绝');
      if (!approved) setRejectModal(null);
      loadActivities();
    } catch (err) {
      console.error('审核失败', err);
      addToast('error', '审核失败，请重试');
    } finally {
      setProcessing(null);
    }
  };

  /**
   * 确认拒绝活动
   */
  const confirmReject = async () => {
    if (!rejectModal) return;
    if (!rejectReason.trim()) {
      addToast('warning', '请输入拒绝原因');
      return;
    }
    await handleAudit(rejectModal.id, false, rejectReason);
  };

  /**
   * 删除活动
   */
  const handleDelete = async (id: number) => {
    setProcessing(id);
    try {
      await deleteActivityAdmin(id);
      addToast('success', '活动已删除');
      setDeleteModal(null);
      loadActivities();
    } catch (err) {
      console.error('删除失败', err);
      addToast('error', '删除活动失败，请重试');
    } finally {
      setProcessing(null);
    }
  };

  /**
   * 格式化日期显示
   */
  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  };

  /** 审核状态筛选标签配置 */
  const approvalFilterTabs = [
    { key: 'all', label: '全部', icon: <LayoutGrid size={16} /> },
    { key: 'pending', label: '待审核', icon: <Clock size={16} /> },
    { key: 'approved', label: '已通过', icon: <CheckCircle size={16} /> },
    { key: 'rejected', label: '已驳回', icon: <XCircle size={16} /> },
  ];

  /** 活动状态筛选标签配置 */
  const statusFilterTabs = [
    { key: 'all', label: '全部', icon: <LayoutGrid size={16} /> },
    { key: 'draft', label: '草稿', icon: <FileEdit size={16} /> },
    { key: 'published', label: '进行中', icon: <PlayCircle size={16} /> },
    { key: 'cancelled', label: '已取消', icon: <Ban size={16} /> },
    { key: 'ended', label: '已结束', icon: <Flag size={16} /> },
  ];

  const currentTabs = filterDimension === 'approval' ? approvalFilterTabs : statusFilterTabs;

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
              <h1 className="text-xl font-bold text-[#4C1D95]">活动管理</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="bg-white rounded-2xl shadow-card overflow-hidden">
            {/* 筛选标签区域 */}
            <div className="px-6 py-5 border-b border-violet-100">
              {/* 维度切换 */}
              <div className="flex items-center justify-between gap-4 mb-4">
                <div className="flex items-center gap-2 bg-violet-50 rounded-xl p-1">
                  <button
                    onClick={() => handleDimensionChange('approval')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer ${
                      filterDimension === 'approval'
                        ? 'bg-violet-600 text-white shadow-md'
                        : 'text-[#4C1D95] hover:bg-violet-100'
                    }`}
                  >
                    按审核状态
                  </button>
                  <button
                    onClick={() => handleDimensionChange('status')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer ${
                      filterDimension === 'status'
                        ? 'bg-violet-600 text-white shadow-md'
                        : 'text-[#4C1D95] hover:bg-violet-100'
                    }`}
                  >
                    按活动状态
                  </button>
                </div>

                {/* 视图切换 */}
                <div className="flex items-center gap-1 bg-violet-50 rounded-xl p-1">
                  <button
                    onClick={() => setViewMode('grid')}
                    className={`p-2 rounded-lg transition-colors duration-200 cursor-pointer ${
                      viewMode === 'grid' ? 'bg-violet-600 text-white' : 'text-[#4C1D95] hover:bg-violet-100'
                    }`}
                    title="卡片视图"
                  >
                    <LayoutGrid size={18} />
                  </button>
                  <button
                    onClick={() => setViewMode('list')}
                    className={`p-2 rounded-lg transition-colors duration-200 cursor-pointer ${
                      viewMode === 'list' ? 'bg-violet-600 text-white' : 'text-[#4C1D95] hover:bg-violet-100'
                    }`}
                    title="列表视图"
                  >
                    <List size={18} />
                  </button>
                </div>
              </div>

              {/* Pill 样式筛选标签 */}
              <div className="flex items-center gap-2">
                {currentTabs.map((tab) => (
                  <button
                    key={tab.key}
                    onClick={() => setStatusFilter(tab.key)}
                    className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer ${
                      statusFilter === tab.key
                        ? 'bg-violet-600 text-white shadow-md'
                        : 'bg-violet-50 text-[#4C1D95] hover:bg-violet-100'
                    }`}
                  >
                    {tab.icon}
                    {tab.label}
                  </button>
                ))}
              </div>

              <p className="mt-3 text-sm text-gray-500">
                共 <span className="font-semibold text-[#4C1D95]">{activities.length}</span> 个活动
              </p>
            </div>

            {/* 活动列表 */}
            <div className="p-6">
              {loading ? (
                <div className="flex flex-col items-center justify-center py-16 gap-3">
                  <Loader2 className="animate-spin text-violet-600" size={32} />
                  <p className="text-gray-500">加载中...</p>
                </div>
              ) : activities.length === 0 ? (
                /* 空状态展示 */
                <div className="flex flex-col items-center justify-center py-16 gap-4">
                  <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                    <Calendar className="text-violet-400" size={36} />
                  </div>
                  <div className="text-center">
                    <p className="text-lg font-medium text-[#4C1D95]">暂无活动数据</p>
                    <p className="text-sm text-gray-400 mt-1">
                      {statusFilter !== 'all' ? '当前筛选条件下没有活动' : '目前还没有创建活动'}
                    </p>
                  </div>
                </div>
              ) : viewMode === 'grid' ? (
                /* 卡片视图 */
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  {activities.map((activity) => (
                    <ActivityCard
                      key={activity.id}
                      activity={activity}
                      formatDate={formatDate}
                      filterDimension={filterDimension}
                      onView={() => navigate(`/activities/${activity.id}`)}
                      onApprove={() => handleAudit(activity.id, true)}
                      onReject={() => openRejectModal(activity)}
                      onDelete={() => setDeleteModal({ id: activity.id, title: activity.title })}
                      isProcessing={processing === activity.id}
                    />
                  ))}
                </div>
              ) : (
                /* 列表视图 */
                <div className="space-y-3">
                  {activities.map((activity) => (
                    <ActivityListItem
                      key={activity.id}
                      activity={activity}
                      formatDate={formatDate}
                      filterDimension={filterDimension}
                      onView={() => navigate(`/activities/${activity.id}`)}
                      onApprove={() => handleAudit(activity.id, true)}
                      onReject={() => openRejectModal(activity)}
                      onDelete={() => setDeleteModal({ id: activity.id, title: activity.title })}
                      isProcessing={processing === activity.id}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>
        </main>
      </div>

      {/* 拒绝原因弹窗 */}
      {rejectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
            <div className="px-6 py-4 border-b border-violet-100">
              <h3 className="text-lg font-semibold text-[#4C1D95]">拒绝活动</h3>
              <p className="text-sm text-gray-500 mt-1">{rejectModal.title}</p>
            </div>
            <div className="p-6">
              <label className="block text-sm font-medium text-[#4C1D95] mb-2">拒绝原因</label>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="请输入拒绝原因..."
                rows={4}
                className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent resize-none transition-colors duration-200"
              />
            </div>
            <div className="px-6 py-4 bg-violet-50/50 flex justify-end gap-3">
              <button
                onClick={() => setRejectModal(null)}
                className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-xl transition-colors duration-200 cursor-pointer"
              >
                取消
              </button>
              <button
                onClick={confirmReject}
                disabled={processing === rejectModal.id}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              >
                {processing === rejectModal.id ? '处理中...' : '确认拒绝'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 删除确认弹窗 */}
      {deleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
            <div className="px-6 py-4 border-b border-violet-100">
              <h3 className="text-lg font-semibold text-[#4C1D95]">删除活动</h3>
              <p className="text-sm text-gray-500 mt-1">{deleteModal.title}</p>
            </div>
            <div className="p-6">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center shrink-0 mt-0.5">
                  <Trash2 className="text-red-500" size={20} />
                </div>
                <p className="text-gray-600">
                  确定要删除此活动吗？此操作不可撤销，活动将被永久移除。
                </p>
              </div>
            </div>
            <div className="px-6 py-4 bg-violet-50/50 flex justify-end gap-3">
              <button
                onClick={() => setDeleteModal(null)}
                className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-xl transition-colors duration-200 cursor-pointer"
              >
                取消
              </button>
              <button
                onClick={() => handleDelete(deleteModal.id)}
                disabled={processing === deleteModal.id}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              >
                {processing === deleteModal.id ? '删除中...' : '确认删除'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * 审核状态徽章组件（基于 approvalStatus）
 */
function ApprovalStatusBadge({ approvalStatus }: { approvalStatus: string }) {
  const config: Record<string, { bg: string; text: string; icon: React.ReactNode; label: string }> = {
    approved: {
      bg: 'bg-green-100',
      text: 'text-green-700',
      icon: <CheckCircle size={14} />,
      label: '已通过',
    },
    pending: {
      bg: 'bg-amber-100',
      text: 'text-amber-700',
      icon: <Clock size={14} />,
      label: '待审核',
    },
    rejected: {
      bg: 'bg-red-100',
      text: 'text-red-700',
      icon: <XCircle size={14} />,
      label: '已驳回',
    },
  };

  const style = config[approvalStatus] || config.pending;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold ${style.bg} ${style.text}`}
    >
      {style.icon}
      {style.label}
    </span>
  );
}

/**
 * 活动状态徽章组件（基于 status）
 */
function ActivityStatusBadge({ status }: { status: string }) {
  const config: Record<string, { bg: string; text: string; icon: React.ReactNode; label: string }> = {
    draft: {
      bg: 'bg-gray-100',
      text: 'text-gray-700',
      icon: <FileEdit size={14} />,
      label: '草稿',
    },
    published: {
      bg: 'bg-green-100',
      text: 'text-green-700',
      icon: <PlayCircle size={14} />,
      label: '进行中',
    },
    cancelled: {
      bg: 'bg-red-100',
      text: 'text-red-700',
      icon: <Ban size={14} />,
      label: '已取消',
    },
    ended: {
      bg: 'bg-blue-100',
      text: 'text-blue-700',
      icon: <Flag size={14} />,
      label: '已结束',
    },
  };

  const style = config[status] || config.draft;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold ${style.bg} ${style.text}`}
    >
      {style.icon}
      {style.label}
    </span>
  );
}

/**
 * 活动卡片组件（网格视图）
 */
function ActivityCard({
  activity,
  formatDate,
  filterDimension,
  onView,
  onApprove,
  onReject,
  onDelete,
  isProcessing,
}: {
  activity: Activity;
  formatDate: (date: string) => string;
  filterDimension: FilterDimension;
  onView: () => void;
  onApprove: () => void;
  onReject: () => void;
  onDelete: () => void;
  isProcessing: boolean;
}) {
  const showAuditButtons = filterDimension === 'approval' && activity.approvalStatus === 'pending';

  return (
    <div className="bg-gradient-to-br from-white to-violet-50/30 border border-violet-100 rounded-2xl p-5 hover:shadow-lg transition-all duration-200 group">
      {/* 标题和状态 */}
      <div className="flex items-start justify-between gap-2 mb-3">
        <h3 className="font-semibold text-[#4C1D95] line-clamp-2 group-hover:text-violet-600 transition-colors duration-200">
          {activity.title}
        </h3>
        {filterDimension === 'approval' ? (
          <ApprovalStatusBadge approvalStatus={activity.approvalStatus} />
        ) : (
          <ActivityStatusBadge status={activity.status} />
        )}
      </div>

      {/* 发布者信息 */}
      <p className="text-sm text-gray-500 mb-3">
        发布者: <span className="font-medium text-gray-700">{activity.publisherName}</span>
      </p>

      {/* 活动详情 */}
      <div className="space-y-2 mb-4">
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <Calendar size={14} className="text-violet-400" />
          <span>{formatDate(activity.startTime)}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <MapPin size={14} className="text-violet-400" />
          <span>{activity.location}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <Users size={14} className="text-violet-400" />
          <span>人数: {activity.maxParticipants}</span>
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="flex items-center gap-2 pt-3 border-t border-violet-100">
        <button
          onClick={onView}
          className="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 bg-violet-50 text-violet-600 hover:bg-violet-100 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer"
        >
          <Eye size={16} />
          查看
        </button>
        {showAuditButtons && (
          <>
            <button
              onClick={onApprove}
              disabled={isProcessing}
              className="flex items-center justify-center gap-1 px-3 py-2 bg-green-500 hover:bg-green-600 text-white rounded-xl text-sm font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              title="通过"
            >
              {isProcessing ? <Loader2 size={16} className="animate-spin" /> : <CheckCircle size={16} />}
            </button>
            <button
              onClick={onReject}
              disabled={isProcessing}
              className="flex items-center justify-center gap-1 px-3 py-2 bg-amber-500 hover:bg-amber-600 text-white rounded-xl text-sm font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              title="拒绝"
            >
              <XCircle size={16} />
            </button>
          </>
        )}
        <button
          onClick={onDelete}
          disabled={isProcessing}
          className="flex items-center justify-center gap-1 px-3 py-2 bg-red-500 hover:bg-red-600 text-white rounded-xl text-sm font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
          title="删除"
        >
          <Trash2 size={16} />
        </button>
      </div>
    </div>
  );
}

/**
 * 活动列表项组件（列表视图）
 */
function ActivityListItem({
  activity,
  formatDate,
  filterDimension,
  onView,
  onApprove,
  onReject,
  onDelete,
  isProcessing,
}: {
  activity: Activity;
  formatDate: (date: string) => string;
  filterDimension: FilterDimension;
  onView: () => void;
  onApprove: () => void;
  onReject: () => void;
  onDelete: () => void;
  isProcessing: boolean;
}) {
  const showAuditButtons = filterDimension === 'approval' && activity.approvalStatus === 'pending';

  return (
    <div className="flex items-center justify-between gap-4 p-4 bg-gradient-to-r from-white to-violet-50/30 border border-violet-100 rounded-xl hover:shadow-md transition-all duration-200">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-3 mb-2">
          <h3 className="font-semibold text-[#4C1D95] truncate">{activity.title}</h3>
          {filterDimension === 'approval' ? (
            <ApprovalStatusBadge approvalStatus={activity.approvalStatus} />
          ) : (
            <ActivityStatusBadge status={activity.status} />
          )}
        </div>
        <div className="flex items-center gap-4 text-sm text-gray-500">
          <span className="flex items-center gap-1">
            <Calendar size={14} className="text-violet-400" />
            {formatDate(activity.startTime)}
          </span>
          <span className="flex items-center gap-1">
            <MapPin size={14} className="text-violet-400" />
            {activity.location}
          </span>
          <span className="flex items-center gap-1">
            <Users size={14} className="text-violet-400" />
            {activity.maxParticipants}人
          </span>
        </div>
      </div>

      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={onView}
          className="p-2 text-violet-600 hover:bg-violet-50 rounded-xl transition-colors duration-200 cursor-pointer"
          title="查看详情"
        >
          <Eye size={18} />
        </button>
        {showAuditButtons && (
          <>
            <button
              onClick={onApprove}
              disabled={isProcessing}
              className="p-2 bg-green-500 hover:bg-green-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              title="通过"
            >
              {isProcessing ? <Loader2 size={18} className="animate-spin" /> : <CheckCircle size={18} />}
            </button>
            <button
              onClick={onReject}
              disabled={isProcessing}
              className="p-2 bg-amber-500 hover:bg-amber-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              title="拒绝"
            >
              <XCircle size={18} />
            </button>
          </>
        )}
        <button
          onClick={onDelete}
          disabled={isProcessing}
          className="p-2 bg-red-500 hover:bg-red-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
          title="删除"
        >
          <Trash2 size={18} />
        </button>
      </div>
    </div>
  );
}
