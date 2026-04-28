package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(String email);

    @Update("UPDATE user SET last_login_time = NOW(), last_login_ip = #{ip} WHERE id = #{userId}")
    void updateLoginInfo(Long userId, String ip);
}