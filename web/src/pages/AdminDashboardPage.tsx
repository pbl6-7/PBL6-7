import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Users,
  Calendar,
  BarChart3,
  Settings,
  LogOut,
  Bell,
  Loader2,
  CheckCircle,
  XCircle,
  Clock,
  TrendingUp,
  ArrowRight,
  ArrowLeft,
  GraduationCap,
} from 'lucide-react';
import { getPendingActivities, getOverviewStatistics, getActivityStatistics, getUserStatistics, auditActivity } from '@/api/admin';
import type { Activity } from '@/types/activity';
import type { OverviewStatistics, ActivityStatistics, UserStatistics } from '@/types/admin';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台仪表盘页面
 * 展示系统概览统计、待审核活动列表和快捷管理菜单
 */
export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [loading, setLoading] = useState(true);
  const [pendingActivities, setPendingActivities] = useState<Activity[]>([]);
  const [overviewStats, setOverviewStats] = useState<OverviewStatistics | null>(null);
  const [activityStats, setActivityStats] = useState<ActivityStatistics | null>(null);
  const [userStats, setUserStats] = useState<UserStatistics | null>(null);
  const [processing, setProcessing] = useState<number | null>(null);
  const [rejectModal, setRejectModal] = useState<{ id: number; title: string } | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  useEffect(() => {
    checkAdmin();
    loadData();
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
   * 加载仪表盘数据
   */
  const loadData = async () => {
    setLoading(true);
    try {
      const [pendingRes, overviewRes, activityRes, userRes] = await Promise.all([
        getPendingActivities(),
        getOverviewStatistics(),
        getActivityStatistics(),
        getUserStatistics(),
      ]);
      setPendingActivities(pendingRes.data.data);
      setOverviewStats(overviewRes.data.data);
      setActivityStats(activityRes.data.data);
      setUserStats(userRes.data.data);
    } catch (err) {
      console.error('加载数据失败', err);
      addToast('error', '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 通过活动审核
   */
  const handleApprove = async (id: number) => {
    setProcessing(id);
    try {
      await auditActivity(id, true);
      addToast('success', '活动已通过审核');
      loadData();
    } catch (err) {
      console.error('审核失败', err);
      addToast('error', '审核失败，请重试');
    } finally {
      setProcessing(null);
    }
  };

  /**
   * 打开拒绝原因弹窗
   */
  const openRejectModal = (activity: Activity) => {
    setRejectModal({ id: activity.id, title: activity.title });
    setRejectReason('');
  };

  /**
   * 确认拒绝活动
   */
  const handleReject = async () => {
    if (!rejectModal) return;
    if (!rejectReason.trim()) {
      addToast('warning', '请输入拒绝原因');
      return;
    }
    setProcessing(rejectModal.id);
    try {
      await auditActivity(rejectModal.id, false, rejectReason);
      addToast('success', '活动已拒绝');
      setRejectModal(null);
      loadData();
    } catch (err) {
      console.error('审核失败', err);
      addToast('error', '审核失败，请重试');
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

  /**
   * 退出登录
   */
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#FAF5FF] flex items-center justify-center">
        <Loader2 className="animate-spin text-violet-600" size={40} />
      </div>
    );
  }

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
              <h1 className="text-xl font-bold text-[#4C1D95]">仪表盘</h1>
            </div>
            <div className="flex items-center gap-3">
              <Link
                to="/notifications"
                className="p-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-xl transition-colors duration-200 cursor-pointer"
              >
                <Bell size={20} />
              </Link>
              <Link
                to="/"
                className="flex items-center gap-2 px-4 py-2 text-[#4C1D95] hover:bg-violet-50 rounded-xl transition-colors duration-200 cursor-pointer"
              >
                <ArrowLeft size={18} />
                <span className="text-sm font-medium">返回前台</span>
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50 rounded-xl transition-colors duration-200 cursor-pointer"
              >
                <LogOut size={18} />
                <span className="text-sm font-medium">退出登录</span>
              </button>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          {/* 统计卡片 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
            <StatCard
              icon={<Users size={24} />}
              label="用户总数"
              value={overviewStats?.totalUsers || 0}
              gradient="from-violet-500 to-purple-600"
              subLabel={`7日新增 ${overviewStats?.newUsers7Days || 0}`}
            />
            <StatCard
              icon={<Calendar size={24} />}
              label="活动总数"
              value={overviewStats?.totalActivities || 0}
              gradient="from-green-500 to-emerald-600"
              subLabel={`7日新增 ${overviewStats?.newActivities7Days || 0}`}
            />
            <StatCard
              icon={<BarChart3 size={24} />}
              label="报名总数"
              value={overviewStats?.totalRegistrations || 0}
              gradient="from-blue-500 to-cyan-600"
              subLabel={`7日新增 ${overviewStats?.newRegistrations7Days || 0}`}
            />
            <StatCard
              icon={<Clock size={24} />}
              label="待审核"
              value={overviewStats?.pendingActivities || 0}
              gradient="from-amber-500 to-orange-600"
              subLabel={overviewStats?.pendingActivities ? '需处理' : '无待审核'}
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 待审核活动 */}
            <div className="lg:col-span-2">
              <div className="bg-white rounded-2xl shadow-card overflow-hidden">
                <div className="px-6 py-4 border-b border-violet-100 flex items-center justify-between">
                  <h2 className="text-lg font-semibold text-[#4C1D95]">待审核活动</h2>
                  {pendingActivities.length > 0 && (
                    <span className="px-3 py-1 bg-amber-100 text-amber-700 rounded-full text-sm font-medium">
                      {pendingActivities.length} 项待处理
                    </span>
                  )}
                </div>
                <div className="p-6">
                  {pendingActivities.length > 0 ? (
                    <div className="space-y-4">
                      {pendingActivities.map((activity) => (
                        <div
                          key={activity.id}
                          className="group p-4 bg-gradient-to-r from-violet-50/50 to-transparent border border-violet-100 rounded-xl hover:shadow-md transition-all duration-200"
                        >
                          <div className="flex items-start justify-between gap-4">
                            <div className="flex-1 min-w-0">
                              <h3 className="font-semibold text-[#4C1D95] truncate">{activity.title}</h3>
                              <p className="text-sm text-gray-500 mt-1">
                                发布者: {activity.publisherName} · {formatDate(activity.createdAt)}
                              </p>
                              <div className="flex items-center gap-4 mt-2 text-sm text-gray-500">
                                <span className="flex items-center gap-1">
                                  <Calendar size={14} className="text-violet-400" />
                                  {formatDate(activity.startTime)}
                                </span>
                                <span>{activity.location}</span>
                                <span>人数: {activity.maxParticipants}</span>
                              </div>
                            </div>
                            <div className="flex gap-2 shrink-0">
                              <button
                                onClick={() => handleApprove(activity.id)}
                                disabled={processing === activity.id}
                                className="flex items-center gap-1.5 px-4 py-2 bg-green-500 hover:bg-green-600 text-white rounded-xl text-sm font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
                              >
                                {processing === activity.id ? (
                                  <Loader2 size={16} className="animate-spin" />
                                ) : (
                                  <CheckCircle size={16} />
                                )}
                                通过
                              </button>
                              <button
                                onClick={() => openRejectModal(activity)}
                                disabled={processing === activity.id}
                                className="flex items-center gap-1.5 px-4 py-2 bg-white border border-red-200 text-red-600 hover:bg-red-50 rounded-xl text-sm font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
                              >
                                <XCircle size={16} />
                                拒绝
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-12">
                      <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <CheckCircle className="text-green-500" size={32} />
                      </div>
                      <p className="text-gray-500 font-medium">暂无待审核活动</p>
                      <p className="text-sm text-gray-400 mt-1">所有活动已处理完毕</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* 右侧面板 */}
            <div className="space-y-6">
              {/* 快捷链接 */}
              <div className="bg-white rounded-2xl shadow-card p-6">
                <h2 className="text-lg font-semibold text-[#4C1D95] mb-4">管理菜单</h2>
                <div className="grid grid-cols-2 gap-3">
                  <QuickLink to="/admin/users" icon={<Users size={20} />} label="用户管理" color="violet" />
                  <QuickLink to="/admin/activities" icon={<Calendar size={20} />} label="活动管理" color="green" />
                  <QuickLink to="/admin/statistics" icon={<BarChart3 size={20} />} label="数据统计" color="blue" />
                  <QuickLink to="/admin/settings" icon={<Settings size={20} />} label="系统设置" color="amber" />
                </div>
              </div>

              {/* 活动统计 */}
              <div className="bg-white rounded-2xl shadow-card p-6">
                <h2 className="text-lg font-semibold text-[#4C1D95] mb-4">活动统计</h2>
                <div className="space-y-4">
                  <StatBar label="进行中" value={activityStats?.publishedActivities || 0} color="green" />
                  <StatBar label="待发布" value={activityStats?.draftActivities || 0} color="amber" />
                  <StatBar label="已结束" value={activityStats?.endedActivities || 0} color="gray" />
                  <StatBar label="已取消" value={activityStats?.cancelledActivities || 0} color="red" />
                </div>
              </div>
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
                onClick={handleReject}
                disabled={processing === rejectModal.id}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-xl transition-colors duration-200 disabled:opacity-50 cursor-pointer"
              >
                {processing === rejectModal.id ? '处理中...' : '确认拒绝'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * 统计卡片组件
 */
function StatCard({
  icon,
  label,
  value,
  gradient,
  subLabel,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
  gradient: string;
  subLabel: string;
}) {
  const [displayValue, setDisplayValue] = useState(0);
  const valueRef = useRef(value);

  useEffect(() => {
    const target = value;
    const duration = 1000;
    const startTime = Date.now();
    const startValue = valueRef.current;

    const animate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const easeOut = 1 - Math.pow(1 - progress, 3);
      const current = Math.floor(startValue + (target - startValue) * easeOut);
      setDisplayValue(current);
      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };
    animate();
    valueRef.current = target;
  }, [value]);

  return (
    <div className={`bg-gradient-to-br ${gradient} rounded-2xl p-5 text-white shadow-lg`}>
      <div className="flex items-center justify-between">
        <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">{icon}</div>
        <div className="flex items-center gap-1 px-2 py-1 bg-white/20 rounded-full text-xs font-medium">
          <TrendingUp size={12} />
          {subLabel}
        </div>
      </div>
      <div className="mt-4">
        <p className="text-white/80 text-sm">{label}</p>
        <p className="text-3xl font-bold mt-1">{displayValue.toLocaleString()}</p>
      </div>
    </div>
  );
}

/**
 * 快捷链接卡片组件
 */
function QuickLink({
  to,
  icon,
  label,
  color,
}: {
  to: string;
  icon: React.ReactNode;
  label: string;
  color: 'violet' | 'green' | 'blue' | 'amber';
}) {
  const colors = {
    violet: 'bg-violet-50 text-violet-600 hover:bg-violet-100',
    green: 'bg-green-50 text-green-600 hover:bg-green-100',
    blue: 'bg-blue-50 text-blue-600 hover:bg-blue-100',
    amber: 'bg-amber-50 text-amber-600 hover:bg-amber-100',
  };

  return (
    <Link
      to={to}
      className={`flex flex-col items-center gap-2 p-4 rounded-xl transition-colors duration-200 cursor-pointer group ${colors[color]}`}
    >
      <div className="group-hover:scale-110 transition-transform duration-200">{icon}</div>
      <span className="text-sm font-medium">{label}</span>
    </Link>
  );
}

/**
 * 统计进度条组件
 */
function StatBar({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: 'green' | 'amber' | 'gray' | 'red';
}) {
  const colors = {
    green: 'bg-green-500',
    amber: 'bg-amber-500',
    gray: 'bg-gray-400',
    red: 'bg-red-500',
  };

  const maxValue = 100;

  return (
    <div>
      <div className="flex justify-between items-center mb-1.5">
        <span className="text-sm text-gray-600">{label}</span>
        <span className="text-sm font-semibold text-[#4C1D95]">{value}</span>
      </div>
      <div className="h-2 bg-violet-100 rounded-full overflow-hidden">
        <div
          className={`h-full ${colors[color]} rounded-full transition-all duration-500`}
          style={{ width: `${Math.min((value / maxValue) * 100, 100)}%` }}
        />
      </div>
    </div>
  );
}