import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users,
  Search,
  Shield,
  UserCheck,
  UserX,
  Loader2,
  GraduationCap,
  Filter,
  Lock,
  Unlock,
} from 'lucide-react';
import { getAllUsers, enableUser, disableUser, updateUserRole, unlockUserAccount, getLockList } from '@/api/admin';
import type { UserResponse as AdminUserResponse } from '@/types/admin';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台用户管理页面
 * 提供用户列表展示、搜索、角色切换和启用/禁用功能
 */
export default function AdminUsersPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [users, setUsers] = useState<AdminUserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeTab, setActiveTab] = useState<'users' | 'locked'>('users');
  const [lockedUsers, setLockedUsers] = useState<any[]>([]);
  const [lockedLoading, setLockedLoading] = useState(false);

  useEffect(() => {
    checkAdmin();
    loadUsers();
    loadLockedUsers();
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
   * 加载所有用户数据
   */
  const loadUsers = async () => {
    setLoading(true);
    try {
      const res = await getAllUsers();
      setUsers(res.data.data || []);
    } catch (err) {
      console.error('加载用户失败', err);
      addToast('error', '加载用户列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 启用指定用户
   */
  const handleEnable = async (userId: number) => {
    try {
      await enableUser(userId);
      addToast('success', '用户已启用');
      loadUsers();
    } catch (err) {
      console.error('启用用户失败', err);
      addToast('error', '启用用户失败，请重试');
    }
  };

  /**
   * 禁用指定用户
   */
  const handleDisable = async (userId: number) => {
    try {
      await disableUser(userId);
      addToast('success', '用户已禁用');
      loadUsers();
    } catch (err) {
      console.error('禁用用户失败', err);
      addToast('error', '禁用用户失败，请重试');
    }
  };

  /**
   * 更改用户角色
   */
  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      await updateUserRole(userId, { role: newRole as any });
      addToast('success', `角色已更改为${newRole === 'USER' ? '普通用户' : newRole === 'PUBLISHER' ? '发布者' : '管理员'}`);
      loadUsers();
    } catch (err) {
      console.error('更新角色失败', err);
      addToast('error', '更新角色失败，请重试');
    }
  };

  /**
   * 加载锁定用户列表
   */
  const loadLockedUsers = async () => {
    setLockedLoading(true);
    try {
      const res = await getLockList();
      setLockedUsers(res.data.data || []);
    } catch (err) {
      console.error('加载锁定用户失败', err);
    } finally {
      setLockedLoading(false);
    }
  };

  /**
   * 解锁用户账户
   */
  const handleUnlockUser = async (userId: number) => {
    try {
      await unlockUserAccount(userId);
      addToast('success', '用户已解锁');
      loadUsers();
      loadLockedUsers();
    } catch (err) {
      console.error('解锁用户失败', err);
      addToast('error', '解锁用户失败，请重试');
    }
  };

  /** 根据搜索关键词过滤用户列表 */
  const filteredUsers = users.filter(
    (user) =>
      user.username?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      user.realName?.toLowerCase().includes(searchKeyword.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-[#FAF5FF] flex">
      <Toast />
      <AdminSidebar />

      {/* 主内容区 */}
      <div className="flex-1 flex flex-col">
        {/* 顶部导航栏 */}
        <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-violet-100 shadow-sm">
          <div className="flex items-center justify-between px-6 h-16">
            <div className="flex items-center gap-3">
              <GraduationCap size={24} className="text-violet-600" />
              <h1 className="text-xl font-bold text-[#4C1D95]">用户管理</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          {/* 标签页导航 */}
          <div className="bg-white rounded-2xl shadow-card mb-6 p-2">
            <div className="flex gap-2">
              <button
                onClick={() => setActiveTab('users')}
                className={`inline-flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer ${
                  activeTab === 'users'
                    ? 'bg-violet-600 text-white shadow-md'
                    : 'bg-violet-50 text-[#4C1D95] hover:bg-violet-100'
                }`}
              >
                <Users size={18} />
                用户管理
              </button>
              <button
                onClick={() => setActiveTab('locked')}
                className={`inline-flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer ${
                  activeTab === 'locked'
                    ? 'bg-violet-600 text-white shadow-md'
                    : 'bg-violet-50 text-[#4C1D95] hover:bg-violet-100'
                }`}
              >
                <Lock size={18} />
                锁定用户
                {lockedUsers.length > 0 && (
                  <span className="px-2 py-0.5 bg-red-500 text-white rounded-full text-xs">{lockedUsers.length}</span>
                )}
              </button>
            </div>
          </div>

          {activeTab === 'users' && (
          <div className="bg-white rounded-2xl shadow-card overflow-hidden">
            {/* 搜索栏区域 */}
            <div className="px-6 py-5 border-b border-violet-100">
              <div className="relative max-w-md">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                  type="text"
                  placeholder="搜索用户名或姓名..."
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400"
                />
              </div>
              <p className="mt-3 text-sm text-gray-500">
                共 <span className="font-semibold text-[#4C1D95]">{filteredUsers.length}</span> 位用户
              </p>
            </div>

            {/* 用户列表/表格 */}
            <div className="overflow-x-auto">
              {loading ? (
                <div className="flex flex-col items-center justify-center py-16 gap-3">
                  <Loader2 className="animate-spin text-violet-600" size={32} />
                  <p className="text-gray-500">加载中...</p>
                </div>
              ) : filteredUsers.length === 0 ? (
                /* 空状态展示 */
                <div className="flex flex-col items-center justify-center py-16 gap-4">
                  <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                    <Users className="text-violet-400" size={36} />
                  </div>
                  <div className="text-center">
                    <p className="text-lg font-medium text-[#4C1D95]">暂无用户数据</p>
                    <p className="text-sm text-gray-400 mt-1">
                      {searchKeyword ? '尝试更换搜索关键词' : '目前还没有注册用户'}
                    </p>
                  </div>
                </div>
              ) : (
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-violet-100 bg-[#FAF5FF]/50">
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        用户信息
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        角色
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        状态
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        注册时间
                      </th>
                      <th className="px-6 py-4 text-right text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        操作
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-violet-50">
                    {filteredUsers.map((user) => (
                      <tr key={user.id} className="hover:bg-violet-50/30 transition-colors duration-200">
                        {/* 用户头像和信息 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-violet-400 to-purple-500 rounded-full flex items-center justify-center shadow-sm">
                              <span className="text-white font-semibold text-sm">
                                {(user.realName || user.username || '?').charAt(0).toUpperCase()}
                              </span>
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-semibold text-[#4C1D95]">
                                {user.realName || user.username}
                              </div>
                              <div className="text-sm text-gray-400">@{user.username}</div>
                            </div>
                          </div>
                        </td>

                        {/* 角色选择器 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <select
                            value={user.role}
                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                            className="px-3 py-1.5 bg-violet-50 border border-violet-200 rounded-lg text-sm font-medium text-[#4C1D95] focus:ring-2 focus:ring-violet-500 cursor-pointer transition-colors duration-200"
                          >
                            <option value="USER">普通用户</option>
                            <option value="PUBLISHER">发布者</option>
                            <option value="ADMIN">管理员</option>
                          </select>
                        </td>

                        {/* 状态徽章 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <StatusBadge status={user.status} />
                        </td>

                        {/* 注册时间 */}
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {user.createdAt ? new Date(user.createdAt).toLocaleDateString('zh-CN') : '-'}
                        </td>

                        {/* 操作按钮 */}
                        <td className="px-6 py-4 whitespace-nowrap text-right">
                          <div className="flex items-center justify-end gap-2">
                            {user.status === 'disabled' && (
                              <button
                                onClick={() => handleUnlockUser(user.id)}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-amber-600 hover:bg-amber-50 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                                title="解锁用户"
                              >
                                <Unlock size={16} />
                                解锁
                              </button>
                            )}
                            {user.status === 'enabled' ? (
                              <button
                                onClick={() => handleDisable(user.id)}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-red-600 hover:bg-red-50 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                                title="禁用用户"
                              >
                                <UserX size={16} />
                                禁用
                              </button>
                            ) : (
                              <button
                                onClick={() => handleEnable(user.id)}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-green-600 hover:bg-green-50 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                                title="启用用户"
                              >
                                <UserCheck size={16} />
                                启用
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
          )}

          {activeTab === 'locked' && (
            <div className="bg-white rounded-2xl shadow-card overflow-hidden">
              <div className="px-6 py-5 border-b border-violet-100">
                <div className="flex items-center gap-3">
                  <Lock className="text-red-500" size={20} />
                  <h2 className="text-lg font-semibold text-[#4C1D95]">登录锁定用户</h2>
                  <span className="px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm font-medium">
                    {lockedUsers.length} 个锁定
                  </span>
                </div>
                <p className="mt-2 text-sm text-gray-500">因多次登录失败被锁定的账户，可手动解除锁定</p>
              </div>

              <div className="overflow-x-auto">
                {lockedLoading ? (
                  <div className="flex flex-col items-center justify-center py-16 gap-3">
                    <Loader2 className="animate-spin text-violet-600" size={32} />
                    <p className="text-gray-500">加载中...</p>
                  </div>
                ) : lockedUsers.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-16 gap-4">
                    <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
                      <Unlock className="text-green-500" size={36} />
                    </div>
                    <div className="text-center">
                      <p className="text-lg font-medium text-[#4C1D95]">暂无锁定用户</p>
                      <p className="text-sm text-gray-400 mt-1">所有用户登录状态正常</p>
                    </div>
                  </div>
                ) : (
                  <table className="w-full">
                    <thead>
                      <tr className="border-b border-violet-100 bg-[#FAF5FF]/50">
                        <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">用户名</th>
                        <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">锁定时间</th>
                        <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">锁定原因</th>
                        <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">失败次数</th>
                        <th className="px-6 py-4 text-right text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">操作</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-violet-50">
                      {lockedUsers.map((item: any, index: number) => (
                        <tr key={index} className="hover:bg-violet-50/30 transition-colors duration-200">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center gap-3">
                              <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                                <Lock size={14} className="text-red-500" />
                              </div>
                              <span className="text-sm font-semibold text-[#4C1D95]">{item.username || '-'}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {item.lockTime || '-'}
                          </td>
                          <td className="px-6 py-4 text-sm text-gray-500">
                            {item.lockReason || '多次登录失败'}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className="px-2.5 py-1 bg-red-100 text-red-700 rounded-full text-xs font-semibold">
                              {item.lockCount || '-'} 次
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right">
                            <button
                              onClick={() => handleUnlockUser(item.userId || item.id)}
                              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-green-600 hover:bg-green-50 rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                            >
                              <Unlock size={16} />
                              解锁
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

/**
 * 用户状态徽章组件
 */
function StatusBadge({ status }: { status: string }) {
  const config =
    status === 'enabled'
      ? {
          bg: 'bg-green-100',
          text: 'text-green-700',
          icon: <UserCheck size={12} />,
          label: '已启用',
        }
      : {
          bg: 'bg-red-100',
          text: 'text-red-700',
          icon: <UserX size={12} />,
          label: '已禁用',
        };

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold ${config.bg} ${config.text}`}
    >
      {config.icon}
      {config.label}
    </span>
  );
}