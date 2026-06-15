import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Calendar, MapPin, Users, Star, Heart, Bell, ArrowLeft, Loader2, MessageSquare } from 'lucide-react';
import { getActivityById } from '@/api/activity';
import { registerForActivity, cancelRegistration, getMyRegistrations } from '@/api/registration';
import { addFavorite, removeFavorite, isFavorited as checkFavoriteStatus } from '@/api/favorite';
import { subscribeActivity, unsubscribeActivity, checkSubscriptionStatus } from '@/api/subscription';
import { getComments, publishComment, deleteComment } from '@/api/comment';
import ActivityAlbum from '@/components/ActivityAlbum';
import type { Activity } from '@/types/activity';
import type { CommentRequest, CommentResponse } from '@/types/comment';
import type { LoginResponse } from '@/types/user';

export default function ActivityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activity, setActivity] = useState<Activity | null>(null);
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // 用户状态
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [isFavorited, setIsFavorited] = useState(false);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [isRegistered, setIsRegistered] = useState(false);
  const [isOwner, setIsOwner] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);

  // 评论状态
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);

  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  useEffect(() => {
    if (id) {
      loadActivity();
      loadComments();
    }
  }, [id]);

  // 当 user 或 id 改变时，检查收藏、订阅、报名的状态
  useEffect(() => {
    if (id && user) {
      checkStatuses();
    }
  }, [id, user]);

  // 当 activity 或 user 加载完成后，检查是否是活动发布者
  useEffect(() => {
    if (user && activity) {
      setIsOwner(user.userId === activity.publisherId);
      setIsAdmin(user.role === 'ADMIN');
    }
  }, [user, activity]);

  const loadActivity = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await getActivityById(Number(id));
      setActivity(res.data.data);
    } catch (err) {
      console.error('加载活动详情失败', err);
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    if (!id) return;
    try {
      const res = await getComments(Number(id));
      setComments(res.data.data);
    } catch (err) {
      console.error('加载评论失败', err);
    }
  };

  const checkStatuses = async () => {
    if (!id) return;
    try {
      const activityId = Number(id);
      const [favRes, subRes, regRes] = await Promise.all([
        checkFavoriteStatus(activityId),
        checkSubscriptionStatus(activityId),
        getMyRegistrations(1, 100),
      ]);
      setIsFavorited(favRes.data.data?.collected || false);
      setIsSubscribed(subRes.data.data?.subscribed || false);
      // 检查是否已报名此活动
      const registrations = regRes.data.data?.list || [];
      const isRegisteredThisActivity = registrations.some((r: any) => r.activityId === activityId);
      setIsRegistered(isRegisteredThisActivity);
    } catch (err) {
      console.error('检查状态失败', err);
    }
  };

  const handleRegister = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!id) return;
    setActionLoading(true);
    try {
      await registerForActivity({ activityId: Number(id) });
      setIsRegistered(true);
      alert('报名成功！');
      loadActivity();
    } catch (err: any) {
      alert(err.message || '报名失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelRegister = async () => {
    if (!id) return;
    if (!confirm('确定要取消报名吗？')) return;
    setActionLoading(true);
    try {
      await cancelRegistration(Number(id));
      setIsRegistered(false);
      alert('已取消报名');
      loadActivity();
    } catch (err: any) {
      alert(err.message || '取消失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleFavorite = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!id) return;
    setActionLoading(true);
    try {
      if (isFavorited) {
        await removeFavorite(Number(id));
        setIsFavorited(false);
      } else {
        await addFavorite(Number(id));
        setIsFavorited(true);
      }
    } catch (err) {
      console.error('收藏操作失败', err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubscribe = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!id) return;
    setActionLoading(true);
    try {
      if (isSubscribed) {
        await unsubscribeActivity(Number(id));
        setIsSubscribed(false);
      } else {
        await subscribeActivity(Number(id));
        setIsSubscribed(true);
      }
    } catch (err) {
      console.error('订阅操作失败', err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    if (!id || !newComment.trim()) return;

    setSubmittingComment(true);
    try {
      await publishComment(Number(id), { content: newComment });
      setNewComment('');
      loadComments();
    } catch (err: any) {
      alert(err.message || '评论失败');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!confirm('确定要删除这条评论吗？')) return;
    try {
      await deleteComment(commentId);
      loadComments();
    } catch (err) {
      console.error('删除评论失败', err);
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (status: string, approvalStatus: string) => {
    if (approvalStatus === 'pending') {
      return 'bg-yellow-100 text-yellow-700';
    }
    const styles: Record<string, string> = {
      published: 'bg-green-100 text-green-700',
      pending: 'bg-yellow-100 text-yellow-700',
      cancelled: 'bg-red-100 text-red-700',
      ended: 'bg-gray-100 text-gray-700',
    };
    return styles[status] || 'bg-gray-100 text-gray-700';
  };

  const getStatusText = (status: string, approvalStatus: string) => {
    if (approvalStatus === 'pending') return '审核中';
    const texts: Record<string, string> = {
      published: '进行中',
      pending: '待发布',
      cancelled: '已取消',
      ended: '已结束',
    };
    return texts[status] || status;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="animate-spin text-indigo-600" size={32} />
      </div>
    );
  }

  if (!activity) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500">活动不存在</p>
          <Link to="/activities" className="text-indigo-600 hover:underline mt-4 inline-block">
            返回活动列表
          </Link>
        </div>
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
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧主内容 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 活动封面 */}
            <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl h-64 relative overflow-hidden">
              <div className="absolute top-4 right-4">
                <span className={`px-3 py-1.5 rounded-full text-sm font-medium ${getStatusBadge(activity.status, activity.approvalStatus || '')}`}>
                  {getStatusText(activity.status, activity.approvalStatus || '')}
                </span>
              </div>
            </div>

            {/* 活动信息 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h1 className="text-2xl font-bold text-gray-900 mb-4">{activity.title}</h1>

              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <Calendar className="text-indigo-600 mt-0.5" size={20} />
                  <div>
                    <p className="text-gray-600">活动时间</p>
                    <p className="text-gray-900">
                      {formatDate(activity.startTime)} - {formatDate(activity.endTime)}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <MapPin className="text-indigo-600 mt-0.5" size={20} />
                  <div>
                    <p className="text-gray-600">活动地点</p>
                    <p className="text-gray-900">{activity.location}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <Users className="text-indigo-600 mt-0.5" size={20} />
                  <div>
                    <p className="text-gray-600">参与人数</p>
                    <p className="text-gray-900">{activity.maxParticipants} 人</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <Star className="text-indigo-600 mt-0.5" size={20} />
                  <div>
                    <p className="text-gray-600">活动类型</p>
                    <p className="text-gray-900">{activity.activityTypeName}</p>
                  </div>
                </div>

                {activity.tags && activity.tags.length > 0 && (
                  <div className="flex items-start gap-3">
                    <div className="text-indigo-600 mt-0.5">
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                      </svg>
                    </div>
                    <div>
                      <p className="text-gray-600">活动标签</p>
                      <div className="flex flex-wrap gap-2 mt-1">
                        {activity.tags.map((tag) => (
                          <span
                            key={tag.id}
                            className="px-3 py-1 bg-indigo-50 text-indigo-600 rounded-full text-sm"
                          >
                            {tag.name}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 活动详情 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">活动详情</h2>
              <div className="prose max-w-none text-gray-700 whitespace-pre-wrap">
                {activity.description || '暂无详细描述'}
              </div>
            </div>

            {/* 相册区域 */}
            <ActivityAlbum activityId={activity.id} isOwner={isOwner || isAdmin} />

            {/* 评论区域 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <MessageSquare size={20} />
                评论 ({comments.length})
              </h2>

              {/* 发表评论 */}
              {user && (
                <form onSubmit={handleSubmitComment} className="mb-6">
                  <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="发表你的看法..."
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
                    rows={3}
                  />
                  <div className="flex justify-end mt-2">
                    <button
                      type="submit"
                      disabled={submittingComment || !newComment.trim()}
                      className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
                    >
                      {submittingComment ? '发表中...' : '发表评论'}
                    </button>
                  </div>
                </form>
              )}

              {/* 评论列表 */}
              <div className="space-y-4">
                {comments.map((comment) => (
                  <div key={comment.id} className="border-b border-gray-100 pb-4">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-medium text-gray-900">{comment.realName || comment.username}</p>
                        <p className="text-sm text-gray-500 mt-1">{comment.content}</p>
                      </div>
                      {(user?.userId === comment.userId || user?.role === 'admin') && (
                        <button
                          onClick={() => handleDeleteComment(comment.id)}
                          className="text-red-500 hover:text-red-600 text-sm"
                        >
                          删除
                        </button>
                      )}
                    </div>
                    <p className="text-xs text-gray-400 mt-2">
                      {new Date(comment.createdAt).toLocaleString('zh-CN')}
                    </p>
                  </div>
                ))}

                {comments.length === 0 && (
                  <p className="text-center text-gray-500 py-4">暂无评论，来发表第一条评论吧</p>
                )}
              </div>
            </div>
          </div>

          {/* 右侧边栏 */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 sticky top-20">
              {/* 发布者信息 */}
              <div className="flex items-center gap-3 mb-6 pb-6 border-b border-gray-100">
                <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center">
                  <span className="text-indigo-600 font-semibold">
                    {activity.publisherName?.charAt(0) || 'U'}
                  </span>
                </div>
                <div>
                  <p className="text-sm text-gray-500">活动发布者</p>
                  <p className="font-medium text-gray-900">{activity.publisherName}</p>
                </div>
              </div>

              {/* 操作按钮 */}
              <div className="space-y-3">
                {/* 判断是否是发布者，且活动为草稿状态 */}
                {user?.userId === activity.publisherId && activity.status === 'draft' && (
                  <>
                    <Link
                      to={`/activities/${activity.id}/edit`}
                      className="w-full py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition text-center block"
                    >
                      编辑活动
                    </Link>
                    <Link
                      to={`/activities/${activity.id}/registrations`}
                      className="w-full py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition text-center block"
                    >
                      查看报名 ({activity.maxParticipants}人)
                    </Link>
                  </>
                )}

                {/* 判断是否是发布者，活动已发布 */}
                {user?.userId === activity.publisherId && activity.status === 'published' && (
                  <Link
                    to={`/activities/${activity.id}/registrations`}
                    className="w-full py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition text-center block"
                  >
                    查看报名 ({activity.maxParticipants}人)
                  </Link>
                )}

                {/* 非发布者显示报名按钮 */}
                {user?.userId !== activity.publisherId && activity.status === 'published' && activity.approvalStatus !== 'pending' && (
                  <>
                    {isRegistered ? (
                      <button
                        onClick={handleCancelRegister}
                        disabled={actionLoading}
                        className="w-full py-3 border-2 border-red-500 text-red-500 rounded-lg font-medium hover:bg-red-50 transition disabled:opacity-50"
                      >
                        {actionLoading ? '处理中...' : '取消报名'}
                      </button>
                    ) : (
                      <button
                        onClick={handleRegister}
                        disabled={actionLoading}
                        className="w-full py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition disabled:opacity-50"
                      >
                        {actionLoading ? '处理中...' : '立即报名'}
                      </button>
                    )}

                    <div className="grid grid-cols-2 gap-3">
                      <button
                        onClick={handleFavorite}
                        disabled={actionLoading}
                        className={`py-2 rounded-lg font-medium transition flex items-center justify-center gap-2 ${
                          isFavorited
                            ? 'bg-red-50 text-red-500 border border-red-200'
                            : 'bg-gray-50 text-gray-600 border border-gray-200 hover:bg-gray-100'
                        }`}
                      >
                        <Heart size={18} fill={isFavorited ? 'currentColor' : 'none'} />
                        {isFavorited ? '已收藏' : '收藏'}
                      </button>

                      <button
                        onClick={handleSubscribe}
                        disabled={actionLoading}
                        className={`py-2 rounded-lg font-medium transition flex items-center justify-center gap-2 ${
                          isSubscribed
                            ? 'bg-indigo-50 text-indigo-600 border border-indigo-200'
                            : 'bg-gray-50 text-gray-600 border border-gray-200 hover:bg-gray-100'
                        }`}
                      >
                        <Bell size={18} fill={isSubscribed ? 'currentColor' : 'none'} />
                        {isSubscribed ? '已订阅' : '订阅'}
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
