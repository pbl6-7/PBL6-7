// 搜索相关类型

// 搜索建议响应
export interface SearchSuggestionResponse {
  suggestions: string[];
  hotSearches: string[];
  recentSearches: string[];
}

// 搜索历史
export interface SearchHistory {
  id: number;
  keyword: string;
  searchTime: string;
  resultCount: number;
}

// 搜索结果
export interface SearchResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

// 自动补全建议
export interface AutocompleteSuggestion {
  text: string;
  score: number;
}
