import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Bell, User, LogOut, Menu, X, Calendar, Users, Star } from 'lucide-react';
import { getActivityList } from '@/api/activity';
import { getHotSearches, getSearchSuggestions } from '@/api/search';
import type { Activity, ActivityPageResponse } from '@/types/activity';
import type { LoginResponse } from '@/types/user';

export default function HomePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [hotSearches, setHotSearches] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // 加载用户信息
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  // 加载活动列表
  useEffect(() => {
    loadActivities();
  }, []);

  // 加载热门搜索
  useEffect(() => {
    loadHotSearches();
  }, []);

  const loadActivities = async () => {
    setLoading(true);
    try {
      const res = await getActivityList({ page: 1, size: 10 });
      setActivities(res.data.data.list);
    } catch (err) {
      console.error('加载活动列表失败', err);
    } finally {
      setLoading(false);
    }
  };

  const loadHotSearches = async () => {
    try {
      const res = await getHotSearches();
      setHotSearches(res.data.data || []);
    } catch (err) {
      console.error('加载热门搜索失败', err);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchKeyword.trim()) {
      navigate(`/activities?keyword=${encodeURIComponent(searchKeyword)}`);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  // 格式化日期
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // 获取状态标签样式
  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      published: 'bg-green-100 text-green-700',
      pending: 'bg-yellow-100 text-yellow-700',
      cancelled: 'bg-red-100 text-red-700',
      ended: 'bg-gray-100 text-gray-700',
    };
    return styles[status] || 'bg-gray-100 text-gray-700';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 导航栏 */}
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center">
              <Link to="/" className="text-xl font-bold text-indigo-600">
                校园活动平台
              </Link>
            </div>

            {/* 搜索框 - 桌面端 */}
            <div className="hidden md:flex flex-1 max-w-lg mx-8">
              <form onSubmit={handleSearch} className="w-full flex">
                <input
                  type="text"
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  placeholder="搜索活动..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-l-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                />
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 text-white rounded-r-lg hover:bg-indigo-700 transition"
                >
                  <Search size={18} />
                </button>
              </form>
            </div>

            {/* 导航链接 - 桌面端 */}
            <div className="hidden md:flex items-center space-x-4">
              <Link
                to="/activities"
                className="text-gray-600 hover:text-indigo-600 transition"
              >
                活动列表
              </Link>

              {user ? (
                <>
                  <Link
                    to="/notifications"
                    className="text-gray-600 hover:text-indigo-600 transition relative"
                  >
                    <Bell size={20} />
                  </Link>
                  <div className="relative group">
                    <button className="flex items-center gap-2 text-gray-600 hover:text-indigo-600 transition">
                      <User size={20} />
                      <span>{user.username}</span>
                    </button>
                    {/* 下拉菜单 */}
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-100 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all">
                      <Link
                        to="/profile"
                        className="block px-4 py-2 text-gray-700 hover:bg-gray-50"
                      >
                        个人中心
                      </Link>
                      <Link
                        to="/my-activities"
                        className="block px-4 py-2 text-gray-700 hover:bg-gray-50"
                      >
                        我的活动
                      </Link>
                      <Link
                        to="/my-registrations"
                        className="block px-4 py-2 text-gray-700 hover:bg-gray-50"
                      >
                        报名记录
                      </Link>
                      {user.role === 'ADMIN' && (
                        <Link
                          to="/admin"
                          className="block px-4 py-2 text-gray-700 hover:bg-gray-50"
                        >
                          管理后台
                        </Link>
                      )}
                      <button
                        onClick={handleLogout}
                        className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-50 flex items-center gap-2"
                      >
                        <LogOut size={16} />
                        退出登录
                      </button>
                    </div>
                  </div>
                </>
              ) : (
                <Link
                  to="/login"
                  className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                >
                  登录 / 注册
                </Link>
              )}
            </div>

            {/* 移动端菜单按钮 */}
            <button
              className="md:hidden p-2 text-gray-600"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>

          {/* 移动端菜单 */}
          {mobileMenuOpen && (
            <div className="md:hidden py-4 border-t border-gray-100">
              {/* 搜索框 */}
              <form onSubmit={handleSearch} className="mb-4 flex">
                <input
                  type="text"
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  placeholder="搜索活动..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-l-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                />
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 text-white rounded-r-lg"
                >
                  <Search size={18} />
                </button>
              </form>

              {/* 链接 */}
              <div className="space-y-2">
                <Link
                  to="/activities"
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg"
                >
                  活动列表
                </Link>
                {user ? (
                  <>
                    <Link
                      to="/notifications"
                      className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg"
                    >
                      通知
                    </Link>
                    <Link
                      to="/profile"
                      className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg"
                    >
                      个人中心
                    </Link>
                    {user.role === 'ADMIN' && (
                      <Link
                        to="/admin"
                        className="block px-4 py-2 text-indigo-600 font-medium hover:bg-gray-50 rounded-lg"
                      >
                        管理后台
                      </Link>
                    )}
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-50 rounded-lg"
                    >
                      退出登录
                    </button>
                  </>
                ) : (
                  <Link
                    to="/login"
                    className="block px-4 py-2 text-indigo-600 hover:bg-gray-50 rounded-lg"
                  >
                    登录 / 注册
                  </Link>
                )}
              </div>
            </div>
          )}
        </div>
      </nav>

      {/* 热门搜索 */}
      {hotSearches.length > 0 && (
        <div className="bg-white border-b border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-500">热门搜索:</span>
              <div className="flex flex-wrap gap-2">
                {hotSearches.map((keyword, index) => (
                  <Link
                    key={index}
                    to={`/activities?keyword=${encodeURIComponent(keyword)}`}
                    className="px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-sm hover:bg-indigo-100 hover:text-indigo-600 transition"
                  >
                    {keyword}
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 主内容 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Hero 区域 */}
        <div className="bg-gradient-to-r from-indigo-600 to-purple-600 rounded-2xl p-8 mb-8 text-white">
          <h1 className="text-3xl md:text-4xl font-bold mb-4">
            发现精彩校园活动
          </h1>
          <p className="text-lg text-indigo-100 mb-6">
            参与精彩活动，丰富校园生活，结识志同道合的伙伴
          </p>
          <div className="flex flex-wrap gap-4">
            <Link
              to="/activities"
              className="px-6 py-3 bg-white text-indigo-600 rounded-lg font-medium hover:bg-indigo-50 transition"
            >
              浏览全部活动
            </Link>
            {user && (
              <Link
                to="/activities/create"
                className="px-6 py-3 bg-indigo-500 text-white rounded-lg font-medium hover:bg-indigo-400 transition"
              >
                发布活动
              </Link>
            )}
          </div>
        </div>

        {/* 活动列表 */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">最新活动</h2>
            <Link
              to="/activities"
              className="text-indigo-600 hover:text-indigo-700 font-medium"
            >
              查看更多 →
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="bg-white rounded-xl shadow-sm p-6 animate-pulse">
                  <div className="h-40 bg-gray-200 rounded-lg mb-4"></div>
                  <div className="h-6 bg-gray-200 rounded mb-2"></div>
                  <div className="h-4 bg-gray-200 rounded w-2/3"></div>
                </div>
              ))}
            </div>
          ) : activities.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {activities.map((activity) => (
                <Link
                  key={activity.id}
                  to={`/activities/${activity.id}`}
                  className="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition group"
                >
                  {/* 封面图 */}
                  <div className="h-40 bg-gradient-to-br from-indigo-400 to-purple-500 relative">
                    <div className="absolute top-3 right-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusBadge(activity.approvalStatus || activity.status)}`}>
                        {activity.approvalStatus === 'pending' ? '审核中' :
                         activity.status === 'published' ? '进行中' :
                         activity.status === 'ended' ? '已结束' :
                         activity.status === 'cancelled' ? '已取消' : '草稿'}
                      </span>
                    </div>
                  </div>

                  {/* 内容 */}
                  <div className="p-4">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-indigo-600 transition line-clamp-2">
                      {activity.title}
                    </h3>

                    <div className="space-y-2 text-sm text-gray-600">
                      <div className="flex items-center gap-2">
                        <Calendar size={14} />
                        <span>{formatDate(activity.startTime)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Star size={14} />
                        <span>{activity.location}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Users size={14} />
                        <span>{activity.maxParticipants} 人</span>
                      </div>
                    </div>

                    {activity.tags && activity.tags.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-3">
                        {activity.tags.slice(0, 3).map((tag) => (
                          <span
                            key={tag.id}
                            className="px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded text-xs"
                          >
                            {tag.name}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-gray-500">
              <p className="text-lg">暂无活动</p>
              {user && (
                <Link
                  to="/activities/create"
                  className="mt-4 inline-block text-indigo-600 hover:text-indigo-700"
                >
                  发布第一个活动
                </Link>
              )}
            </div>
          )}
        </div>
      </main>

      {/* 页脚 */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center text-gray-500 text-sm">
            <p>© 2024 校园活动平台. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
