package com.example.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.health.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsernameIncludingDeleted(String username);

    @Update("UPDATE user SET deleted=0, password=#{password}, update_time=NOW() WHERE id=#{id}")
    int restoreUser(@Param("id") Long id, @Param("password") String password);
}
