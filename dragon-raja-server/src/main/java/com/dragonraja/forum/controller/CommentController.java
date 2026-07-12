package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.CommentDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.CommentService;
import com.dragonraja.forum.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 * 提供评论列表查询、发表评论、删除评论等接口
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询帖子的评论列表
     * 公开接口，无需登录
     *
     * @param postId  帖子ID
     * @param current 当前页（默认1）
     * @param size    每页大小（默认10）
     * @return 分页评论列表
     */
    @GetMapping("/post/{postId}")
    public Result<PageResult<CommentVO>> getCommentList(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<CommentVO> pageResult = commentService.getCommentPage(postId, current, size);
        return Result.success(pageResult);
    }

    /**
     * 发表评论
     * 对帖子发表一级评论，同时帖子评论数 +1
     *
     * @param dto 评论参数（帖子ID、评论内容）
     * @return 新评论ID
     */
    @PostMapping
    public Result<Long> createComment(@Valid @RequestBody CommentDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        Long commentId = commentService.createComment(dto, userId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     * 仅评论者本人可删除，同时帖子评论数 -1
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        commentService.deleteComment(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 管理员删除评论
     * 管理员可强制删除任何评论
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteComment(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("仅管理员可删除评论");
        }
        commentService.adminDeleteComment(id);
        return Result.success("评论已删除");
    }
}
