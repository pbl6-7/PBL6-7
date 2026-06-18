package com.campus.activity.mapper;

import com.campus.activity.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TagMapper {
    List<Tag> selectAll();
    Tag selectById(@Param("id") Long id);
    Tag selectByName(@Param("name") String name);
    int insert(Tag tag);
    int updateById(Tag tag);
    int deleteById(@Param("id") Long id);
}
