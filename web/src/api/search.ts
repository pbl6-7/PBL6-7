import apiClient from './client';
import type { SearchSuggestionResponse, SearchHistory } from '@/types/search';
import type { ApiResponse } from '@/types/common';

/** 获取搜索建议 */
export const getSearchSuggestions = (prefix?: string) =>
  apiClient.get<ApiResponse<SearchSuggestionResponse>>('/v1/search/suggestions', { params: { prefix } });

/** 搜索自动补全 */
export const autocomplete = (prefix?: string) =>
  apiClient.get<ApiResponse<string[]>>('/v1/search/autocomplete', { params: { prefix } });

/** 获取热门搜索 */
export const getHotSearches = () =>
  apiClient.get<ApiResponse<string[]>>('/v1/search/hot');

/** 获取搜索历史 */
export const getSearchHistory = () =>
  apiClient.get<ApiResponse<SearchHistory[]>>('/v1/search/history');

/** 清除搜索历史 */
export const clearSearchHistory = () =>
  apiClient.delete<ApiResponse<number>>('/v1/search/history');
