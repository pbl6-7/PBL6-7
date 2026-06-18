import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  MapPin,
  Users,
  Tag,
  FileText,
  Loader2,
  Sparkles,
  Clock,
  Type,
} from 'lucide-react';
import { createActivity } from '@/api/activity';
import { getAllActivityTypes } from '@/api/activityType';
import type { ActivityTypeResponse } from '@/api/activityType';
import { useToastStore } from '@/components/Toast';
import Navbar from '@/components/Navbar';

/**
 * 活动发布页面
 * 提供活动发布表单，支持创建包含标题、描述、地点、时间、类型、标签等信息的活动
 */
export default function PublishActivityPage() {
  const navigate = useNavigate();
  const { addToast } = useToastStore();
  const [loading, setLoading] = useState(false);
  const [activityTypes, setActivityTypes] = useState<ActivityTypeResponse[]>([]);
  const [form, setForm] = useState({
    title: '',
    description: '',
    location: '',
    startTime: '',
    endTime: '',
    typeId: 0,
    maxParticipants: 50,
    tags: '',
  });

  /** 从后端加载活动类型列表 */
  useEffect(() => {
    loadActivityTypes();
  }, []);

  const loadActivityTypes = async () => {
    try {
      const res = await getAllActivityTypes();
      const types = res.data.data || [];
      setActivityTypes(types);
      /* 默认选中第一个类型 */
      if (types.length > 0 && form.typeId === 0) {
        setForm((prev) => ({ ...prev, typeId: types[0].id }));
      }
    } catch (err) {
      console.error('加载活动类型失败', err);
    }
  };

  /**
   * 处理表单输入变化
   * @param e - 表单事件对象
   */
  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === 'maxParticipants' || name === 'typeId' ? Number(value) : value,
    }));
  };

  /**
   * 处理表单提交，发布活动
   * @param e - 表单提交事件
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!form.title.trim()) {
      addToast('warning', '请填写活动标题');
      return;
    }
    if (!form.location.trim()) {
      addToast('warning', '请填写活动地点');
      return;
    }
    if (!form.startTime) {
      addToast('warning', '请选择活动开始时间');
      return;
    }
    if (!form.endTime) {
      addToast('warning', '请选择活动结束时间');
      return;
    }
    if (new Date(form.endTime) <= new Date(form.startTime)) {
      addToast('warning', '结束时间必须晚于开始时间');
      return;
    }
    if (new Date(form.startTime) <= new Date()) {
      addToast('warning', '开始时间必须在当前时间之后');
      return;
    }
    if (form.title.trim().length > 100) {
      addToast('warning', '活动标题不能超过100个字符');
      return;
    }
    if (form.location.trim().length > 200) {
      addToast('warning', '活动地点不能超过200个字符');
      return;
    }

    setLoading(true);
    try {
      const tagList = form.tags
        .split(/[,，]/)
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0);

      await createActivity({
        title: form.title,
        description: form.description,
        location: form.location,
        startTime: form.startTime,
        endTime: form.endTime,
        typeId: form.typeId,
        maxParticipants: form.maxParticipants,
        tags: tagList.length > 0 ? tagList : undefined,
      });
      addToast('success', '活动发布成功，等待审核');
      navigate('/profile');
    } catch (err: any) {
      addToast('error', err.message || '发布失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#FAF5FF]">
      <Navbar />

      {/* 页面头部 */}
      <div className="bg-gradient-to-r from-violet-600 via-purple-600 to-indigo-600 pt-8 pb-16">
        <div className="max-w-3xl mx-auto px-4 sm:px-6">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-violet-200 hover:text-white transition-colors duration-200 mb-6 cursor-pointer"
          >
            <ArrowLeft size={18} />
            <span className="text-sm font-medium">返回</span>
          </button>
          <div className="flex items-center gap-3">
            <div className="p-3 bg-white/10 backdrop-blur-sm rounded-2xl border border-white/20">
              <Sparkles size={28} className="text-white" />
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                发布活动
              </h1>
              <p className="text-violet-200 text-sm mt-1">
                填写活动信息，发布后需等待审核通过
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 表单内容 */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 -mt-8 pb-12">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 基本信息 */}
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/20 shadow-lg p-6 sm:p-8">
            <div className="flex items-center gap-2 mb-6">
              <FileText size={20} className="text-violet-600" />
              <h2 className="text-lg font-semibold text-[#4C1D95]">基本信息</h2>
            </div>

            {/* 活动标题 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                活动标题 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="请输入活动标题"
                className="w-full px-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] placeholder:text-gray-400 transition-all duration-200"
                maxLength={100}
                required
              />
            </div>

            {/* 活动描述 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                活动描述
              </label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                placeholder="请详细描述活动内容..."
                rows={5}
                className="w-full px-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none resize-none text-[#4C1D95] placeholder:text-gray-400 transition-all duration-200"
                maxLength={2000}
              />
            </div>

            {/* 活动类型 + 人数上限 */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              {/* 活动类型 */}
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                  活动类型 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Type
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />
                  <select
                    name="typeId"
                    value={form.typeId}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] transition-all duration-200 appearance-none cursor-pointer"
                    required
                  >
                    {activityTypes.map((type) => (
                      <option key={type.id} value={type.id}>
                        {type.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 人数上限 */}
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                  人数上限
                </label>
                <div className="relative">
                  <Users
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />
                  <input
                    type="number"
                    name="maxParticipants"
                    value={form.maxParticipants}
                    onChange={handleChange}
                    min={1}
                    max={10000}
                    className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] transition-all duration-200"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* 时间地点 */}
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/20 shadow-lg p-6 sm:p-8">
            <div className="flex items-center gap-2 mb-6">
              <Clock size={20} className="text-violet-600" />
              <h2 className="text-lg font-semibold text-[#4C1D95]">时间地点</h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-6">
              {/* 开始时间 */}
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                  开始时间 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />
                  <input
                    type="datetime-local"
                    name="startTime"
                    value={form.startTime}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] transition-all duration-200"
                    required
                  />
                </div>
              </div>

              {/* 结束时间 */}
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                  结束时间 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar
                    size={18}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />
                  <input
                    type="datetime-local"
                    name="endTime"
                    value={form.endTime}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] transition-all duration-200"
                    required
                  />
                </div>
              </div>
            </div>

            {/* 活动地点 */}
            <div>
              <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                活动地点 <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <MapPin
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                />
                <input
                  type="text"
                  name="location"
                  value={form.location}
                  onChange={handleChange}
                  placeholder="请输入活动地点"
                  maxLength={200}
                  className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] placeholder:text-gray-400 transition-all duration-200"
                  required
                />
              </div>
            </div>
          </div>

          {/* 标签设置 */}
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/20 shadow-lg p-6 sm:p-8">
            <div className="flex items-center gap-2 mb-6">
              <Tag size={20} className="text-violet-600" />
              <h2 className="text-lg font-semibold text-[#4C1D95]">标签设置</h2>
            </div>
            <div>
              <label className="block text-sm font-medium text-[#4C1D95] mb-2">
                活动标签
              </label>
              <div className="relative">
                <Tag
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                />
                <input
                  type="text"
                  name="tags"
                  value={form.tags}
                  onChange={handleChange}
                  placeholder="多个标签用逗号分隔，如：学术,科技,创新"
                  className="w-full pl-10 pr-4 py-3 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-[#4C1D95] placeholder:text-gray-400 transition-all duration-200"
                />
              </div>
              <p className="mt-2 text-sm text-gray-500">
                添加标签有助于活动被更多人发现
              </p>
            </div>
          </div>

          {/* 提交按钮 */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => navigate('/profile')}
              className="flex-1 px-6 py-3.5 border border-violet-200 text-[#4C1D95] rounded-xl hover:bg-violet-50 transition-all duration-200 font-medium cursor-pointer"
            >
              取消
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-6 py-3.5 bg-gradient-to-r from-violet-600 to-purple-600 text-white rounded-xl hover:from-violet-700 hover:to-purple-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 font-medium shadow-lg shadow-violet-200 cursor-pointer"
            >
              {loading && <Loader2 size={18} className="animate-spin" />}
              {loading ? '发布中...' : '发布活动'}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
