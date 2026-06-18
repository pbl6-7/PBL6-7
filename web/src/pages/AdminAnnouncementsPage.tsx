import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  GraduationCap,
  Megaphone,
  Send,
  Trash2,
  Loader2,
  Mail,
} from 'lucide-react';
import {
  publishAnnouncement,
  getAdminNotifications,
  deleteAdminNotification,
} from '@/api/admin';
import type { Notification, NotificationPageResponse } from '@/types/notification';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台系统公告发布页面
 * 提供发布公告表单和已发送通知列表管理功能
 */
export default function AdminAnnouncementsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);

  /** 公告标题 */
  const [title, setTitle] = useState('');
  /** 公告内容 */
  const [content, setContent] = useState('');
  /** 发布中加载状态 */
  const [publishing, setPublishing] = useState(false);
  /** 通知列表 */
  const [notifications, setNotifications] = useState<Notification[]>([]);
  /** 列表加载状态 */
  const [loading, setLoading] = useState(true);
  /** 删除中加载状态（存储正在删除的通知 ID） */
  const [deletingId, setDeletingId] = useState<number | null>(null);
  /** 当前页码 */
  const [currentPage, setCurrentPage] = useState(1);
  /** 总页数 */
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    checkAdmin();
    loadNotifications(1);
  }, []);

  /**
   * 检查当前用户是否为管理员
   */
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

  /**
   * 加载通知列表
   * @param page 页码
   */
  const loadNotifications = async (page: number) => {
    setLoading(true);
    try {
      const res = await getAdminNotifications(page, 20);
      const data: NotificationPageResponse = res.data.data;
      setNotifications(data.records || []);
      setTotalPages(data.pages || 1);
      setCurrentPage(data.current || page);
    } catch (err) {
      console.error('加载通知列表失败', err);
      addToast('error', '加载通知列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 发布系统公告
   */
  const handlePublish = async () => {
    if (!title.trim()) {
      addToast('warning', '请输入公告标题');
      return;
    }
    if (title.trim().length > 100) {
      addToast('warning', '公告标题不能超过100个字符');
      return;
    }
    if (!content.trim()) {
      addToast('warning', '请输入公告内容');
      return;
    }
    if (content.trim().length > 5000) {
      addToast('warning', '公告内容不能超过5000个字符');
      return;
    }
    setPublishing(true);
    try {
      await publishAnnouncement({ title: title.trim(), content: content.trim() });
      addToast('success', '公告发布成功');
      setTitle('');
      setContent('');
      loadNotifications(1);
    } catch (err: any) {
      addToast('error', err.message || '公告发布失败');
    } finally {
      setPublishing(false);
    }
  };

  /**
   * 删除通知
   * @param id 通知 ID
   */
  const handleDelete = async (id: number) => {
    setDeletingId(id);
    try {
      await deleteAdminNotification(id);
      addToast('success', '通知已删除');
      loadNotifications(currentPage);
    } catch (err: any) {
      addToast('error', err.message || '删除通知失败');
    } finally {
      setDeletingId(null);
    }
  };

  /**
   * 截取内容摘要
   * @param text 原始内容
   * @param maxLen 最大长度
   * @returns 截取后的摘要字符串
   */
  const truncateContent = (text: string, maxLen = 40) => {
    if (!text) return '';
    return text.length > maxLen ? text.slice(0, maxLen) + '...' : text;
  };

  /**
   * 格式化时间显示
   * @param timeStr ISO 时间字符串
   * @returns 格式化后的时间字符串
   */
  const formatTime = (timeStr: string) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="flex min-h-screen bg-[#FAF5FF]">
      <AdminSidebar />
      <Toast />

      <main className="flex-1 flex flex-col">
        {/* 顶部导航栏 */}
        <header className="flex items-center gap-3 px-8 h-16 bg-white/70 backdrop-blur-xl border-b border-violet-100 shadow-sm">
          <GraduationCap size={24} className="text-[#4C1D95]" />
          <h1 className="text-xl font-bold text-[#4C1D95]">系统公告</h1>
        </header>

        {/* 主体内容区 */}
        <div className="flex-1 p-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* 左侧：发布公告表单 */}
            <div className="bg-white/60 backdrop-blur-xl rounded-2xl border border-violet-100 shadow-lg p-6">
              <div className="flex items-center gap-2 mb-6">
                <Megaphone size={20} className="text-[#4C1D95]" />
                <h2 className="text-lg font-semibold text-[#4C1D95]">发布公告</h2>
              </div>

              <div className="space-y-4">
                {/* 标题输入 */}
                <div>
                  <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                    公告标题
                  </label>
                  <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="请输入公告标题"
                    maxLength={100}
                    className="w-full px-4 py-2.5 rounded-xl border border-violet-200 bg-white/80 text-[#4C1D95] placeholder-violet-300 focus:outline-none focus:ring-2 focus:ring-violet-400 focus:border-transparent transition-all duration-200"
                  />
                </div>

                {/* 内容文本域 */}
                <div>
                  <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                    公告内容
                  </label>
                  <textarea
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="请输入公告内容"
                    rows={8}
                    maxLength={5000}
                    className="w-full px-4 py-2.5 rounded-xl border border-violet-200 bg-white/80 text-[#4C1D95] placeholder-violet-300 focus:outline-none focus:ring-2 focus:ring-violet-400 focus:border-transparent transition-all duration-200 resize-none"
                  />
                </div>

                {/* 发布按钮 */}
                <button
                  onClick={handlePublish}
                  disabled={publishing}
                  className="w-full flex items-center justify-center gap-2 px-6 py-2.5 rounded-xl bg-[#4C1D95] text-white font-medium hover:bg-violet-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
                >
                  {publishing ? (
                    <Loader2 size={18} className="animate-spin" />
                  ) : (
                    <Send size={18} />
                  )}
                  {publishing ? '发布中...' : '发布公告'}
                </button>
              </div>
            </div>

            {/* 右侧：已发送公告列表 */}
            <div className="bg-white/60 backdrop-blur-xl rounded-2xl border border-violet-100 shadow-lg p-6">
              <div className="flex items-center gap-2 mb-6">
                <Mail size={20} className="text-[#4C1D95]" />
                <h2 className="text-lg font-semibold text-[#4C1D95]">已发送公告</h2>
              </div>

              {loading ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 size={32} className="animate-spin text-violet-400" />
                </div>
              ) : notifications.length === 0 ? (
                <div className="text-center py-12 text-violet-300">
                  <Mail size={48} className="mx-auto mb-3 opacity-50" />
                  <p>暂无已发送公告</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-violet-100">
                          <th className="text-left py-3 px-3 text-[#4C1D95] font-semibold">标题</th>
                          <th className="text-left py-3 px-3 text-[#4C1D95] font-semibold">内容摘要</th>
                          <th className="text-left py-3 px-3 text-[#4C1D95] font-semibold">时间</th>
                          <th className="text-center py-3 px-3 text-[#4C1D95] font-semibold">操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        {notifications.map((n) => (
                          <tr
                            key={n.id}
                            className="border-b border-violet-50 hover:bg-violet-50/50 transition-colors duration-150"
                          >
                            <td className="py-3 px-3 text-[#4C1D95] font-medium max-w-[120px] truncate">
                              {n.title}
                            </td>
                            <td className="py-3 px-3 text-gray-600 max-w-[200px] truncate">
                              {truncateContent(n.content)}
                            </td>
                            <td className="py-3 px-3 text-gray-400 whitespace-nowrap text-xs">
                              {formatTime(n.createTime)}
                            </td>
                            <td className="py-3 px-3 text-center">
                              <button
                                onClick={() => handleDelete(n.id)}
                                disabled={deletingId === n.id}
                                className="inline-flex items-center justify-center gap-1 px-3 py-1.5 rounded-lg text-red-500 hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
                              >
                                {deletingId === n.id ? (
                                  <Loader2 size={14} className="animate-spin" />
                                ) : (
                                  <Trash2 size={14} />
                                )}
                                删除
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* 分页控制 */}
                  {totalPages > 1 && (
                    <div className="flex items-center justify-center gap-2 mt-6">
                      <button
                        onClick={() => loadNotifications(currentPage - 1)}
                        disabled={currentPage <= 1}
                        className="px-3 py-1.5 rounded-lg text-sm text-[#4C1D95] bg-violet-50 hover:bg-violet-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
                      >
                        上一页
                      </button>
                      <span className="text-sm text-[#4C1D95]">
                        {currentPage} / {totalPages}
                      </span>
                      <button
                        onClick={() => loadNotifications(currentPage + 1)}
                        disabled={currentPage >= totalPages}
                        className="px-3 py-1.5 rounded-lg text-sm text-[#4C1D95] bg-violet-50 hover:bg-violet-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-200 cursor-pointer"
                      >
                        下一页
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
