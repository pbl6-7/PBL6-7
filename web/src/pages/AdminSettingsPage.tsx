import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Settings,
  Shield,
  Trash2,
  RefreshCw,
  Loader2,
  GraduationCap,
  Plus,
  FileText,
  X,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import {
  getSensitiveWords,
  createSensitiveWord,
  deleteSensitiveWord,
  batchCreateSensitiveWords,
  reloadSensitiveWords,
} from '@/api/admin';
import AdminSidebar from '@/components/AdminSidebar';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 管理后台系统设置页面
 * 提供敏感词管理功能，包括单个添加、批量添加和删除
 */
export default function AdminSettingsPage() {
  const navigate = useNavigate();
  const addToast = useToastStore((s) => s.addToast);
  const [activeTab, setActiveTab] = useState('sensitive-words');
  const [sensitiveWords, setSensitiveWords] = useState<{ id: number; word: string }[]>([]);
  const [newWord, setNewWord] = useState('');
  const [batchWords, setBatchWords] = useState('');
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    checkAdmin();
    loadSensitiveWords();
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
   * 加载敏感词列表
   */
  const loadSensitiveWords = async () => {
    setLoading(true);
    try {
      const res = await getSensitiveWords();
      const words = (res.data.data || []).map((item: any) => ({ id: item.id, word: item.word }));
      setSensitiveWords(words);
    } catch (err) {
      console.error('加载敏感词失败', err);
      addToast('error', '加载敏感词列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 添加单个敏感词
   */
  const handleAddWord = async () => {
    if (!newWord.trim()) {
      addToast('warning', '请输入敏感词');
      return;
    }
    if (newWord.trim().length > 50) {
      addToast('warning', '单个敏感词不能超过50个字符');
      return;
    }
    setActionLoading('add');
    try {
      await createSensitiveWord({ word: newWord.trim() });
      setNewWord('');
      loadSensitiveWords();
      addToast('success', '敏感词添加成功');
    } catch (err) {
      console.error('添加敏感词失败', err);
      addToast('error', '添加敏感词失败，请重试');
    } finally {
      setActionLoading(null);
    }
  };

  /**
   * 删除指定敏感词
   */
  const handleDeleteWord = async (id: number, word: string) => {
    setActionLoading(`delete-${word}`);
    try {
      await deleteSensitiveWord(id);
      loadSensitiveWords();
      addToast('success', '敏感词删除成功');
    } catch (err) {
      console.error('删除敏感词失败', err);
      addToast('error', '删除敏感词失败，请重试');
    } finally {
      setActionLoading(null);
    }
  };

  /**
   * 批量添加敏感词
   */
  const handleBatchAdd = async () => {
    if (!batchWords.trim()) {
      addToast('warning', '请输入敏感词');
      return;
    }
    const words = batchWords.split('\n').filter((w) => w.trim());
    if (words.length === 0) {
      addToast('warning', '请输入有效的敏感词');
      return;
    }
    if (words.length > 100) {
      addToast('warning', '单次批量添加不能超过100个敏感词');
      return;
    }
    const tooLong = words.find((w) => w.trim().length > 50);
    if (tooLong) {
      addToast('warning', `敏感词「${tooLong.trim().slice(0, 10)}...」超过50个字符限制`);
      return;
    }
    setActionLoading('batch');
    try {
      await batchCreateSensitiveWords({ words });
      setBatchWords('');
      loadSensitiveWords();
      addToast('success', `批量添加成功，共 ${words.length} 个敏感词`);
    } catch (err) {
      console.error('批量添加失败', err);
      addToast('error', '批量添加失败，请重试');
    } finally {
      setActionLoading(null);
    }
  };

  /**
   * 重新加载敏感词库
   */
  const handleReload = async () => {
    setActionLoading('reload');
    try {
      await reloadSensitiveWords();
      loadSensitiveWords();
      addToast('success', '敏感词库已重新加载');
    } catch (err) {
      console.error('重新加载失败', err);
      addToast('error', '重新加载失败，请重试');
    } finally {
      setActionLoading(null);
    }
  };

  /** 计算批量添加的行数 */
  const batchLinesCount = batchWords.split('\n').filter((w) => w.trim()).length;

  /** 标签页配置 */
  const tabs = [
    { key: 'sensitive-words', label: '敏感词管理', icon: <Shield size={18} /> },
  ];

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
              <h1 className="text-xl font-bold text-[#4C1D95]">系统设置</h1>
            </div>
          </div>
        </header>

        {/* 内容区域 */}
        <main className="flex-1 p-6 overflow-auto">
          {/* 标签页导航 */}
          <div className="bg-white rounded-2xl shadow-card mb-6 p-2">
            <div className="flex gap-2">
              {tabs.map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => setActiveTab(tab.key)}
                  className={`inline-flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-medium transition-colors duration-200 cursor-pointer ${
                    activeTab === tab.key
                      ? 'bg-violet-600 text-white shadow-md'
                      : 'bg-violet-50 text-[#4C1D95] hover:bg-violet-100'
                  }`}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {activeTab === 'sensitive-words' && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* 添加敏感词区域 */}
              <div className="bg-white rounded-2xl shadow-card p-6">
                <h2 className="text-lg font-semibold text-[#4C1D95] mb-6">添加敏感词</h2>

                {/* 单个添加 */}
                <div className="mb-6">
                  <label className="block text-sm font-medium text-[#4C1D95] mb-2">单个添加</label>
                  <div className="flex gap-3">
                    <input
                      type="text"
                      value={newWord}
                      onChange={(e) => setNewWord(e.target.value)}
                      placeholder="输入敏感词..."
                      maxLength={50}
                      className="flex-1 px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-colors duration-200 placeholder:text-gray-400"
                    />
                    <button
                      onClick={handleAddWord}
                      disabled={actionLoading === 'add'}
                      className="inline-flex items-center gap-2 px-5 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
                    >
                      {actionLoading === 'add' ? (
                        <Loader2 size={18} className="animate-spin" />
                      ) : (
                        <Plus size={18} />
                      )}
                      添加
                    </button>
                  </div>
                </div>

                {/* 批量添加 */}
                <div className="mb-6">
                  <label className="block text-sm font-medium text-[#4C1D95] mb-2">批量添加</label>
                  <div className="relative">
                    <textarea
                      value={batchWords}
                      onChange={(e) => setBatchWords(e.target.value)}
                      placeholder="输入多个敏感词，每行一个..."
                      rows={5}
                      className="w-full px-4 py-3 bg-[#FAF5FF] border border-violet-200 rounded-xl focus:ring-2 focus:ring-violet-500 focus:border-transparent resize-none transition-colors duration-200 placeholder:text-gray-400"
                    />
                    {/* 字符计数 */}
                    <div className="absolute bottom-3 right-3 text-xs text-gray-400">
                      {batchLinesCount > 0 && (
                        <span className="px-2 py-1 bg-violet-100 text-violet-600 rounded-full">
                          {batchLinesCount} 个词
                        </span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={handleBatchAdd}
                    disabled={actionLoading === 'batch'}
                    className="mt-3 inline-flex items-center gap-2 px-5 py-3 bg-green-500 hover:bg-green-600 text-white rounded-xl font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
                  >
                    {actionLoading === 'batch' ? (
                      <Loader2 size={18} className="animate-spin" />
                    ) : (
                      <FileText size={18} />
                    )}
                    批量添加
                  </button>
                </div>

                {/* 重新加载按钮 */}
                <div className="pt-4 border-t border-violet-100">
                  <button
                    onClick={handleReload}
                    disabled={actionLoading === 'reload'}
                    className="inline-flex items-center gap-2 px-5 py-3 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded-xl font-medium transition-colors duration-200 disabled:opacity-50 cursor-pointer"
                  >
                    {actionLoading === 'reload' ? (
                      <Loader2 size={18} className="animate-spin" />
                    ) : (
                      <RefreshCw size={18} />
                    )}
                    重新加载敏感词库
                  </button>
                </div>
              </div>

              {/* 敏感词列表区域 */}
              <div className="bg-white rounded-2xl shadow-card p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-lg font-semibold text-[#4C1D95]">敏感词列表</h2>
                  <span className="px-3 py-1 bg-violet-100 text-violet-700 rounded-full text-sm font-medium">
                    共 {sensitiveWords.length} 个
                  </span>
                </div>

                <div className="max-h-[400px] overflow-y-auto pr-2">
                  {loading ? (
                    <div className="flex flex-col items-center justify-center py-16 gap-3">
                      <Loader2 className="animate-spin text-violet-600" size={32} />
                      <p className="text-gray-500">加载中...</p>
                    </div>
                  ) : sensitiveWords.length === 0 ? (
                    /* 空状态展示 */
                    <div className="flex flex-col items-center justify-center py-16 gap-4">
                      <div className="w-16 h-16 bg-violet-100 rounded-full flex items-center justify-center">
                        <Shield className="text-violet-400" size={28} />
                      </div>
                      <div className="text-center">
                        <p className="text-lg font-medium text-[#4C1D95]">暂无敏感词</p>
                        <p className="text-sm text-gray-400 mt-1">添加敏感词以过滤不当内容</p>
                      </div>
                    </div>
                  ) : (
                    /* 标签云展示 */
                    <div className="flex flex-wrap gap-2">
                      {sensitiveWords.map((item, index) => (
                        <div
                          key={index}
                          className="group inline-flex items-center gap-1.5 px-3 py-2 bg-gradient-to-r from-red-50 to-red-100 border border-red-200 rounded-xl text-sm font-medium text-red-700 hover:shadow-md transition-all duration-200"
                        >
                          <AlertCircle size={14} className="text-red-500" />
                          <span>{item.word}</span>
                          <button
                            onClick={() => handleDeleteWord(item.id, item.word)}
                            disabled={actionLoading === `delete-${item.word}`}
                            className="ml-1 p-1 hover:bg-red-200 rounded-lg transition-colors duration-200 cursor-pointer disabled:opacity-50"
                            title="删除"
                          >
                            {actionLoading === `delete-${item.word}` ? (
                              <Loader2 size={12} className="animate-spin" />
                            ) : (
                              <X size={12} className="text-red-500" />
                            )}
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}