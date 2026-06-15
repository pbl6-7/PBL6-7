import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Settings, Shield, Trash2, RefreshCw, AlertTriangle, Check } from 'lucide-react';
import { getSensitiveWords, createSensitiveWord, deleteSensitiveWord, batchCreateSensitiveWords, reloadSensitiveWords, getSensitiveWordStatistics } from '@/api/admin';

export default function AdminSettingsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('sensitive-words');
  const [sensitiveWords, setSensitiveWords] = useState<string[]>([]);
  const [newWord, setNewWord] = useState('');
  const [batchWords, setBatchWords] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    checkAdmin();
    loadSensitiveWords();
  }, []);

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

  const loadSensitiveWords = async () => {
    setLoading(true);
    try {
      const res = await getSensitiveWords({ page: 1, size: 100 });
      const words = res.data.data?.list?.map((item: any) => item.word) || [];
      setSensitiveWords(words);
    } catch (err) {
      console.error('加载敏感词失败', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddWord = async () => {
    if (!newWord.trim()) return;
    try {
      await createSensitiveWord({ word: newWord.trim() });
      setNewWord('');
      loadSensitiveWords();
      setMessage({ type: 'success', text: '添加成功' });
    } catch (err) {
      setMessage({ type: 'error', text: '添加失败' });
    }
  };

  const handleDeleteWord = async (word: string) => {
    try {
      await deleteSensitiveWord(word as any);
      loadSensitiveWords();
      setMessage({ type: 'success', text: '删除成功' });
    } catch (err) {
      setMessage({ type: 'error', text: '删除失败' });
    }
  };

  const handleBatchAdd = async () => {
    if (!batchWords.trim()) return;
    const words = batchWords.split('\n').filter(w => w.trim());
    if (words.length === 0) return;
    try {
      await batchCreateSensitiveWords({ words });
      setBatchWords('');
      loadSensitiveWords();
      setMessage({ type: 'success', text: `批量添加成功 (${words.length}个)` });
    } catch (err) {
      setMessage({ type: 'error', text: '批量添加失败' });
    }
  };

  const handleReload = async () => {
    try {
      await reloadSensitiveWords();
      loadSensitiveWords();
      setMessage({ type: 'success', text: '重新加载成功' });
    } catch (err) {
      setMessage({ type: 'error', text: '重新加载失败' });
    }
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
              <h1 className="text-xl font-bold text-indigo-600">系统设置</h1>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 标签页 */}
        <div className="bg-white rounded-xl shadow-sm mb-6">
          <div className="flex border-b border-gray-200">
            <button
              onClick={() => setActiveTab('sensitive-words')}
              className={`px-6 py-4 text-sm font-medium ${
                activeTab === 'sensitive-words'
                  ? 'text-indigo-600 border-b-2 border-indigo-600'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <Shield size={16} className="inline mr-2" />
              敏感词管理
            </button>
          </div>
        </div>

        {message && (
          <div className={`mb-4 p-4 rounded-lg flex items-center gap-2 ${
            message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
          }`}>
            {message.type === 'success' ? <Check size={18} /> : <AlertTriangle size={18} />}
            {message.text}
          </div>
        )}

        {activeTab === 'sensitive-words' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* 添加敏感词 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4">添加敏感词</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">单个添加</label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newWord}
                      onChange={(e) => setNewWord(e.target.value)}
                      placeholder="输入敏感词"
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                    />
                    <button
                      onClick={handleAddWord}
                      className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                    >
                      添加
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">批量添加（每行一个）</label>
                  <textarea
                    value={batchWords}
                    onChange={(e) => setBatchWords(e.target.value)}
                    placeholder="输入多个敏感词，每行一个"
                    rows={5}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                  />
                  <button
                    onClick={handleBatchAdd}
                    className="mt-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
                  >
                    批量添加
                  </button>
                </div>

                <button
                  onClick={handleReload}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
                >
                  <RefreshCw size={16} />
                  重新加载敏感词库
                </button>
              </div>
            </div>

            {/* 敏感词列表 */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-semibold">敏感词列表</h2>
                <span className="text-sm text-gray-500">共 {sensitiveWords.length} 个</span>
              </div>
              <div className="max-h-96 overflow-y-auto">
                {loading ? (
                  <div className="text-center py-8 text-gray-500">加载中...</div>
                ) : sensitiveWords.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">暂无敏感词</div>
                ) : (
                  <div className="flex flex-wrap gap-2">
                    {sensitiveWords.map((word, index) => (
                      <span
                        key={index}
                        className="inline-flex items-center gap-1 px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm"
                      >
                        {word}
                        <button
                          onClick={() => handleDeleteWord(word)}
                          className="hover:text-red-600"
                        >
                          <Trash2 size={14} />
                        </button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
