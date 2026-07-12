package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供通用 CRUD
 *
 * @author Kou
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    User selectByUsername(@Param("username") String username);
}
