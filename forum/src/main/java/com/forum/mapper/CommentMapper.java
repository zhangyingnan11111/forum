package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comment WHERE post_id = #{postId} AND status = 1 ORDER BY create_time ASC")
    List<Comment> selectByPostId(Long postId);

    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId} AND status = 1")
    Long countByPostId(Long postId);

    @Update("UPDATE comment SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    void updateLikeCount(Long commentId, int delta);
}