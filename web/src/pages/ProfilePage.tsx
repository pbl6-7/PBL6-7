import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Calendar, MessageSquare, Settings, LogOut, Loader2, ChevronRight, Bell, Heart, ArrowLeft, Edit3, Shield, Lock, Eye, EyeOff, Check, X, Clock, MapPin, Users, AlertCircle, Info, CheckCircle, XCircle } from 'lucide-react';
import { getProfile, updateProfile, changePassword, getSecurityQuestions, getUserSecurityQuestion, setSecurity } from '@/api/user';
import { getMyRegistrations } from '@/api/registration';
import { getMyActivities } from '@/api/activity';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification';
import { getFavorites } from '@/api/favorite';
import type { User as UserType, LoginResponse, SecurityQuestion } from '@/types/user';
import type { RegistrationResponse } from '@/types/registration';
import type { Activity } from '@/types/activity';
import type { Notification } from '@/types/notification';
import Navbar from '@/components/Navbar';
import { useToastStore } from '@/components/Toast';

/**
 * 个人中心页面
 * 提供用户资料管理、活动发布、报名记录、收藏、设置和通知等功能
 */
export default function ProfilePage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
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
  const [showPassword, setShowPassword] = useState({ old: false, new: false });

  // 密保问题状态
  const [securityQuestions, setSecurityQuestions] = useState<SecurityQuestion[]>([]);
  const [currentSecurity, setCurrentSecurity] = useState<{ questionId: number; question: string } | null>(null);
  const [securityForm, setSecurityForm] = useState({ questionId: 0, answer: '', password: '' });
  const [showSecurityForm, setShowSecurityForm] = useState(false);

  /**
   * 初始化加载用户信息和数据
   */
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      navigate('/login');
      return;
    }
    setUser(JSON.parse(userStr));
    loadData();
  }, []);

  /**
   * 加载所有用户相关数据
   */
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
      setUnreadCount(unreadRes.data.data?.unreadCount || 0);
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
      addToast('error', '加载数据失败，请刷新页面重试');
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
      addToast('error', '加载通知失败');
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
   * 处理用户退出登录
   */
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    addToast('info', '已退出登录');
    navigate('/login');
  };

  /**
   * 处理更新用户资料
   * @param e - 表单提交事件
   */
  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    /* 真实姓名长度校验 */
    if (editForm.realName.trim() && (editForm.realName.trim().length < 2 || editForm.realName.trim().length > 20)) {
      addToast('warning', '真实姓名需2-20个字符');
      return;
    }
    /* 联系方式格式校验（手机号或邮箱） */
    if (editForm.contact.trim()) {
      const phoneRegex = /^1[3-9]\d{9}$/;
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!phoneRegex.test(editForm.contact.trim()) && !emailRegex.test(editForm.contact.trim())) {
        addToast('warning', '联系方式格式不正确，请输入手机号或邮箱');
        return;
      }
    }
    try {
      await updateProfile(editForm);
      setEditing(false);
      loadData();
      addToast('success', '资料更新成功');
    } catch (err: any) {
      addToast('error', err.message || '更新失败');
    }
  };

  /**
   * 处理修改密码
   * @param e - 表单提交事件
   */
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    /* 密码强度验证：与注册一致，至少8位，包含大小写字母、数字和特殊字符 */
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!passwordRegex.test(passwordForm.newPassword)) {
      addToast('warning', '密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符');
      return;
    }
    try {
      await changePassword(passwordForm);
      setPasswordForm({ oldPassword: '', newPassword: '' });
      addToast('success', '密码修改成功');
    } catch (err: any) {
      addToast('error', err.message || '修改失败');
    }
  };

  /**
   * 处理密保问题设置/修改提交
   * @param e - 表单提交事件
   */
  const handleSetSecurity = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!securityForm.questionId) {
      addToast('warning', '请选择密保问题');
      return;
    }
    if (!securityForm.answer.trim()) {
      addToast('warning', '请填写密保答案');
      return;
    }
    if (!securityForm.password) {
      addToast('warning', '请填写当前密码');
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
      addToast('success', '密保问题设置成功');
    } catch (err: any) {
      addToast('error', err.message || '设置失败');
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

  /**
   * 格式化日期显示
   * @param dateStr - 日期字符串
   */
  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  /**
   * 格式化日期时间显示
   * @param dateStr - 日期字符串
   */
  const formatDateTime = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * 计算密码强度
   * @param password - 密码字符串
   */
  const getPasswordStrength = (password: string) => {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    return strength;
  };

  /**
   * 获取密码强度标签和颜色
   * @param strength - 强度值 0-5
   */
  const getStrengthInfo = (strength: number) => {
    if (strength <= 1) return { label: '弱', color: 'bg-red-500', text: 'text-red-500' };
    if (strength <= 2) return { label: '较弱', color: 'bg-orange-500', text: 'text-orange-500' };
    if (strength <= 3) return { label: '中等', color: 'bg-yellow-500', text: 'text-yellow-500' };
    if (strength <= 4) return { label: '强', color: 'bg-green-500', text: 'text-green-500' };
    return { label: '很强', color: 'bg-green-600', text: 'text-green-600' };
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#FAF5FF] flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="animate-spin text-violet-600 mx-auto mb-4" size={48} />
          <p className="text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#FAF5FF]">
      <Navbar />
      
      {/* 渐变头部横幅 */}
      <div className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-violet-600 via-violet-500 to-purple-600 opacity-90" />
        <div className="absolute inset-0 opacity-30" style={{ backgroundImage: "url(\"data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E\")" }} />
        
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex items-start gap-8">
            {/* 头像 */}
            <div className="relative">
              <div className="w-28 h-28 rounded-full bg-white/20 backdrop-blur-sm border-4 border-white/40 flex items-center justify-center shadow-2xl">
                <span className="text-4xl font-bold text-white">
                  {profile?.realName?.charAt(0) || user?.username?.charAt(0) || 'U'}
                </span>
              </div>
              <div className="absolute -bottom-1 -right-1 w-8 h-8 bg-green-500 rounded-full border-4 border-violet-600 flex items-center justify-center">
                <Check size={14} className="text-white" />
              </div>
            </div>

            {/* 用户信息 */}
            <div className="flex-1 pt-2">
              <div className="flex items-center gap-3 mb-2">
                <h1 className="text-3xl font-bold text-white">
                  {profile?.realName || user?.username}
                </h1>
                <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-sm text-white border border-white/30">
                  @{user?.username}
                </span>
              </div>
              
              <div className="flex items-center gap-3 mb-4">
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                  user?.role === 'ADMIN' 
                    ? 'bg-amber-500/20 text-amber-100 border border-amber-400/30' 
                    : user?.role === 'PUBLISHER' 
                      ? 'bg-blue-500/20 text-blue-100 border border-blue-400/30'
                      : 'bg-green-500/20 text-green-100 border border-green-400/30'
                }`}>
                  {user?.role === 'ADMIN' ? '管理员' : user?.role === 'PUBLISHER' ? '发布者' : '普通用户'}
                </span>
                {profile?.contact && (
                  <span className="text-violet-100 text-sm">
                    {profile.contact}
                  </span>
                )}
              </div>

              <div className="flex items-center gap-4 text-violet-100 text-sm">
                <div className="flex items-center gap-1">
                  <Calendar size={16} />
                  <span>{myActivities.length} 个活动</span>
                </div>
                <div className="flex items-center gap-1">
                  <MessageSquare size={16} />
                  <span>{registrations.length} 次报名</span>
                </div>
                <div className="flex items-center gap-1">
                  <Heart size={16} />
                  <span>{favoriteActivities.length} 个收藏</span>
                </div>
              </div>
            </div>

            {/* 编辑按钮 */}
            <button
              onClick={() => {
                setEditForm({ realName: profile?.realName || '', contact: profile?.contact || '' });
                setEditing(true);
                setActiveTab('settings');
              }}
              className="px-5 py-2.5 bg-white/20 backdrop-blur-sm hover:bg-white/30 rounded-xl text-white font-medium border border-white/30 transition-all duration-200 flex items-center gap-2 cursor-pointer"
            >
              <Edit3 size={18} />
              编辑资料
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* 玻璃态侧边栏 */}
          <div className="lg:col-span-1">
            <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-4 sticky top-24">
              <nav className="space-y-1">
                <button
                  onClick={() => setActiveTab('activities')}
                  className={`w-full flex items-center justify-between px-4 py-3.5 rounded-xl transition-all duration-200 ${
                    activeTab === 'activities'
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-200'
                      : 'text-[#4C1D95] hover:bg-violet-50'
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <Calendar size={18} />
                    我的活动
                  </span>
                  {myActivities.length > 0 && (
                    <span className={`px-2 py-0.5 rounded-full text-xs ${
                      activeTab === 'activities' ? 'bg-white/20' : 'bg-violet-100 text-violet-600'
                    }`}>
                      {myActivities.length}
                    </span>
                  )}
                </button>
                
                <button
                  onClick={() => setActiveTab('registrations')}
                  className={`w-full flex items-center justify-between px-4 py-3.5 rounded-xl transition-all duration-200 ${
                    activeTab === 'registrations'
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-200'
                      : 'text-[#4C1D95] hover:bg-violet-50'
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <MessageSquare size={18} />
                    报名记录
                  </span>
                  {registrations.length > 0 && (
                    <span className={`px-2 py-0.5 rounded-full text-xs ${
                      activeTab === 'registrations' ? 'bg-white/20' : 'bg-violet-100 text-violet-600'
                    }`}>
                      {registrations.length}
                    </span>
                  )}
                </button>
                
                <button
                  onClick={() => setActiveTab('favorites')}
                  className={`w-full flex items-center justify-between px-4 py-3.5 rounded-xl transition-all duration-200 ${
                    activeTab === 'favorites'
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-200'
                      : 'text-[#4C1D95] hover:bg-violet-50'
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <Heart size={18} />
                    我的收藏
                  </span>
                  {favoriteActivities.length > 0 && (
                    <span className={`px-2 py-0.5 rounded-full text-xs ${
                      activeTab === 'favorites' ? 'bg-white/20' : 'bg-violet-100 text-violet-600'
                    }`}>
                      {favoriteActivities.length}
                    </span>
                  )}
                </button>
                
                <button
                  onClick={() => setActiveTab('settings')}
                  className={`w-full flex items-center gap-3 px-4 py-3.5 rounded-xl transition-all duration-200 ${
                    activeTab === 'settings'
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-200'
                      : 'text-[#4C1D95] hover:bg-violet-50'
                  }`}
                >
                  <Settings size={18} />
                  账号设置
                </button>
                
                <button
                  onClick={() => { setActiveTab('notifications'); loadNotifications(); }}
                  className={`w-full flex items-center justify-between px-4 py-3.5 rounded-xl transition-all duration-200 ${
                    activeTab === 'notifications'
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-200'
                      : 'text-[#4C1D95] hover:bg-violet-50'
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <Bell size={18} />
                    我的通知
                  </span>
                  {unreadCount > 0 && (
                    <span className="px-2 py-0.5 bg-red-500 text-white text-xs font-bold rounded-full">
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </button>

                <div className="border-t border-violet-100 my-2" />
                
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-3 px-4 py-3.5 rounded-xl text-red-600 hover:bg-red-50 transition-all duration-200 font-medium"
                >
                  <LogOut size={18} />
                  退出登录
                </button>
              </nav>
            </div>
          </div>

          {/* 主内容区域 */}
          <div className="lg:col-span-3">
            {/* 我的活动 */}
            {activeTab === 'activities' && (
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-xl font-bold text-[#4C1D95]">我发布的活动</h2>
                  <Link
                    to="/activities/create"
                    className="px-5 py-2.5 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 flex items-center gap-2 cursor-pointer"
                  >
                    <Calendar size={18} />
                    发布新活动
                  </Link>
                </div>

                {myActivities.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {myActivities.map((activity) => (
                      <Link
                        key={activity.id}
                        to={`/activities/${activity.id}`}
                        className="group bg-white/60 hover:bg-white/80 rounded-xl p-5 border border-violet-100 hover:border-violet-300 transition-all duration-200 cursor-pointer"
                      >
                        <div className="flex items-start justify-between mb-3">
                          <h3 className="font-semibold text-[#4C1D95] group-hover:text-violet-600 transition-colors line-clamp-2">
                            {activity.title}
                          </h3>
                          <ChevronRight className="text-gray-400 group-hover:text-violet-600 transition-colors flex-shrink-0" size={20} />
                        </div>
                        
                        <div className="space-y-2 text-sm text-gray-600">
                          <div className="flex items-center gap-2">
                            <Clock size={16} className="text-violet-500" />
                            <span>{formatDate(activity.startTime)}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin size={16} className="text-violet-500" />
                            <span className="truncate">{activity.location}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Users size={16} className="text-violet-500" />
                            <span>{activity.currentParticipants || 0} / {activity.maxParticipants} 人</span>
                          </div>
                        </div>

                        <div className="mt-4 pt-3 border-t border-violet-100 flex items-center justify-between">
                          <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                            activity.approvalStatus === 'approved' 
                              ? 'bg-green-100 text-green-700' 
                              : activity.approvalStatus === 'pending'
                                ? 'bg-yellow-100 text-yellow-700'
                                : 'bg-red-100 text-red-700'
                          }`}>
                            {activity.approvalStatus === 'approved' ? '已通过' : activity.approvalStatus === 'pending' ? '审核中' : '已拒绝'}
                          </span>
                          {activity.tags && activity.tags.length > 0 && (
                            <div className="flex gap-1">
                              {activity.tags.slice(0, 2).map((tag) => (
                                <span
                                  key={tag.id}
                                  className="px-2 py-0.5 bg-violet-50 text-violet-600 rounded text-xs"
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
                ) : (
                  <div className="text-center py-12">
                    <Calendar size={64} className="mx-auto mb-4 text-violet-200" />
                    <p className="text-gray-500 mb-4">你还没有发布任何活动</p>
                    <Link
                      to="/activities/create"
                      className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer"
                    >
                      <Calendar size={18} />
                      发布第一个活动
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* 报名记录 */}
            {activeTab === 'registrations' && (
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                <h2 className="text-xl font-bold text-[#4C1D95] mb-6">我的报名记录</h2>

                {registrations.length > 0 ? (
                  <div className="space-y-4">
                    {registrations.map((reg) => (
                      <Link
                        key={reg.id}
                        to={`/activities/${reg.activityId}`}
                        className="group bg-white/60 hover:bg-white/80 rounded-xl p-5 border border-violet-100 hover:border-violet-300 transition-all duration-200 cursor-pointer"
                      >
                        <div className="flex items-start justify-between mb-3">
                          <div className="flex-1">
                            <h3 className="font-semibold text-[#4C1D95] group-hover:text-violet-600 transition-colors mb-2">
                              {reg.activityTitle}
                            </h3>
                            <div className="flex items-center gap-4 text-sm text-gray-600">
                              <div className="flex items-center gap-1">
                                <Clock size={16} className="text-violet-500" />
                                <span>{formatDate(reg.registrationTime)}</span>
                              </div>
                            </div>
                          </div>
                          <span className={`px-3 py-1.5 rounded-full text-sm font-medium ${
                            reg.status === 'confirmed' 
                              ? 'bg-green-100 text-green-700' 
                              : reg.status === 'cancelled' 
                                ? 'bg-red-100 text-red-700'
                                : 'bg-yellow-100 text-yellow-700'
                          }`}>
                            {reg.status === 'confirmed' ? '已确认' : reg.status === 'cancelled' ? '已取消' : '待确认'}
                          </span>
                        </div>

                        {reg.status === 'confirmed' && (
                          <div className="mt-3 pt-3 border-t border-violet-100">
                            <div className="flex items-center gap-2 text-sm text-green-600">
                              <CheckCircle size={16} />
                              <span>报名成功，请准时参加</span>
                            </div>
                          </div>
                        )}
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <MessageSquare size={64} className="mx-auto mb-4 text-violet-200" />
                    <p className="text-gray-500 mb-4">你还没有报名任何活动</p>
                    <Link
                      to="/activities"
                      className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer"
                    >
                      <Calendar size={18} />
                      浏览活动
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* 我的收藏 */}
            {activeTab === 'favorites' && (
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                <h2 className="text-xl font-bold text-[#4C1D95] mb-6">我的收藏</h2>

                {favoriteActivities.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {favoriteActivities.map((activity) => (
                      <Link
                        key={activity.id || (activity as any).activityId}
                        to={`/activities/${activity.id || (activity as any).activityId}`}
                        className="group bg-white/60 hover:bg-white/80 rounded-xl p-5 border border-violet-100 hover:border-violet-300 transition-all duration-200 cursor-pointer"
                      >
                        <div className="flex items-start justify-between mb-3">
                          <h3 className="font-semibold text-[#4C1D95] group-hover:text-violet-600 transition-colors line-clamp-2">
                            {activity.title || (activity as any).activityTitle}
                          </h3>
                          <ChevronRight className="text-gray-400 group-hover:text-violet-600 transition-colors flex-shrink-0" size={20} />
                        </div>
                        
                        <div className="space-y-2 text-sm text-gray-600">
                          <div className="flex items-center gap-2">
                            <Clock size={16} className="text-violet-500" />
                            <span>{formatDate(activity.startTime || (activity as any).startTime)}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin size={16} className="text-violet-500" />
                            <span className="truncate">{activity.location || (activity as any).activityLocation}</span>
                          </div>
                        </div>

                        {(activity.tags && activity.tags.length > 0) && (
                          <div className="mt-4 pt-3 border-t border-violet-100 flex flex-wrap gap-1">
                            {activity.tags.slice(0, 3).map((tag) => (
                              <span
                                key={tag.id}
                                className="px-2 py-0.5 bg-violet-50 text-violet-600 rounded text-xs"
                              >
                                {tag.name}
                              </span>
                            ))}
                          </div>
                        )}
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Heart size={64} className="mx-auto mb-4 text-violet-200" />
                    <p className="text-gray-500 mb-4">你还没有收藏任何活动</p>
                    <Link
                      to="/activities"
                      className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer"
                    >
                      <Calendar size={18} />
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
                <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center">
                      <User size={20} className="text-violet-600" />
                    </div>
                    <h2 className="text-xl font-bold text-[#4C1D95]">编辑资料</h2>
                  </div>
                  
                  <form onSubmit={handleUpdateProfile} className="space-y-5">
                    <div>
                      <label className="block text-sm font-medium text-[#4C1D95] mb-2">真实姓名</label>
                      <input
                        type="text"
                        value={editing ? editForm.realName : (profile?.realName || '')}
                        onChange={(e) => setEditForm({ ...editForm, realName: e.target.value })}
                        onClick={() => { if (!editing) setEditForm({ realName: profile?.realName || '', contact: profile?.contact || '' }); setEditing(true); }}
                        disabled={!editing}
                        maxLength={20}
                        className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none disabled:bg-gray-50 disabled:cursor-not-allowed transition-all duration-200"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-[#4C1D95] mb-2">联系方式</label>
                      <input
                        type="text"
                        value={editing ? editForm.contact : (profile?.contact || '')}
                        onChange={(e) => setEditForm({ ...editForm, contact: e.target.value })}
                        disabled={!editing}
                        maxLength={50}
                        placeholder="手机号或邮箱"
                        className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none disabled:bg-gray-50 disabled:cursor-not-allowed transition-all duration-200"
                      />
                    </div>
                    {editing && (
                      <div className="flex gap-3">
                        <button
                          type="submit"
                          className="px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer font-medium"
                        >
                          保存
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditing(false)}
                          className="px-6 py-3 border border-violet-200 text-[#4C1D95] rounded-xl hover:bg-violet-50 transition-all duration-200 cursor-pointer font-medium"
                        >
                          取消
                        </button>
                      </div>
                    )}
                  </form>
                </div>

                {/* 修改密码 */}
                <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center">
                      <Lock size={20} className="text-violet-600" />
                    </div>
                    <h2 className="text-xl font-bold text-[#4C1D95]">修改密码</h2>
                  </div>
                  
                  <form onSubmit={handleChangePassword} className="space-y-5">
                    <div>
                      <label className="block text-sm font-medium text-[#4C1D95] mb-2">旧密码</label>
                      <div className="relative">
                        <input
                          type={showPassword.old ? 'text' : 'password'}
                          value={passwordForm.oldPassword}
                          onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                          className="w-full px-4 py-3 pr-12 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-all duration-200"
                          required
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword({ ...showPassword, old: !showPassword.old })}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-violet-600 transition-colors cursor-pointer"
                        >
                          {showPassword.old ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-[#4C1D95] mb-2">新密码</label>
                      <div className="relative">
                        <input
                          type={showPassword.new ? 'text' : 'password'}
                          value={passwordForm.newPassword}
                          onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                          className="w-full px-4 py-3 pr-12 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-all duration-200"
                          placeholder="至少8位，包含大小写字母、数字和特殊字符"
                          required
                          minLength={8}
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword({ ...showPassword, new: !showPassword.new })}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-violet-600 transition-colors cursor-pointer"
                        >
                          {showPassword.new ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                      </div>
                      {passwordForm.newPassword && (
                        <div className="mt-2">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-xs text-gray-500">密码强度：</span>
                            <span className={`text-xs font-medium ${getStrengthInfo(getPasswordStrength(passwordForm.newPassword)).text}`}>
                              {getStrengthInfo(getPasswordStrength(passwordForm.newPassword)).label}
                            </span>
                          </div>
                          <div className="flex gap-1">
                            {[1, 2, 3, 4, 5].map((i) => (
                              <div
                                key={i}
                                className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
                                  i <= getPasswordStrength(passwordForm.newPassword)
                                    ? getStrengthInfo(getPasswordStrength(passwordForm.newPassword)).color
                                    : 'bg-gray-200'
                                }`}
                              />
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                    <button
                      type="submit"
                      className="w-full px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer font-medium"
                    >
                      修改密码
                    </button>
                  </form>
                </div>

                {/* 密保问题设置 */}
                <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                  <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center">
                        <Shield size={20} className="text-violet-600" />
                      </div>
                      <h2 className="text-xl font-bold text-[#4C1D95]">密保问题</h2>
                    </div>
                    {currentSecurity && !showSecurityForm && (
                      <button
                        onClick={handleEditSecurity}
                        className="px-4 py-2 text-violet-600 hover:bg-violet-50 rounded-xl transition-all duration-200 cursor-pointer font-medium"
                      >
                        修改
                      </button>
                    )}
                  </div>
                  
                  {!showSecurityForm ? (
                    <div className="py-2">
                      {currentSecurity ? (
                        <div className="flex items-center gap-3 p-4 bg-violet-50 rounded-xl">
                          <Shield size={20} className="text-violet-600" />
                          <div>
                            <p className="text-sm text-gray-600 mb-1">您的问题：</p>
                            <p className="font-medium text-[#4C1D95]">{currentSecurity.question}</p>
                          </div>
                        </div>
                      ) : (
                        <div className="text-center py-8">
                          <Shield size={48} className="mx-auto mb-4 text-violet-200" />
                          <p className="text-gray-500 mb-4">您尚未设置密保问题</p>
                          <button
                            onClick={() => setShowSecurityForm(true)}
                            className="px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer font-medium"
                          >
                            设置密保问题
                          </button>
                        </div>
                      )}
                    </div>
                  ) : (
                    <form onSubmit={handleSetSecurity} className="space-y-5">
                      <div>
                        <label className="block text-sm font-medium text-[#4C1D95] mb-2">选择密保问题</label>
                        <select
                          value={securityForm.questionId}
                          onChange={(e) => setSecurityForm({ ...securityForm, questionId: Number(e.target.value) })}
                          className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none bg-white transition-all duration-200"
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
                        <label className="block text-sm font-medium text-[#4C1D95] mb-2">密保答案</label>
                        <input
                          type="text"
                          value={securityForm.answer}
                          onChange={(e) => setSecurityForm({ ...securityForm, answer: e.target.value })}
                          placeholder="请填写答案"
                          className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-all duration-200"
                          required
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-[#4C1D95] mb-2">确认密码（当前密码）</label>
                        <input
                          type="password"
                          value={securityForm.password}
                          onChange={(e) => setSecurityForm({ ...securityForm, password: e.target.value })}
                          placeholder="请输入当前密码"
                          className="w-full px-4 py-3 border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-all duration-200"
                          required
                        />
                      </div>
                      <div className="flex gap-3">
                        <button
                          type="submit"
                          className="px-6 py-3 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:shadow-lg hover:shadow-violet-200 transition-all duration-200 cursor-pointer font-medium"
                        >
                          保存
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setShowSecurityForm(false);
                            setSecurityForm({ questionId: 0, answer: '', password: '' });
                          }}
                          className="px-6 py-3 border border-violet-200 text-[#4C1D95] rounded-xl hover:bg-violet-50 transition-all duration-200 cursor-pointer font-medium"
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
              <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-white/20 p-6">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-xl font-bold text-[#4C1D95]">我的通知</h2>
                  {unreadCount > 0 && (
                    <button
                      onClick={handleMarkAllAsRead}
                      className="px-4 py-2 text-violet-600 hover:bg-violet-50 rounded-xl transition-all duration-200 cursor-pointer font-medium"
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
                        className={`p-5 rounded-xl border transition-all duration-200 cursor-pointer ${
                          notification.isRead
                            ? 'border-violet-100 bg-white/40 hover:bg-white/60'
                            : 'border-violet-300 bg-violet-50/50 hover:bg-violet-50'
                        }`}
                        onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
                      >
                        <div className="flex items-start gap-4">
                          <div className={`w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 ${
                            notification.isRead ? 'bg-gray-100' : 'bg-violet-100'
                          }`}>
                            {notification.type === 'success' ? (
                              <CheckCircle size={20} className={notification.isRead ? 'text-gray-400' : 'text-green-600'} />
                            ) : notification.type === 'error' ? (
                              <XCircle size={20} className={notification.isRead ? 'text-gray-400' : 'text-red-600'} />
                            ) : notification.type === 'warning' ? (
                              <AlertCircle size={20} className={notification.isRead ? 'text-gray-400' : 'text-yellow-600'} />
                            ) : (
                              <Info size={20} className={notification.isRead ? 'text-gray-400' : 'text-violet-600'} />
                            )}
                          </div>
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              {!notification.isRead && (
                                <span className="w-2 h-2 bg-violet-600 rounded-full"></span>
                              )}
                              <h3 className={`font-medium ${notification.isRead ? 'text-gray-600' : 'text-[#4C1D95]'}`}>
                                {notification.title}
                              </h3>
                            </div>
                            <p className={`text-sm mb-2 ${notification.isRead ? 'text-gray-500' : 'text-gray-700'}`}>
                              {notification.content}
                            </p>
                            <p className="text-xs text-gray-400">
                              {formatDateTime(notification.createTime)}
                            </p>
                          </div>
                          {notification.isRead && (
                            <span className="text-xs text-gray-400 px-2 py-1 bg-gray-100 rounded-full">已读</span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Bell size={64} className="mx-auto mb-4 text-violet-200" />
                    <p className="text-gray-500">暂无通知</p>
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