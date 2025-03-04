package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM user WHERE openid = #{openid} AND deleted = 0")
    User selectByOpenid(@Param("openid") String openid);
    
    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    @Update("UPDATE user SET last_login_time = #{lastLoginTime} WHERE id = #{userId} AND deleted = 0")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") String lastLoginTime);
}
