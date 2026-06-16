package com.campus.activity.mapper;

import com.campus.activity.entity.SearchHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 搜索历史Mapper接口
 */
@Mapper
public interface SearchHistoryMapper {

    @Insert("INSERT INTO search_history (user_id, search_keyword, search_type, search_time) " +
            "VALUES (#{userId}, #{searchKeyword}, #{searchType}, #{searchTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SearchHistory searchHistory);

    @Select("SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY search_time DESC LIMIT #{limit}")
    List<SearchHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("DELETE FROM search_history WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT DISTINCT search_keyword FROM search_history WHERE search_keyword LIKE CONCAT(#{prefix}, '%') ORDER BY COUNT(*) DESC LIMIT #{limit}")
    List<String> selectSuggestionsByPrefix(@Param("prefix") String prefix, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM search_history WHERE search_keyword = #{keyword}")
    int countByKeyword(@Param("keyword") String keyword);

    /**
     * 删除过期的搜索历史
     */
    void deleteOldSearchHistory(@Param("cutoff") java.time.LocalDateTime cutoff);
}
