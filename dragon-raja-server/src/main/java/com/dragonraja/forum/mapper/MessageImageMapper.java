package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.MessageImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MessageImageMapper extends BaseMapper<MessageImage> {
    @Select("SELECT * FROM message_image WHERE message_id = #{messageId} ORDER BY sort_order")
    List<MessageImage> selectByMessageId(Long messageId);

    @Delete("DELETE FROM message_image WHERE message_id = #{messageId}")
    int deleteByMessageId(Long messageId);
}
