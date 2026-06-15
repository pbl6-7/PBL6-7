import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Calendar, MapPin, Users, Tag, FileText, Loader2 } from 'lucide-react';
import { createActivity } from '@/api/activity';
import type { ActivityType } from '@/types/activity';

/**
 * 活动发布页面
 * 提供活动发布表单，支持创建包含标题、描述、地点、时间、类型、标签等信息的活动
 */
export default function PublishActivityPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '',
    description: '',
    location: '',
    startTime: '',
    endTime: '',
    typeId: 1,
    maxParticipants: 50,
    tags: '',
  });

  // 活动类型选项
  const activityTypes: ActivityType[] = [
    { id: 1, name: '学术讲座' },
    { id: 2, name: '体育运动' },
    { id: 3, name: '文艺演出' },
    { id: 4, name: '社会实践' },
    { id: 5, name: '志愿者活动' },
    { id: 6, name: '竞赛比赛' },
    { id: 7, name: '社团活动' },
    { id: 8, name: '其他' },
  ];

  /**
   * 处理表单输入变化
   * @param e - 表单事件对象
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
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
    
    // 校验表单
    if (!form.title.trim()) {
      alert('请填写活动标题');
      return;
    }
    if (!form.location.trim()) {
      alert('请填写活动地点');
      return;
    }
    if (!form.startTime) {
      alert('请选择活动开始时间');
      return;
    }
    if (!form.endTime) {
      alert('请选择活动结束时间');
      return;
    }
    if (new Date(form.endTime) <= new Date(form.startTime)) {
      alert('结束时间必须晚于开始时间');
      return;
    }

    setLoading(true);
    try {
      // 解析标签
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
      alert('活动发布成功，等待审核');
      navigate('/profile');
    } catch (err: any) {
      alert(err.message || '发布失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center h-16">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 text-gray-600 hover:text-indigo-600 transition"
            >
              <ArrowLeft size={20} />
              <span>返回</span>
            </button>
            <h1 className="flex-1 text-center text-lg font-semibold text-gray-900">
              发布活动
            </h1>
            <div className="w-16" />
          </div>
        </div>
      </nav>

      {/* 表单内容 */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 基本信息 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">基本信息</h2>
            
            {/* 活动标题 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                活动标题 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="请输入活动标题"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                maxLength={100}
                required
              />
            </div>

            {/* 活动描述 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                活动描述
              </label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                placeholder="请详细描述活动内容..."
                rows={5}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
                maxLength={2000}
              />
            </div>

            {/* 活动类型 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                活动类型 <span className="text-red-500">*</span>
              </label>
              <select
                name="typeId"
                value={form.typeId}
                onChange={handleChange}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none bg-white"
                required
              >
                {activityTypes.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 人数上限 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                人数上限
              </label>
              <input
                type="number"
                name="maxParticipants"
                value={form.maxParticipants}
                onChange={handleChange}
                min={1}
                max={10000}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
              />
            </div>
          </div>

          {/* 时间地点 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">时间地点</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              {/* 开始时间 */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  开始时间 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="datetime-local"
                    name="startTime"
                    value={form.startTime}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
              </div>

              {/* 结束时间 */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  结束时间 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Calendar size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="datetime-local"
                    name="endTime"
                    value={form.endTime}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
              </div>
            </div>

            {/* 活动地点 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                活动地点 <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <MapPin size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  name="location"
                  value={form.location}
                  onChange={handleChange}
                  placeholder="请输入活动地点"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                  required
                />
              </div>
            </div>
          </div>

          {/* 标签设置 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">标签设置</h2>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                活动标签
              </label>
              <div className="relative">
                <Tag size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  name="tags"
                  value={form.tags}
                  onChange={handleChange}
                  placeholder="请输入标签，多个标签用逗号分隔，如：学术,科技,创新"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                />
              </div>
              <p className="mt-2 text-sm text-gray-500">
                多个标签用逗号分隔，有助于活动被更多人发现
              </p>
            </div>
          </div>

          {/* 提交按钮 */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => navigate('/profile')}
              className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
            >
              取消
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
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
