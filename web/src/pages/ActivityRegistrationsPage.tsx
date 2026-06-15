import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Users, CheckCircle, XCircle, Clock, Loader2 } from 'lucide-react';
import { getActivityById } from '@/api/activity';
import { getActivityRegistrations, updateRegistrationStatus } from '@/api/registration';
import type { Activity } from '@/types/activity';
import type { RegistrationResponse } from '@/types/registration';

export default function ActivityRegistrationsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activity, setActivity] = useState<Activity | null>(null);
  const [registrations, setRegistrations] = useState<RegistrationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const activityRes = await getActivityById(Number(id));
      setActivity(activityRes.data.data);
      
      // 获取活动报名列表（不分页，获取全部）
      const regRes = await getActivityRegistrations(Number(id), 1, 100);
      const list = regRes.data.data?.list || [];
      setRegistrations(list);
    } catch (err) {
      console.error('加载失败', err);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (registrationId: number) => {
    setActionLoading(registrationId);
    try {
      await updateRegistrationStatus({ registrationId, status: 'confirmed' });
      alert('已确认报名');
      loadData();
    } catch (err: any) {
      alert(err.message || '操作失败');
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancel = async (registrationId: number) => {
    if (!confirm('确定要取消该报名吗？')) return;
    setActionLoading(registrationId);
    try {
      await updateRegistrationStatus({ registrationId, status: 'cancelled' });
      alert('已取消报名');
      loadData();
    } catch (err: any) {
      alert(err.message || '操作失败');
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'confirmed':
        return (
          <span className="flex items-center gap-1 px-2 py-1 bg-green-100 text-green-700 text-xs rounded-full">
            <CheckCircle size={12} />
            已确认
          </span>
        );
      case 'cancelled':
        return (
          <span className="flex items-center gap-1 px-2 py-1 bg-red-100 text-red-700 text-xs rounded-full">
            <XCircle size={12} />
            已取消
          </span>
        );
      case 'pending':
      default:
        return (
          <span className="flex items-center gap-1 px-2 py-1 bg-yellow-100 text-yellow-700 text-xs rounded-full">
            <Clock size={12} />
            待确认
          </span>
        );
    }
  };

  const formatDate = (dateStr: string) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('zh-CN');
  };

  const stats = {
    total: registrations.length,
    confirmed: registrations.filter(r => r.status === 'confirmed').length,
    pending: registrations.filter(r => r.status === 'pending').length,
    cancelled: registrations.filter(r => r.status === 'cancelled').length,
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
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-gray-600 hover:text-indigo-600 transition"
          >
            <ArrowLeft size={18} />
            返回
          </button>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 活动信息 */}
        {activity && (
          <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
            <h1 className="text-xl font-bold text-gray-900 mb-2">{activity.title}</h1>
            <div className="flex items-center gap-6 text-sm text-gray-500">
              <span>报名人数上限: {activity.maxParticipants}人</span>
              <span>活动地点: {activity.location}</span>
            </div>
          </div>
        )}

        {/* 统计卡片 */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-xl shadow-sm p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center">
                <Users className="text-indigo-600" size={20} />
              </div>
              <div>
                <p className="text-sm text-gray-500">总报名</p>
                <p className="text-2xl font-bold text-gray-900">{stats.total}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="text-green-600" size={20} />
              </div>
              <div>
                <p className="text-sm text-gray-500">已确认</p>
                <p className="text-2xl font-bold text-gray-900">{stats.confirmed}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center">
                <Clock className="text-yellow-600" size={20} />
              </div>
              <div>
                <p className="text-sm text-gray-500">待确认</p>
                <p className="text-2xl font-bold text-gray-900">{stats.pending}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                <XCircle className="text-red-600" size={20} />
              </div>
              <div>
                <p className="text-sm text-gray-500">已取消</p>
                <p className="text-2xl font-bold text-gray-900">{stats.cancelled}</p>
              </div>
            </div>
          </div>
        </div>

        {/* 报名列表 */}
        <div className="bg-white rounded-xl shadow-sm">
          <div className="p-6 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">报名列表</h2>
          </div>

          {registrations.length === 0 ? (
            <div className="p-12 text-center">
              <Users className="mx-auto text-gray-400 mb-4" size={48} />
              <p className="text-gray-500">暂无报名记录</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户名</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">报名时间</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {registrations.map((reg) => (
                    <tr key={reg.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="font-medium text-gray-900">{reg.userName || '未知用户'}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(reg.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(reg.registrationTime)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right">
                        {reg.status === 'pending' && (
                          <button
                            onClick={() => handleConfirm(reg.id)}
                            disabled={actionLoading === reg.id}
                            className="text-green-600 hover:text-green-700 text-sm font-medium disabled:opacity-50"
                          >
                            {actionLoading === reg.id ? '处理中...' : '确认'}
                          </button>
                        )}
                        {reg.status === 'cancelled' && (
                          <button
                            onClick={() => handleConfirm(reg.id)}
                            disabled={actionLoading === reg.id}
                            className="text-green-600 hover:text-green-700 text-sm font-medium disabled:opacity-50"
                          >
                            {actionLoading === reg.id ? '处理中...' : '恢复'}
                          </button>
                        )}
                        {reg.status !== 'cancelled' && (
                          <button
                            onClick={() => handleCancel(reg.id)}
                            disabled={actionLoading === reg.id}
                            className="text-red-600 hover:text-red-700 text-sm font-medium ml-4 disabled:opacity-50"
                          >
                            取消
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
