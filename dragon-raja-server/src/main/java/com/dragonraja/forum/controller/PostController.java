package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.PostDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.PostService;
import com.dragonraja.forum.vo.PostListVO;
import com.dragonraja.forum.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 * 提供帖子 CRUD、热门帖子、置顶等接口
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 分页查询帖子列表
     * 支持分类筛选和标题关键词搜索，置顶帖子优先
     *
     * @param current  当前页（默认1）
     * @param size     每页大小（默认10）
     * @param category 分类（可选）：1-闲聊 2-史料 3-委托 4-交换
     * @param keyword  标题关键词（可选）
     * @return 分页帖子列表
     */
    @GetMapping
    public Result<PageResult<PostListVO>> getPostList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword) {
        PageResult<PostListVO> pageResult = postService.getPostPage(current, size, category, keyword);
        return Result.success(pageResult);
    }

    /**
     * 获取热门帖子列表
     * 按浏览量+评论数加权排序
     *
     * @param current 当前页（默认1）
     * @param size    每页大小（默认10）
     * @return 分页热门帖子列表
     */
    @GetMapping("/hot")
    public Result<PageResult<PostListVO>> getHotPosts(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<PostListVO> pageResult = postService.getHotPostPage(current, size);
        return Result.success(pageResult);
    }

    /**
     * 获取帖子详情
     * 每次访问浏览量 +1
     *
     * @param id 帖子ID
     * @return 帖子详情 VO
     */
    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        PostVO vo = postService.getPostDetail(id);
        return Result.success(vo);
    }

    /**
     * 发布帖子
     *
     * @param dto 帖子参数（标题、内容、分类）
     * @return 新帖子ID
     */
    @PostMapping
    public Result<Long> createPost(@Valid @RequestBody PostDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        Long postId = postService.createPost(dto, userId);
        return Result.success("发布成功", postId);
    }

    /**
     * 编辑帖子
     * 仅作者本人可编辑
     *
     * @param id  帖子ID
     * @param dto 帖子参数
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> updatePost(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        postService.updatePost(id, dto, userId);
        return Result.success("编辑成功");
    }

    /**
     * 删除帖子
     * 仅作者本人可删除
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        postService.deletePost(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 置顶/取消置顶帖子
     * 仅管理员可操作
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("仅管理员可置顶帖子");
        }
        postService.toggleTop(id);
        return Result.success("操作成功");
    }

    /** 获取某用户发布的帖子 */
    @GetMapping("/user/{userId}")
    public Result<PageResult<PostListVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(postService.getUserPosts(userId, current, size));
    }
}
