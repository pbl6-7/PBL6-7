import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Search,
  X,
  TrendingUp,
  Clock,
  Trash2,
  Loader2,
  Calendar,
  MapPin,
  Tag,
  FileSearch,
  ArrowLeft,
} from 'lucide-react';
import {
  executeSearch,
  getSearchSuggestions,
  autocomplete,
  getHotSearches,
  getSearchHistory,
  clearSearchHistory,
  deleteSearchHistoryItem,
} from '@/api/search';
import type { SearchHistory, SearchResult } from '@/types/search';
import type { Activity } from '@/types/activity';
import Navbar from '@/components/Navbar';
import { Toast, useToastStore } from '@/components/Toast';

/** 搜索结果项类型（兼容 Activity 结构） */
interface SearchItem {
  id: number;
  title: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  location?: string;
  activityTypeName?: string;
  status?: string;
  [key: string]: unknown;
}

/** 卡片渐变色配置 */
const CARD_GRADIENTS = [
  'from-violet-500 to-purple-600',
  'from-indigo-500 to-blue-600',
  'from-fuchsia-500 to-pink-600',
  'from-emerald-500 to-teal-600',
  'from-amber-500 to-orange-600',
  'from-cyan-500 to-blue-600',
  'from-rose-500 to-red-600',
  'from-lime-500 to-green-600',
];

/**
 * 根据项目 ID 获取对应的渐变配色
 * @param id - 项目 ID
 * @returns Tailwind 渐变类名字符串
 */
function getCardGradient(id: number): string {
  return CARD_GRADIENTS[id % CARD_GRADIENTS.length];
}

/**
 * 全局搜索页面组件
 * 支持关键词搜索、搜索建议、热门搜索、搜索历史
 */
export default function SearchPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const addToast = useToastStore((s) => s.addToast);

  /* 搜索相关状态 */
  const [keyword, setKeyword] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<SearchItem[]>([]);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize] = useState(12);

  /* 搜索建议与自动补全 */
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [autocompleteItems, setAutocompleteItems] = useState<string[]>([]);

  /* 热门搜索与搜索历史 */
  const [hotSearches, setHotSearches] = useState<string[]>([]);
  const [searchHistory, setSearchHistory] = useState<SearchHistory[]>([]);

  /* 搜索建议下拉的防抖定时器 */
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  /* 搜索框容器 ref，用于点击外部关闭建议下拉 */
  const searchBoxRef = useRef<HTMLDivElement>(null);

  /* 是否已执行过搜索（控制显示搜索结果还是热门/历史区域） */
  const [hasSearched, setHasSearched] = useState(false);

  /**
   * 加载热门搜索列表
   */
  const loadHotSearches = useCallback(async () => {
    try {
      const res = await getHotSearches();
      setHotSearches(res.data.data || []);
    } catch {
      console.error('加载热门搜索失败');
    }
  }, []);

  /**
   * 加载搜索历史列表
   */
  const loadSearchHistory = useCallback(async () => {
    try {
      const res = await getSearchHistory();
      setSearchHistory(res.data.data || []);
    } catch {
      console.error('加载搜索历史失败');
    }
  }, []);

  /* 初始化：加载热门搜索和搜索历史 */
  useEffect(() => {
    loadHotSearches();
    loadSearchHistory();
  }, [loadHotSearches, loadSearchHistory]);

  /* 监听 URL 中的 keyword 参数，自动执行搜索 */
  useEffect(() => {
    const urlKeyword = searchParams.get('keyword');
    if (urlKeyword && urlKeyword.trim()) {
      setKeyword(urlKeyword.trim());
      doSearch(urlKeyword.trim(), 1);
    }
  }, [searchParams]);

  /* 点击外部关闭搜索建议下拉 */
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (searchBoxRef.current && !searchBoxRef.current.contains(e.target as Node)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /**
   * 执行搜索操作
   * @param searchKeyword - 搜索关键词
   * @param page - 页码，默认为 1
   */
  const doSearch = async (searchKeyword: string, page = 1) => {
    if (!searchKeyword.trim()) return;
    setIsSearching(true);
    setHasSearched(true);
    setShowSuggestions(false);
    try {
      const res = await executeSearch({
        keyword: searchKeyword.trim(),
        page,
        size: pageSize,
      });
      const data = res.data.data;
      /* 后端 ActivityPageResponse 使用 list 字段而非 items */
      setSearchResults(data.list || []);
      setTotalResults(data.total || 0);
      setCurrentPage(data.page || page);
      setTotalPages(data.totalPages || Math.ceil((data.total || 0) / pageSize));
      /* 搜索成功后刷新搜索历史 */
      loadSearchHistory();
    } catch {
      addToast('error', '搜索失败，请稍后重试');
    } finally {
      setIsSearching(false);
    }
  };

  /**
   * 处理搜索表单提交
   * @param e - 表单事件对象
   */
  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    doSearch(keyword, 1);
  };

  /**
   * 处理搜索框输入变化，实时获取搜索建议和自动补全
   * @param value - 输入值
   */
  const handleInputChange = (value: string) => {
    setKeyword(value);
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    if (!value.trim()) {
      setSuggestions([]);
      setAutocompleteItems([]);
      setShowSuggestions(false);
      return;
    }
    debounceRef.current = setTimeout(async () => {
      try {
        const [sugRes, autoRes] = await Promise.all([
          getSearchSuggestions(value.trim()),
          autocomplete(value.trim()),
        ]);
        const sugData = sugRes.data.data;
        setSuggestions(sugData?.suggestions || []);
        setAutocompleteItems(autoRes.data.data || []);
        setShowSuggestions(true);
      } catch {
        setSuggestions([]);
        setAutocompleteItems([]);
      }
    }, 300);
  };

  /**
   * 点击搜索建议项进行搜索
   * @param text - 建议文本
   */
  const handleSuggestionClick = (text: string) => {
    setKeyword(text);
    doSearch(text, 1);
  };

  /**
   * 点击热门搜索标签进行搜索
   * @param hotKeyword - 热门关键词
   */
  const handleHotSearchClick = (hotKeyword: string) => {
    setKeyword(hotKeyword);
    doSearch(hotKeyword, 1);
  };

  /**
   * 清除全部搜索历史
   */
  const handleClearHistory = async () => {
    try {
      await clearSearchHistory();
      setSearchHistory([]);
      addToast('success', '搜索历史已清除');
    } catch {
      addToast('error', '清除搜索历史失败');
    }
  };

  /**
   * 删除单条搜索历史
   * @param id - 搜索历史记录 ID
   */
  const handleDeleteHistoryItem = async (id: number) => {
    try {
      await deleteSearchHistoryItem(id);
      setSearchHistory((prev) => prev.filter((item) => item.id !== id));
    } catch {
      addToast('error', '删除搜索历史失败');
    }
  };

  /**
   * 切换搜索结果分页
   * @param page - 目标页码
   */
  const handlePageChange = (page: number) => {
    doSearch(keyword, page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /**
   * 格式化日期字符串为可读格式
   * @param dateStr - ISO 日期字符串
   * @returns 格式化后的日期文本
   */
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * 合并搜索建议和自动补全结果（去重）
   * @returns 合并后的建议列表
   */
  const mergedSuggestions = [...new Set([...autocompleteItems, ...suggestions])].slice(0, 8);

  return (
    <div className="min-h-screen bg-[#FAF5FF]">
      {/* 全局导航栏（隐藏搜索框，搜索页自身有搜索框） */}
      <Navbar hideSearch />
      {/* Toast 通知容器 */}
      <Toast />

      {/* ==================== 搜索头部区域 ==================== */}
      <div className="relative overflow-hidden bg-gradient-to-br from-violet-600 via-purple-700 to-indigo-800 pt-8 pb-12 px-4 sm:px-6 lg:px-8">
        {/* 装饰性背景 */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-0 left-1/4 w-64 h-64 bg-white rounded-full blur-3xl" />
          <div className="absolute bottom-0 right-1/4 w-80 h-80 bg-violet-400 rounded-full blur-3xl" />
        </div>

        <div className="relative max-w-3xl mx-auto text-center">
          {/* 返回首页按钮 */}
          <button
            onClick={() => navigate('/')}
            className="absolute left-0 top-1 flex items-center gap-2 px-4 py-2 bg-white/10 backdrop-blur-sm text-white/90 hover:bg-white/20 hover:text-white rounded-xl border border-white/10 transition-all duration-200 cursor-pointer text-sm font-medium"
          >
            <ArrowLeft size={16} />
            返回首页
          </button>

          <h1 className="text-3xl sm:text-4xl font-bold text-white mb-2 tracking-tight">
            全局搜索
          </h1>
          <p className="text-violet-200 text-base sm:text-lg mb-6">
            搜索活动、发现精彩 ✨
          </p>

          {/* 搜索框 */}
          <div ref={searchBoxRef} className="relative max-w-2xl mx-auto">
            <form onSubmit={handleSearchSubmit}>
              <div className="relative flex items-center">
                <Search size={20} className="absolute left-4 text-violet-400" />
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => handleInputChange(e.target.value)}
                  onFocus={() => {
                    if (mergedSuggestions.length > 0) setShowSuggestions(true);
                  }}
                  placeholder="输入关键词搜索活动..."
                  className="w-full pl-12 pr-12 py-4 bg-white/90 backdrop-blur-xl border border-white/30 rounded-2xl text-[#4C1D95] placeholder:text-violet-300 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-base shadow-lg transition-all duration-200"
                />
                {keyword && (
                  <button
                    type="button"
                    onClick={() => {
                      setKeyword('');
                      setSuggestions([]);
                      setAutocompleteItems([]);
                      setShowSuggestions(false);
                    }}
                    className="absolute right-4 p-1 rounded-full hover:bg-violet-100 text-violet-400 transition-colors duration-200 cursor-pointer"
                  >
                    <X size={18} />
                  </button>
                )}
              </div>
            </form>

            {/* 搜索建议下拉 */}
            {showSuggestions && mergedSuggestions.length > 0 && (
              <div className="absolute top-full left-0 right-0 mt-2 bg-white/95 backdrop-blur-xl rounded-2xl shadow-xl border border-violet-100 overflow-hidden z-50">
                {mergedSuggestions.map((text, index) => (
                  <button
                    key={index}
                    onClick={() => handleSuggestionClick(text)}
                    className="w-full flex items-center gap-3 px-5 py-3 text-left text-[#4C1D95] hover:bg-violet-50 transition-colors duration-150 cursor-pointer"
                  >
                    <Search size={14} className="text-violet-400 shrink-0" />
                    <span className="truncate text-sm">{text}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 -mt-4 relative z-10">
        {/* ==================== 未搜索时：热门搜索 + 搜索历史 ==================== */}
        {!hasSearched && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* 热门搜索 */}
            <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-violet-100/50 p-6">
              <div className="flex items-center gap-2 mb-4">
                <TrendingUp size={20} className="text-violet-600" />
                <h2 className="text-lg font-semibold text-[#4C1D95]">热门搜索</h2>
              </div>
              {hotSearches.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {hotSearches.map((hot, index) => (
                    <button
                      key={index}
                      onClick={() => handleHotSearchClick(hot)}
                      className="px-4 py-2 bg-violet-50 text-violet-700 rounded-full text-sm font-medium hover:bg-violet-100 hover:text-violet-900 border border-violet-200/50 transition-all duration-200 cursor-pointer"
                    >
                      {hot}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-violet-400">暂无热门搜索</p>
              )}
            </div>

            {/* 搜索历史 */}
            <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg border border-violet-100/50 p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <Clock size={20} className="text-violet-600" />
                  <h2 className="text-lg font-semibold text-[#4C1D95]">搜索历史</h2>
                </div>
                {searchHistory.length > 0 && (
                  <button
                    onClick={handleClearHistory}
                    className="flex items-center gap-1 text-sm text-violet-400 hover:text-red-500 transition-colors duration-200 cursor-pointer"
                  >
                    <Trash2 size={14} />
                    清除全部
                  </button>
                )}
              </div>
              {searchHistory.length > 0 ? (
                <div className="space-y-2 max-h-64 overflow-y-auto">
                  {searchHistory.map((item) => (
                    <div
                      key={item.id}
                      className="flex items-center justify-between px-4 py-2.5 bg-violet-50/50 rounded-xl hover:bg-violet-100/50 transition-colors duration-150 group"
                    >
                      <button
                        onClick={() => handleHotSearchClick(item.searchKeyword)}
                        className="flex-1 flex items-center gap-3 text-left cursor-pointer"
                      >
                        <Clock size={14} className="text-violet-400 shrink-0" />
                        <span className="text-sm text-[#4C1D95] truncate">{item.searchKeyword}</span>
                        <span className="text-xs text-violet-400 shrink-0">
                          {formatDate(item.searchTime)}
                        </span>
                      </button>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteHistoryItem(item.id);
                        }}
                        className="p-1.5 rounded-lg text-violet-300 hover:text-red-500 hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-all duration-200 cursor-pointer"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-violet-400">暂无搜索历史</p>
              )}
            </div>
          </div>
        )}

        {/* ==================== 搜索结果区域 ==================== */}
        {hasSearched && (
          <>
            {/* 结果统计 */}
            <div className="flex items-center justify-between mb-6">
              <p className="text-[#4C1D95]">
                搜索「
                <span className="font-semibold text-violet-700">{keyword}</span>
                」共找到{' '}
                <span className="font-semibold text-violet-700 text-lg">{totalResults}</span>{' '}
                个结果
              </p>
              <button
                onClick={() => {
                  setKeyword('');
                  setHasSearched(false);
                  setSearchResults([]);
                }}
                className="text-sm text-violet-500 hover:text-violet-700 underline transition-colors duration-200 cursor-pointer"
              >
                返回搜索
              </button>
            </div>

            {/* 加载状态 */}
            {isSearching ? (
              <div className="flex flex-col items-center justify-center py-20">
                <Loader2 size={40} className="text-violet-500 animate-spin mb-4" />
                <p className="text-violet-500 text-sm">正在搜索中...</p>
              </div>
            ) : searchResults.length > 0 ? (
              <>
                {/* 搜索结果卡片网格 */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {searchResults.map((item) => (
                    <div
                      key={item.id}
                      onClick={() => navigate(`/activities/${item.id}`)}
                      className="group cursor-pointer bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg border border-violet-100/30 overflow-hidden hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
                    >
                      {/* 渐变封面区域 */}
                      <div className={`h-36 bg-gradient-to-br ${getCardGradient(item.id)} relative`}>
                        {/* 类型标签 */}
                        {item.activityTypeName && (
                          <div className="absolute top-3 left-3">
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-white/20 backdrop-blur-sm text-white rounded-full text-xs font-medium border border-white/20">
                              <Tag size={12} />
                              {item.activityTypeName}
                            </span>
                          </div>
                        )}
                        {/* 装饰性光晕 */}
                        <div className="absolute -bottom-8 -right-8 w-24 h-24 bg-white/10 rounded-full blur-xl" />
                      </div>

                      {/* 卡片内容 */}
                      <div className="p-5">
                        {/* 活动标题 */}
                        <h3 className="font-semibold text-[#4C1D95] mb-2 group-hover:text-violet-600 transition-colors duration-200 line-clamp-2 leading-snug">
                          {item.title}
                        </h3>

                        {/* 描述摘要 */}
                        {item.description && (
                          <p className="text-sm text-violet-400 line-clamp-2 mb-3">
                            {item.description}
                          </p>
                        )}

                        {/* 元信息 */}
                        <div className="space-y-1.5 text-sm text-violet-500">
                          {item.startTime && (
                            <div className="flex items-center gap-2">
                              <Calendar size={14} className="shrink-0 text-violet-400" />
                              <span className="truncate">{formatDate(item.startTime)}</span>
                            </div>
                          )}
                          {item.location && (
                            <div className="flex items-center gap-2">
                              <MapPin size={14} className="shrink-0 text-violet-400" />
                              <span className="truncate">{item.location}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 分页 */}
                {totalPages > 1 && (
                  <div className="flex items-center justify-center gap-2 mt-8">
                    <button
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={currentPage <= 1}
                      className="px-4 py-2 bg-white/70 backdrop-blur-xl text-[#4C1D95] rounded-xl border border-violet-200/50 hover:bg-violet-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200 cursor-pointer text-sm font-medium"
                    >
                      上一页
                    </button>
                    {Array.from({ length: totalPages }, (_, i) => i + 1)
                      .filter((p) => {
                        /* 显示首页、末页及当前页附近页码 */
                        return p === 1 || p === totalPages || Math.abs(p - currentPage) <= 1;
                      })
                      .map((p, idx, arr) => {
                        const prev = arr[idx - 1];
                        const showEllipsis = prev !== undefined && p - prev > 1;
                        return (
                          <span key={p} className="flex items-center gap-2">
                            {showEllipsis && <span className="text-violet-400 text-sm">...</span>}
                            <button
                              onClick={() => handlePageChange(p)}
                              className={`w-10 h-10 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer ${
                                p === currentPage
                                  ? 'bg-violet-600 text-white shadow-md'
                                  : 'bg-white/70 text-[#4C1D95] border border-violet-200/50 hover:bg-violet-50'
                              }`}
                            >
                              {p}
                            </button>
                          </span>
                        );
                      })}
                    <button
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={currentPage >= totalPages}
                      className="px-4 py-2 bg-white/70 backdrop-blur-xl text-[#4C1D95] rounded-xl border border-violet-200/50 hover:bg-violet-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200 cursor-pointer text-sm font-medium"
                    >
                      下一页
                    </button>
                  </div>
                )}
              </>
            ) : (
              /* 空结果状态 */
              <div className="text-center py-20 px-4">
                <div className="inline-flex items-center justify-center w-24 h-24 bg-violet-100 rounded-full mb-6">
                  <FileSearch size={40} className="text-violet-400" />
                </div>
                <h3 className="text-xl font-semibold text-[#4C1D95] mb-2">
                  未找到相关活动
                </h3>
                <p className="text-violet-400 mb-6 max-w-md mx-auto">
                  没有找到与「{keyword}」相关的活动，试试其他关键词吧~
                </p>
                <div className="flex items-center justify-center gap-3">
                  <button
                    onClick={() => {
                      setKeyword('');
                      setHasSearched(false);
                      setSearchResults([]);
                    }}
                    className="px-6 py-2.5 bg-violet-600 text-white rounded-xl hover:bg-violet-700 transition-colors duration-200 shadow-md font-medium cursor-pointer"
                  >
                    重新搜索
                  </button>
                  <button
                    onClick={() => navigate('/activities')}
                    className="px-6 py-2.5 bg-white text-violet-600 border border-violet-200 rounded-xl hover:bg-violet-50 transition-colors duration-200 font-medium cursor-pointer"
                  >
                    浏览全部活动
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
