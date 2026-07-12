package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.Message;
import com.dragonraja.forum.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    @Select("SELECT m.* FROM message m WHERE m.id IN (" +
            "SELECT MAX(id) FROM message WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND type = 'PRIVATE' AND sender_id != receiver_id AND receiver_id > 0 GROUP BY " +
            "CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END" +
            ") ORDER BY m.create_time DESC")
    List<Message> selectSessionList(@Param("userId") Long userId);

    @Select("SELECT * FROM message WHERE ((sender_id = #{userId} AND receiver_id = #{targetId}) OR (sender_id = #{targetId} AND receiver_id = #{userId})) " +
            "AND type = 'PRIVATE' ORDER BY create_time ASC")
    List<Message> selectChatHistory(@Param("userId") Long userId, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{userId} AND sender_id = #{targetId} AND is_read = 0 AND type = 'PRIVATE'")
    Long selectUnreadCountByTarget(@Param("userId") Long userId, @Param("targetId") Long targetId);
}
