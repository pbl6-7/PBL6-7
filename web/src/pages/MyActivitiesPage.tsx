import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Calendar,
  MapPin,
  Users,
  Loader2,
  Plus,
  Sparkles,
  FileEdit,
  Eye,
  ArrowLeft,
} from 'lucide-react';
import { getMyActivities } from '@/api/activity';
import type { Activity } from '@/types/activity';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 我的活动页面组件 - 显示用户发布的所有活动
 */
export default function MyActivitiesPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadMyActivities();
  }, []);

  /**
   * 加载用户发布的活动列表
   */
  const loadMyActivities = async () => {
    setLoading(true);
    try {
      const res = await getMyActivities();
      setActivities(res.data.data || []);
    } catch (err) {
      console.error('加载失败', err);
      addToast('error', '加载活动失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 格式化日期为中文格式
   * @param dateStr - 日期字符串
   */
  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  };

  /**
   * 获取活动状态对应的样式配置
   * @param status - 活动状态
   */
  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'published':
        return { label: '已发布', bg: 'bg-accent-50', text: 'text-accent-600', border: 'border-accent-200', icon: <Eye size={12} /> };
      case 'draft':
        return { label: '草稿', bg: 'bg-yellow-50', text: 'text-yellow-600', border: 'border-yellow-200', icon: <FileEdit size={12} /> };
      case 'cancelled':
        return { label: '已取消', bg: 'bg-red-50', text: 'text-red-600', border: 'border-red-200', icon: <span>✕</span> };
      case 'ended':
        return { label: '已结束', bg: 'bg-gray-50', text: 'text-gray-500', border: 'border-gray-200', icon: <span>—</span> };
      default:
        return { label: status, bg: 'bg-gray-50', text: 'text-gray-500', border: 'border-gray-200', icon: null };
    }
  };

  /**
   * 获取活动卡片渐变封面色
   * @param index - 活动索引，用于交替渐变
   */
  const getCoverGradient = (index: number) => {
    const gradients = [
      'from-primary-500 to-secondary-400',
      'from-accent-500 to-emerald-400',
      'from-blue-500 to-cyan-400',
      'from-pink-500 to-rose-400',
      'from-amber-500 to-yellow-400',
      'from-indigo-500 to-violet-400',
    ];
    return gradients[index % gradients.length];
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface-50">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="animate-spin text-primary-600" size={40} />
          <p className="text-text-muted font-body text-sm">加载活动中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      <Navbar />

      {/* 渐变横幅头部 */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-500 to-secondary-400 text-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate('/', { replace: true })}
              className="cursor-pointer flex items-center gap-2 px-4 py-2.5 glass-dark text-white rounded-xl hover:bg-white/20 transition-all duration-200 group"
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-0.5 transition-transform duration-200" />
              返回首页
            </button>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
                <Sparkles size={28} className="text-white" />
              </div>
              <div>
                <h1 className="text-2xl sm:text-3xl font-heading font-bold">我的活动</h1>
                <p className="text-white/70 text-sm mt-1">
                  管理您发布的所有活动
                </p>
              </div>
            </div>
            <Link
              to="/activities/create"
              className="flex items-center gap-2 px-5 py-2.5 bg-accent-500 hover:bg-accent-600 text-white rounded-xl transition-colors duration-200 cursor-pointer shadow-button hover:shadow-button-hover text-sm font-medium"
            >
              <Plus size={18} />
              发布活动
            </Link>
          </div>
        </div>
      </div>

      {/* 活动列表 */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 -mt-4 pb-12">
        {activities.length === 0 ? (
          /* 空状态 */
          <div className="bg-white/80 backdrop-blur-sm rounded-2xl shadow-card p-16 text-center animate-fadeIn">
            <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <Calendar size={36} className="text-primary-300" />
            </div>
            <h3 className="text-xl font-heading font-semibold text-text-primary mb-2">
              暂无活动
            </h3>
            <p className="text-gray-400 text-sm mb-6">
              您还没有发布任何活动，快来创建一个吧
            </p>
            <Link
              to="/activities/create"
              className="inline-flex items-center gap-2 px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 cursor-pointer shadow-button hover:shadow-button-hover text-sm font-medium"
            >
              <Plus size={18} />
              发布活动
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {activities.map((activity, index) => {
              const statusConfig = getStatusConfig(activity.status);
              return (
                <Link
                  key={activity.id}
                  to={`/activities/${activity.id}`}
                  className="group bg-white/80 backdrop-blur-sm rounded-2xl shadow-card hover:shadow-card-hover overflow-hidden transition-all duration-300 animate-fadeInUp cursor-pointer"
                  style={{ animationDelay: `${index * 80}ms` }}
                >
                  {/* 渐变封面 */}
                  <div className={`h-28 bg-gradient-to-br ${getCoverGradient(index)} relative overflow-hidden`}>
                    <div className="absolute inset-0 bg-black/5" />
                    {/* 状态徽章 */}
                    <div className="absolute top-3 right-3">
                      <span className={`flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-lg border backdrop-blur-sm ${statusConfig.bg} ${statusConfig.text} ${statusConfig.border}`}>
                        {statusConfig.icon}
                        {statusConfig.label}
                      </span>
                    </div>
                    {/* 活动类型标签 */}
                    {activity.activityTypeName && (
                      <div className="absolute bottom-3 left-3">
                        <span className="px-2.5 py-1 bg-white/20 backdrop-blur-sm text-white text-xs font-medium rounded-lg">
                          {activity.activityTypeName}
                        </span>
                      </div>
                    )}
                  </div>

                  {/* 活动信息 */}
                  <div className="p-5">
                    <h3 className="font-heading font-semibold text-text-primary line-clamp-2 mb-3 group-hover:text-primary-600 transition-colors duration-200">
                      {activity.title}
                    </h3>
                    <div className="space-y-2 text-sm text-gray-500">
                      <div className="flex items-center gap-2">
                        <Calendar size={14} className="text-primary-400 flex-shrink-0" />
                        <span className="truncate">{formatDate(activity.startTime)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin size={14} className="text-primary-400 flex-shrink-0" />
                        <span className="truncate">{activity.location}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Users size={14} className="text-primary-400 flex-shrink-0" />
                        <span>
                          {activity.currentParticipants ?? 0}/{activity.maxParticipants}人
                        </span>
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>

      <Toast />
    </div>
  );
}
