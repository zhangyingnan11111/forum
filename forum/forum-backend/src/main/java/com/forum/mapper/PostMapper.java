package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    void incrementViewCount(Long postId);

    @Update("UPDATE post SET like_count = like_count + #{delta} WHERE id = #{postId}")
    void updateLikeCount(Long postId, int delta);

    @Update("UPDATE post SET comment_count = comment_count + #{delta} WHERE id = #{postId}")
    void updateCommentCount(Long postId, int delta);

    @Select("SELECT COUNT(*) FROM post WHERE author_id = #{userId} AND status = 1")
    Long countUserPosts(Long userId);
}