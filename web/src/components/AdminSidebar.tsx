import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Calendar,
  BarChart3,
  Settings,
  GraduationCap,
  ArrowLeft,
  FileText,
  Megaphone,
  Tag,
  Layers,
  MessageSquare,
} from 'lucide-react';

/** 侧边栏导航项配置 */
interface SidebarItem {
  /** 导航路径 */
  path: string;
  /** 显示名称 */
  label: string;
  /** 图标组件 */
  icon: React.ElementType;
}

/** 侧边栏导航项列表 */
const sidebarItems: SidebarItem[] = [
  { path: '/admin', label: '仪表盘', icon: LayoutDashboard },
  { path: '/admin/users', label: '用户管理', icon: Users },
  { path: '/admin/activities', label: '活动管理', icon: Calendar },
  { path: '/admin/tags', label: '标签管理', icon: Tag },
  { path: '/admin/activity-types', label: '活动类型', icon: Layers },
  { path: '/admin/topics', label: '话题管理', icon: MessageSquare },
  { path: '/admin/announcements', label: '系统公告', icon: Megaphone },
  { path: '/admin/audit-logs', label: '审计日志', icon: FileText },
  { path: '/admin/statistics', label: '数据统计', icon: BarChart3 },
  { path: '/admin/settings', label: '系统设置', icon: Settings },
];

/** 管理后台侧边栏组件，提供导航和当前路由高亮 */
export default function AdminSidebar() {
  const location = useLocation();

  /** 判断导航项是否为当前激活状态 */
  const isActive = (path: string) => {
    if (path === '/admin') {
      return location.pathname === '/admin';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <aside className="hidden lg:flex flex-col w-64 min-h-screen bg-white/70 backdrop-blur-xl border-r border-violet-100 shadow-sm">
      {/* Logo 区域 */}
      <div className="flex items-center gap-2 px-6 h-16 border-b border-violet-100">
        <GraduationCap size={24} className="text-violet-600" />
        <span className="text-lg font-bold text-violet-600">管理后台</span>
      </div>

      {/* 导航链接 */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {sidebarItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.path);
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`
                flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium
                transition-colors duration-200 cursor-pointer
                ${
                  active
                    ? 'bg-violet-100 text-violet-700'
                    : 'text-[#4C1D95] hover:bg-violet-50 hover:text-violet-600'
                }
              `}
            >
              <Icon size={18} className={active ? 'text-violet-600' : ''} />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* 返回首页 */}
      <div className="px-3 py-4 border-t border-violet-100">
        <Link
          to="/"
          className="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium text-[#4C1D95] hover:bg-violet-50 hover:text-violet-600 transition-colors duration-200 cursor-pointer"
        >
          <ArrowLeft size={18} />
          返回首页
        </Link>
      </div>
    </aside>
  );
}
