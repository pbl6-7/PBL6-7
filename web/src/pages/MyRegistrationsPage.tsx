import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Calendar,
  MapPin,
  Users,
  Loader2,
  CheckCircle,
  XCircle,
  Clock,
  ClipboardList,
  ArrowRight,
  ArrowLeft,
} from 'lucide-react';
import { getMyRegistrations } from '@/api/registration';
import type { RegistrationResponse } from '@/types/registration';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 我的报名记录页面组件 - 显示用户的所有报名记录
 */
export default function MyRegistrationsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [registrations, setRegistrations] = useState<RegistrationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRegistrations();
  }, []);

  /**
   * 加载报名记录列表
   */
  const loadRegistrations = async () => {
    setLoading(true);
    try {
      const res = await getMyRegistrations();
      const list = res.data.data?.list || [];
      setRegistrations(list);
    } catch (err) {
      console.error('加载失败', err);
      addToast('error', '加载报名记录失败，请稍后重试');
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
   * 获取报名状态对应的徽章组件
   * @param status - 报名状态
   */
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'confirmed':
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-accent-50 text-accent-600 text-xs font-medium rounded-lg border border-accent-200">
            <CheckCircle size={14} />
            已确认
          </span>
        );
      case 'cancelled':
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-red-50 text-red-600 text-xs font-medium rounded-lg border border-red-200">
            <XCircle size={14} />
            已取消
          </span>
        );
      case 'pending':
      default:
        return (
          <span className="flex items-center gap-1.5 px-3 py-1.5 bg-yellow-50 text-yellow-600 text-xs font-medium rounded-lg border border-yellow-200">
            <Clock size={14} />
            待确认
          </span>
        );
    }
  };

  /**
   * 获取报名状态对应的左侧边框颜色
   * @param status - 报名状态
   */
  const getStatusBorder = (status: string) => {
    switch (status) {
      case 'confirmed':
        return 'border-l-accent-500';
      case 'cancelled':
        return 'border-l-red-400';
      case 'pending':
      default:
        return 'border-l-yellow-400';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface-50">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="animate-spin text-primary-600" size={40} />
          <p className="text-text-muted font-body text-sm">加载报名记录中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      <Navbar />

      {/* 渐变横幅头部 */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-500 to-secondary-400 text-white">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate('/', { replace: true })}
              className="cursor-pointer flex items-center gap-2 px-4 py-2.5 glass-dark text-white rounded-xl hover:bg-white/20 transition-all duration-200 group"
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-0.5 transition-transform duration-200" />
              返回首页
            </button>
          </div>
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
              <ClipboardList size={28} className="text-white" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-heading font-bold">报名记录</h1>
              <p className="text-white/70 text-sm mt-1">
                查看您参加过的所有活动报名
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 报名记录列表 */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 -mt-4 pb-12">
        {registrations.length === 0 ? (
          /* 空状态 */
          <div className="bg-white/80 backdrop-blur-sm rounded-2xl shadow-card p-16 text-center animate-fadeIn">
            <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <Users size={36} className="text-primary-300" />
            </div>
            <h3 className="text-xl font-heading font-semibold text-text-primary mb-2">
              暂无报名记录
            </h3>
            <p className="text-gray-400 text-sm mb-6">
              您还没有报名任何活动，去发现精彩活动吧
            </p>
            <Link
              to="/activities"
              className="inline-flex items-center gap-2 px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 cursor-pointer shadow-button hover:shadow-button-hover text-sm font-medium"
            >
              浏览活动
              <ArrowRight size={16} />
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {registrations.map((reg, index) => (
              <Link
                key={reg.id}
                to={`/activities/${reg.activityId}`}
                className={`
                  group block bg-white/80 backdrop-blur-sm rounded-2xl shadow-card hover:shadow-card-hover
                  border-l-4 ${getStatusBorder(reg.status)}
                  transition-all duration-300 animate-fadeInUp cursor-pointer
                `}
                style={{ animationDelay: `${index * 60}ms` }}
              >
                <div className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <h3 className="font-heading font-semibold text-text-primary mb-2 group-hover:text-primary-600 transition-colors duration-200 truncate">
                        {reg.activityTitle || `活动 #${reg.activityId}`}
                      </h3>
                      <div className="flex flex-wrap gap-x-5 gap-y-2 text-sm text-gray-500">
                        <div className="flex items-center gap-1.5">
                          <Calendar size={14} className="text-primary-400 flex-shrink-0" />
                          <span>报名时间：{formatDate(reg.registrationTime)}</span>
                        </div>
                        {reg.activityLocation && (
                          <div className="flex items-center gap-1.5">
                            <MapPin size={14} className="text-primary-400 flex-shrink-0" />
                            <span className="truncate">{reg.activityLocation}</span>
                          </div>
                        )}
                        {reg.activityStartTime && (
                          <div className="flex items-center gap-1.5">
                            <Calendar size={14} className="text-primary-400 flex-shrink-0" />
                            <span>活动时间：{formatDate(reg.activityStartTime)}</span>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex-shrink-0">
                      {getStatusBadge(reg.status)}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      <Toast />
    </div>
  );
}
