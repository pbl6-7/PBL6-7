import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Users, Calendar, BarChart3, Settings, LogOut, Bell, Loader2, CheckCircle, XCircle, Clock } from 'lucide-react';
import { getPendingActivities, approveActivity, rejectActivity, getApprovalStatistics, auditActivity } from '@/api/admin';
import { getOverviewStatistics, getActivityStatistics, getUserStatistics } from '@/api/admin';
import type { Activity } from '@/types/activity';
import type { OverviewStatistics, ActivityStatistics, UserStatistics } from '@/types/admin';

export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [pendingActivities, setPendingActivities] = useState<Activity[]>([]);
  const [overviewStats, setOverviewStats] = useState<OverviewStatistics | null>(null);
  const [activityStats, setActivityStats] = useState<ActivityStatistics | null>(null);
  const [userStats, setUserStats] = useState<UserStatistics | null>(null);
  const [processing, setProcessing] = useState<number | null>(null);

  useEffect(() => {
    checkAdmin();
    loadData();
  }, []);

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
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id: number) => {
    setProcessing(id);
    try {
      await auditActivity(id, true);
      loadData();
    } catch (err) {
      console.error('审核失败', err);
    } finally {
      setProcessing(null);
    }
  };

  const handleReject = async (id: number) => {
    const reason = prompt('请输入拒绝原因：');
    if (reason === null) return;
    setProcessing(id);
    try {
      await auditActivity(id, false, reason);
      loadData();
    } catch (err) {
      console.error('审核失败', err);
    } finally {
      setProcessing(null);
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="animate-spin text-indigo-600" size={32} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* 顶部导航 */}
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <h1 className="text-xl font-bold text-indigo-600">管理后台</h1>
              <span className="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm">
                管理员
              </span>
            </div>
            <div className="flex items-center gap-4">
              <Link
                to="/notifications"
                className="p-2 text-gray-600 hover:text-indigo-600 transition"
              >
                <Bell size={20} />
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 text-gray-600 hover:text-red-600 transition"
              >
                <LogOut size={18} />
                退出
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-100 rounded-lg">
                <Users className="text-blue-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">用户总数</p>
                <p className="text-2xl font-bold text-gray-900">{overviewStats?.totalUsers || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-green-100 rounded-lg">
                <Calendar className="text-green-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">活动总数</p>
                <p className="text-2xl font-bold text-gray-900">{overviewStats?.totalActivities || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-purple-100 rounded-lg">
                <BarChart3 className="text-purple-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">报名总数</p>
                <p className="text-2xl font-bold text-gray-900">{overviewStats?.totalRegistrations || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-yellow-100 rounded-lg">
                <Clock className="text-yellow-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">待审核</p>
                <p className="text-2xl font-bold text-gray-900">{pendingActivities.length}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 待审核活动 */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-sm">
              <div className="p-6 border-b border-gray-100">
                <h2 className="text-lg font-semibold">待审核活动</h2>
              </div>
              <div className="p-6">
                {pendingActivities.length > 0 ? (
                  <div className="space-y-4">
                    {pendingActivities.map((activity) => (
                      <div
                        key={activity.id}
                        className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
                      >
                        <div className="flex-1">
                          <h3 className="font-medium text-gray-900">{activity.title}</h3>
                          <p className="text-sm text-gray-500 mt-1">
                            发布者: {activity.publisherName} · {formatDate(activity.createdAt)}
                          </p>
                          <p className="text-sm text-gray-500">
                            地点: {activity.location} · 人数: {activity.maxParticipants}
                          </p>
                        </div>
                        <div className="flex gap-2 ml-4">
                          <button
                            onClick={() => handleApprove(activity.id)}
                            disabled={processing === activity.id}
                            className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition disabled:opacity-50"
                            title="通过"
                          >
                            <CheckCircle size={20} />
                          </button>
                          <button
                            onClick={() => handleReject(activity.id)}
                            disabled={processing === activity.id}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition disabled:opacity-50"
                            title="拒绝"
                          >
                            <XCircle size={20} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <CheckCircle className="mx-auto text-green-400 mb-2" size={32} />
                    <p>暂无待审核活动</p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 快捷链接 */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
              <h2 className="text-lg font-semibold mb-4">管理菜单</h2>
              <div className="space-y-2">
                <Link
                  to="/admin/users"
                  className="flex items-center gap-3 p-3 text-gray-700 hover:bg-gray-50 rounded-lg transition"
                >
                  <Users size={18} />
                  用户管理
                </Link>
                <Link
                  to="/admin/activities"
                  className="flex items-center gap-3 p-3 text-gray-700 hover:bg-gray-50 rounded-lg transition"
                >
                  <Calendar size={18} />
                  活动管理
                </Link>
                <Link
                  to="/admin/statistics"
                  className="flex items-center gap-3 p-3 text-gray-700 hover:bg-gray-50 rounded-lg transition"
                >
                  <BarChart3 size={18} />
                  数据统计
                </Link>
                <Link
                  to="/admin/settings"
                  className="flex items-center gap-3 p-3 text-gray-700 hover:bg-gray-50 rounded-lg transition"
                >
                  <Settings size={18} />
                  系统设置
                </Link>
              </div>
            </div>

            {/* 活动统计 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4">活动统计</h2>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">进行中</span>
                  <span className="font-semibold text-green-600">{activityStats?.publishedActivities || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">待发布</span>
                  <span className="font-semibold text-yellow-600">{activityStats?.draftActivities || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">已结束</span>
                  <span className="font-semibold text-gray-600">0</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">已取消</span>
                  <span className="font-semibold text-red-600">{activityStats?.cancelledActivities || 0}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
