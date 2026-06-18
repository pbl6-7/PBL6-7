import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileText,
  Search,
  Filter,
  Calendar,
  Loader2,
  GraduationCap,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { getAuditLogs, getAuditLogStats } from '@/api/auditLog';
import type { AuditLog } from '@/api/auditLog';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/** 操作类型中文映射 */
const operationMap: Record<string, string> = {
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  LOGIN: '登录',
  LOGOUT: '登出',
  EXPORT: '导出',
  APPROVE: '审批',
  REJECT: '驳回',
};

/** 资源类型中文映射 */
const resourceTypeMap: Record<string, string> = {
  USER: '用户',
  ACTIVITY: '活动',
  COMMENT: '评论',
  REGISTRATION: '报名',
  SYSTEM: '系统',
  TAG: '标签',
  TOPIC: '话题',
};

/** 操作类型选项列表 */
const operationOptions = [
  { value: '', label: '全部操作' },
  { value: 'CREATE', label: '创建' },
  { value: 'UPDATE', label: '更新' },
  { value: 'DELETE', label: '删除' },
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'EXPORT', label: '导出' },
  { value: 'APPROVE', label: '审批' },
  { value: 'REJECT', label: '驳回' },
];

/** 资源类型选项列表 */
const resourceTypeOptions = [
  { value: '', label: '全部资源' },
  { value: 'USER', label: '用户' },
  { value: 'ACTIVITY', label: '活动' },
  { value: 'COMMENT', label: '评论' },
  { value: 'REGISTRATION', label: '报名' },
  { value: 'SYSTEM', label: '系统' },
  { value: 'TAG', label: '标签' },
  { value: 'TOPIC', label: '话题' },
];

/** 操作类型对应的颜色样式 */
const operationColorMap: Record<string, string> = {
  CREATE: 'bg-green-100 text-green-700',
  UPDATE: 'bg-blue-100 text-blue-700',
  DELETE: 'bg-red-100 text-red-700',
  LOGIN: 'bg-violet-100 text-violet-700',
  LOGOUT: 'bg-gray-100 text-gray-700',
  EXPORT: 'bg-amber-100 text-amber-700',
  APPROVE: 'bg-emerald-100 text-emerald-700',
  REJECT: 'bg-rose-100 text-rose-700',
};

/** 资源类型对应的颜色样式 */
const resourceTypeColorMap: Record<string, string> = {
  USER: 'bg-indigo-100 text-indigo-700',
  ACTIVITY: 'bg-purple-100 text-purple-700',
  COMMENT: 'bg-cyan-100 text-cyan-700',
  REGISTRATION: 'bg-teal-100 text-teal-700',
  SYSTEM: 'bg-slate-100 text-slate-700',
  TAG: 'bg-orange-100 text-orange-700',
  TOPIC: 'bg-pink-100 text-pink-700',
};

/**
 * 管理后台审计日志页面
 * 提供审计日志的筛选、分页查看和统计展示功能
 */
export default function AdminAuditLogsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);

  /** 日志列表数据 */
  const [logs, setLogs] = useState<AuditLog[]>([]);
  /** 加载状态 */
  const [loading, setLoading] = useState(true);
  /** 当前页码 */
  const [page, setPage] = useState(1);
  /** 每页数量 */
  const [size] = useState(10);
  /** 总记录数 */
  const [total, setTotal] = useState(0);
  /** 总页数 */
  const [totalPages, setTotalPages] = useState(0);
  /** 统计数据 */
  const [stats, setStats] = useState<any>(null);

  /** 筛选条件：操作类型 */
  const [filterOperation, setFilterOperation] = useState('');
  /** 筛选条件：资源类型 */
  const [filterResourceType, setFilterResourceType] = useState('');
  /** 筛选条件：开始时间 */
  const [filterStartTime, setFilterStartTime] = useState('');
  /** 筛选条件：结束时间 */
  const [filterEndTime, setFilterEndTime] = useState('');

  useEffect(() => {
    checkAdmin();
  }, []);

  useEffect(() => {
    loadLogs();
  }, [page, filterOperation, filterResourceType, filterStartTime, filterEndTime]);

  useEffect(() => {
    loadStats();
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
   * 加载审计日志列表
   */
  const loadLogs = async () => {
    setLoading(true);
    try {
      const params: any = { page, size };
      if (filterOperation) params.operation = filterOperation;
      if (filterResourceType) params.resourceType = filterResourceType;
      if (filterStartTime) params.startTime = filterStartTime;
      if (filterEndTime) params.endTime = filterEndTime;

      const res = await getAuditLogs(params);
      const data = res.data.data;
      setLogs(data.list || []);
      setTotal(data.total || 0);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('加载审计日志失败', err);
      addToast('error', '加载审计日志失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载审计日志统计数据
   */
  const loadStats = async () => {
    try {
      const res = await getAuditLogStats();
      setStats(res.data.data);
    } catch (err) {
      console.error('加载统计数据失败', err);
    }
  };

  /**
   * 获取操作类型的中文标签
   */
  const getOperationLabel = (op: string) => operationMap[op] || op;

  /**
   * 获取资源类型的中文标签
   */
  const getResourceTypeLabel = (rt: string) => resourceTypeMap[rt] || rt;

  /**
   * 格式化时间为可读字符串
   */
  const formatTime = (timeStr: string) => {
    if (!timeStr) return '-';
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  /**
   * 跳转到指定页码
   */
  const goToPage = (p: number) => {
    if (p >= 1 && p <= totalPages) {
      setPage(p);
    }
  };

  /**
   * 重置所有筛选条件
   */
  const handleResetFilters = () => {
    setFilterOperation('');
    setFilterResourceType('');
    setFilterStartTime('');
    setFilterEndTime('');
    setPage(1);
  };

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
              <h1 className="text-xl font-bold text-[#4C1D95]">审计日志</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto space-y-6">
          {/* 统计卡片区域 */}
          {stats && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <StatsCard
                title="总操作数"
                value={stats.totalOperations ?? 0}
                icon={<FileText size={20} className="text-violet-600" />}
              />
              <StatsCard
                title="今日操作"
                value={stats.todayOperations ?? 0}
                icon={<Calendar size={20} className="text-blue-600" />}
              />
              <StatsCard
                title="活跃用户"
                value={stats.activeUsers ?? 0}
                icon={<Search size={20} className="text-green-600" />}
              />
              <StatsCard
                title="异常操作"
                value={stats.abnormalOperations ?? 0}
                icon={<Filter size={20} className="text-amber-600" />}
              />
            </div>
          )}

          {/* 筛选区域 */}
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-card p-6 border border-violet-100/50">
            <div className="flex items-center gap-2 mb-4">
              <Filter size={18} className="text-violet-600" />
              <h2 className="text-base font-semibold text-[#4C1D95]">筛选条件</h2>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
              {/* 操作类型筛选 */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5">操作类型</label>
                <select
                  value={filterOperation}
                  onChange={(e) => { setFilterOperation(e.target.value); setPage(1); }}
                  className="w-full px-3 py-2.5 bg-[#FAF5FF] border border-violet-200 rounded-xl text-sm text-[#4C1D95] focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 cursor-pointer"
                >
                  {operationOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              {/* 资源类型筛选 */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5">资源类型</label>
                <select
                  value={filterResourceType}
                  onChange={(e) => { setFilterResourceType(e.target.value); setPage(1); }}
                  className="w-full px-3 py-2.5 bg-[#FAF5FF] border border-violet-200 rounded-xl text-sm text-[#4C1D95] focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 cursor-pointer"
                >
                  {resourceTypeOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              {/* 开始时间 */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5">开始时间</label>
                <input
                  type="datetime-local"
                  value={filterStartTime}
                  onChange={(e) => { setFilterStartTime(e.target.value); setPage(1); }}
                  className="w-full px-3 py-2.5 bg-[#FAF5FF] border border-violet-200 rounded-xl text-sm text-[#4C1D95] focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200"
                />
              </div>

              {/* 结束时间 */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5">结束时间</label>
                <input
                  type="datetime-local"
                  value={filterEndTime}
                  onChange={(e) => { setFilterEndTime(e.target.value); setPage(1); }}
                  className="w-full px-3 py-2.5 bg-[#FAF5FF] border border-violet-200 rounded-xl text-sm text-[#4C1D95] focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200"
                />
              </div>

              {/* 重置按钮 */}
              <div className="flex items-end">
                <button
                  onClick={handleResetFilters}
                  className="w-full px-4 py-2.5 bg-violet-100 hover:bg-violet-200 text-[#4C1D95] rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer"
                >
                  重置筛选
                </button>
              </div>
            </div>
          </div>

          {/* 日志列表表格 */}
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-card overflow-hidden border border-violet-100/50">
            {/* 表头信息 */}
            <div className="px-6 py-4 border-b border-violet-100 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <FileText size={18} className="text-violet-600" />
                <h2 className="text-base font-semibold text-[#4C1D95]">日志列表</h2>
              </div>
              <p className="text-sm text-gray-500">
                共 <span className="font-semibold text-[#4C1D95]">{total}</span> 条记录
              </p>
            </div>

            {/* 表格内容 */}
            <div className="overflow-x-auto">
              {loading ? (
                <div className="flex flex-col items-center justify-center py-16 gap-3">
                  <Loader2 className="animate-spin text-violet-600" size={32} />
                  <p className="text-gray-500">加载中...</p>
                </div>
              ) : logs.length === 0 ? (
                /* 空状态展示 */
                <div className="flex flex-col items-center justify-center py-16 gap-4">
                  <div className="w-20 h-20 bg-violet-100 rounded-full flex items-center justify-center">
                    <FileText className="text-violet-400" size={36} />
                  </div>
                  <div className="text-center">
                    <p className="text-lg font-medium text-[#4C1D95]">暂无审计日志</p>
                    <p className="text-sm text-gray-400 mt-1">当前筛选条件下没有匹配的日志记录</p>
                  </div>
                </div>
              ) : (
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-violet-100 bg-[#FAF5FF]/50">
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        操作用户
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        操作类型
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        资源类型
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        资源ID
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        操作详情
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        IP地址
                      </th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-[#4C1D95] uppercase tracking-wider">
                        操作时间
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-violet-50">
                    {logs.map((log) => (
                      <tr key={log.id} className="hover:bg-violet-50/30 transition-colors duration-200">
                        {/* 操作用户 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center gap-3">
                            <div className="flex-shrink-0 w-8 h-8 bg-gradient-to-br from-violet-400 to-purple-500 rounded-full flex items-center justify-center shadow-sm">
                              <span className="text-white font-semibold text-xs">
                                {(log.username || '?').charAt(0).toUpperCase()}
                              </span>
                            </div>
                            <span className="text-sm font-medium text-[#4C1D95]">{log.username}</span>
                          </div>
                        </td>

                        {/* 操作类型 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${
                              operationColorMap[log.operation] || 'bg-gray-100 text-gray-700'
                            }`}
                          >
                            {getOperationLabel(log.operation)}
                          </span>
                        </td>

                        {/* 资源类型 */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${
                              resourceTypeColorMap[log.resourceType] || 'bg-gray-100 text-gray-700'
                            }`}
                          >
                            {getResourceTypeLabel(log.resourceType)}
                          </span>
                        </td>

                        {/* 资源ID */}
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                          {log.resourceId || '-'}
                        </td>

                        {/* 操作详情 */}
                        <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate" title={log.responseMessage}>
                          {log.responseMessage || '-'}
                        </td>

                        {/* IP地址 */}
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                          {log.clientIp || '-'}
                        </td>

                        {/* 操作时间 */}
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatTime(log.createdAt)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {/* 分页控件 */}
            {!loading && totalPages > 0 && (
              <div className="px-6 py-4 border-t border-violet-100 flex items-center justify-between">
                <p className="text-sm text-gray-500">
                  第 <span className="font-medium text-[#4C1D95]">{page}</span> / <span className="font-medium text-[#4C1D95]">{totalPages}</span> 页
                </p>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => goToPage(page - 1)}
                    disabled={page <= 1}
                    className="inline-flex items-center gap-1 px-3 py-2 bg-violet-50 hover:bg-violet-100 disabled:opacity-40 disabled:cursor-not-allowed text-[#4C1D95] rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                  >
                    <ChevronLeft size={16} />
                    上一页
                  </button>
                  <button
                    onClick={() => goToPage(page + 1)}
                    disabled={page >= totalPages}
                    className="inline-flex items-center gap-1 px-3 py-2 bg-violet-50 hover:bg-violet-100 disabled:opacity-40 disabled:cursor-not-allowed text-[#4C1D95] rounded-lg text-sm font-medium transition-colors duration-200 cursor-pointer"
                  >
                    下一页
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

/**
 * 统计卡片组件，展示单项统计数据
 */
function StatsCard({
  title,
  value,
  icon,
}: {
  /** 统计项标题 */
  title: string;
  /** 统计项数值 */
  value: number;
  /** 图标元素 */
  icon: React.ReactNode;
}) {
  return (
    <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-card p-5 border border-violet-100/50 flex items-center gap-4">
      <div className="w-12 h-12 bg-[#FAF5FF] rounded-xl flex items-center justify-center flex-shrink-0">
        {icon}
      </div>
      <div>
        <p className="text-sm text-gray-500">{title}</p>
        <p className="text-2xl font-bold text-[#4C1D95]">{value}</p>
      </div>
    </div>
  );
}
