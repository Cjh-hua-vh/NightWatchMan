package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.CommentDTO;
import com.dragonraja.forum.entity.Comment;
import com.dragonraja.forum.vo.CommentVO;

/**
 * 评论服务接口
 * 继承 MyBatis-Plus IService，提供通用 CRUD + 自定义业务方法
 *
 * @author Kou
 */
public interface CommentService extends IService<Comment> {

    /**
     * 分页查询帖子的评论列表
     *
     * @param postId  帖子ID
     * @param current 当前页
     * @param size    每页大小
     * @return 分页评论列表
     */
    PageResult<CommentVO> getCommentPage(Long postId, Long current, Long size);

    /**
     * 发表评论
     * 同时将帖子评论数 +1
     *
     * @param dto    评论参数
     * @param userId 评论者用户ID
     * @return 新评论ID
     */
    Long createComment(CommentDTO dto, Long userId);

    /**
     * 删除评论（仅评论者本人可删除）
     * 同时将帖子评论数 -1
     *
     * @param id     评论ID
     * @param userId 当前用户ID
     */
    void deleteComment(Long id, Long userId);

    /**
     * 管理员删除评论（可删除任意评论）
     * 同时将帖子评论数 -1
     *
     * @param id 评论ID
     */
    void adminDeleteComment(Long id);
}
