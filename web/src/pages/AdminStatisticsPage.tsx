import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BarChart3, Users, Calendar, TrendingUp } from 'lucide-react';
import { getOverviewStatistics, getActivityStatistics, getUserStatistics, getRegistrationStatistics, getTrendStatistics } from '@/api/admin';

export default function AdminStatisticsPage() {
  const navigate = useNavigate();
  const [overview, setOverview] = useState<any>(null);
  const [activityStats, setActivityStats] = useState<any>(null);
  const [userStats, setUserStats] = useState<any>(null);
  const [registrationStats, setRegistrationStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAdmin();
    loadStatistics();
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
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-gray-500">加载中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <button onClick={() => navigate('/admin')} className="text-gray-600 hover:text-indigo-600">
                ← 返回
              </button>
              <h1 className="text-xl font-bold text-indigo-600">数据统计</h1>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 概览统计 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-100 rounded-lg">
                <Users className="text-blue-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">用户总数</p>
                <p className="text-2xl font-bold text-gray-900">{overview?.totalUsers || 0}</p>
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
                <p className="text-2xl font-bold text-gray-900">{overview?.totalActivities || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-purple-100 rounded-lg">
                <TrendingUp className="text-purple-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">报名总数</p>
                <p className="text-2xl font-bold text-gray-900">{overview?.totalRegistrations || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-yellow-100 rounded-lg">
                <BarChart3 className="text-yellow-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">收藏总数</p>
                <p className="text-2xl font-bold text-gray-900">{overview?.totalCollects || 0}</p>
              </div>
            </div>
          </div>
        </div>

        {/* 活动统计 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold mb-4">活动统计</h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">进行中</span>
                <span className="font-semibold text-green-600">{activityStats?.published || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">待审核</span>
                <span className="font-semibold text-yellow-600">{activityStats?.pending || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">已结束</span>
                <span className="font-semibold text-gray-600">{activityStats?.ended || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">已取消</span>
                <span className="font-semibold text-red-600">{activityStats?.cancelled || 0}</span>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold mb-4">用户统计</h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">普通用户</span>
                <span className="font-semibold">{userStats?.userCount || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">发布者</span>
                <span className="font-semibold">{userStats?.publisherCount || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">管理员</span>
                <span className="font-semibold">{userStats?.adminCount || 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">禁用用户</span>
                <span className="font-semibold text-red-600">{userStats?.disabledCount || 0}</span>
              </div>
            </div>
          </div>
        </div>

        {/* 报名统计 */}
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-4">报名统计</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">总报名人数</p>
              <p className="text-2xl font-bold text-gray-900">{registrationStats?.totalRegistrations || 0}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">今日报名</p>
              <p className="text-2xl font-bold text-green-600">{registrationStats?.todayRegistrations || 0}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">本周报名</p>
              <p className="text-2xl font-bold text-blue-600">{registrationStats?.weekRegistrations || 0}</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">本月报名</p>
              <p className="text-2xl font-bold text-purple-600">{registrationStats?.monthRegistrations || 0}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
