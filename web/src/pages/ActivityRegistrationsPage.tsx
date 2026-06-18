import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Users,
  CheckCircle,
  XCircle,
  Clock,
  Loader2,
  ShieldCheck,
  AlertTriangle,
  User,
  ArrowLeft,
} from 'lucide-react';
import { getActivityById } from '@/api/activity';
import { getActivityRegistrations, updateRegistrationStatus } from '@/api/registration';
import type { Activity } from '@/types/activity';
import type { RegistrationResponse } from '@/types/registration';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/** 确认弹窗属性接口 */
interface ConfirmModalProps {
  /** 是否显示弹窗 */
  open: boolean;
  /** 弹窗标题 */
  title: string;
  /** 弹窗描述信息 */
  message: string;
  /** 确认按钮文本 */
  confirmText?: string;
  /** 取消按钮文本 */
  cancelText?: string;
  /** 确认回调 */
  onConfirm: () => void;
  /** 取消回调 */
  onCancel: () => void;
}

/**
 * 自定义确认弹窗组件 - 替代原生 confirm()
 */
function ConfirmModal({ open, title, message, confirmText = '确认', cancelText = '取消', onConfirm, onCancel }: ConfirmModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[9998] flex items-center justify-center">
      {/* 遮罩层 */}
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm animate-fadeIn" onClick={onCancel} />
      {/* 弹窗主体 */}
      <div className="relative bg-white rounded-2xl shadow-modal p-6 w-full max-w-md mx-4 animate-scaleIn">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 bg-red-50 rounded-xl flex items-center justify-center flex-shrink-0">
            <AlertTriangle size={20} className="text-red-500" />
          </div>
          <h3 className="text-lg font-heading font-semibold text-text-primary">{title}</h3>
        </div>
        <p className="text-gray-500 text-sm mb-6 leading-relaxed">{message}</p>
        <div className="flex items-center justify-end gap-3">
          <button
            onClick={onCancel}
            className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-xl transition-colors duration-200 cursor-pointer font-medium"
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-2 text-sm bg-red-500 text-white rounded-xl hover:bg-red-600 transition-colors duration-200 cursor-pointer shadow-button font-medium"
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

/**
 * 活动报名管理页面组件 - 管理指定活动的报名记录
 */
export default function ActivityRegistrationsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [activity, setActivity] = useState<Activity | null>(null);
  const [registrations, setRegistrations] = useState<RegistrationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [confirmModal, setConfirmModal] = useState<{
    open: boolean;
    title: string;
    message: string;
    onConfirm: () => void;
  }>({ open: false, title: '', message: '', onConfirm: () => {} });

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  /**
   * 加载活动信息和报名列表数据
   */
  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const activityRes = await getActivityById(Number(id));
      setActivity(activityRes.data.data);

      const regRes = await getActivityRegistrations(Number(id), 1, 100);
      const list = regRes.data.data?.list || [];
      setRegistrations(list);
    } catch (err) {
      console.error('加载失败', err);
      addToast('error', '加载数据失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 处理确认报名操作
   * @param registrationId - 报名记录ID
   */
  const handleConfirm = async (registrationId: number) => {
    setActionLoading(registrationId);
    try {
      await updateRegistrationStatus({ registrationId, status: 'confirmed' });
      addToast('success', '已确认报名');
      loadData();
    } catch (err: any) {
      addToast('error', err.message || '操作失败');
    } finally {
      setActionLoading(null);
    }
  };

  /**
   * 处理取消报名操作 - 弹出自定义确认弹窗
   * @param registrationId - 报名记录ID
   */
  const handleCancel = (registrationId: number) => {
    setConfirmModal({
      open: true,
      title: '取消报名',
      message: '确定要取消该报名吗？取消后用户将收到通知。',
      onConfirm: () => {
        setConfirmModal((prev) => ({ ...prev, open: false }));
        performCancel(registrationId);
      },
    });
  };

  /**
   * 执行取消报名的 API 调用
   * @param registrationId - 报名记录ID
   */
  const performCancel = async (registrationId: number) => {
    setActionLoading(registrationId);
    try {
      await updateRegistrationStatus({ registrationId, status: 'cancelled' });
      addToast('success', '已取消报名');
      loadData();
    } catch (err: any) {
      addToast('error', err.message || '操作失败');
    } finally {
      setActionLoading(null);
    }
  };

  /**
   * 获取报名状态对应的徽章组件
   * @param status - 报名状态
   */
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'confirmed':
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-accent-50 text-accent-600 text-xs font-medium rounded-lg border border-accent-200">
            <CheckCircle size={14} />
            已确认
          </span>
        );
      case 'cancelled':
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-red-50 text-red-600 text-xs font-medium rounded-lg border border-red-200">
            <XCircle size={14} />
            已取消
          </span>
        );
      case 'pending':
      default:
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-yellow-50 text-yellow-600 text-xs font-medium rounded-lg border border-yellow-200">
            <Clock size={14} />
            待确认
          </span>
        );
    }
  };

  /**
   * 格式化日期为中文格式
   * @param dateStr - 日期字符串
   */
  const formatDate = (dateStr: string) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('zh-CN');
  };

  /** 计算报名统计数据 */
  const stats = {
    total: registrations.length,
    confirmed: registrations.filter(r => r.status === 'confirmed').length,
    pending: registrations.filter(r => r.status === 'pending').length,
    cancelled: registrations.filter(r => r.status === 'cancelled').length,
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface-50">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="animate-spin text-primary-600" size={40} />
          <p className="text-text-muted font-body text-sm">加载报名数据中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      <Navbar />

      {/* 渐变横幅头部 */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-500 to-secondary-400 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate(-1)}
              className="cursor-pointer flex items-center gap-2 px-4 py-2.5 glass-dark text-white rounded-xl hover:bg-white/20 transition-all duration-200 group"
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-0.5 transition-transform duration-200" />
              返回
            </button>
          </div>
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
              <ShieldCheck size={28} className="text-white" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-heading font-bold">
                {activity?.title || '报名管理'}
              </h1>
              {activity && (
                <p className="text-white/70 text-sm mt-1">
                  最多 {activity.maxParticipants} 人 · {activity.location}
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-4 pb-12">
        {/* 统计卡片 */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          {/* 总报名 */}
          <div className="bg-gradient-to-br from-primary-500 to-primary-600 rounded-2xl p-5 text-white shadow-card animate-fadeInUp">
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <Users size={20} />
              </div>
            </div>
            <p className="text-3xl font-heading font-bold">{stats.total}</p>
            <p className="text-white/70 text-sm mt-1">总报名</p>
          </div>

          {/* 已确认 */}
          <div className="bg-gradient-to-br from-accent-500 to-emerald-500 rounded-2xl p-5 text-white shadow-card animate-fadeInUp" style={{ animationDelay: '80ms' }}>
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <CheckCircle size={20} />
              </div>
            </div>
            <p className="text-3xl font-heading font-bold">{stats.confirmed}</p>
            <p className="text-white/70 text-sm mt-1">已确认</p>
          </div>

          {/* 待确认 */}
          <div className="bg-gradient-to-br from-yellow-400 to-amber-500 rounded-2xl p-5 text-white shadow-card animate-fadeInUp" style={{ animationDelay: '160ms' }}>
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <Clock size={20} />
              </div>
            </div>
            <p className="text-3xl font-heading font-bold">{stats.pending}</p>
            <p className="text-white/70 text-sm mt-1">待确认</p>
          </div>

          {/* 已取消 */}
          <div className="bg-gradient-to-br from-red-400 to-rose-500 rounded-2xl p-5 text-white shadow-card animate-fadeInUp" style={{ animationDelay: '240ms' }}>
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <XCircle size={20} />
              </div>
            </div>
            <p className="text-3xl font-heading font-bold">{stats.cancelled}</p>
            <p className="text-white/70 text-sm mt-1">已取消</p>
          </div>
        </div>

        {/* 报名列表 */}
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl shadow-card overflow-hidden animate-fadeInUp" style={{ animationDelay: '300ms' }}>
          <div className="p-6 border-b border-primary-100">
            <h2 className="text-lg font-heading font-semibold text-text-primary">报名列表</h2>
          </div>

          {registrations.length === 0 ? (
            /* 空状态 */
            <div className="p-16 text-center">
              <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
                <Users size={36} className="text-primary-300" />
              </div>
              <h3 className="text-xl font-heading font-semibold text-text-primary mb-2">
                暂无报名记录
              </h3>
              <p className="text-gray-400 text-sm">
                还没有用户报名此活动
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-surface-50">
                    <th className="px-6 py-4 text-left text-xs font-medium text-text-muted uppercase tracking-wider">用户</th>
                    <th className="px-6 py-4 text-left text-xs font-medium text-text-muted uppercase tracking-wider">状态</th>
                    <th className="px-6 py-4 text-left text-xs font-medium text-text-muted uppercase tracking-wider">报名时间</th>
                    <th className="px-6 py-4 text-right text-xs font-medium text-text-muted uppercase tracking-wider">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-primary-50">
                  {registrations.map((reg) => (
                    <tr key={reg.id} className="hover:bg-surface-50/50 transition-colors duration-200">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary-400 to-secondary-400 flex items-center justify-center flex-shrink-0">
                            <User size={16} className="text-white" />
                          </div>
                          <span className="font-medium text-text-primary">
                            {reg.userName || '未知用户'}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(reg.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(reg.registrationTime)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-2">
                          {reg.status === 'pending' && (
                            <button
                              onClick={() => handleConfirm(reg.id)}
                              disabled={actionLoading === reg.id}
                              className="px-3 py-1.5 text-xs font-medium text-accent-600 bg-accent-50 hover:bg-accent-100 border border-accent-200 rounded-lg transition-colors duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {actionLoading === reg.id ? '处理中...' : '确认'}
                            </button>
                          )}
                          {reg.status === 'cancelled' && (
                            <button
                              onClick={() => handleConfirm(reg.id)}
                              disabled={actionLoading === reg.id}
                              className="px-3 py-1.5 text-xs font-medium text-primary-600 bg-primary-50 hover:bg-primary-100 border border-primary-200 rounded-lg transition-colors duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {actionLoading === reg.id ? '处理中...' : '恢复'}
                            </button>
                          )}
                          {reg.status !== 'cancelled' && (
                            <button
                              onClick={() => handleCancel(reg.id)}
                              disabled={actionLoading === reg.id}
                              className="px-3 py-1.5 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 border border-red-200 rounded-lg transition-colors duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              取消
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* 自定义确认弹窗 */}
      <ConfirmModal
        open={confirmModal.open}
        title={confirmModal.title}
        message={confirmModal.message}
        confirmText="确认取消"
        cancelText="返回"
        onConfirm={confirmModal.onConfirm}
        onCancel={() => setConfirmModal((prev) => ({ ...prev, open: false }))}
      />

      <Toast />
    </div>
  );
}
