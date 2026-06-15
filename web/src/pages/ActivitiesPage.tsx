import { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { Search, Calendar, MapPin, Users, ChevronLeft, ChevronRight, Loader2, ArrowLeft } from 'lucide-react';
import { getActivityList } from '@/api/activity';
import type { Activity, ActivityPageResponse, TagResponse } from '@/types/activity';

export default function ActivitiesPage() {
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

  // 是否有搜索条件
  const hasFilters = keyword || selectedTagName || status;

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
    } finally {
      setLoading(false);
    }
  };
  
  // 收集所有活动的标签名称（用于筛选下拉框）
  const allTagNames = [...new Set(
    allActivities.flatMap(activity => activity.tags?.map(tag => tag.name) || [])
  )].sort();
  
  // 根据标签名称筛选活动
  const filteredActivities = selectedTagName
    ? allActivities.filter(activity => 
        activity.tags && activity.tags.some(tag => tag.name === selectedTagName)
      )
    : allActivities;

  useEffect(() => {
    loadActivities();
  }, [page, typeId, status, keyword]);

  // 搜索处理
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

  // 格式化日期
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // 获取状态标签
  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      published: 'bg-green-100 text-green-700',
      pending: 'bg-yellow-100 text-yellow-700',
      cancelled: 'bg-red-100 text-red-700',
      ended: 'bg-gray-100 text-gray-700',
    };
    return styles[status] || 'bg-gray-100 text-gray-700';
  };

  const getStatusText = (status: string, approvalStatus: string) => {
    if (approvalStatus === 'pending') return '审核中';
    const texts: Record<string, string> = {
      published: '进行中',
      pending: '待发布',
      cancelled: '已取消',
      ended: '已结束',
    };
    return texts[status] || status;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 头部 */}
      <div className="bg-indigo-600 text-white py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate('/', { replace: true })}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/30 rounded-lg transition"
            >
              <ArrowLeft size={20} />
              返回
            </button>
            <h1 className="text-3xl font-bold">活动列表</h1>
          </div>
          <p className="text-indigo-100">发现精彩活动，参与校园生活</p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 搜索和筛选 */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-4">
            {/* 搜索框 */}
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="搜索活动名称..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                />
              </div>
            </div>

            {/* 状态筛选 */}
            <select
              value={status}
              onChange={(e) => { setStatus(e.target.value); setPage(1); }}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            >
              <option value="">全部状态</option>
              <option value="published">进行中</option>
              <option value="pending">待发布</option>
              <option value="ended">已结束</option>
              <option value="cancelled">已取消</option>
            </select>

            {/* 标签筛选 */}
            <select
              value={selectedTagName}
              onChange={(e) => { setSelectedTagName(e.target.value); setPage(1); }}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            >
              <option value="">全部标签</option>
              {allTagNames.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>

            <button
              type="submit"
              className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition flex items-center justify-center gap-2"
            >
              <Search size={18} />
              搜索
            </button>
          </form>
        </div>

        {/* 结果统计 */}
        <div className="flex justify-between items-center mb-4">
          <p className="text-gray-600">
            共找到 <span className="font-semibold text-indigo-600">{filteredActivities.length}</span> 个活动
            {selectedTagName && <span className="text-gray-500">（已按标签「{selectedTagName}」筛选）</span>}
          </p>
        </div>

        {/* 活动列表 */}
        {loading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="animate-spin text-indigo-600" size={32} />
          </div>
        ) : filteredActivities.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {filteredActivities.map((activity) => (
                <Link
                  key={activity.id}
                  to={`/activities/${activity.id}`}
                  className="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition group"
                >
                  {/* 封面 */}
                  <div className="h-36 bg-gradient-to-br from-indigo-400 to-purple-500 relative">
                    <div className="absolute top-3 right-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusBadge(activity.approvalStatus || activity.status)}`}>
                        {getStatusText(activity.status, activity.approvalStatus || '')}
                      </span>
                    </div>
                  </div>

                  {/* 内容 */}
                  <div className="p-4">
                    <h3 className="font-semibold text-gray-900 mb-2 group-hover:text-indigo-600 transition line-clamp-2">
                      {activity.title}
                    </h3>

                    <div className="space-y-1 text-sm text-gray-500">
                      <div className="flex items-center gap-2">
                        <Calendar size={14} />
                        <span>{formatDate(activity.startTime)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin size={14} />
                        <span className="truncate">{activity.location}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Users size={14} />
                        <span>{activity.maxParticipants}人</span>
                      </div>
                    </div>

                    {activity.tags && activity.tags.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-3">
                        {activity.tags.slice(0, 2).map((tag) => (
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
          </>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">未找到相关活动</p>
            <p className="text-sm mt-2">试试其他搜索条件</p>
          </div>
        )}
      </div>
    </div>
  );
}
