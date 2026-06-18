import apiClient from './client';
import type { SearchSuggestionResponse, SearchHistory } from '@/types/search';
import type { ApiResponse } from '@/types/common';

/** 获取搜索建议 */
export const getSearchSuggestions = (prefix?: string) =>
  apiClient.get<ApiResponse<SearchSuggestionResponse>>('/search/suggestions', { params: { prefix } });

/** 搜索自动补全 */
export const autocomplete = (prefix?: string) =>
  apiClient.get<ApiResponse<string[]>>('/search/autocomplete', { params: { prefix } });

/** 获取热门搜索 */
export const getHotSearches = () =>
  apiClient.get<ApiResponse<string[]>>('/search/hot');

/** 获取搜索历史 */
export const getSearchHistory = () =>
  apiClient.get<ApiResponse<SearchHistory[]>>('/search/history');

/** 清除搜索历史 */
export const clearSearchHistory = () =>
  apiClient.delete<ApiResponse<number>>('/search/history');

/** 删除单条搜索历史 */
export const deleteSearchHistoryItem = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/search/history/${id}`);

/** 执行搜索 */
export const executeSearch = (params: {
  keyword: string;
  type?: string;
  status?: string;
  page?: number;
  size?: number;
}) =>
  apiClient.get<ApiResponse<any>>('/search/execute', { params });
