import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Calendar,
  MapPin,
  Users,
  Star,
  Heart,
  Bell,
  ArrowLeft,
  Loader2,
  MessageSquare,
  Send,
  Trash2,
  User,
  Clock,
  Tag,
  Edit3,
  ClipboardList,
  CheckCircle,
  XCircle,
  Reply,
  X,
} from 'lucide-react';
import { getActivityById, deleteActivity, publishActivityStatus, cancelActivity, endActivity } from '@/api/activity';
import { registerForActivity, cancelRegistration, getMyRegistrations } from '@/api/registration';
import { addFavorite, removeFavorite, isFavorited as checkFavoriteStatus } from '@/api/favorite';
import { subscribeActivity, unsubscribeActivity, checkSubscriptionStatus } from '@/api/subscription';
import { getComments, publishComment, deleteComment, updateComment } from '@/api/comment';
import ActivityAlbum from '@/components/ActivityAlbum';
import { Toast, useToastStore } from '@/components/Toast';
import type { Activity } from '@/types/activity';
import type { CommentRequest, CommentResponse } from '@/types/comment';
import type { LoginResponse } from '@/types/user';

/** 卡片渐变色配置 - 根据活动 ID 分配不同渐变 */
const BANNER_GRADIENTS = [
  'from-violet-600 via-purple-600 to-indigo-700',
  'from-indigo-600 via-blue-600 to-cyan-700',
  'from-fuchsia-600 via-pink-600 to-rose-700',
  'from-emerald-600 via-teal-600 to-cyan-700',
  'from-amber-600 via-orange-600 to-red-700',
  'from-cyan-600 via-blue-600 to-indigo-700',
  'from-rose-600 via-pink-600 to-fuchsia-700',
  'from-lime-600 via-green-600 to-emerald-700',
];

/**
 * 根据活动 ID 获取对应的渐变配色
 * @param activityId - 活动 ID
 * @returns Tailwind 渐变类名字符串
 */
function getBannerGradient(activityId: number): string {
  return BANNER_GRADIENTS[activityId % BANNER_GRADIENTS.length];
}

/**
 * 确认对话框组件 - 用于替代原生 confirm()
 * @param isOpen - 是否显示对话框
 * @param title - 对话框标题
 * @param message - 对话框消息内容
 * @param onConfirm - 确认回调
 * @param onCancel - 取消回调
 */
function ConfirmModal({
  isOpen,
  title,
  message,
  onConfirm,
  onCancel,
}: {
  isOpen: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[9998] flex items-center justify-center p-4 animate-fadeIn">
      {/* 背景遮罩 */}
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onCancel}
      />
      {/* 对话框主体 */}
      <div className="relative bg-white rounded-2xl shadow-modal p-6 max-w-md w-full animate-scaleIn">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
            <XCircle size={20} className="text-red-500" />
          </div>
          <h3 className="font-heading font-semibold text-text-primary">{title}</h3>
        </div>
        <p className="text-text-muted mb-6">{message}</p>
        <div className="flex gap-3 justify-end">
          <button
            onClick={onCancel}
            className="cursor-pointer px-4 py-2 bg-gray-100 text-text-secondary rounded-xl hover:bg-gray-200 transition-colors duration-200 font-medium"
          >
            取消
          </button>
          <button
            onClick={onConfirm}
            className="cursor-pointer px-4 py-2 bg-red-500 text-white rounded-xl hover:bg-red-600 transition-colors duration-200 font-medium"
          >
            确认
          </button>
        </div>
      </div>
    </div>
  );
}

/**
 * 将树形评论结构扁平化为列表
 * @param commentList - 树形评论列表
 * @returns 扁平化的评论列表
 */
function flattenComments(commentList: CommentResponse[]): CommentResponse[] {
  const result: CommentResponse[] = [];
  const walk = (list: CommentResponse[]) => {
    for (const c of list) {
      result.push(c);
      if (c.replies && c.replies.length > 0) {
        walk(c.replies);
      }
    }
  };
  walk(commentList);
  return result;
}

/**
 * 评论项组件 - 扁平式展示，支持无限层级回复
 * @param comment - 评论数据
 * @param user - 当前登录用户
 * @param onReply - 回复回调
 * @param onEdit - 编辑回调
 * @param onDelete - 删除回调
 * @param onUpdate - 更新回调
 * @param editingCommentId - 正在编辑的评论ID
 * @param editingContent - 编辑内容
 * @param setEditingContent - 设置编辑内容
 * @param cancelEdit - 取消编辑回调
 * @param formatShortDate - 日期格式化函数
 */
function CommentItem({
  comment,
  user,
  onReply,
  onEdit,
  onDelete,
  onUpdate,
  editingCommentId,
  editingContent,
  setEditingContent,
  cancelEdit,
  formatShortDate,
}: {
  comment: CommentResponse;
  user: LoginResponse | null;
  onReply: (comment: CommentResponse) => void;
  onEdit: (comment: CommentResponse) => void;
  onDelete: (commentId: number) => void;
  onUpdate: (commentId: number) => void;
  editingCommentId: number | null;
  editingContent: string;
  setEditingContent: (value: string) => void;
  cancelEdit: () => void;
  formatShortDate: (dateStr: string) => string;
}) {
  const isEditing = editingCommentId === comment.id;
  const displayName = comment.realName || comment.username;

  return (
    <div className="group p-4 bg-primary-50/50 rounded-xl hover:bg-primary-50 transition-colors duration-200">
      <div className="flex items-start gap-3">
        {/* 用户头像 */}
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center shrink-0 text-white font-semibold text-sm">
          {displayName?.charAt(0) || 'U'}
        </div>
        {/* 评论内容 */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between mb-1">
            <div className="flex items-center gap-2">
              <span className="font-medium text-text-primary">
                {displayName}
              </span>
              {/* 回复对象提示 */}
              {comment.replyToUsername && (
                <span className="text-xs text-primary-400 flex items-center gap-1">
                  <Reply size={10} />
                  回复 {comment.replyToUsername}
                </span>
              )}
            </div>
            {(user?.userId === comment.userId || user?.role === 'ADMIN') && (
              <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                {user?.userId === comment.userId && (
                  <button
                    onClick={() => onEdit(comment)}
                    className="cursor-pointer p-1.5 text-gray-400 hover:text-primary-500 hover:bg-primary-50 rounded-lg transition-colors duration-200"
                    title="编辑评论"
                  >
                    <Edit3 size={14} />
                  </button>
                )}
                <button
                  onClick={() => onDelete(comment.id)}
                  className="cursor-pointer p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors duration-200"
                  title="删除评论"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            )}
          </div>
          {isEditing ? (
            <div className="mt-1">
              <textarea
                value={editingContent}
                onChange={(e) => setEditingContent(e.target.value)}
                className="w-full px-3 py-2 bg-white border border-primary-200 rounded-lg focus:ring-2 focus:ring-primary-400/50 focus:border-primary-400 outline-none resize-none text-text-primary text-sm"
                rows={2}
                maxLength={500}
              />
              <div className="flex justify-end gap-2 mt-2">
                <button
                  onClick={cancelEdit}
                  className="cursor-pointer px-3 py-1.5 text-sm text-gray-500 hover:text-gray-700 transition-colors duration-200"
                >
                  取消
                </button>
                <button
                  onClick={() => onUpdate(comment.id)}
                  disabled={!editingContent.trim()}
                  className="cursor-pointer px-4 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors duration-200 disabled:opacity-50"
                >
                  保存
                </button>
              </div>
            </div>
          ) : (
            <>
              <p className="text-text-secondary text-sm leading-relaxed">{comment.content}</p>
              <div className="flex items-center gap-3 mt-2 text-xs text-text-muted">
                <span className="flex items-center gap-1">
                  <Clock size={12} />
                  {formatShortDate(comment.createdAt)}
                </span>
                {/* 回复按钮 - 无限层级 */}
                {user && (
                  <button
                    onClick={() => onReply(comment)}
                    className="cursor-pointer flex items-center gap-1 text-primary-400 hover:text-primary-600 transition-colors duration-200"
                  >
                    <Reply size={12} />
                    回复
                  </button>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

/**
 * 骨架屏组件 - 加载状态时的占位元素
 */
function DetailSkeleton() {
  return (
    <div className="min-h-screen bg-surface-50 font-body">
      {/* 横幅骨架 */}
      <div className="h-64 animate-shimmer rounded-b-3xl" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧骨架 */}
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white rounded-2xl shadow-card p-6 space-y-4 animate-shimmer h-48" />
            <div className="bg-white rounded-2xl shadow-card p-6 animate-shimmer h-32" />
            <div className="bg-white rounded-2xl shadow-card p-6 animate-shimmer h-64" />
          </div>
          {/* 右侧骨架 */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-card p-6 animate-shimmer h-48 sticky top-24" />
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * 活动详情页面组件
 * 展示单个活动的完整信息，包括报名、收藏、订阅和评论功能
 */
export default function ActivityDetailPage() {
  const addToast = useToastStore((s) => s.addToast);
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
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editingContent, setEditingContent] = useState('');
  // 回复状态
  const [replyToId, setReplyToId] = useState<number | null>(null);
  const [replyToUsername, setReplyToUsername] = useState<string>('');

  // 确认对话框状态
  const [confirmModal, setConfirmModal] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    onConfirm: () => void;
  }>({ isOpen: false, title: '', message: '', onConfirm: () => {} });

  /* 初始化：从 localStorage 读取用户信息 */
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  /* 加载活动详情和评论 */
  useEffect(() => {
    if (id) {
      loadActivity();
      loadComments();
    }
  }, [id]);

  /* 检查收藏、订阅、报名状态 */
  useEffect(() => {
    if (id && user) {
      checkStatuses();
    }
  }, [id, user]);

  /* 检查是否是活动发布者 */
  useEffect(() => {
    if (user && activity) {
      setIsOwner(user.userId === activity.publisherId);
      setIsAdmin(user.role === 'ADMIN');
    }
  }, [user, activity]);

  /**
   * 从后端加载活动详情数据
   */
  const loadActivity = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await getActivityById(Number(id));
      setActivity(res.data.data);
    } catch (err) {
      console.error('加载活动详情失败', err);
      addToast('error', '加载活动详情失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 从后端加载评论列表
   */
  const loadComments = async () => {
    if (!id) return;
    try {
      const res = await getComments(Number(id));
      setComments(res.data.data);
    } catch (err) {
      console.error('加载评论失败', err);
    }
  };

  /**
   * 检查用户的收藏、订阅、报名状态
   */
  const checkStatuses = async () => {
    if (!id) return;
    try {
      const activityId = Number(id);
      const [favRes, subRes, regRes] = await Promise.all([
        checkFavoriteStatus(activityId),
        checkSubscriptionStatus(activityId),
        getMyRegistrations(1, 100),
      ]);
      setIsFavorited(favRes.data.data?.favorited || false);
      setIsSubscribed(subRes.data.data?.subscribed || false);
      // 检查是否已报名此活动
      const registrations = regRes.data.data?.list || [];
      const isRegisteredThisActivity = registrations.some((r: any) => r.activityId === activityId);
      setIsRegistered(isRegisteredThisActivity);
    } catch (err) {
      console.error('检查状态失败', err);
    }
  };

  /**
   * 处理活动报名
   */
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
      addToast('success', '报名成功！期待您的参与 ✨');
      loadActivity();
    } catch (err: any) {
      addToast('error', err.message || '报名失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * 处理取消报名 - 显示确认对话框
   */
  const handleCancelRegister = async () => {
    if (!id) return;
    setConfirmModal({
      isOpen: true,
      title: '取消报名',
      message: '确定要取消报名吗？取消后需要重新报名才能参与活动。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        setActionLoading(true);
        try {
          await cancelRegistration(Number(id));
          setIsRegistered(false);
          addToast('success', '已取消报名');
          loadActivity();
        } catch (err: any) {
          addToast('error', err.message || '取消失败');
        } finally {
          setActionLoading(false);
        }
      },
    });
  };

  /**
   * 处理收藏/取消收藏
   */
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
        addToast('info', '已取消收藏');
      } else {
        await addFavorite(Number(id));
        setIsFavorited(true);
        addToast('success', '已添加到收藏 💜');
      }
    } catch (err) {
      console.error('收藏操作失败', err);
      addToast('error', '操作失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * 处理订阅/取消订阅
   */
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
        addToast('info', '已取消订阅');
      } else {
        await subscribeActivity(Number(id));
        setIsSubscribed(true);
        addToast('success', '已订阅活动通知 🔔');
      }
    } catch (err) {
      console.error('订阅操作失败', err);
      addToast('error', '操作失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * 处理发表评论
   * @param e - 表单事件对象
   */
  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    if (!id || !newComment.trim()) return;
    if (newComment.trim().length > 500) {
      addToast('warning', '评论内容不能超过500个字符');
      return;
    }

    setSubmittingComment(true);
    try {
      await publishComment(Number(id), {
        content: newComment,
        replyToId: replyToId || undefined,
      });
      setNewComment('');
      setReplyToId(null);
      setReplyToUsername('');
      addToast('success', replyToId ? '回复成功' : '评论发表成功');
      loadComments();
    } catch (err: any) {
      addToast('error', err.message || '评论失败');
    } finally {
      setSubmittingComment(false);
    }
  };

  /**
   * 处理删除评论 - 显示确认对话框
   * @param commentId - 评论 ID
   */
  const handleDeleteComment = async (commentId: number) => {
    setConfirmModal({
      isOpen: true,
      title: '删除评论',
      message: '确定要删除这条评论吗？删除后无法恢复。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        try {
          await deleteComment(commentId);
          addToast('success', '评论已删除');
          loadComments();
        } catch (err) {
          console.error('删除评论失败', err);
          addToast('error', '删除失败');
        }
      },
    });
  };

  /**
   * 开始编辑评论
   * @param comment - 要编辑的评论
   */
  const startEditComment = (comment: CommentResponse) => {
    setEditingCommentId(comment.id);
    setEditingContent(comment.content);
  };

  /**
   * 取消编辑评论
   */
  const cancelEditComment = () => {
    setEditingCommentId(null);
    setEditingContent('');
  };

  /**
   * 开始回复评论
   * @param comment - 要回复的评论
   */
  const startReplyComment = (comment: CommentResponse) => {
    setReplyToId(comment.id);
    setReplyToUsername(comment.realName || comment.username);
    setNewComment('');
    // 聚焦到评论输入框
    const textarea = document.getElementById('comment-input');
    textarea?.focus();
  };

  /**
   * 取消回复
   */
  const cancelReply = () => {
    setReplyToId(null);
    setReplyToUsername('');
    setNewComment('');
  };

  /**
   * 保存编辑后的评论
   */
  const handleUpdateComment = async (commentId: number) => {
    if (!editingContent.trim()) return;
    if (editingContent.trim().length > 500) {
      addToast('warning', '评论内容不能超过500个字符');
      return;
    }
    try {
      await updateComment(commentId, editingContent.trim());
      addToast('success', '评论已更新');
      setEditingCommentId(null);
      setEditingContent('');
      loadComments();
    } catch (err: any) {
      addToast('error', err.message || '更新失败');
    }
  };

  /**
   * 处理发布活动（将草稿状态的活动发布）
   */
  const handlePublishActivity = async () => {
    if (!id) return;
    setConfirmModal({
      isOpen: true,
      title: '发布活动',
      message: '确定要发布此活动吗？发布后将等待管理员审核。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        setActionLoading(true);
        try {
          await publishActivityStatus(Number(id));
          addToast('success', '活动已发布，等待审核');
          loadActivity();
        } catch (err: any) {
          addToast('error', err.message || '发布失败');
        } finally {
          setActionLoading(false);
        }
      },
    });
  };

  /**
   * 处理取消活动
   */
  const handleCancelActivity = async () => {
    if (!id) return;
    setConfirmModal({
      isOpen: true,
      title: '取消活动',
      message: '确定要取消此活动吗？取消后已报名的用户将收到通知。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        setActionLoading(true);
        try {
          await cancelActivity(Number(id));
          addToast('success', '活动已取消');
          loadActivity();
        } catch (err: any) {
          addToast('error', err.message || '取消失败');
        } finally {
          setActionLoading(false);
        }
      },
    });
  };

  /**
   * 处理结束活动
   */
  const handleEndActivity = async () => {
    if (!id) return;
    setConfirmModal({
      isOpen: true,
      title: '结束活动',
      message: '确定要结束此活动吗？结束后将无法恢复。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        setActionLoading(true);
        try {
          await endActivity(Number(id));
          addToast('success', '活动已结束');
          loadActivity();
        } catch (err: any) {
          addToast('error', err.message || '操作失败');
        } finally {
          setActionLoading(false);
        }
      },
    });
  };

  /**
   * 处理删除活动
   */
  const handleDeleteActivity = async () => {
    if (!id) return;
    setConfirmModal({
      isOpen: true,
      title: '删除活动',
      message: '确定要删除此活动吗？删除后无法恢复，所有报名信息将丢失。',
      onConfirm: async () => {
        setConfirmModal({ ...confirmModal, isOpen: false });
        setActionLoading(true);
        try {
          await deleteActivity(Number(id));
          addToast('success', '活动已删除');
          navigate('/my-activities');
        } catch (err: any) {
          addToast('error', err.message || '删除失败');
        } finally {
          setActionLoading(false);
        }
      },
    });
  };

  /**
   * 格式化日期字符串为可读格式
   * @param dateStr - ISO 日期字符串
   * @returns 格式化后的日期文本
   */
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

  /**
   * 格式化简短日期（用于评论时间）
   * @param dateStr - ISO 日期字符串
   * @returns 简短格式的日期文本
   */
  const formatShortDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return '刚刚';
    if (diffMins < 60) return `${diffMins} 分钟前`;
    if (diffHours < 24) return `${diffHours} 小时前`;
    if (diffDays < 7) return `${diffDays} 天前`;
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
  };

  /**
   * 根据活动状态获取对应的状态徽章样式类名
   * @param status - 活动状态
   * @param approvalStatus - 审核状态
   * @returns Tailwind CSS 类名字符串
   */
  const getStatusBadge = (status: string, approvalStatus: string) => {
    if (approvalStatus === 'pending') {
      return 'bg-amber-500/20 text-amber-300 border-amber-400/30';
    }
    const styles: Record<string, string> = {
      published: 'bg-accent-500/20 text-accent-300 border-accent-400/30',
      pending: 'bg-amber-500/20 text-amber-300 border-amber-400/30',
      cancelled: 'bg-red-500/20 text-red-300 border-red-400/30',
      ended: 'bg-gray-500/20 text-gray-300 border-gray-400/30',
    };
    return styles[status] || 'bg-gray-500/20 text-gray-300 border-gray-400/30';
  };

  /**
   * 根据活动和审核状态获取显示文本
   * @param status - 活动状态
   * @param approvalStatus - 审核状态
   * @returns 状态显示文本
   */
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

  /**
   * 计算报名进度百分比
   * @param current - 当前报名人数
   * @param max - 最大参与人数
   * @returns 百分比数值
   */
  const getProgressPercent = (current: number, max: number) => {
    if (!max) return 0;
    return Math.min(Math.round((current / max) * 100), 100);
  };

  /**
   * 递归计算评论总数（包含所有回复）
   * @param commentList - 评论列表
   * @returns 评论总数
   */
  const countAllComments = (commentList: CommentResponse[]): number => {
    let count = 0;
    for (const c of commentList) {
      count += 1;
      if (c.replies && c.replies.length > 0) {
        count += countAllComments(c.replies);
      }
    }
    return count;
  };

  /* 加载状态 - 显示骨架屏 */
  if (loading) {
    return <DetailSkeleton />;
  }

  /* 活动不存在状态 */
  if (!activity) {
    return (
      <div className="min-h-screen bg-surface-50 font-body flex items-center justify-center">
        <Toast />
        <div className="text-center px-4">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-primary-100 rounded-full mb-6">
            <XCircle size={36} className="text-primary-400" />
          </div>
          <h2 className="font-heading text-xl font-semibold text-text-primary mb-2">活动不存在</h2>
          <p className="text-text-muted mb-6">该活动可能已被删除或链接无效</p>
          <Link
            to="/activities"
            className="cursor-pointer inline-flex items-center gap-2 px-6 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 font-medium"
          >
            <ArrowLeft size={16} />
            返回活动列表
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface-50 font-body">
      {/* Toast 通知容器 */}
      <Toast />

      {/* 确认对话框 */}
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={confirmModal.title}
        message={confirmModal.message}
        onConfirm={confirmModal.onConfirm}
        onCancel={() => setConfirmModal({ ...confirmModal, isOpen: false })}
      />

      {/* ==================== 全宽渐变横幅 ==================== */}
      <div className={`relative h-72 sm:h-80 bg-gradient-to-br ${getBannerGradient(activity.id)} overflow-hidden`}>
        {/* 装饰性背景图案 */}
        <div className="absolute inset-0 opacity-20">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white rounded-full blur-3xl translate-x-1/2 -translate-y-1/2" />
          <div className="absolute bottom-0 left-0 w-48 h-48 bg-violet-400 rounded-full blur-3xl -translate-x-1/2 translate-y-1/2" />
          <svg className="absolute bottom-0 left-0 w-full h-24" viewBox="0 0 1440 120" fill="none" preserveAspectRatio="none">
            <path
              d="M0,64L80,69.3C160,75,320,85,480,80C640,75,800,64,960,69.3C1120,75,1280,85,1360,90.7L1440,96L1440,120L1360,117.3C1280,112,1120,101,960,96C800,90,640,90,480,96C320,101,160,112,80,117.3L0,120Z"
              fill="#FAF5FF"
              fillOpacity="0.2"
            />
          </svg>
        </div>

        {/* 横幅内容覆盖层 */}
        <div className="absolute inset-0 flex flex-col justify-end p-6 sm:p-8">
          <div className="max-w-7xl mx-auto w-full">
            {/* 玻璃效果返回按钮 */}
            <button
              onClick={() => navigate(-1)}
              className="cursor-pointer absolute top-6 left-6 sm:top-8 sm:left-8 flex items-center gap-2 px-4 py-2.5 glass text-white rounded-xl hover:bg-white/30 transition-all duration-200 group backdrop-blur-md"
            >
              <ArrowLeft size={18} className="group-hover:-translate-x-0.5 transition-transform duration-200" />
              返回
            </button>

            {/* 活动标题和状态 */}
            <div className="relative z-10">
              <div className="flex items-center gap-3 mb-3">
                <span
                  className={`inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-sm font-medium border backdrop-blur-md ${getStatusBadge(activity.status, activity.approvalStatus || '')}`}
                >
                  <span className={`w-2 h-2 rounded-full ${
                    activity.approvalStatus === 'pending' ? 'bg-amber-400' :
                    activity.status === 'published' ? 'bg-accent-400' :
                    activity.status === 'cancelled' ? 'bg-red-400' :
                    'bg-gray-400'
                  }`} />
                  {getStatusText(activity.status, activity.approvalStatus || '')}
                </span>
                {activity.activityTypeName && (
                  <span className="px-3 py-1 bg-white/20 text-white/90 rounded-full text-sm backdrop-blur-sm">
                    {activity.activityTypeName}
                  </span>
                )}
              </div>
              <h1 className="font-heading text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-2 tracking-tight drop-shadow-lg">
                {activity.title}
              </h1>
              <div className="flex items-center gap-4 text-white/80 text-sm">
                <span className="flex items-center gap-1.5">
                  <MapPin size={14} />
                  {activity.location}
                </span>
                <span className="flex items-center gap-1.5">
                  <Users size={14} />
                  {activity.currentParticipants || 0}/{activity.maxParticipants} 人
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ==================== 主内容区域 - 两栏布局 ==================== */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 -mt-4 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* ========== 左侧主内容 (2/3) ========== */}
          <div className="lg:col-span-2 space-y-6">
            {/* 活动信息卡片网格 */}
            <div className="bg-white rounded-2xl shadow-card p-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* 时间信息 */}
                <div className="flex items-start gap-4 p-4 bg-primary-50 rounded-xl">
                  <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center shrink-0">
                    <Calendar size={20} className="text-primary-600" />
                  </div>
                  <div>
                    <p className="text-sm text-text-muted mb-1">活动时间</p>
                    <p className="text-text-primary font-medium text-sm">
                      {formatDate(activity.startTime)}
                    </p>
                    <p className="text-text-muted text-xs mt-0.5">
                      至 {formatDate(activity.endTime)}
                    </p>
                  </div>
                </div>

                {/* 地点信息 */}
                <div className="flex items-start gap-4 p-4 bg-primary-50 rounded-xl">
                  <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center shrink-0">
                    <MapPin size={20} className="text-primary-600" />
                  </div>
                  <div>
                    <p className="text-sm text-text-muted mb-1">活动地点</p>
                    <p className="text-text-primary font-medium">{activity.location}</p>
                  </div>
                </div>

                {/* 参与人数 */}
                <div className="flex items-start gap-4 p-4 bg-primary-50 rounded-xl">
                  <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center shrink-0">
                    <Users size={20} className="text-primary-600" />
                  </div>
                  <div>
                    <p className="text-sm text-text-muted mb-1">参与人数</p>
                    <p className="text-text-primary font-medium">{activity.maxParticipants} 人上限</p>
                    <p className="text-text-muted text-xs mt-0.5">
                      已报名 {activity.currentParticipants || 0} 人
                    </p>
                  </div>
                </div>

                {/* 活动类型 */}
                <div className="flex items-start gap-4 p-4 bg-primary-50 rounded-xl">
                  <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center shrink-0">
                    <Star size={20} className="text-primary-600" />
                  </div>
                  <div>
                    <p className="text-sm text-text-muted mb-1">活动类型</p>
                    <p className="text-text-primary font-medium">{activity.activityTypeName}</p>
                  </div>
                </div>
              </div>

              {/* 标签区域 */}
              {activity.tags && activity.tags.length > 0 && (
                <div className="mt-4 pt-4 border-t border-primary-100">
                  <div className="flex items-center gap-2 mb-3">
                    <Tag size={16} className="text-primary-400" />
                    <span className="text-sm text-text-muted">活动标签</span>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {activity.tags.map((tag) => (
                      <span
                        key={tag.id}
                        className="px-3 py-1.5 bg-primary-100 text-primary-600 rounded-full text-sm font-medium hover:bg-primary-200 transition-colors duration-200 cursor-pointer"
                      >
                        #{tag.name}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* 活动详情描述 */}
            <div className="bg-white rounded-2xl shadow-card p-6">
              <h2 className="font-heading text-lg font-semibold text-text-primary mb-4 flex items-center gap-2">
                <Edit3 size={18} className="text-primary-400" />
                活动详情
              </h2>
              <div className="prose prose-violet max-w-none text-text-secondary whitespace-pre-wrap leading-relaxed">
                {activity.description || '暂无详细描述'}
              </div>
            </div>

            {/* 相册区域 */}
            <ActivityAlbum activityId={activity.id} isOwner={isOwner || isAdmin} />

            {/* ========== 评论区域 ========== */}
            <div className="bg-white rounded-2xl shadow-card p-6">
              <h2 className="font-heading text-lg font-semibold text-text-primary mb-4 flex items-center gap-2">
                <MessageSquare size={18} className="text-primary-400" />
                评论 ({countAllComments(comments)})
              </h2>

              {/* 发表评论表单 */}
              {user && (
                <form onSubmit={handleSubmitComment} className="mb-6">
                  {/* 回复提示 */}
                  {replyToId && (
                    <div className="flex items-center gap-2 mb-3 px-4 py-2.5 bg-primary-50 border border-primary-200 rounded-xl">
                      <Reply size={14} className="text-primary-500" />
                      <span className="text-sm text-primary-600">
                        回复 <span className="font-medium">{replyToUsername}</span>
                      </span>
                      <button
                        type="button"
                        onClick={cancelReply}
                        className="ml-auto cursor-pointer p-1 text-gray-400 hover:text-red-500 rounded-lg transition-colors duration-200"
                        title="取消回复"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  )}
                  <div className="flex gap-3">
                    <div className="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center shrink-0">
                      <User size={18} className="text-primary-600" />
                    </div>
                    <div className="flex-1">
                      <textarea
                        id="comment-input"
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder={replyToId ? `回复 ${replyToUsername}...` : '发表你的看法...'}
                        className="w-full px-4 py-3 bg-primary-50 border border-primary-200/50 rounded-xl focus:ring-2 focus:ring-primary-400/50 focus:border-primary-400 outline-none resize-none text-text-primary placeholder:text-primary-300 transition-colors duration-200"
                        rows={3}
                        maxLength={500}
                      />
                      <div className="flex justify-end mt-2">
                        <button
                          type="submit"
                          disabled={submittingComment || !newComment.trim()}
                          className="cursor-pointer flex items-center gap-2 px-5 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed font-medium shadow-button"
                        >
                          {submittingComment ? (
                            <>
                              <Loader2 size={16} className="animate-spin" />
                              提交中...
                            </>
                          ) : (
                            <>
                              <Send size={16} />
                              {replyToId ? '回复' : '发表评论'}
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  </div>
                </form>
              )}

              {/* 评论列表 - 扁平式结构 */}
              <div className="space-y-3">
                {comments.length > 0 ? (
                  flattenComments(comments).map((comment) => (
                    <CommentItem
                      key={comment.id}
                      comment={comment}
                      user={user}
                      onReply={startReplyComment}
                      onEdit={startEditComment}
                      onDelete={handleDeleteComment}
                      onUpdate={handleUpdateComment}
                      editingCommentId={editingCommentId}
                      editingContent={editingContent}
                      setEditingContent={setEditingContent}
                      cancelEdit={cancelEditComment}
                      formatShortDate={formatShortDate}
                    />
                  ))
                ) : (
                  /* 空评论状态 */
                  <div className="text-center py-12">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 rounded-full mb-4">
                      <MessageSquare size={28} className="text-primary-400" />
                    </div>
                    <p className="text-text-muted">暂无评论</p>
                    <p className="text-text-muted text-sm mt-1">
                      {user ? '来发表第一条评论吧~' : '登录后即可发表评论'}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* ========== 右侧边栏 (1/3) - 粘性定位 ========== */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-card p-6 sticky top-24 space-y-6">
              {/* 发布者信息卡片 */}
              <div className="flex items-center gap-4 p-4 bg-gradient-to-r from-primary-50 to-primary-100/50 rounded-xl">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white font-bold text-lg shadow-glow">
                  {activity.publisherName?.charAt(0) || 'U'}
                </div>
                <div>
                  <p className="text-xs text-text-muted mb-0.5">活动发布者</p>
                  <p className="font-heading font-semibold text-text-primary">{activity.publisherName}</p>
                </div>
              </div>

              {/* 报名进度条 */}
              {activity.maxParticipants > 0 && (
                <div className="p-4 bg-primary-50 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm text-text-muted flex items-center gap-1.5">
                      <Users size={14} className="text-primary-400" />
                      报名进度
                    </span>
                    <span className="text-sm font-semibold text-text-primary">
                      {activity.currentParticipants || 0}/{activity.maxParticipants}
                    </span>
                  </div>
                  <div className="w-full h-3 bg-primary-100 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        getProgressPercent(activity.currentParticipants || 0, activity.maxParticipants) >= 90
                          ? 'bg-gradient-to-r from-red-400 to-red-500'
                          : 'bg-gradient-to-r from-primary-500 to-accent-500'
                      }`}
                      style={{
                        width: `${getProgressPercent(activity.currentParticipants || 0, activity.maxParticipants)}%`,
                      }}
                    />
                  </div>
                  {getProgressPercent(activity.currentParticipants || 0, activity.maxParticipants) >= 90 && (
                    <p className="text-xs text-red-500 mt-2 flex items-center gap-1">
                      <XCircle size={12} />
                      名额即将用尽，抓紧报名！
                    </p>
                  )}
                </div>
              )}

              {/* 操作按钮区域 */}
              <div className="space-y-3">
                {/* 发布者专属操作 */}
                {user?.userId === activity.publisherId && (
                  <>
                    {/* 草稿状态：编辑 + 发布 */}
                    {activity.status === 'draft' && (
                      <>
                        <Link
                          to={`/activities/${activity.id}/edit`}
                          className="cursor-pointer w-full py-3 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl font-medium hover:from-primary-700 hover:to-primary-800 transition-all duration-200 text-center block shadow-button flex items-center justify-center gap-2"
                        >
                          <Edit3 size={18} />
                          编辑活动
                        </Link>
                        <button
                          onClick={handlePublishActivity}
                          disabled={actionLoading}
                          className="cursor-pointer w-full py-3 bg-gradient-to-r from-accent-500 to-accent-600 text-white rounded-xl font-medium hover:from-accent-600 hover:to-accent-700 transition-all duration-200 disabled:opacity-50 flex items-center justify-center gap-2 shadow-glow-accent"
                        >
                          <CheckCircle size={18} />
                          发布活动
                        </button>
                        <Link
                          to={`/activities/${activity.id}/registrations`}
                          className="cursor-pointer w-full py-3 bg-primary-100 text-primary-700 rounded-xl font-medium hover:bg-primary-200 transition-colors duration-200 text-center block flex items-center justify-center gap-2"
                        >
                          <ClipboardList size={18} />
                          查看报名名单
                        </Link>
                      </>
                    )}

                    {/* 已发布状态：查看报名 + 取消 + 结束 */}
                    {activity.status === 'published' && (
                      <>
                        <Link
                          to={`/activities/${activity.id}/registrations`}
                          className="cursor-pointer w-full py-3 bg-primary-100 text-primary-700 rounded-xl font-medium hover:bg-primary-200 transition-colors duration-200 text-center block flex items-center justify-center gap-2"
                        >
                          <ClipboardList size={18} />
                          查看报名名单
                        </Link>
                        <button
                          onClick={handleEndActivity}
                          disabled={actionLoading}
                          className="cursor-pointer w-full py-3 bg-primary-50 text-primary-600 border border-primary-200 rounded-xl font-medium hover:bg-primary-100 transition-colors duration-200 disabled:opacity-50 flex items-center justify-center gap-2"
                        >
                          <CheckCircle size={18} />
                          结束活动
                        </button>
                        <button
                          onClick={handleCancelActivity}
                          disabled={actionLoading}
                          className="cursor-pointer w-full py-3 border border-amber-300 text-amber-600 rounded-xl font-medium hover:bg-amber-50 transition-colors duration-200 disabled:opacity-50 flex items-center justify-center gap-2"
                        >
                          <XCircle size={18} />
                          取消活动
                        </button>
                      </>
                    )}

                    {/* 已取消/已结束状态：删除 */}
                    {(activity.status === 'cancelled' || activity.status === 'ended') && (
                      <button
                        onClick={handleDeleteActivity}
                        disabled={actionLoading}
                        className="cursor-pointer w-full py-3 border border-red-300 text-red-500 rounded-xl font-medium hover:bg-red-50 transition-colors duration-200 disabled:opacity-50 flex items-center justify-center gap-2"
                      >
                        <Trash2 size={18} />
                        删除活动
                      </button>
                    )}
                  </>
                )}

                {/* 非发布者操作按钮 */}
                {user?.userId !== activity.publisherId && activity.status === 'published' && activity.approvalStatus !== 'pending' && (
                  <>
                    {/* 报名/取消报名按钮 */}
                    {isRegistered ? (
                      <button
                        onClick={handleCancelRegister}
                        disabled={actionLoading}
                        className="cursor-pointer w-full py-3 border-2 border-red-400 text-red-500 rounded-xl font-medium hover:bg-red-50 transition-colors duration-200 disabled:opacity-50 flex items-center justify-center gap-2"
                      >
                        {actionLoading ? (
                          <>
                            <Loader2 size={18} className="animate-spin" />
                            处理中...
                          </>
                        ) : (
                          <>
                            <XCircle size={18} />
                            取消报名
                          </>
                        )}
                      </button>
                    ) : (
                      <button
                        onClick={handleRegister}
                        disabled={actionLoading}
                        className="cursor-pointer w-full py-3 bg-gradient-to-r from-accent-500 to-accent-600 text-white rounded-xl font-medium hover:from-accent-600 hover:to-accent-700 transition-all duration-200 disabled:opacity-50 shadow-glow-accent flex items-center justify-center gap-2"
                      >
                        {actionLoading ? (
                          <>
                            <Loader2 size={18} className="animate-spin" />
                            处理中...
                          </>
                        ) : (
                          <>
                            <CheckCircle size={18} />
                            立即报名
                          </>
                        )}
                      </button>
                    )}

                    {/* 收藏/订阅按钮组 */}
                    <div className="grid grid-cols-2 gap-3">
                      <button
                        onClick={handleFavorite}
                        disabled={actionLoading}
                        className={`cursor-pointer py-3 rounded-xl font-medium transition-all duration-200 flex items-center justify-center gap-2 ${
                          isFavorited
                            ? 'bg-red-50 text-red-500 border-2 border-red-300 hover:bg-red-100'
                            : 'bg-primary-50 text-primary-600 border border-primary-200 hover:bg-primary-100'
                        }`}
                      >
                        <Heart size={18} fill={isFavorited ? 'currentColor' : 'none'} />
                        {isFavorited ? '已收藏' : '收藏'}
                      </button>

                      <button
                        onClick={handleSubscribe}
                        disabled={actionLoading}
                        className={`cursor-pointer py-3 rounded-xl font-medium transition-all duration-200 flex items-center justify-center gap-2 ${
                          isSubscribed
                            ? 'bg-primary-100 text-primary-700 border-2 border-primary-300 hover:bg-primary-200'
                            : 'bg-primary-50 text-primary-600 border border-primary-200 hover:bg-primary-100'
                        }`}
                      >
                        <Bell size={18} fill={isSubscribed ? 'currentColor' : 'none'} />
                        {isSubscribed ? '已订阅' : '订阅'}
                      </button>
                    </div>
                  </>
                )}

                {/* 未登录提示 */}
                {!user && activity.status === 'published' && (
                  <div className="text-center py-4 bg-primary-50 rounded-xl">
                    <p className="text-text-muted text-sm mb-3">登录后可报名活动</p>
                    <Link
                      to="/login"
                      className="cursor-pointer inline-flex items-center gap-2 px-5 py-2.5 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors duration-200 font-medium"
                    >
                      登录 / 注册
                    </Link>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}