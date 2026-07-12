package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dragonraja.forum.entity.Post;
import com.dragonraja.forum.vo.PostListVO;
import com.dragonraja.forum.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供通用 CRUD
 * 额外提供关联查询用户信息的自定义 SQL
 *
 * @author Kou
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表（关联用户信息）
     * 支持分类筛选和标题关键词搜索，置顶帖子优先
     *
     * @param page     分页对象
     * @param category 分类（null 查全部）
     * @param keyword  标题关键词（null 不搜索）
     * @return 分页结果（PostListVO 列表）
     */
    IPage<PostListVO> selectPostPage(IPage<PostListVO> page,
                                     @Param("category") Integer category,
                                     @Param("keyword") String keyword);

    /**
     * 分页查询热门帖子（按浏览量+评论数排序）
     *
     * @param page 分页对象
     * @return 分页结果（PostListVO 列表）
     */
    IPage<PostListVO> selectHotPostPage(IPage<PostListVO> page);

    /**
     * 分页查询指定用户的帖子
     */
    IPage<PostListVO> selectPostPageByUserId(IPage<PostListVO> page, @Param("userId") Long userId);

    /**
     * 查询帖子详情（关联用户信息）
     *
     * @param id 帖子ID
     * @return 帖子详情 VO
     */
    PostVO selectPostDetailById(@Param("id") Long id);

    /**
     * 帖子浏览量 +1
     *
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 帖子评论数 +1
     *
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 帖子评论数 -1
     *
     * @param id 帖子ID
     * @return 影响行数
     */
    int decrementCommentCount(@Param("id") Long id);
}
