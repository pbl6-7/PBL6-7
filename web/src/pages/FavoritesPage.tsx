import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Heart,
  Trash2,
  Calendar,
  Loader2,
  Search,
  MapPin,
  Clock,
  Users,
  ImageOff,
} from 'lucide-react';
import { getFavorites, removeFavorite } from '@/api/favorite';
import { getMyCollects, uncollectActivity } from '@/api/collect';
import type { Activity } from '@/types/activity';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 用户收藏页面组件
 * 展示用户收藏的活动列表，支持取消收藏操作
 * 同时兼容 favorite 和 collect 两套收藏系统
 */
export default function FavoritesPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [favorites, setFavorites] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

  /** 页面初始化时加载收藏列表 */
  useEffect(() => {
    loadFavorites();
  }, []);

  /**
   * 加载用户收藏列表
   * 优先使用 favorite 接口，失败时回退到 collect 接口
   */
  const loadFavorites = async () => {
    setLoading(true);
    try {
      const res = await getFavorites();
      setFavorites(res.data.data || []);
    } catch (err) {
      // favorite 接口失败时，尝试使用 collect 接口
      try {
        const collectRes = await getMyCollects();
        setFavorites(collectRes.data.data || []);
      } catch (collectErr) {
        console.error('加载收藏列表失败', collectErr);
        addToast('error', '加载收藏列表失败，请稍后重试');
      }
    } finally {
      setLoading(false);
    }
  };

  /**
   * 取消收藏操作
   * 优先使用 removeFavorite，失败时回退到 uncollectActivity
   * @param activityId - 活动ID
   * @param e - 鼠标事件，阻止链接跳转
   */
  const handleRemoveFavorite = async (activityId: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    setRemovingIds((prev) => new Set(prev).add(activityId));
    try {
      await removeFavorite(activityId);
      setFavorites((prev) => prev.filter((a) => (a.id || (a as any).activityId) !== activityId));
      addToast('success', '已取消收藏');
    } catch (err) {
      // removeFavorite 失败时，尝试 uncollectActivity
      try {
        await uncollectActivity(activityId);
        setFavorites((prev) => prev.filter((a) => (a.id || (a as any).activityId) !== activityId));
        addToast('success', '已取消收藏');
      } catch (collectErr) {
        console.error('取消收藏失败', collectErr);
        addToast('error', '取消收藏失败，请稍后重试');
      }
    } finally {
      setRemovingIds((prev) => {
        const next = new Set(prev);
        next.delete(activityId);
        return next;
      });
    }
  };

  /**
   * 格式化日期为中文格式
   * @param dateStr - 日期字符串
   */
  const formatDate = (dateStr: string) => {
    if (!dateStr) return '待定';
    return new Date(dateStr).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  /**
   * 获取活动的唯一标识
   * @param activity - 活动对象
   */
  const getActivityId = (activity: Activity): number => {
    return activity.id || (activity as any).activityId;
  };

  /**
   * 获取活动标题
   * @param activity - 活动对象
   */
  const getActivityTitle = (activity: Activity): string => {
    return activity.title || (activity as any).activityTitle || '未知活动';
  };

  /**
   * 获取活动地点
   * @param activity - 活动对象
   */
  const getActivityLocation = (activity: Activity): string => {
    return activity.location || (activity as any).activityLocation || '';
  };

  /**
   * 获取活动封面图
   * @param activity - 活动对象
   */
  const getActivityCover = (activity: Activity): string | null => {
    const images = (activity as any).images || (activity as any).album;
    if (Array.isArray(images) && images.length > 0) {
      return images[0].fileUrl || images[0].url || null;
    }
    if ((activity as any).coverUrl) {
      return (activity as any).coverUrl;
    }
    return null;
  };

  /** 渲染骨架屏加载状态 */
  const renderSkeleton = () => (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 overflow-hidden animate-pulse"
        >
          <div className="h-40 bg-violet-100/60" />
          <div className="p-5 space-y-3">
            <div className="h-5 bg-violet-100/60 rounded-lg w-3/4" />
            <div className="h-4 bg-violet-100/40 rounded-lg w-1/2" />
            <div className="h-4 bg-violet-100/40 rounded-lg w-2/3" />
          </div>
        </div>
      ))}
    </div>
  );

  /** 渲染空状态 */
  const renderEmpty = () => (
    <div className="text-center py-20 animate-fadeIn">
      <div className="w-24 h-24 bg-violet-100 rounded-full flex items-center justify-center mx-auto mb-6">
        <Heart size={48} className="text-violet-300" />
      </div>
      <h3 className="text-xl font-bold text-[#4C1D95] mb-2">暂无收藏</h3>
      <p className="text-gray-400 text-sm mb-8">
        你还没有收藏任何活动，去发现精彩活动吧
      </p>
      <Link
        to="/activities"
        className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer font-medium"
      >
        <Search size={18} />
        浏览活动
      </Link>
    </div>
  );

  return (
    <div className="min-h-screen bg-[#FAF5FF]">
      <Navbar />

      {/* 紫色渐变 banner */}
      <div className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-violet-600 via-violet-500 to-purple-600 opacity-90" />
        <div
          className="absolute inset-0 opacity-30"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E\")",
          }}
        />

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center border border-white/30">
              <Heart size={32} className="text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-white">我的收藏</h1>
              <p className="text-violet-100 text-sm mt-1">管理你收藏的活动</p>
            </div>
          </div>
        </div>
      </div>

      {/* 收藏列表主体 */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loading ? (
          renderSkeleton()
        ) : favorites.length === 0 ? (
          <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-8">
            {renderEmpty()}
          </div>
        ) : (
          <>
            {/* 收藏数量统计 */}
            <div className="flex items-center justify-between mb-6">
              <p className="text-sm text-gray-500">
                共 <span className="font-semibold text-[#4C1D95]">{favorites.length}</span> 个收藏
              </p>
            </div>

            {/* 卡片网格布局 */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {favorites.map((activity, index) => {
                const id = getActivityId(activity);
                const cover = getActivityCover(activity);
                const isRemoving = removingIds.has(id);

                return (
                  <Link
                    key={id}
                    to={`/activities/${id}`}
                    className="group bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 overflow-hidden hover:shadow-xl hover:border-violet-300 transition-all duration-300 cursor-pointer animate-fadeInUp"
                    style={{ animationDelay: `${index * 60}ms` }}
                  >
                    {/* 封面图 */}
                    <div className="relative h-40 bg-gradient-to-br from-violet-100 to-purple-100 overflow-hidden">
                      {cover ? (
                        <img
                          src={cover}
                          alt={getActivityTitle(activity)}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center">
                          <ImageOff size={40} className="text-violet-300" />
                        </div>
                      )}
                      {/* 收藏按钮 */}
                      <button
                        onClick={(e) => handleRemoveFavorite(id, e)}
                        disabled={isRemoving}
                        className="absolute top-3 right-3 w-9 h-9 bg-white/80 backdrop-blur-sm rounded-full flex items-center justify-center hover:bg-red-50 border border-white/40 shadow-md transition-all duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                        title="取消收藏"
                      >
                        {isRemoving ? (
                          <Loader2 size={16} className="animate-spin text-violet-500" />
                        ) : (
                          <Trash2 size={16} className="text-red-400 hover:text-red-600 transition-colors" />
                        )}
                      </button>
                    </div>

                    {/* 卡片内容 */}
                    <div className="p-5">
                      <h3 className="font-bold text-[#4C1D95] group-hover:text-violet-600 transition-colors line-clamp-2 mb-3">
                        {getActivityTitle(activity)}
                      </h3>

                      <div className="space-y-2 text-sm text-gray-500">
                        <div className="flex items-center gap-2">
                          <Clock size={14} className="text-violet-400 flex-shrink-0" />
                          <span className="truncate">
                            {formatDate(activity.startTime || (activity as any).startTime)}
                          </span>
                        </div>
                        {getActivityLocation(activity) && (
                          <div className="flex items-center gap-2">
                            <MapPin size={14} className="text-violet-400 flex-shrink-0" />
                            <span className="truncate">{getActivityLocation(activity)}</span>
                          </div>
                        )}
                        {activity.maxParticipants && (
                          <div className="flex items-center gap-2">
                            <Users size={14} className="text-violet-400 flex-shrink-0" />
                            <span>
                              {activity.currentParticipants || 0} / {activity.maxParticipants} 人
                            </span>
                          </div>
                        )}
                      </div>

                      {/* 标签 */}
                      {activity.tags && activity.tags.length > 0 && (
                        <div className="mt-4 pt-3 border-t border-violet-100 flex flex-wrap gap-1.5">
                          {activity.tags.slice(0, 3).map((tag) => (
                            <span
                              key={tag.id}
                              className="px-2.5 py-0.5 bg-violet-50 text-violet-600 rounded-lg text-xs font-medium"
                            >
                              {tag.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </Link>
                );
              })}
            </div>
          </>
        )}
      </div>

      <Toast />
    </div>
  );
}
