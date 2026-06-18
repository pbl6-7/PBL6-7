import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart3,
  Users,
  Calendar,
  TrendingUp,
  Loader2,
  GraduationCap,
  ArrowUpRight,
  Star,
  Clock,
  CheckCircle,
  XCircle,
  UserCheck,
  Shield,
  UserX,
} from 'lucide-react';
import {
  getOverviewStatistics,
  getActivityStatistics,
  getUserStatistics,
  getRegistrationStatistics,
} from '@/api/admin';
import type { OverviewStatistics, ActivityStatistics, UserStatistics, RegistrationStatistics } from '@/types/admin';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台数据统计页面
 * 展示系统概览统计、活动统计、用户统计和报名统计的可视化数据
 */
export default function AdminStatisticsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [overview, setOverview] = useState<OverviewStatistics | null>(null);
  const [activityStats, setActivityStats] = useState<ActivityStatistics | null>(null);
  const [userStats, setUserStats] = useState<UserStatistics | null>(null);
  const [registrationStats, setRegistrationStats] = useState<RegistrationStatistics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAdmin();
    loadStatistics();
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
   * 加载所有统计数据
   */
  const loadStatistics = async () => {
    setLoading(true);
    try {
      const [overviewRes, activityRes, userRes, registrationRes] = await Promise.all([
        getOverviewStatistics(),
        getActivityStatistics(),
        getUserStatistics(),
        getRegistrationStatistics(),
      ]);
      setOverview(overviewRes.data.data);
      setActivityStats(activityRes.data.data);
      setUserStats(userRes.data.data);
      setRegistrationStats(registrationRes.data.data);
    } catch (err) {
      console.error('加载统计数据失败', err);
      addToast('error', '加载统计数据失败');
    } finally {
      setLoading(false);
    }
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
              <h1 className="text-xl font-bold text-[#4C1D95]">数据统计</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          {/* 概览统计卡片 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
            <OverviewStatCard
              icon={<Users size={24} />}
              label="用户总数"
              value={overview?.totalUsers || 0}
              gradient="from-violet-500 to-purple-600"
              subLabel={`7日新增 ${overview?.newUsers7Days || 0}`}
            />
            <OverviewStatCard
              icon={<Calendar size={24} />}
              label="活动总数"
              value={overview?.totalActivities || 0}
              gradient="from-green-500 to-emerald-600"
              subLabel={`7日新增 ${overview?.newActivities7Days || 0}`}
            />
            <OverviewStatCard
              icon={<TrendingUp size={24} />}
              label="报名总数"
              value={overview?.totalRegistrations || 0}
              gradient="from-blue-500 to-cyan-600"
              subLabel={`7日新增 ${overview?.newRegistrations7Days || 0}`}
            />
            <OverviewStatCard
              icon={<Star size={24} />}
              label="今日新增活动"
              value={overview?.todayActivities || 0}
              gradient="from-amber-500 to-orange-600"
              subLabel={`今日报名 ${overview?.todayRegistrations || 0}`}
            />
          </div>

          {/* 活动统计和用户统计 */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            {/* 活动统计 - 柱状图 */}
            <div className="bg-white rounded-2xl shadow-card p-6">
              <h2 className="text-lg font-semibold text-[#4C1D95] mb-6">活动统计</h2>
              <div className="flex items-end justify-around h-48 mb-4">
                <BarChartColumn
                  label="已发布"
                  value={activityStats?.publishedActivities || 0}
                  color="bg-green-500"
                  maxValue={Math.max(activityStats?.publishedActivities || 0, activityStats?.draftActivities || 0, activityStats?.cancelledActivities || 0, activityStats?.endedActivities || 0, 1)}
                />
                <BarChartColumn
                  label="草稿"
                  value={activityStats?.draftActivities || 0}
                  color="bg-amber-500"
                  maxValue={Math.max(activityStats?.publishedActivities || 0, activityStats?.draftActivities || 0, activityStats?.cancelledActivities || 0, activityStats?.endedActivities || 0, 1)}
                />
                <BarChartColumn
                  label="已结束"
                  value={activityStats?.endedActivities || 0}
                  color="bg-blue-500"
                  maxValue={Math.max(activityStats?.publishedActivities || 0, activityStats?.draftActivities || 0, activityStats?.cancelledActivities || 0, activityStats?.endedActivities || 0, 1)}
                />
                <BarChartColumn
                  label="已取消"
                  value={activityStats?.cancelledActivities || 0}
                  color="bg-red-500"
                  maxValue={Math.max(activityStats?.publishedActivities || 0, activityStats?.draftActivities || 0, activityStats?.cancelledActivities || 0, activityStats?.endedActivities || 0, 1)}
                />
              </div>
              {/* 活动状态列表 */}
              <div className="grid grid-cols-2 gap-4">
                <StatItem
                  icon={<CheckCircle size={18} className="text-green-500" />}
                  label="已发布"
                  value={activityStats?.publishedActivities || 0}
                />
                <StatItem
                  icon={<Clock size={18} className="text-amber-500" />}
                  label="草稿"
                  value={activityStats?.draftActivities || 0}
                />
                <StatItem
                  icon={<TrendingUp size={18} className="text-blue-500" />}
                  label="总浏览量"
                  value={activityStats?.totalViews || 0}
                />
                <StatItem
                  icon={<XCircle size={18} className="text-red-500" />}
                  label="已取消"
                  value={activityStats?.cancelledActivities || 0}
                />
              </div>
            </div>

            {/* 用户统计 - 饼图 */}
            <div className="bg-white rounded-2xl shadow-card p-6">
              <h2 className="text-lg font-semibold text-[#4C1D95] mb-6">用户统计</h2>
              <div className="flex items-center justify-center mb-6">
                <PieChart
                  data={[
                    { value: userStats?.roleDistribution?.USER || 0, color: '#7C3AED', label: '普通用户' },
                    { value: userStats?.roleDistribution?.PUBLISHER || 0, color: '#22C55E', label: '发布者' },
                    { value: userStats?.roleDistribution?.ADMIN || 0, color: '#3B82F6', label: '管理员' },
                  ]}
                  size={160}
                />
              </div>
              {/* 用户角色列表 */}
              <div className="grid grid-cols-2 gap-4">
                <StatItem
                  icon={<Users size={18} className="text-violet-500" />}
                  label="用户总数"
                  value={userStats?.totalUsers || 0}
                />
                <StatItem
                  icon={<UserCheck size={18} className="text-green-500" />}
                  label="活跃用户"
                  value={userStats?.activeUsers || 0}
                />
                <StatItem
                  icon={<Shield size={18} className="text-blue-500" />}
                  label="今日新增"
                  value={userStats?.newUsersToday || 0}
                />
                <StatItem
                  icon={<UserX size={18} className="text-red-500" />}
                  label="不活跃用户"
                  value={userStats?.inactiveUsers || 0}
                />
              </div>
            </div>
          </div>

          {/* 报名统计 */}
          <div className="bg-white rounded-2xl shadow-card p-6">
            <h2 className="text-lg font-semibold text-[#4C1D95] mb-6">报名统计</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-5">
              <RegistrationCard
                icon={<BarChart3 size={24} />}
                label="总报名人数"
                value={registrationStats?.totalRegistrations || 0}
                color="violet"
              />
              <RegistrationCard
                icon={<TrendingUp size={24} />}
                label="待确认"
                value={registrationStats?.pendingRegistrations || 0}
                color="green"
                highlight={true}
              />
              <RegistrationCard
                icon={<Calendar size={24} />}
                label="已确认"
                value={registrationStats?.approvedRegistrations || 0}
                color="blue"
              />
              <RegistrationCard
                icon={<Clock size={24} />}
                label="已拒绝"
                value={registrationStats?.rejectedRegistrations || 0}
                color="amber"
              />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

/**
 * 概览统计卡片组件（带渐变背景和子标签）
 */
function OverviewStatCard({
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
  return (
    <div className={`bg-gradient-to-br ${gradient} rounded-2xl p-5 text-white shadow-lg`}>
      <div className="flex items-center justify-between">
        <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">{icon}</div>
        <div className="flex items-center gap-1 px-2 py-1 bg-white/20 rounded-full text-xs font-medium">
          <ArrowUpRight size={12} />
          {subLabel}
        </div>
      </div>
      <div className="mt-4">
        <p className="text-white/80 text-sm">{label}</p>
        <p className="text-3xl font-bold mt-1">{value.toLocaleString()}</p>
      </div>
    </div>
  );
}

/**
 * 柱状图列组件
 */
function BarChartColumn({
  label,
  value,
  color,
  maxValue,
}: {
  label: string;
  value: number;
  color: string;
  maxValue: number;
}) {
  const heightPercent = Math.max((value / maxValue) * 100, 5);

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="relative w-12 h-32 bg-violet-50 rounded-xl overflow-hidden">
        <div
          className={`absolute bottom-0 left-0 right-0 ${color} rounded-xl transition-all duration-500`}
          style={{ height: `${heightPercent}%` }}
        />
        <span className="absolute inset-0 flex items-center justify-center text-sm font-semibold text-[#4C1D95]">
          {value}
        </span>
      </div>
      <span className="text-xs text-gray-500 font-medium">{label}</span>
    </div>
  );
}

/**
 * 饼图组件（使用 CSS conic-gradient）
 */
function PieChart({
  data,
  size,
}: {
  data: { value: number; color: string; label: string }[];
  size: number;
}) {
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (total === 0) {
    return (
      <div
        className="rounded-full bg-violet-100 flex items-center justify-center"
        style={{ width: size, height: size }}
      >
        <span className="text-sm text-gray-400">暂无数据</span>
      </div>
    );
  }

  let cumulativePercent = 0;
  const gradientStops = data.map((item) => {
    const percent = (item.value / total) * 100;
    const start = cumulativePercent;
    cumulativePercent += percent;
    return `${item.color} ${start}% ${cumulativePercent}%`;
  });

  return (
    <div
      className="rounded-full shadow-lg"
      style={{
        width: size,
        height: size,
        background: `conic-gradient(${gradientStops.join(', ')})`,
      }}
    >
      <div
        className="rounded-full bg-white flex items-center justify-center"
        style={{
          width: size * 0.6,
          height: size * 0.6,
          margin: size * 0.2,
        }}
      >
        <span className="text-lg font-bold text-[#4C1D95]">{total}</span>
      </div>
    </div>
  );
}

/**
 * 统计项组件
 */
function StatItem({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
}) {
  return (
    <div className="flex items-center gap-3 p-3 bg-violet-50 rounded-xl">
      <div className="p-2 bg-white rounded-lg shadow-sm">{icon}</div>
      <div className="flex-1">
        <p className="text-xs text-gray-500">{label}</p>
        <p className="text-lg font-semibold text-[#4C1D95]">{value}</p>
      </div>
    </div>
  );
}

/**
 * 报名统计卡片组件
 */
function RegistrationCard({
  icon,
  label,
  value,
  color,
  highlight = false,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
  color: 'violet' | 'green' | 'blue' | 'amber';
  highlight?: boolean;
}) {
  const colors = {
    violet: {
      bg: 'bg-violet-50',
      iconBg: 'bg-violet-100',
      iconColor: 'text-violet-600',
      valueColor: 'text-violet-700',
    },
    green: {
      bg: 'bg-green-50',
      iconBg: 'bg-green-100',
      iconColor: 'text-green-600',
      valueColor: 'text-green-700',
    },
    blue: {
      bg: 'bg-blue-50',
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
      valueColor: 'text-blue-700',
    },
    amber: {
      bg: 'bg-amber-50',
      iconBg: 'bg-amber-100',
      iconColor: 'text-amber-600',
      valueColor: 'text-amber-700',
    },
  };

  const config = colors[color];

  return (
    <div
      className={`p-5 rounded-xl ${config.bg} ${highlight ? 'ring-2 ring-green-300 ring-offset-2' : ''} transition-all duration-200`}
    >
      <div className="flex items-center gap-3 mb-3">
        <div className={`p-2.5 ${config.iconBg} rounded-xl`}>
          <span className={config.iconColor}>{icon}</span>
        </div>
        {highlight && (
          <span className="px-2 py-1 bg-green-500 text-white rounded-full text-xs font-medium">热门</span>
        )}
      </div>
      <p className="text-sm text-gray-500">{label}</p>
      <p className={`text-2xl font-bold mt-1 ${config.valueColor}`}>{value.toLocaleString()}</p>
    </div>
  );
}