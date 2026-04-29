package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT COUNT(*) FROM notification WHERE receiver_id = #{userId} AND is_read = 0")
    Long countUnread(Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE receiver_id = #{userId} AND is_read = 0")
    void markAllAsRead(Long userId);
}