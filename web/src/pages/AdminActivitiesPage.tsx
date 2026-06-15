import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, Search, CheckCircle, XCircle, Clock, Eye } from 'lucide-react';
import { getActivitiesByApprovalStatus, auditActivity } from '@/api/admin';
import type { Activity } from '@/types/activity';

export default function AdminActivitiesPage() {
  const navigate = useNavigate();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('all');
  const [processing, setProcessing] = useState<number | null>(null);

  useEffect(() => {
    checkAdmin();
    loadActivities();
  }, [statusFilter]);

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

  const loadActivities = async () => {
    setLoading(true);
    try {
      let res;
      if (statusFilter === 'all') {
        // Load all activities from different statuses
        const [published, pending, ended, cancelled] = await Promise.all([
          getActivitiesByApprovalStatus('published'),
          getActivitiesByApprovalStatus('pending'),
          getActivitiesByApprovalStatus('ended'),
          getActivitiesByApprovalStatus('cancelled'),
        ]);
        setActivities([
          ...(published.data.data || []),
          ...(pending.data.data || []),
          ...(ended.data.data || []),
          ...(cancelled.data.data || []),
        ]);
      } else {
        res = await getActivitiesByApprovalStatus(statusFilter);
        setActivities(res.data.data || []);
      }
    } catch (err) {
      console.error('加载活动失败', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAudit = async (id: number, approved: boolean, reason?: string) => {
    setProcessing(id);
    try {
      await auditActivity(id, approved, reason);
      loadActivities();
    } catch (err) {
      console.error('审核失败', err);
    } finally {
      setProcessing(null);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { bg: string; text: string; icon: React.ReactNode }> = {
      published: { bg: 'bg-green-100', text: 'text-green-800', icon: <CheckCircle size={14} /> },
      pending: { bg: 'bg-yellow-100', text: 'text-yellow-800', icon: <Clock size={14} /> },
      ended: { bg: 'bg-gray-100', text: 'text-gray-800', icon: <Clock size={14} /> },
      cancelled: { bg: 'bg-red-100', text: 'text-red-800', icon: <XCircle size={14} /> },
    };
    const style = statusMap[status] || statusMap.pending;
    return (
      <span className={`inline-flex items-center gap-1 px-2 py-1 text-xs rounded-full ${style.bg} ${style.text}`}>
        {style.icon}
        {status === 'published' ? '已发布' : status === 'pending' ? '待审核' : status === 'ended' ? '已结束' : '已取消'}
      </span>
    );
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('zh-CN');
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <button onClick={() => navigate('/admin')} className="text-gray-600 hover:text-indigo-600">
                ← 返回
              </button>
              <h1 className="text-xl font-bold text-indigo-600">活动管理</h1>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-xl shadow-sm">
          <div className="p-6 border-b border-gray-100">
            <div className="flex items-center gap-4">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg"
              >
                <option value="all">全部</option>
                <option value="published">已发布</option>
                <option value="pending">待审核</option>
                <option value="ended">已结束</option>
                <option value="cancelled">已取消</option>
              </select>
            </div>
          </div>

          <div className="divide-y divide-gray-200">
            {loading ? (
              <div className="p-8 text-center text-gray-500">加载中...</div>
            ) : activities.length === 0 ? (
              <div className="p-8 text-center text-gray-500">暂无活动</div>
            ) : (
              activities.map((activity) => (
                <div key={activity.id} className="p-6 hover:bg-gray-50">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-lg font-medium text-gray-900">{activity.title}</h3>
                        {getStatusBadge(activity.status)}
                      </div>
                      <p className="text-sm text-gray-500 mb-2">
                        发布者: {activity.publisherName} · {formatDate(activity.createdAt)}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span className="flex items-center gap-1">
                          <Calendar size={14} />
                          {formatDate(activity.startTime)}
                        </span>
                        <span>{activity.location}</span>
                        <span>人数: {activity.maxParticipants}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 ml-4">
                      <button
                        onClick={() => navigate(`/activities/${activity.id}`)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
                        title="查看"
                      >
                        <Eye size={20} />
                      </button>
                      {activity.approvalStatus === 'pending' && (
                        <>
                          <button
                            onClick={() => handleAudit(activity.id, true)}
                            disabled={processing === activity.id}
                            className="p-2 text-green-600 hover:bg-green-50 rounded-lg disabled:opacity-50"
                            title="通过"
                          >
                            <CheckCircle size={20} />
                          </button>
                          <button
                            onClick={() => {
                              const reason = prompt('请输入拒绝原因：');
                              if (reason) handleAudit(activity.id, false, reason);
                            }}
                            disabled={processing === activity.id}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg disabled:opacity-50"
                            title="拒绝"
                          >
                            <XCircle size={20} />
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
