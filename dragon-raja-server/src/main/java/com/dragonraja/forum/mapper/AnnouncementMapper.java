package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供通用 CRUD
 *
 * @author Kou
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
