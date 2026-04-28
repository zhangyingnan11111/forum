package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    @Select("SELECT * FROM like_record WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    LikeRecord selectByUserAndTarget(@Param("userId") Long userId,
                                     @Param("targetType") String targetType,
                                     @Param("targetId") Long targetId);
}