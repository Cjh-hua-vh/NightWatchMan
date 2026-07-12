package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.PostDTO;
import com.dragonraja.forum.entity.Post;
import com.dragonraja.forum.vo.PostListVO;
import com.dragonraja.forum.vo.PostVO;

/**
 * 帖子服务接口
 * 继承 MyBatis-Plus IService，提供通用 CRUD + 自定义业务方法
 *
 * @author Kou
 */
public interface PostService extends IService<Post> {

    /**
     * 分页查询帖子列表
     * 支持分类筛选和标题关键词搜索，置顶帖子优先
     *
     * @param current  当前页
     * @param size     每页大小
     * @param category 分类（null 查全部）
     * @param keyword  标题关键词（null 不搜索）
     * @return 分页帖子列表
     */
    PageResult<PostListVO> getPostPage(Long current, Long size, Integer category, String keyword);

    /**
     * 分页查询热门帖子
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页热门帖子列表
     */
    PageResult<PostListVO> getHotPostPage(Long current, Long size);

    /**
     * 获取帖子详情（浏览量 +1）
     *
     * @param id 帖子ID
     * @return 帖子详情 VO
     */
    PostVO getPostDetail(Long id);

    /**
     * 发布帖子
     *
     * @param dto    帖子参数
     * @param userId 发布者用户ID
     * @return 新帖子ID
     */
    Long createPost(PostDTO dto, Long userId);

    /**
     * 编辑帖子（仅作者本人可编辑）
     *
     * @param id     帖子ID
     * @param dto    帖子参数
     * @param userId 当前用户ID
     */
    void updatePost(Long id, PostDTO dto, Long userId);

    /**
     * 删除帖子（仅作者本人可删除）
     *
     * @param id     帖子ID
     * @param userId 当前用户ID
     */
    void deletePost(Long id, Long userId);

    /**
     * 置顶/取消置顶帖子（管理员操作）
     *
     * @param id 帖子ID
     */
    void toggleTop(Long id);

    /**
     * 管理员删除帖子
     *
     * @param id 帖子ID
     */
    void adminDeletePost(Long id);

    /**
     * 管理员编辑帖子（不限作者）
     *
     * @param id  帖子ID
     * @param dto 帖子参数
     */
    void adminUpdatePost(Long id, PostDTO dto);

    /**
     * 分页查询所有帖子（管理员用）
     *
     * @param current  当前页
     * @param size     每页大小
     * @param category 分类（可选）
     * @param keyword  标题关键词（可选）
     * @return 分页帖子列表
     */
    PageResult<PostListVO> getPostPageForAdmin(Long current, Long size, Integer category, String keyword);

    /** 获取某用户发布的帖子 */
    PageResult<PostListVO> getUserPosts(Long userId, Long current, Long size);
}
