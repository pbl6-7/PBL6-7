import request from '@/utils/request'

export interface SearchSuggestionsResponse {
  suggestions: string[]
  hotSearches: string[]
}

export const getSearchSuggestions = (prefix?: string) => {
  return request.get<any, { data: SearchSuggestionsResponse }>('/search/suggestions', {
    params: { prefix }
  })
}

export const getAutocomplete = (prefix: string) => {
  return request.get<any, { data: string[] }>('/search/autocomplete', {
    params: { prefix }
  })
}

export const getHotSearches = () => {
  return request.get<any, { data: string[] }>('/search/hot')
}
