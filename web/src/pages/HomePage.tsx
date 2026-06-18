import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Calendar,
  Users,
  MapPin,
  Sparkles,
  ArrowRight,
  TrendingUp,
  Clock,
  Flame,
} from 'lucide-react';
import { getActivityList } from '@/api/activity';
import { getAllActivityTypes, type ActivityTypeResponse } from '@/api/activityType';
import { getHotSearches } from '@/api/search';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';
import type { Activity } from '@/types/activity';
import type { LoginResponse } from '@/types/user';

/**
 * 校园活动平台首页组件
 * 展示活动列表、热门搜索、Hero 区域等核心内容
 */
export default function HomePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [totalActivities, setTotalActivities] = useState(0);
  const [hotSearches, setHotSearches] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activityTypes, setActivityTypes] = useState<ActivityTypeResponse[]>([]);
  const [selectedTypeId, setSelectedTypeId] = useState<number | undefined>(undefined);

  /* 初始化：从 localStorage 读取用户信息 */
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  /* 加载活动列表 */
  useEffect(() => {
    loadActivities();
  }, [selectedTypeId]);

  /* 加载活动类型列表 */
  useEffect(() => {
    loadActivityTypes();
  }, []);

  /* 加载热门搜索 */
  useEffect(() => {
    loadHotSearches();
  }, []);

  /**
   * 加载活动列表数据
   */
  const loadActivities = async () => {
    setLoading(true);
    try {
      const res = await getActivityList({
        page: 1,
        size: 10,
        typeId: selectedTypeId,
      });
      setActivities(res.data.data.list);
      setTotalActivities(res.data.data.total || 0);
    } catch (err) {
      console.error('加载活动列表失败', err);
      useToastStore.getState().addToast('error', '加载活动列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载活动类型列表
   */
  const loadActivityTypes = async () => {
    try {
      const res = await getAllActivityTypes();
      setActivityTypes(res.data.data || []);
    } catch (err) {
      console.error('加载活动类型失败', err);
    }
  };

  /**
   * 加载热门搜索关键词
   */
  const loadHotSearches = async () => {
    try {
      const res = await getHotSearches();
      setHotSearches(res.data.data || []);
    } catch (err) {
      console.error('加载热门搜索失败', err);
    }
  };

  /**
   * 处理搜索提交
   * @param keyword - 搜索关键词
   */
  const handleSearch = (keyword: string) => {
    setSearchKeyword(keyword);
    if (keyword.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(keyword.trim())}`);
    }
  };

  /**
   * 格式化日期显示
   * @param dateStr - 日期字符串
   * @returns 格式化后的日期文本
   */
  const formatDate = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  /**
   * 获取活动状态指示器配置
   * @param activity - 活动对象
   * @returns 状态标签的样式和文本配置
   */
  const getActivityStatus = (activity: Activity): { text: string; style: string; icon: React.ElementType } => {
    const now = new Date();
    const startTime = new Date(activity.startTime);
    const endTime = new Date(activity.endTime);

    if (activity.approvalStatus === 'pending') {
      return { text: '审核中', style: 'bg-yellow-100 text-yellow-700 border-yellow-200', icon: Clock };
    }
    if (activity.status === 'cancelled') {
      return { text: '已取消', style: 'bg-gray-100 text-gray-600 border-gray-200', icon: Clock };
    }
    if (now < startTime) {
      return { text: '即将开始', style: 'bg-blue-100 text-blue-700 border-blue-200', icon: Clock };
    }
    if (now >= startTime && now <= endTime) {
      return { text: '进行中', style: 'bg-green-100 text-green-700 border-green-200', icon: Sparkles };
    }
    return { text: '已结束', style: 'bg-gray-100 text-gray-600 border-gray-200', icon: Clock };
  };

  /**
   * 根据活动 ID 获取卡片封面渐变色
   * @param id - 活动ID
   * @returns 渐变色类名
   */
  const getCardGradient = (id: number): string => {
    const gradients = [
      'from-violet-400 to-purple-600',
      'from-blue-400 to-indigo-600',
      'from-pink-400 to-rose-600',
      'from-teal-400 to-cyan-600',
      'from-orange-400 to-amber-600',
      'from-emerald-400 to-green-600',
      'from-fuchsia-400 to-pink-600',
      'from-sky-400 to-blue-600',
    ];
    return gradients[id % gradients.length];
  };

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      {/* 导航栏 */}
      <Navbar searchKeyword={searchKeyword} onSearch={handleSearch} />

      {/* 热门搜索 */}
      {hotSearches.length > 0 && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-6">
          <div className="flex items-center gap-3 flex-wrap">
            <div className="flex items-center gap-1.5 text-sm text-text-muted">
              <Flame size={14} className="text-accent-500" />
              <span>热门搜索</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {hotSearches.map((keyword, index) => (
                <Link
                  key={index}
                  to={`/search?keyword=${encodeURIComponent(keyword)}`}
                  className="px-3 py-1.5 bg-gradient-to-r from-primary-100 to-primary-50 text-primary-700 rounded-full text-sm font-medium hover:from-primary-200 hover:to-primary-100 transition-colors duration-200 cursor-pointer border border-primary-200/50"
                >
                  {keyword}
                </Link>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* 主内容 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Hero 区域 */}
        <div className="relative bg-gradient-to-br from-primary-600 via-primary-700 to-primary-900 rounded-3xl p-8 md:p-12 mb-10 overflow-hidden">
          {/* 动画装饰元素 */}
          <div className="absolute inset-0 overflow-hidden">
            {/* 浮动圆形 1 */}
            <div className="absolute -top-20 -right-20 w-64 h-64 bg-white/10 rounded-full blur-3xl animate-pulse-soft" />
            {/* 浮动圆形 2 */}
            <div className="absolute top-1/2 -left-32 w-48 h-48 bg-accent-500/20 rounded-full blur-2xl animate-pulse-soft" style={{ animationDelay: '1s' }} />
            {/* 浮动圆形 3 */}
            <div className="absolute -bottom-16 right-1/4 w-32 h-32 bg-primary-400/30 rounded-full blur-xl animate-pulse-soft" style={{ animationDelay: '0.5s' }} />
            {/* 小装饰点 */}
            <div className="absolute top-8 left-8 w-4 h-4 bg-white/20 rounded-full" />
            <div className="absolute top-16 right-16 w-3 h-3 bg-accent-400/40 rounded-full" />
            <div className="absolute bottom-12 left-1/3 w-5 h-5 bg-primary-300/30 rounded-full" />
          </div>

          {/* 内容 */}
          <div className="relative z-10">
            <h1 className="text-3xl md:text-5xl font-heading font-bold mb-4 text-white">
              <span className="bg-gradient-to-r from-white via-primary-100 to-accent-400 bg-clip-text text-transparent">
                发现精彩校园活动
              </span>
            </h1>
            <p className="text-lg md:text-xl text-primary-100 mb-8 max-w-xl font-body">
              参与精彩活动，丰富校园生活，结识志同道合的伙伴
            </p>

            {/* 统计计数器 */}
            <div className="flex flex-wrap gap-6 mb-8">
              <div className="flex items-center gap-2 bg-white/10 backdrop-blur-sm rounded-xl px-4 py-2">
                <Calendar size={20} className="text-accent-400" />
                <div>
                  <div className="text-2xl font-bold text-white">{totalActivities}</div>
                  <div className="text-xs text-primary-200">活动数量</div>
                </div>
              </div>
              <div className="flex items-center gap-2 bg-white/10 backdrop-blur-sm rounded-xl px-4 py-2">
                <Users size={20} className="text-accent-400" />
                <div>
                  <div className="text-2xl font-bold text-white">
                    {activities.reduce((sum, a) => sum + (a.currentParticipants || 0), 0)}
                  </div>
                  <div className="text-xs text-primary-200">参与人次</div>
                </div>
              </div>
              <div className="flex items-center gap-2 bg-white/10 backdrop-blur-sm rounded-xl px-4 py-2">
                <TrendingUp size={20} className="text-accent-400" />
                <div>
                  <div className="text-2xl font-bold text-white">98%</div>
                  <div className="text-xs text-primary-200">好评率</div>
                </div>
              </div>
            </div>

            {/* CTA 按钮 */}
            <div className="flex flex-wrap gap-4">
              <Link
                to="/activities"
                className="group flex items-center gap-2 px-6 py-3 bg-white text-primary-700 rounded-xl font-semibold hover:bg-primary-50 transition-colors duration-200 cursor-pointer shadow-button hover:shadow-button-hover"
              >
                <Sparkles size={18} />
                浏览全部活动
                <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform duration-200" />
              </Link>
              {user && (
                <Link
                  to="/activities/create"
                  className="group flex items-center gap-2 px-6 py-3 bg-accent-500 text-white rounded-xl font-semibold hover:bg-accent-600 transition-colors duration-200 cursor-pointer shadow-button hover:shadow-glow-accent"
                >
                  <Calendar size={18} />
                  发布活动
                </Link>
              )}
            </div>
          </div>
        </div>

        {/* 活动分类导航 */}
        {activityTypes.length > 0 && (
          <div className="mb-10">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-heading font-bold text-text-primary">活动分类</h2>
              <Link
                to="/activities"
                className="flex items-center gap-1 text-primary-600 hover:text-primary-700 font-medium text-sm transition-colors duration-200 cursor-pointer"
              >
                查看全部
                <ArrowRight size={14} />
              </Link>
            </div>
            <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
              {/* 全部按钮 */}
              <button
                onClick={() => setSelectedTypeId(undefined)}
                className={`cursor-pointer flex items-center gap-2 px-5 py-3 rounded-2xl font-medium transition-all duration-200 whitespace-nowrap shrink-0 ${
                  selectedTypeId === undefined
                    ? 'bg-primary-600 text-white shadow-button hover:bg-primary-700'
                    : 'bg-white text-text-secondary border border-primary-200/50 hover:bg-primary-50 hover:border-primary-300 shadow-sm'
                }`}
              >
                <Sparkles size={16} />
                全部
              </button>
              {/* 各分类按钮 */}
              {activityTypes.map((type) => (
                <button
                  key={type.id}
                  onClick={() => setSelectedTypeId(type.id)}
                  className={`cursor-pointer flex items-center gap-2 px-5 py-3 rounded-2xl font-medium transition-all duration-200 whitespace-nowrap shrink-0 ${
                    selectedTypeId === type.id
                      ? 'bg-primary-600 text-white shadow-button hover:bg-primary-700'
                      : 'bg-white text-text-secondary border border-primary-200/50 hover:bg-primary-50 hover:border-primary-300 shadow-sm'
                  }`}
                >
                  {type.name}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* 活动列表 */}
        <div className="mb-12">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-heading font-bold text-text-primary">
              {selectedTypeId ? activityTypes.find(t => t.id === selectedTypeId)?.name || '最新活动' : '最新活动'}
            </h2>
            <Link
              to="/activities"
              className="flex items-center gap-1 text-primary-600 hover:text-primary-700 font-medium transition-colors duration-200 cursor-pointer"
            >
              查看更多
              <ArrowRight size={16} />
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="bg-white rounded-2xl shadow-card p-6 animate-pulse">
                  <div className="h-40 bg-gradient-to-br from-primary-200 to-primary-100 rounded-xl mb-4" />
                  <div className="h-6 bg-primary-100 rounded-lg mb-2" />
                  <div className="h-4 bg-primary-50 rounded-lg w-2/3" />
                </div>
              ))}
            </div>
          ) : activities.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {activities.map((activity) => {
                const status = getActivityStatus(activity);
                const StatusIcon = status.icon;
                return (
                  <Link
                    key={activity.id}
                    to={`/activities/${activity.id}`}
                    className="group bg-white rounded-2xl shadow-card overflow-hidden hover:shadow-card-hover transition-all duration-300 cursor-pointer hover:-translate-y-1"
                  >
                    {/* 封面图 */}
                    <div className={`h-40 bg-gradient-to-br ${getCardGradient(activity.id)} relative overflow-hidden`}>
                      {/* 装饰图案 */}
                      <div className="absolute inset-0 opacity-20">
                        <div className="absolute top-4 right-4 w-16 h-16 border-2 border-white/30 rounded-full" />
                        <div className="absolute bottom-4 left-4 w-8 h-8 border border-white/20 rounded-full" />
                      </div>
                      {/* 状态标签 */}
                      <div className="absolute top-3 right-3">
                        <span className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium border ${status.style}`}>
                          <StatusIcon size={12} />
                          {status.text}
                        </span>
                      </div>
                      {/* 活动类型 */}
                      <div className="absolute bottom-3 left-3">
                        <span className="px-2.5 py-1 bg-white/90 backdrop-blur-sm rounded-full text-xs font-medium text-primary-700">
                          {activity.activityTypeName}
                        </span>
                      </div>
                    </div>

                    {/* 内容 */}
                    <div className="p-5">
                      <h3 className="text-lg font-heading font-semibold text-text-primary mb-3 group-hover:text-primary-600 transition-colors duration-200 line-clamp-2">
                        {activity.title}
                      </h3>

                      <div className="space-y-2.5 text-sm text-text-secondary">
                        <div className="flex items-center gap-2">
                          <Calendar size={14} className="text-primary-500" />
                          <span>{formatDate(activity.startTime)}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <MapPin size={14} className="text-primary-500" />
                          <span className="truncate">{activity.location}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Users size={14} className="text-primary-500" />
                          <span>
                            {/* 显示参与人数（假设有 currentParticipants 字段，若无则显示最大人数） */}
                            {activity.maxParticipants} 人可参加
                          </span>
                        </div>
                      </div>

                      {/* 标签 */}
                      {activity.tags && activity.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 mt-4">
                          {activity.tags.slice(0, 3).map((tag) => (
                            <span
                              key={tag.id}
                              className="px-2.5 py-1 bg-primary-50 text-primary-600 rounded-full text-xs font-medium border border-primary-100"
                            >
                              {tag.name}
                            </span>
                          ))}
                          {activity.tags.length > 3 && (
                            <span className="px-2.5 py-1 bg-gray-50 text-gray-500 rounded-full text-xs">
                              +{activity.tags.length - 3}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </Link>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-16 bg-white rounded-2xl shadow-card">
              <div className="w-16 h-16 mx-auto mb-4 bg-primary-100 rounded-full flex items-center justify-center">
                <Calendar size={32} className="text-primary-500" />
              </div>
              <p className="text-lg text-text-secondary mb-2">暂无活动</p>
              <p className="text-sm text-text-muted mb-6">快来发布第一个精彩活动吧</p>
              {user && (
                <Link
                  to="/activities/create"
                  className="inline-flex items-center gap-2 px-5 py-2.5 bg-accent-500 text-white rounded-xl font-medium hover:bg-accent-600 transition-colors duration-200 cursor-pointer"
                >
                  <Sparkles size={16} />
                  发布活动
                </Link>
              )}
            </div>
          )}
        </div>
      </main>

      {/* 页脚 */}
      <footer className="bg-gradient-to-br from-primary-900 to-primary-950 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {/* Logo 和简介 */}
            <div className="md:col-span-1">
              <div className="flex items-center gap-2 mb-4">
                <div className="w-10 h-10 bg-accent-500 rounded-xl flex items-center justify-center">
                  <Sparkles size={20} className="text-white" />
                </div>
                <span className="text-xl font-heading font-bold text-white">校园活动平台</span>
              </div>
              <p className="text-sm text-primary-200 mb-4">
                连接校园精彩，让每一次参与都成为难忘回忆
              </p>
              <div className="flex gap-3">
                <div className="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center hover:bg-white/20 transition-colors duration-200 cursor-pointer">
                  <Users size={16} className="text-white" />
                </div>
                <div className="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center hover:bg-white/20 transition-colors duration-200 cursor-pointer">
                  <Calendar size={16} className="text-white" />
                </div>
                <div className="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center hover:bg-white/20 transition-colors duration-200 cursor-pointer">
                  <MapPin size={16} className="text-white" />
                </div>
              </div>
            </div>

            {/* 关于我们 */}
            <div>
              <h3 className="text-sm font-semibold text-white mb-4">关于我们</h3>
              <ul className="space-y-2">
                <li>
                  <Link to="/about" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    平台介绍
                  </Link>
                </li>
                <li>
                  <Link to="/team" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    团队成员
                  </Link>
                </li>
                <li>
                  <Link to="/help" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    使用帮助
                  </Link>
                </li>
              </ul>
            </div>

            {/* 快速链接 */}
            <div>
              <h3 className="text-sm font-semibold text-white mb-4">快速链接</h3>
              <ul className="space-y-2">
                <li>
                  <Link to="/activities" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    活动列表
                  </Link>
                </li>
                <li>
                  <Link to="/activities/create" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    发布活动
                  </Link>
                </li>
                <li>
                  <Link to="/my-activities" className="text-sm text-primary-200 hover:text-white transition-colors duration-200 cursor-pointer">
                    我的活动
                  </Link>
                </li>
              </ul>
            </div>

            {/* 联系方式 */}
            <div>
              <h3 className="text-sm font-semibold text-white mb-4">联系方式</h3>
              <ul className="space-y-2">
                <li className="flex items-center gap-2 text-sm text-primary-200">
                  <MapPin size={14} />
                  <span>某某大学校园中心</span>
                </li>
                <li className="flex items-center gap-2 text-sm text-primary-200">
                  <Users size={14} />
                  <span>activity@campus.edu</span>
                </li>
                <li className="flex items-center gap-2 text-sm text-primary-200">
                  <Calendar size={14} />
                  <span>周一至周五 9:00-18:00</span>
                </li>
              </ul>
            </div>
          </div>

          {/* 底部版权 */}
          <div className="border-t border-primary-800 mt-8 pt-6">
            <div className="flex flex-col md:flex-row justify-between items-center gap-4">
              <p className="text-sm text-primary-300">
                © 2024 校园活动平台. All rights reserved.
              </p>
              <div className="flex gap-4">
                <Link to="/privacy" className="text-sm text-primary-300 hover:text-white transition-colors duration-200 cursor-pointer">
                  隐私政策
                </Link>
                <Link to="/terms" className="text-sm text-primary-300 hover:text-white transition-colors duration-200 cursor-pointer">
                  服务条款
                </Link>
              </div>
            </div>
          </div>
        </div>
      </footer>

      {/* Toast 通知组件 */}
      <Toast />
    </div>
  );
}