package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.PostImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostImageMapper extends BaseMapper<PostImage> {
    @Select("SELECT * FROM post_image WHERE post_id = #{postId} ORDER BY sort_order")
    List<PostImage> selectByPostId(Long postId);

    @Select("<script>" +
            "SELECT * FROM post_image WHERE post_id IN " +
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY post_id, sort_order" +
            "</script>")
    List<PostImage> selectByPostIds(@Param("postIds") List<Long> postIds);

    @Delete("DELETE FROM post_image WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);
}
