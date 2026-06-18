import { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import {
  Search,
  Calendar,
  MapPin,
  Users,
  ArrowLeft,
  Filter,
  X,
  Sparkles,
  PartyPopper,
} from 'lucide-react';
import { getActivityList } from '@/api/activity';
import { getAllActivityTypes, type ActivityTypeResponse } from '@/api/activityType';
import type { Activity } from '@/types/activity';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/** 卡片渐变色配置 - 根据活动类型或 ID 分配不同渐变 */
const CARD_GRADIENTS = [
  'from-violet-500 to-purple-600',
  'from-indigo-500 to-blue-600',
  'from-fuchsia-500 to-pink-600',
  'from-emerald-500 to-teal-600',
  'from-amber-500 to-orange-600',
  'from-cyan-500 to-blue-600',
  'from-rose-500 to-red-600',
  'from-lime-500 to-green-600',
];

/**
 * 根据活动 ID 获取对应的渐变配色
 * @param activityId - 活动 ID
 * @returns Tailwind 渐变类名字符串
 */
function getCardGradient(activityId: number): string {
  return CARD_GRADIENTS[activityId % CARD_GRADIENTS.length];
}

/** 状态筛选选项配置（使用后端 status 值，非 approvalStatus） */
const STATUS_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'published', label: '进行中' },
  { value: 'draft', label: '草稿' },
  { value: 'ended', label: '已结束' },
  { value: 'cancelled', label: '已取消' },
];

/**
 * 骨架屏卡片组件 - 加载状态时展示的占位卡片
 */
function ActivitySkeletonCard() {
  return (
    <div className="bg-white rounded-2xl shadow-card overflow-hidden">
      {/* 封面骨架 */}
      <div className="h-40 animate-shimmer rounded-t-2xl" />
      {/* 内容骨架 */}
      <div className="p-5 space-y-3">
        <div className="h-5 w-3/4 animate-shimmer rounded-lg" />
        <div className="space-y-2">
          <div className="h-4 w-1/2 animate-shimmer rounded" />
          <div className="h-4 w-2/3 animate-shimmer rounded" />
          <div className="h-4 w-1/3 animate-shimmer rounded" />
        </div>
        <div className="flex gap-2 pt-2">
          <div className="h-6 w-14 animate-shimmer rounded-full" />
          <div className="h-6 w-16 animate-shimmer rounded-full" />
        </div>
      </div>
    </div>
  );
}

/**
 * 活动列表页面组件
 * 展示校园活动卡片网格，支持搜索、状态筛选和标签过滤
 */
export default function ActivitiesPage() {
  const addToast = useToastStore((s) => s.addToast);
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [allActivities, setAllActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [total, setTotal] = useState(0);

  // 筛选条件
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [typeId, setTypeId] = useState<number | undefined>(
    searchParams.get('typeId') ? Number(searchParams.get('typeId')) : undefined
  );
  const [status, setStatus] = useState(searchParams.get('status') || '');
  const [selectedTagName, setSelectedTagName] = useState(searchParams.get('tag') || '');
  const [page, setPage] = useState(Number(searchParams.get('page')) || 1);
  const [size] = useState(12);
  const [activityTypes, setActivityTypes] = useState<ActivityTypeResponse[]>([]);

  // 是否有搜索条件
  const hasFilters = keyword || selectedTagName || status || typeId;

  /**
   * 从后端加载活动列表数据
   * 使用大尺寸请求以支持前端标签筛选
   */
  const loadActivities = async () => {
    setLoading(true);
    try {
      // 加载所有活动（不带分页，以便前端标签筛选）
      const res = await getActivityList({
        page: 1,
        size: 1000,
        keyword: keyword || undefined,
        typeId,
        status: status || undefined,
      });
      const data = res.data.data;
      setAllActivities(data.list);
      setTotal(data.total);
    } catch (err) {
      console.error('加载活动列表失败', err);
      addToast('error', '加载活动列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 收集所有活动的标签名称（用于筛选下拉框）
   * @returns 去重排序后的标签名称数组
   */
  const allTagNames = [...new Set(
    allActivities.flatMap(activity => activity.tags?.map(tag => tag.name) || [])
  )].sort();

  /**
   * 根据选中的标签名称对活动进行前端过滤
   * @returns 过滤后的活动数组
   */
  const filteredActivities = selectedTagName
    ? allActivities.filter(activity =>
        activity.tags && activity.tags.some(tag => tag.name === selectedTagName)
      )
    : allActivities;

  useEffect(() => {
    loadActivities();
  }, [page, typeId, status, keyword]);

  /* 加载活动类型列表 */
  useEffect(() => {
    const loadTypes = async () => {
      try {
        const res = await getAllActivityTypes();
        setActivityTypes(res.data.data || []);
      } catch (err) {
        console.error('加载活动类型失败', err);
      }
    };
    loadTypes();
  }, []);

  /**
   * 处理搜索表单提交，同步 URL 搜索参数
   * @param e - 表单事件对象
   */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (typeId) params.set('typeId', String(typeId));
    if (status) params.set('status', status);
    if (selectedTagName) params.set('tag', selectedTagName);
    setSearchParams(params);
  };

  /**
   * 移除单个筛选条件并重置分页
   * @param filterType - 要移除的筛选项类型
   */
  const removeFilter = (filterType: 'keyword' | 'status' | 'tag') => {
    switch (filterType) {
      case 'keyword':
        setKeyword('');
        break;
      case 'status':
        setStatus('');
        break;
      case 'tag':
        setSelectedTagName('');
        break;
    }
    setPage(1);
  };

  /**
   * 清除所有筛选条件
   */
  const clearAllFilters = () => {
    setKeyword('');
    setStatus('');
    setSelectedTagName('');
    setTypeId(undefined);
    setPage(1);
    setSearchParams(new URLSearchParams());
  };

  /**
   * 格式化日期字符串为可读格式
   * @param dateStr - ISO 日期字符串
   * @returns 格式化后的日期文本
   */
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * 根据活动状态获取对应的状态徽章样式类名
   * @param status - 活动状态
   * @returns Tailwind CSS 类名字符串
   */
  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      published: 'bg-accent-500/15 text-accent-600 border-accent-500/30',
      draft: 'bg-amber-100 text-amber-700 border-amber-300/50',
      cancelled: 'bg-red-100 text-red-700 border-red-300/50',
      ended: 'bg-gray-100 text-gray-600 border-gray-300/50',
    };
    return styles[status] || 'bg-gray-100 text-gray-600 border-gray-300/50';
  };

  /**
   * 根据活动和审核状态获取显示文本
   * @param status - 活动状态
   * @param approvalStatus - 审核状态
   * @returns 状态显示文本
   */
  const getStatusText = (status: string, approvalStatus: string) => {
    if (approvalStatus === 'pending') return '审核中';
    if (approvalStatus === 'rejected') return '已驳回';
    const texts: Record<string, string> = {
      published: '进行中',
      draft: '草稿',
      cancelled: '已取消',
      ended: '已结束',
    };
    return texts[status] || status;
  };

  /**
   * 计算参与进度百分比（用于展示报名进度条）
   * @param current - 当前参与人数
   * @param max - 最大参与人数
   * @returns 百分比数值
   */
  const getProgressPercent = (current: number, max: number) => {
    if (!max) return 0;
    return Math.min(Math.round((current / max) * 100), 100);
  };

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      {/* 全局导航栏 */}
      <Navbar />

      {/* Toast 通知容器 */}
      <Toast />

      {/* ==================== 渐变头部区域 ==================== */}
      <div className="relative overflow-hidden bg-gradient-to-br from-primary-600 via-primary-700 to-primary-800 pt-8 pb-16 px-4 sm:px-6 lg:px-8">
        {/* 装饰性背景图案 */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-0 left-1/4 w-64 h-64 bg-white rounded-full blur-3xl" />
          <div className="absolute bottom-0 right-1/4 w-80 h-80 bg-violet-400 rounded-full blur-3xl" />
          <svg className="absolute bottom-0 left-0 w-full" viewBox="0 0 1440 120" fill="none" preserveAspectRatio="none">
            <path
              d="M0,96L48,90.7C96,85,192,75,288,80C384,85,480,107,576,106.7C672,107,768,85,864,74.7C960,64,1056,64,1152,74.7C1248,85,1344,107,1392,117.3L1440,128L1440,120L1392,117.3C1344,110,1248,95,1152,85.3C1056,75,960,69,864,74.7C768,80,672,96,576,101.3C480,107,384,101,288,96C192,90,96,85,48,82.7L0,80Z"
              fill="#FAF5FF"
              fillOpacity="0.15"
            />
          </svg>
        </div>

        {/* 头部内容 */}
        <div className="relative max-w-7xl mx-auto">
          <div className="flex items-center gap-4 mb-3">
            <button
              onClick={() => navigate('/', { replace: true })}
              className="cursor-pointer flex items-center gap-2 px-4 py-2.5 glass-dark text-white rounded-xl hover:bg-white/20 transition-all duration-200 group"
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-0.5 transition-transform duration-200" />
              返回首页
            </button>
          </div>
          <h1 className="font-heading text-3xl sm:text-4xl font-bold text-white mb-2 tracking-tight">
            校园活动中心
          </h1>
          <p className="text-violet-200 text-base sm:text-lg font-body">
            发现精彩活动，参与校园生活，让每一天都充满期待 ✨
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 -mt-6 relative z-10">
        {/* ==================== 玻璃效果搜索筛选栏 ==================== */}
        <div className="glass rounded-2xl shadow-card p-5 sm:p-6 mb-8 backdrop-blur-xl">
          <form onSubmit={handleSearch}>
            <div className="flex flex-col gap-4">
              {/* 搜索行 */}
              <div className="flex flex-col lg:flex-row gap-4">
                {/* 搜索输入框 - 带左侧图标 */}
                <div className="flex-1 relative">
                  <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-primary-400" size={18} />
                  <input
                    type="text"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    placeholder="搜索活动名称..."
                    className="w-full pl-11 pr-4 py-3 bg-white/60 border border-primary-200/50 rounded-xl focus:ring-2 focus:ring-primary-400/50 focus:border-primary-400 outline-none text-text-primary placeholder:text-primary-300 transition-colors duration-200"
                  />
                </div>

                {/* 标签筛选下拉 + 搜索按钮 */}
                <div className="flex items-center gap-2 flex-wrap">
                  {/* 标签筛选下拉 */}
                  <select
                    value={selectedTagName}
                    onChange={(e) => { setSelectedTagName(e.target.value); setPage(1); }}
                    className="px-4 py-2 bg-white/70 border border-primary-200/50 rounded-full text-sm text-text-secondary focus:ring-2 focus:ring-primary-400/50 focus:border-primary-400 outline-none cursor-pointer transition-colors duration-200"
                  >
                    <option value="">全部标签</option>
                    {allTagNames.map((name) => (
                      <option key={name} value={name}>{name}</option>
                    ))}
                  </select>

                  {/* 搜索按钮 */}
                  <button
                    type="submit"
                    className="cursor-pointer px-6 py-2.5 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl hover:from-primary-700 hover:to-primary-800 transition-all duration-200 shadow-button hover:shadow-button-hover flex items-center justify-center gap-2 font-medium"
                  >
                    <Search size={16} />
                    搜索
                  </button>
                </div>
              </div>

              {/* 分类筛选行 */}
              {activityTypes.length > 0 && (
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="hidden lg:flex items-center gap-1.5 text-sm text-primary-500 mr-1">
                    <Filter size={14} />
                    分类：
                  </span>
                  <button
                    type="button"
                    onClick={() => { setTypeId(undefined); setPage(1); }}
                    className={`cursor-pointer px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                      typeId === undefined
                        ? 'bg-primary-600 text-white shadow-button'
                        : 'bg-white/70 text-text-secondary hover:bg-primary-50 border border-primary-200/50'
                    }`}
                  >
                    全部
                  </button>
                  {activityTypes.map((type) => (
                    <button
                      key={type.id}
                      type="button"
                      onClick={() => { setTypeId(type.id); setPage(1); }}
                      className={`cursor-pointer px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                        typeId === type.id
                          ? 'bg-primary-600 text-white shadow-button'
                          : 'bg-white/70 text-text-secondary hover:bg-primary-50 border border-primary-200/50'
                      }`}
                    >
                      {type.name}
                    </button>
                  ))}
                </div>
              )}

              {/* 状态筛选行 */}
              <div className="flex items-center gap-2 flex-wrap">
                <span className="hidden lg:flex items-center gap-1.5 text-sm text-primary-500 mr-1">
                  <Filter size={14} />
                  状态：
                </span>
                <div className="flex gap-1.5 flex-wrap">
                  {STATUS_OPTIONS.map((opt) => (
                    <button
                      key={opt.value}
                      type="button"
                      onClick={() => { setStatus(opt.value); setPage(1); }}
                      className={`cursor-pointer px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 ${
                        status === opt.value
                          ? 'bg-primary-600 text-white shadow-button'
                          : 'bg-white/70 text-text-secondary hover:bg-primary-50 border border-primary-200/50'
                      }`}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </form>

          {/* 已激活的筛选条件指示器 */}
          {hasFilters && (
            <div className="flex items-center gap-2 mt-4 pt-4 border-t border-primary-100/50 flex-wrap">
              <span className="text-sm text-primary-400 flex items-center gap-1">
                <Sparkles size={14} />
                已选条件：
              </span>
              {keyword && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-medium">
                  关键词: {keyword}
                  <button
                    onClick={() => removeFilter('keyword')}
                    className="cursor-pointer hover:bg-primary-200 rounded-full p-0.5 transition-colors duration-200"
                  >
                    <X size={12} />
                  </button>
                </span>
              )}
              {typeId && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-medium">
                  分类: {activityTypes.find(t => t.id === typeId)?.name || typeId}
                  <button
                    onClick={() => { setTypeId(undefined); setPage(1); }}
                    className="cursor-pointer hover:bg-primary-200 rounded-full p-0.5 transition-colors duration-200"
                  >
                    <X size={12} />
                  </button>
                </span>
              )}
              {status && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-medium">
                  状态: {STATUS_OPTIONS.find(o => o.value === status)?.label}
                  <button
                    onClick={() => removeFilter('status')}
                    className="cursor-pointer hover:bg-primary-200 rounded-full p-0.5 transition-colors duration-200"
                  >
                    <X size={12} />
                  </button>
                </span>
              )}
              {selectedTagName && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-medium">
                  标签: {selectedTagName}
                  <button
                    onClick={() => removeFilter('tag')}
                    className="cursor-pointer hover:bg-primary-200 rounded-full p-0.5 transition-colors duration-200"
                  >
                    <X size={12} />
                  </button>
                </span>
              )}
              <button
                onClick={clearAllFilters}
                className="cursor-pointer text-xs text-primary-500 hover:text-primary-700 underline ml-auto transition-colors duration-200"
              >
                清除全部
              </button>
            </div>
          )}
        </div>

        {/* 结果统计栏 */}
        <div className="flex justify-between items-center mb-6">
          <p className="text-text-secondary font-body">
            共找到{' '}
            <span className="font-semibold text-primary-600 text-lg">{filteredActivities.length}</span>{' '}
            个活动
            {selectedTagName && (
              <span className="text-primary-400 text-sm ml-2">
                （已按标签「{selectedTagName}」筛选）
              </span>
            )}
          </p>
        </div>

        {/* ==================== 活动卡片网格 / 骨架屏 / 空状态 ==================== */}
        {loading ? (
          /* 加载状态 - 骨架屏卡片 */
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: 8 }).map((_, i) => (
              <ActivitySkeletonCard key={i} />
            ))}
          </div>
        ) : filteredActivities.length > 0 ? (
          /* 活动卡片网格 */
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {filteredActivities.map((activity) => (
                <Link
                  key={activity.id}
                  to={`/activities/${activity.id}`}
                  className="group cursor-pointer bg-white rounded-2xl shadow-card card-hover-lift overflow-hidden block"
                >
                  {/* 渐变封面区域 */}
                  <div className={`h-40 bg-gradient-to-br ${getCardGradient(activity.id)} relative`}>
                    {/* 状态徽章 - 带圆点指示器 */}
                    <div className="absolute top-3 right-3">
                      <span
                        className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium border backdrop-blur-sm ${getStatusBadge(activity.approvalStatus || activity.status)}`}
                      >
                        <span className={`w-1.5 h-1.5 rounded-full ${
                          activity.approvalStatus === 'pending' ? 'bg-amber-500' :
                          activity.status === 'published' ? 'bg-accent-500' :
                          activity.status === 'cancelled' ? 'bg-red-500' :
                          'bg-gray-400'
                        }`} />
                        {getStatusText(activity.status, activity.approvalStatus || '')}
                      </span>
                    </div>
                    {/* 装饰性光晕 */}
                    <div className="absolute -bottom-8 -right-8 w-24 h-24 bg-white/10 rounded-full blur-xl" />
                  </div>

                  {/* 卡片内容区域 */}
                  <div className="p-5">
                    {/* 活动标题 */}
                    <h3 className="font-heading font-semibold text-text-primary mb-3 group-hover:text-primary-600 transition-colors duration-200 line-clamp-2 leading-snug">
                      {activity.title}
                    </h3>

                    {/* 元信息行 */}
                    <div className="space-y-2 text-sm text-text-muted">
                      <div className="flex items-center gap-2">
                        <Calendar size={14} className="shrink-0 text-primary-400" />
                        <span className="truncate">{formatDate(activity.startTime)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin size={14} className="shrink-0 text-primary-400" />
                        <span className="truncate">{activity.location}</span>
                      </div>
                    </div>

                    {/* 参与人数进度条 */}
                    {activity.currentParticipants != null && activity.maxParticipants > 0 && (
                      <div className="mt-3">
                        <div className="flex items-center justify-between text-xs text-text-muted mb-1.5">
                          <span className="flex items-center gap-1">
                            <Users size={13} className="text-primary-400" />
                            已报名
                          </span>
                          <span className="font-medium text-text-primary">
                            {activity.currentParticipants}/{activity.maxParticipants}
                          </span>
                        </div>
                        <div className="w-full h-1.5 bg-primary-100 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all duration-500 ${
                              getProgressPercent(activity.currentParticipants, activity.maxParticipants) >= 90
                                ? 'bg-red-400'
                                : 'bg-gradient-to-r from-primary-500 to-accent-500'
                            }`}
                            style={{
                              width: `${getProgressPercent(activity.currentParticipants, activity.maxParticipants)}%`,
                            }}
                          />
                        </div>
                      </div>
                    )}

                    {/* 标签胶囊 */}
                    {activity.tags && activity.tags.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mt-3 pt-3 border-t border-primary-50">
                        {activity.tags.slice(0, 3).map((tag) => (
                          <span
                            key={tag.id}
                            className="px-2.5 py-0.5 bg-primary-50 text-primary-600 rounded-full text-xs font-medium hover:bg-primary-100 transition-colors duration-200"
                          >
                            #{tag.name}
                          </span>
                        ))}
                        {activity.tags.length > 3 && (
                          <span className="px-2.5 py-0.5 text-primary-400 text-xs">
                            +{activity.tags.length - 3}
                          </span>
                        )}
                      </div>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          </>
        ) : (
          /* 空状态 - 带插图和操作按钮 */
          <div className="text-center py-20 px-4">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-primary-100 rounded-full mb-6">
              <PartyPopper size={40} className="text-primary-400" />
            </div>
            <h3 className="font-heading text-xl font-semibold text-text-primary mb-2">
              未找到相关活动
            </h3>
            <p className="text-text-muted mb-6 max-w-md mx-auto">
              当前没有匹配的活动，试试调整搜索条件或者浏览其他分类吧~
            </p>
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={clearAllFilters}
                className="cursor-pointer px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 shadow-button font-medium"
              >
                清除筛选条件
              </button>
              <Link
                to="/"
                className="cursor-pointer px-6 py-2.5 bg-white text-primary-600 border border-primary-200 rounded-xl hover:bg-primary-50 transition-colors duration-200 font-medium"
              >
                返回首页
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
