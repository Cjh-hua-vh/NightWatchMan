package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dragonraja.forum.entity.Comment;
import com.dragonraja.forum.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供通用 CRUD
 * 额外提供关联查询用户信息的自定义 SQL
 *
 * @author Kou
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询帖子的评论列表（关联用户信息）
     *
     * @param page   分页对象
     * @param postId 帖子ID
     * @return 分页结果（CommentVO 列表）
     */
    IPage<CommentVO> selectCommentPage(IPage<CommentVO> page,
                                       @Param("postId") Long postId);
}
