import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Calendar, MessageSquare, Settings, LogOut, Loader2, ChevronRight, Bell, Heart, ArrowLeft } from 'lucide-react';
import { getProfile, updateProfile, changePassword, getSecurityQuestions, getUserSecurityQuestion, setSecurity } from '@/api/user';
import { getMyRegistrations } from '@/api/registration';
import { getMyActivities } from '@/api/activity';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification';
import { getFavorites } from '@/api/favorite';
import type { User as UserType, LoginResponse, SecurityQuestion } from '@/types/user';
import type { RegistrationResponse } from '@/types/registration';
import type { Activity } from '@/types/activity';
import type { Notification } from '@/types/notification';

export default function ProfilePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [profile, setProfile] = useState<UserType | null>(null);
  const [registrations, setRegistrations] = useState<RegistrationResponse[]>([]);
  const [myActivities, setMyActivities] = useState<Activity[]>([]);
  const [favoriteActivities, setFavoriteActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'activities' | 'registrations' | 'favorites' | 'settings' | 'notifications'>('activities');

  // 通知状态
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notificationPage, setNotificationPage] = useState(1);
  const [notificationTotal, setNotificationTotal] = useState(0);

  // 编辑资料状态
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ realName: '', contact: '' });
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '' });

  // 密保问题状态
  const [securityQuestions, setSecurityQuestions] = useState<SecurityQuestion[]>([]);
  const [currentSecurity, setCurrentSecurity] = useState<{ questionId: number; question: string } | null>(null);
  const [securityForm, setSecurityForm] = useState({ questionId: 0, answer: '', password: '' });
  const [showSecurityForm, setShowSecurityForm] = useState(false);

  // 加载用户信息
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      navigate('/login');
      return;
    }
    setUser(JSON.parse(userStr));
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const userStr = localStorage.getItem('user');
      const userData = userStr ? JSON.parse(userStr) : null;
      
      const [profileRes, regRes, actRes, questionsRes, unreadRes, favRes] = await Promise.all([
        getProfile(),
        getMyRegistrations(1, 5),
        getMyActivities(),
        getSecurityQuestions(),
        getUnreadCount(),
        getFavorites(),
      ]);
      setProfile(profileRes.data.data);
      setRegistrations(regRes.data.data.list);
      setMyActivities(actRes.data.data);
      setSecurityQuestions(questionsRes.data.data);
      setUnreadCount(unreadRes.data.data?.count || 0);
      setFavoriteActivities(favRes.data.data || []);
      
      // 加载用户当前的密保问题
      if (userData?.userId) {
        try {
          const securityRes = await getUserSecurityQuestion(userData.userId);
          if (securityRes.data.data) {
            setCurrentSecurity({
              questionId: securityRes.data.data.questionId,
              question: securityRes.data.data.question,
            });
          }
        } catch (err) {
          console.error('获取密保问题失败', err);
        }
      }
    } catch (err) {
      console.error('加载数据失败', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载通知列表
   * @param page - 页码
   */
  const loadNotifications = async (page = 1) => {
    try {
      const res = await getNotifications(page, 10);
      setNotifications(res.data.data.records);
      setNotificationTotal(res.data.data.total);
      setNotificationPage(page);
    } catch (err) {
      console.error('加载通知失败', err);
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

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateProfile(editForm);
      setEditing(false);
      loadData();
      alert('资料更新成功');
    } catch (err: any) {
      alert(err.message || '更新失败');
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await changePassword(passwordForm);
      setPasswordForm({ oldPassword: '', newPassword: '' });
      alert('密码修改成功');
    } catch (err: any) {
      alert(err.message || '修改失败');
    }
  };

  /**
   * 处理密保问题设置/修改提交
   * @param e - 表单提交事件
   */
  const handleSetSecurity = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!securityForm.questionId) {
      alert('请选择密保问题');
      return;
    }
    if (!securityForm.answer.trim()) {
      alert('请填写密保答案');
      return;
    }
    if (!securityForm.password) {
      alert('请填写当前密码');
      return;
    }
    try {
      await setSecurity({
        securityQuestionId: securityForm.questionId,
        securityAnswer: securityForm.answer,
        password: securityForm.password,
      });
      setShowSecurityForm(false);
      setSecurityForm({ questionId: 0, answer: '', password: '' });
      loadData();
      alert('密保问题设置成功');
    } catch (err: any) {
      alert(err.message || '设置失败');
    }
  };

  /**
   * 打开密保问题修改表单
   */
  const handleEditSecurity = () => {
    if (currentSecurity) {
      setSecurityForm((prev) => ({ ...prev, questionId: currentSecurity.questionId }));
    }
    setShowSecurityForm(true);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="animate-spin text-indigo-600" size={32} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 头部 */}
      <div className="bg-indigo-600 text-white py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/30 rounded-lg transition"
            >
              <ArrowLeft size={20} />
              返回
            </button>
          </div>
          <div className="flex items-center gap-4">
            <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center">
              <span className="text-3xl font-bold text-indigo-600">
                {profile?.realName?.charAt(0) || user?.username?.charAt(0) || 'U'}
              </span>
            </div>
            <div>
              <h1 className="text-2xl font-bold">{profile?.realName || user?.username}</h1>
              <p className="text-indigo-200">@{user?.username}</p>
              <span className="inline-block mt-1 px-3 py-1 bg-indigo-500 rounded-full text-sm">
                {user?.role === 'admin' ? '管理员' : user?.role === 'publisher' ? '发布者' : '普通用户'}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* 侧边栏 */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-4">
              <nav className="space-y-1">
                <button
                  onClick={() => setActiveTab('activities')}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                    activeTab === 'activities'
                      ? 'bg-indigo-50 text-indigo-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <Calendar size={18} />
                  我的活动
                </button>
                <button
                  onClick={() => setActiveTab('registrations')}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                    activeTab === 'registrations'
                      ? 'bg-indigo-50 text-indigo-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <MessageSquare size={18} />
                  报名记录
                </button>
                <button
                  onClick={() => setActiveTab('favorites')}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                    activeTab === 'favorites'
                      ? 'bg-indigo-50 text-indigo-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <Heart size={18} />
                  我的收藏
                </button>
                <button
                  onClick={() => setActiveTab('settings')}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                    activeTab === 'settings'
                      ? 'bg-indigo-50 text-indigo-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <Settings size={18} />
                  账号设置
                </button>
                <button
                  onClick={() => { setActiveTab('notifications'); loadNotifications(); }}
                  className={`w-full flex items-center justify-between px-4 py-3 rounded-lg transition ${
                    activeTab === 'notifications'
                      ? 'bg-indigo-50 text-indigo-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <Bell size={18} />
                    我的通知
                  </span>
                  {unreadCount > 0 && (
                    <span className="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </button>
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-red-600 hover:bg-red-50 transition"
                >
                  <LogOut size={18} />
                  退出登录
                </button>
              </nav>
            </div>
          </div>

          {/* 主内容 */}
          <div className="lg:col-span-3">
            {/* 我的活动 */}
            {activeTab === 'activities' && (
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-lg font-semibold">我发布的活动</h2>
                  <Link
                    to="/activities/create"
                    className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                  >
                    发布新活动
                  </Link>
                </div>

                {myActivities.length > 0 ? (
                  <div className="space-y-4">
                    {myActivities.map((activity) => (
                      <Link
                        key={activity.id}
                        to={`/activities/${activity.id}`}
                        className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-indigo-300 transition"
                      >
                        <div>
                          <h3 className="font-medium text-gray-900">{activity.title}</h3>
                          <p className="text-sm text-gray-500 mt-1">
                            {formatDate(activity.startTime)} · {activity.location}
                          </p>
                        </div>
                        <ChevronRight className="text-gray-400" size={20} />
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <p>你还没有发布任何活动</p>
                    <Link
                      to="/activities/create"
                      className="text-indigo-600 hover:underline mt-2 inline-block"
                    >
                      发布第一个活动
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* 报名记录 */}
            {activeTab === 'registrations' && (
              <div className="bg-white rounded-xl shadow-sm p-6">
                <h2 className="text-lg font-semibold mb-6">我的报名记录</h2>

                {registrations.length > 0 ? (
                  <div className="space-y-4">
                    {registrations.map((reg) => (
                      <Link
                        key={reg.id}
                        to={`/activities/${reg.activityId}`}
                        className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-indigo-300 transition"
                      >
                        <div>
                          <h3 className="font-medium text-gray-900">{reg.activityTitle}</h3>
                          <p className="text-sm text-gray-500 mt-1">
                            报名时间: {formatDate(reg.registrationTime)}
                          </p>
                        </div>
                        <span className={`px-3 py-1 rounded-full text-sm ${
                          reg.status === 'confirmed' ? 'bg-green-100 text-green-700' :
                          reg.status === 'cancelled' ? 'bg-red-100 text-red-700' :
                          'bg-yellow-100 text-yellow-700'
                        }`}>
                          {reg.status === 'confirmed' ? '已确认' :
                           reg.status === 'cancelled' ? '已取消' : '待确认'}
                        </span>
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <p>你还没有报名任何活动</p>
                    <Link
                      to="/activities"
                      className="text-indigo-600 hover:underline mt-2 inline-block"
                    >
                      浏览活动
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* 我的收藏 */}
            {activeTab === 'favorites' && (
              <div className="bg-white rounded-xl shadow-sm p-6">
                <h2 className="text-lg font-semibold mb-6">我的收藏</h2>

                {favoriteActivities.length > 0 ? (
                  <div className="space-y-4">
                    {favoriteActivities.map((activity) => (
                      <Link
                        key={activity.id || (activity as any).activityId}
                        to={`/activities/${activity.id || (activity as any).activityId}`}
                        className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-indigo-300 transition"
                      >
                        <div>
                          <h3 className="font-medium text-gray-900">{activity.title || (activity as any).activityTitle}</h3>
                          <p className="text-sm text-gray-500 mt-1">
                            {formatDate(activity.startTime || (activity as any).startTime)} · {activity.location || (activity as any).activityLocation}
                          </p>
                          {activity.tags && activity.tags.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-2">
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
                        <ChevronRight className="text-gray-400" size={20} />
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <p>你还没有收藏任何活动</p>
                    <Link
                      to="/activities"
                      className="text-indigo-600 hover:underline mt-2 inline-block"
                    >
                      浏览活动
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* 账号设置 */}
            {activeTab === 'settings' && (
              <div className="space-y-6">
                {/* 编辑资料 */}
                <div className="bg-white rounded-xl shadow-sm p-6">
                  <h2 className="text-lg font-semibold mb-6">编辑资料</h2>
                  <form onSubmit={handleUpdateProfile} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">真实姓名</label>
                      <input
                        type="text"
                        value={editing ? editForm.realName : (profile?.realName || '')}
                        onChange={(e) => setEditForm({ ...editForm, realName: e.target.value })}
                        onClick={() => { if (!editing) setEditForm({ realName: profile?.realName || '', contact: profile?.contact || '' }); setEditing(true); }}
                        disabled={!editing}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none disabled:bg-gray-50"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">联系方式</label>
                      <input
                        type="text"
                        value={editing ? editForm.contact : (profile?.contact || '')}
                        onChange={(e) => setEditForm({ ...editForm, contact: e.target.value })}
                        disabled={!editing}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none disabled:bg-gray-50"
                      />
                    </div>
                    {editing && (
                      <div className="flex gap-3">
                        <button
                          type="submit"
                          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                        >
                          保存
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditing(false)}
                          className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                        >
                          取消
                        </button>
                      </div>
                    )}
                  </form>
                </div>

                {/* 修改密码 */}
                <div className="bg-white rounded-xl shadow-sm p-6">
                  <h2 className="text-lg font-semibold mb-6">修改密码</h2>
                  <form onSubmit={handleChangePassword} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">旧密码</label>
                      <input
                        type="password"
                        value={passwordForm.oldPassword}
                        onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">新密码</label>
                      <input
                        type="password"
                        value={passwordForm.newPassword}
                        onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                        required
                        minLength={6}
                      />
                    </div>
                    <button
                      type="submit"
                      className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                    >
                      修改密码
                    </button>
                  </form>
                </div>

                {/* 密保问题设置 */}
                <div className="bg-white rounded-xl shadow-sm p-6">
                  <div className="flex justify-between items-center mb-6">
                    <h2 className="text-lg font-semibold">密保问题</h2>
                    {currentSecurity && !showSecurityForm && (
                      <button
                        onClick={handleEditSecurity}
                        className="px-4 py-2 text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                      >
                        修改
                      </button>
                    )}
                  </div>
                  
                  {!showSecurityForm ? (
                    <div className="py-2">
                      {currentSecurity ? (
                        <div className="flex items-center gap-3">
                          <span className="text-gray-600">您的问题：</span>
                          <span className="font-medium text-gray-900">{currentSecurity.question}</span>
                        </div>
                      ) : (
                        <div className="text-center py-4">
                          <p className="text-gray-500 mb-4">您尚未设置密保问题</p>
                          <button
                            onClick={() => setShowSecurityForm(true)}
                            className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                          >
                            设置密保问题
                          </button>
                        </div>
                      )}
                    </div>
                  ) : (
                    <form onSubmit={handleSetSecurity} className="space-y-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">选择密保问题</label>
                        <select
                          value={securityForm.questionId}
                          onChange={(e) => setSecurityForm({ ...securityForm, questionId: Number(e.target.value) })}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none bg-white"
                          required
                        >
                          <option value={0}>请选择一个问题</option>
                          {securityQuestions.map((sq) => (
                            <option key={sq.questionId} value={sq.questionId}>
                              {sq.question}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">密保答案</label>
                        <input
                          type="text"
                          value={securityForm.answer}
                          onChange={(e) => setSecurityForm({ ...securityForm, answer: e.target.value })}
                          placeholder="请填写答案"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                          required
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">确认密码（当前密码）</label>
                        <input
                          type="password"
                          value={securityForm.password}
                          onChange={(e) => setSecurityForm({ ...securityForm, password: e.target.value })}
                          placeholder="请输入当前密码"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                          required
                        />
                      </div>
                      <div className="flex gap-3">
                        <button
                          type="submit"
                          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                        >
                          保存
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setShowSecurityForm(false);
                            setSecurityForm({ questionId: 0, answer: '', password: '' });
                          }}
                          className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                        >
                          取消
                        </button>
                      </div>
                    </form>
                  )}
                </div>
              </div>
            )}

            {/* 我的通知 */}
            {activeTab === 'notifications' && (
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-lg font-semibold">我的通知</h2>
                  {unreadCount > 0 && (
                    <button
                      onClick={handleMarkAllAsRead}
                      className="px-4 py-2 text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                    >
                      全部标为已读
                    </button>
                  )}
                </div>

                {notifications.length > 0 ? (
                  <div className="space-y-3">
                    {notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className={`p-4 border rounded-lg transition cursor-pointer ${
                          notification.isRead
                            ? 'border-gray-200 bg-gray-50'
                            : 'border-indigo-200 bg-indigo-50 hover:border-indigo-300'
                        }`}
                        onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-2">
                              {!notification.isRead && (
                                <span className="w-2 h-2 bg-indigo-600 rounded-full"></span>
                              )}
                              <h3 className="font-medium text-gray-900">{notification.title}</h3>
                            </div>
                            <p className="text-sm text-gray-600 mt-1">{notification.content}</p>
                            <p className="text-xs text-gray-400 mt-2">
                              {new Date(notification.createTime).toLocaleString('zh-CN')}
                            </p>
                          </div>
                          {notification.isRead && (
                            <span className="text-xs text-gray-400">已读</span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <Bell size={48} className="mx-auto mb-4 text-gray-300" />
                    <p>暂无通知</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
