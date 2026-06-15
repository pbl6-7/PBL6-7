import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Loader2, ArrowLeft } from 'lucide-react';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification';
import type { Notification } from '@/types/notification';

/**
 * 通知页面组件 - 显示用户所有通知
 */
export default function NotificationsPage() {
  const navigate = useNavigate();
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
      setUnreadCount(unreadRes.data.data?.count || 0);
    } catch (err) {
      console.error('加载通知失败', err);
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
    } catch (err) {
      console.error('标记全部已读失败', err);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader2 className="animate-spin text-indigo-600" size={32} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 头部 */}
      <div className="bg-white shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
              >
                <ArrowLeft size={20} />
                返回
              </button>
              <Bell className="text-indigo-600" size={24} />
              <h1 className="text-xl font-semibold">我的通知</h1>
              {unreadCount > 0 && (
                <span className="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                  {unreadCount} 条未读
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                className="px-4 py-2 text-sm text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
              >
                全部标为已读
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 通知列表 */}
      <div className="max-w-3xl mx-auto px-4 py-6">
        {notifications.length > 0 ? (
          <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`p-4 border-b last:border-b-0 transition cursor-pointer ${
                  notification.isRead
                    ? 'border-gray-100 hover:bg-gray-50'
                    : 'border-indigo-100 bg-indigo-50/50 hover:bg-indigo-50'
                }`}
                onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      {!notification.isRead && (
                        <span className="w-2 h-2 bg-indigo-600 rounded-full flex-shrink-0"></span>
                      )}
                      <h3 className="font-medium text-gray-900">{notification.title}</h3>
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{notification.content}</p>
                    <p className="text-xs text-gray-400 mt-2">
                      {new Date(notification.createTime).toLocaleString('zh-CN')}
                    </p>
                  </div>
                  {notification.isRead && (
                    <span className="text-xs text-gray-400 whitespace-nowrap ml-4">已读</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-sm p-12 text-center">
            <Bell size={48} className="mx-auto mb-4 text-gray-300" />
            <p className="text-gray-500">暂无通知</p>
          </div>
        )}
      </div>
    </div>
  );
}
