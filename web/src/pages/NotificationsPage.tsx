import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  Loader2,
  CheckCircle2,
  AlertTriangle,
  Info,
  MessageSquare,
  CheckCheck,
  BellOff,
} from 'lucide-react';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification';
import type { Notification } from '@/types/notification';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 通知页面组件 - 显示用户所有通知
 */
export default function NotificationsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    loadData();
  }, []);

  /**
   * 加载通知数据
   */
  const loadData = async () => {
    setLoading(true);
    try {
      const [notificationsRes, unreadRes] = await Promise.all([
        getNotifications(1, 50),
        getUnreadCount(),
      ]);
      setNotifications(notificationsRes.data.data.records);
      setUnreadCount(unreadRes.data.data?.unreadCount || 0);
    } catch (err) {
      console.error('加载通知失败', err);
      addToast('error', '加载通知失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 处理单个通知标记已读
   * @param id - 通知ID
   */
  const handleMarkAsRead = async (id: number) => {
    try {
      await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('标记已读失败', err);
      addToast('error', '标记已读失败');
    }
  };

  /**
   * 处理全部标记已读
   */
  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
      addToast('success', '已全部标记为已读');
    } catch (err) {
      console.error('标记全部已读失败', err);
      addToast('error', '标记全部已读失败');
    }
  };

  /**
   * 根据通知类型获取图标
   * @param type - 通知类型
   */
  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'registration':
        return <CheckCircle2 size={20} className="text-accent-500" />;
      case 'approval':
        return <AlertTriangle size={20} className="text-yellow-500" />;
      case 'system':
        return <Info size={20} className="text-primary-500" />;
      case 'comment':
        return <MessageSquare size={20} className="text-blue-500" />;
      default:
        return <Bell size={20} className="text-primary-400" />;
    }
  };

  /**
   * 根据通知类型获取图标背景色
   * @param type - 通知类型
   */
  const getNotificationIconBg = (type: string) => {
    switch (type) {
      case 'registration':
        return 'bg-accent-50';
      case 'approval':
        return 'bg-yellow-50';
      case 'system':
        return 'bg-primary-50';
      case 'comment':
        return 'bg-blue-50';
      default:
        return 'bg-violet-50';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface-50">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="animate-spin text-primary-600" size={40} />
          <p className="text-text-muted font-body text-sm">加载通知中...</p>
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
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
                <Bell size={28} className="text-white" />
              </div>
              <div>
                <h1 className="text-2xl sm:text-3xl font-heading font-bold">我的通知</h1>
                <p className="text-white/70 text-sm mt-1">
                  {unreadCount > 0 ? `${unreadCount} 条未读消息` : '没有未读消息'}
                </p>
              </div>
            </div>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                className="flex items-center gap-2 px-4 py-2.5 bg-white/20 backdrop-blur-sm hover:bg-white/30 rounded-xl transition-colors duration-200 cursor-pointer text-sm font-medium"
              >
                <CheckCheck size={16} />
                全部已读
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 通知列表 */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 -mt-4 pb-12">
        {notifications.length > 0 ? (
          <div className="space-y-3">
            {notifications.map((notification, index) => (
              <div
                key={notification.id}
                className={`
                  bg-white/80 backdrop-blur-sm rounded-2xl shadow-card hover:shadow-card-hover
                  transition-all duration-300 animate-fadeInUp cursor-pointer
                  ${!notification.isRead ? 'border-l-4 border-l-accent-500 bg-accent-50/30' : 'border-l-4 border-l-transparent'}
                `}
                style={{ animationDelay: `${index * 50}ms` }}
                onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
              >
                <div className="p-5">
                  <div className="flex items-start gap-4">
                    {/* 通知类型图标 */}
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${getNotificationIconBg(notification.type)}`}>
                      {getNotificationIcon(notification.type)}
                    </div>

                    {/* 通知内容 */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        {!notification.isRead && (
                          <span className="w-2 h-2 bg-accent-500 rounded-full flex-shrink-0 animate-pulse-soft" />
                        )}
                        <h3 className={`font-medium truncate ${!notification.isRead ? 'text-text-primary' : 'text-gray-700'}`}>
                          {notification.title}
                        </h3>
                      </div>
                      <p className="text-sm text-gray-500 leading-relaxed line-clamp-2">
                        {notification.content}
                      </p>
                      <div className="flex items-center justify-between mt-3">
                        <p className="text-xs text-gray-400">
                          {new Date(notification.createTime).toLocaleString('zh-CN')}
                        </p>
                        {!notification.isRead && (
                          <span className="text-xs text-accent-600 font-medium">
                            点击标记已读
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          /* 空状态 */
          <div className="bg-white/80 backdrop-blur-sm rounded-2xl shadow-card p-16 text-center animate-fadeIn">
            <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <BellOff size={36} className="text-primary-300" />
            </div>
            <h3 className="text-xl font-heading font-semibold text-text-primary mb-2">
              暂无通知
            </h3>
            <p className="text-gray-400 text-sm mb-6">
              当有新的活动通知时，会在这里显示
            </p>
            <button
              onClick={() => navigate('/activities')}
              className="px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 cursor-pointer shadow-button hover:shadow-button-hover text-sm font-medium"
            >
              浏览活动
            </button>
          </div>
        )}
      </div>

      <Toast />
    </div>
  );
}
