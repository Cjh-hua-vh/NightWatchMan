package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.CommentDTO;
import com.dragonraja.forum.entity.Comment;
import com.dragonraja.forum.entity.Post;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.CommentMapper;
import com.dragonraja.forum.mapper.PostMapper;
import com.dragonraja.forum.mapper.UserMapper;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.CommentService;
import com.dragonraja.forum.service.MessageService;
import com.dragonraja.forum.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评论服务实现类
 *
 * @author Kou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final MessageService messageService;
    private final UserMapper userMapper;

    @Override
    public PageResult<CommentVO> getCommentPage(Long postId, Long current, Long size) {
        // 校验帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        Page<CommentVO> page = new Page<>(current, size);
        IPage<CommentVO> resultPage = commentMapper.selectCommentPage(page, postId);

        return PageResult.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentDTO dto, Long userId) {
        // 校验帖子是否存在
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 如果是回复评论，校验被回复的评论是否存在
        Long replyToId = dto.getReplyToId();
        Long replyToUserId = null;
        if (replyToId != null) {
            Comment replyToComment = commentMapper.selectById(replyToId);
            if (replyToComment == null) {
                throw new BusinessException(404, "被回复的评论不存在");
            }
            replyToUserId = replyToComment.getUserId();
        }

        // 防重复：60 秒内同用户同帖子同内容已存在则拒绝
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Comment> dupCheck = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        dupCheck.eq(Comment::getPostId, dto.getPostId())
                .eq(Comment::getUserId, userId)
                .eq(Comment::getContent, dto.getContent())
                .ge(Comment::getCreateTime, LocalDateTime.now().minusSeconds(60));
        if (commentMapper.selectCount(dupCheck) > 0) {
            throw new BusinessException("评论过于频繁，请勿重复发表");
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setReplyToId(replyToId);

        commentMapper.insert(comment);

        // 帖子评论数 +1
        postMapper.incrementCommentCount(dto.getPostId());

        com.dragonraja.forum.entity.User commenter = userMapper.selectById(userId);
        String senderName = commenter != null && commenter.getNickname() != null
            ? commenter.getNickname() : "匿名学员";

        // 如果是回复评论，通知被回复的用户
        if (replyToUserId != null && !replyToUserId.equals(userId)) {
            messageService.sendReplyNotification(replyToUserId, senderName, userId,
                "你的评论有新回复",
                senderName + " 回复了你的评论「" + post.getTitle() + "」",
                post.getId(), comment.getId());
        }
        // 如果是一级评论且不是帖主本人，通知帖主
        else if (replyToId == null && !post.getUserId().equals(userId)) {
            messageService.sendReplyNotification(post.getUserId(), senderName, userId,
                "你的帖子有新评论",
                senderName + " 评论了你的帖子「" + post.getTitle() + "」",
                post.getId(), comment.getId());
        }

        log.info("发表评论成功: commentId={}, postId={}, userId={}, replyToId={}", comment.getId(), dto.getPostId(), userId, replyToId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 仅评论者本人可删除（管理员可通过其他途径处理）
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人的评论");
        }

        commentMapper.deleteById(id);

        // 帖子评论数 -1
        postMapper.decrementCommentCount(comment.getPostId());

        log.info("删除评论成功: commentId={}, userId={}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        commentMapper.deleteById(id);
        postMapper.decrementCommentCount(comment.getPostId());
        log.info("管理员删除评论: commentId={}, operator={}", id, UserContext.getCurrentUsername());
    }
}
