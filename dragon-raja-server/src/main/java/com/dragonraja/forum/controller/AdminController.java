package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.AuditUserDTO;
import com.dragonraja.forum.dto.MessageDTO;
import com.dragonraja.forum.dto.NewsDTO;
import com.dragonraja.forum.dto.PostDTO;
import com.dragonraja.forum.dto.UpdateProfileDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.MessageService;
import com.dragonraja.forum.service.NewsService;
import com.dragonraja.forum.service.PostService;
import com.dragonraja.forum.service.UserService;
import com.dragonraja.forum.vo.NewsVO;
import com.dragonraja.forum.vo.PostListVO;
import com.dragonraja.forum.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器
 * 提供用户审核、封禁、帖子管理等管理员接口
 * 所有接口均需要管理员权限
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final NewsService newsService;
    private final MessageService messageService;

    /**
     * 校验当前用户是否为管理员
     */
    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("无管理员权限");
        }
    }

    /**
     * 校验当前用户是否有审核权限（ADMIN 或 AUDITOR）
     */
    private void checkCanAudit() {
        if (!UserContext.canAudit()) {
            throw BusinessException.forbidden("无审核权限");
        }
    }

    /**
     * 分页查询所有用户
     * 支持用户名/昵称关键词搜索
     *
     * @param current 当前页（默认1）
     * @param size    每页大小（默认10）
     * @param keyword 用户名/昵称关键词（可选）
     * @return 分页用户列表
     */
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        checkAdmin();
        PageResult<UserVO> pageResult = userService.getUserPage(current, size, keyword);
        return Result.success(pageResult);
    }

    /**
     * 审核用户
     * 通过(1)或拒绝(2)待审核用户的注册申请
     * ADMIN 和 AUDITOR 均可操作
     *
     * @param id  用户ID
     * @param dto 审核参数（status: 1-通过 2-拒绝）
     * @return 操作结果
     */
    @PutMapping("/users/{id}/audit")
    public Result<Void> auditUser(@PathVariable Long id, @Valid @RequestBody AuditUserDTO dto) {
        checkCanAudit();
        userService.auditUser(id, dto);
        return Result.success("审核完成");
    }

    /**
     * 获取待审核用户列表
     * ADMIN 和 AUDITOR 均可查看
     */
    @GetMapping("/users/pending")
    public Result<PageResult<UserVO>> getPendingUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        checkCanAudit();
        PageResult<UserVO> pageResult = userService.getPendingUserPage(current, size);
        return Result.success(pageResult);
    }

    /**
     * 封禁/解封用户
     * 切换用户封禁状态（正常→封禁，封禁→正常）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/users/{id}/ban")
    public Result<Void> toggleBan(@PathVariable Long id) {
        checkAdmin();
        userService.toggleBan(id);
        return Result.success("操作成功");
    }

    /**
     * 管理员删除用户
     * 同时清理该用户的帖子和评论（物理删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        checkAdmin();
        userService.deleteUser(id);
        return Result.success("用户已删除");
    }

    /**
     * 管理员更新用户信息
     * 可修改昵称、血统等级、派系、个性签名
     *
     * @param id  用户ID
     * @param dto 更新参数
     * @return 操作结果
     */
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateProfileDTO dto) {
        checkAdmin();
        userService.adminUpdateUser(id, dto);
        return Result.success("用户信息已更新");
    }

    /**
     * 分页查询所有帖子（管理员用）
     * 支持分类筛选和关键词搜索
     *
     * @param current  当前页（默认1）
     * @param size     每页大小（默认10）
     * @param category 分类（可选）
     * @param keyword  标题关键词（可选）
     * @return 分页帖子列表
     */
    @GetMapping("/posts")
    public Result<PageResult<PostListVO>> getPostList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword) {
        checkAdmin();
        PageResult<PostListVO> pageResult = postService.getPostPageForAdmin(current, size, category, keyword);
        return Result.success(pageResult);
    }

    /**
     * 管理员删除帖子
     * 管理员可强制删除任何帖子
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        checkAdmin();
        postService.adminDeletePost(id);
        return Result.success("删除成功");
    }

    /**
     * 管理员编辑帖子（不限作者）
     *
     * @param id  帖子ID
     * @param dto 帖子参数
     * @return 操作结果
     */
    @PutMapping("/posts/{id}")
    public Result<Void> updatePost(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        checkAdmin();
        postService.adminUpdatePost(id, dto);
        return Result.success("编辑成功");
    }

    // ========== 新闻管理 ==========

    @GetMapping("/news")
    public Result<PageResult<NewsVO>> getNewsList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        checkAdmin();
        return Result.success(newsService.getNewsPage(current, size));
    }

    @PostMapping("/news")
    public Result<Long> createNews(@Valid @RequestBody NewsDTO dto) {
        checkAdmin();
        return Result.success("新闻已发布", newsService.createNews(dto));
    }

    @PutMapping("/news/{id}")
    public Result<Void> updateNews(@PathVariable Long id, @Valid @RequestBody NewsDTO dto) {
        checkAdmin();
        newsService.updateNews(id, dto);
        return Result.success("新闻已更新");
    }

    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        checkAdmin();
        newsService.deleteNews(id);
        return Result.success("新闻已删除");
    }

    // ========== 消息群发 ==========

    @PostMapping("/message/broadcast")
    public Result<Void> broadcastMessage(@Valid @RequestBody MessageDTO dto) {
        checkAdmin();
        messageService.broadcastMessage(dto.getTitle(), dto.getContent());
        return Result.success("消息已群发至所有学员");
    }

    @PostMapping("/message/send")
    public Result<Void> sendMessage(@Valid @RequestBody MessageDTO dto) {
        checkAdmin();
        if (dto.getReceiverId() == null) {
            throw new BusinessException("请指定接收者ID");
        }
        messageService.sendMessage(1L, dto);
        return Result.success("消息已发送");
    }
}
