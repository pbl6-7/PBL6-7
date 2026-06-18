import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  GraduationCap,
  Search,
  Bell,
  User,
  LogOut,
  Menu,
  X,
  ChevronDown,
  CalendarDays,
  ClipboardList,
  ShieldCheck,
  Heart,
} from 'lucide-react';
import { getUnreadCount } from '@/api/notification';
import type { LoginResponse } from '@/types/user';

/** Navbar 组件属性接口 */
interface NavbarProps {
  /** 搜索关键词 */
  searchKeyword?: string;
  /** 搜索回调 */
  onSearch?: (keyword: string) => void;
  /** 是否隐藏搜索框（搜索页自身有搜索框时使用） */
  hideSearch?: boolean;
}

/** 浮动导航栏组件，提供搜索、用户菜单、通知等功能 */
export default function Navbar({ searchKeyword = '', onSearch, hideSearch = false }: NavbarProps) {
  const navigate = useNavigate();
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [keyword, setKeyword] = useState(searchKeyword);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const dropdownRef = useRef<HTMLDivElement>(null);

  /* 初始化：从 localStorage 读取用户信息 */
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  /* 同步外部 searchKeyword 变化 */
  useEffect(() => {
    setKeyword(searchKeyword);
  }, [searchKeyword]);

  /* 获取未读通知数量 */
  useEffect(() => {
    if (user) {
      getUnreadCount()
        .then((res) => setUnreadCount(res.data.data?.unreadCount || 0))
        .catch(() => {});
    }
  }, [user]);

  /* 点击外部关闭下拉菜单 */
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /** 处理搜索提交，跳转到搜索页并执行全局搜索 */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      onSearch?.(keyword.trim());
      navigate(`/search?keyword=${encodeURIComponent(keyword.trim())}`);
    }
  };

  /** 处理用户退出登录 */
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setDropdownOpen(false);
    navigate('/login');
  };

  return (
    <nav className="sticky top-4 left-4 right-4 z-50 rounded-2xl bg-white/80 backdrop-blur-xl border border-white/20 shadow-lg transition-colors duration-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 cursor-pointer group">
            <GraduationCap size={28} className="text-violet-600 group-hover:text-violet-700 transition-colors duration-200" />
            <span className="text-xl font-bold text-violet-600 group-hover:text-violet-700 transition-colors duration-200">
              校园活动平台
            </span>
          </Link>

          {/* 搜索框 - 桌面端 */}
          {!hideSearch && (
          <div className="hidden md:flex flex-1 max-w-lg mx-8">
            <form onSubmit={handleSearch} className="w-full flex">
              <div className="relative flex-1">
                <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="搜索活动..."
                  className="w-full pl-9 pr-4 py-2 border border-violet-200 rounded-l-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                />
              </div>
              <button
                type="submit"
                className="px-4 py-2 bg-violet-600 text-white rounded-r-xl hover:bg-violet-700 transition-colors duration-200 cursor-pointer"
              >
                <Search size={16} />
              </button>
            </form>
          </div>
          )}

          {/* 导航链接 - 桌面端 */}
          <div className="hidden md:flex items-center space-x-3">
            <Link
              to="/activities"
              className="flex items-center gap-1.5 px-3 py-2 text-[#4C1D95] hover:text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm font-medium"
            >
              <CalendarDays size={16} />
              活动列表
            </Link>

            {user ? (
              <>
                {/* 通知铃铛 */}
                <Link
                  to="/notifications"
                  className="relative p-2 text-[#4C1D95] hover:text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer"
                >
                  <Bell size={20} />
                  {unreadCount > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 px-1.5 py-0.5 bg-green-500 text-white text-[10px] font-bold rounded-full min-w-[18px] text-center leading-none">
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </Link>

                {/* 用户下拉菜单 */}
                <div className="relative" ref={dropdownRef}>
                  <button
                    onClick={() => setDropdownOpen(!dropdownOpen)}
                    className="flex items-center gap-2 px-3 py-2 text-[#4C1D95] hover:text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer"
                  >
                    <div className="w-7 h-7 rounded-full bg-violet-100 flex items-center justify-center">
                      <User size={14} className="text-violet-600" />
                    </div>
                    <span className="text-sm font-medium">{user.username}</span>
                    <ChevronDown size={14} className={`transition-transform duration-200 ${dropdownOpen ? 'rotate-180' : ''}`} />
                  </button>

                  {dropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white/95 backdrop-blur-xl rounded-xl shadow-lg border border-violet-100 py-1 animate-fadeIn z-50">
                      <Link
                        to="/profile"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2.5 text-sm text-[#4C1D95] hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                      >
                        <User size={15} />
                        个人中心
                      </Link>
                      <Link
                        to="/my-activities"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2.5 text-sm text-[#4C1D95] hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                      >
                        <CalendarDays size={15} />
                        我的活动
                      </Link>
                      <Link
                        to="/my-registrations"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2.5 text-sm text-[#4C1D95] hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                      >
                        <ClipboardList size={15} />
                        报名记录
                      </Link>
                      <Link
                        to="/favorites"
                        onClick={() => setDropdownOpen(false)}
                        className="flex items-center gap-2 px-4 py-2.5 text-sm text-[#4C1D95] hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                      >
                        <Heart size={15} />
                        我的收藏
                      </Link>
                      {user.role === 'ADMIN' && (
                        <Link
                          to="/admin"
                          onClick={() => setDropdownOpen(false)}
                          className="flex items-center gap-2 px-4 py-2.5 text-sm text-violet-600 hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                        >
                          <ShieldCheck size={15} />
                          管理后台
                        </Link>
                      )}
                      <div className="border-t border-violet-100 my-1" />
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors duration-200 cursor-pointer"
                      >
                        <LogOut size={15} />
                        退出登录
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link
                to="/login"
                className="px-4 py-2 bg-green-500 text-white rounded-xl hover:bg-green-600 transition-colors duration-200 cursor-pointer text-sm font-medium"
              >
                登录 / 注册
              </Link>
            )}
          </div>

          {/* 移动端菜单按钮 */}
          <button
            className="md:hidden p-2 text-[#4C1D95] hover:text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* 移动端菜单 */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-violet-100 animate-fadeIn">
            {/* 搜索框 */}
            {!hideSearch && (
            <form onSubmit={handleSearch} className="mb-4 flex">
              <div className="relative flex-1">
                <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="搜索活动..."
                  className="w-full pl-9 pr-4 py-2 border border-violet-200 rounded-l-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                />
              </div>
              <button
                type="submit"
                className="px-4 py-2 bg-violet-600 text-white rounded-r-xl hover:bg-violet-700 transition-colors duration-200 cursor-pointer"
              >
                <Search size={16} />
              </button>
            </form>
            )}

            {/* 链接 */}
            <div className="space-y-1">
              <Link
                to="/activities"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
              >
                <CalendarDays size={16} />
                活动列表
              </Link>
              {user ? (
                <>
                  <Link
                    to="/notifications"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <Bell size={16} />
                    通知
                    {unreadCount > 0 && (
                      <span className="px-1.5 py-0.5 bg-green-500 text-white text-[10px] font-bold rounded-full">
                        {unreadCount > 99 ? '99+' : unreadCount}
                      </span>
                    )}
                  </Link>
                  <Link
                    to="/profile"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <User size={16} />
                    个人中心
                  </Link>
                  <Link
                    to="/my-activities"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <CalendarDays size={16} />
                    我的活动
                  </Link>
                  <Link
                    to="/my-registrations"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <ClipboardList size={16} />
                    报名记录
                  </Link>
                  <Link
                    to="/favorites"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-[#4C1D95] hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <Heart size={16} />
                    我的收藏
                  </Link>
                  {user.role === 'ADMIN' && (
                    <Link
                      to="/admin"
                      onClick={() => setMobileMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2.5 text-violet-600 font-medium hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                    >
                      <ShieldCheck size={16} />
                      管理后台
                    </Link>
                  )}
                  <button
                    onClick={() => { handleLogout(); setMobileMenuOpen(false); }}
                    className="w-full flex items-center gap-2 px-4 py-2.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm"
                  >
                    <LogOut size={16} />
                    退出登录
                  </button>
                </>
              ) : (
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="block px-4 py-2.5 text-violet-600 hover:bg-violet-50 rounded-lg transition-colors duration-200 cursor-pointer text-sm font-medium"
                >
                  登录 / 注册
                </Link>
              )}
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}
