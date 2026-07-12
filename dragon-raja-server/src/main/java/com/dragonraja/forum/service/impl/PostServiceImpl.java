package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.PostDTO;
import com.dragonraja.forum.entity.Post;
import com.dragonraja.forum.entity.PostImage;
import com.dragonraja.forum.entity.PostView;
import com.dragonraja.forum.enums.PostCategory;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.PostImageMapper;
import com.dragonraja.forum.mapper.PostMapper;
import com.dragonraja.forum.mapper.PostViewMapper;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.PostService;
import com.dragonraja.forum.vo.PostListVO;
import com.dragonraja.forum.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 *
 * @author Kou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostViewMapper postViewMapper;

    @Override
    public PageResult<PostListVO> getPostPage(Long current, Long size, Integer category, String keyword) {
        Page<PostListVO> page = new Page<>(current, size);
        IPage<PostListVO> resultPage = postMapper.selectPostPage(page, category, keyword);

        resultPage.getRecords().forEach(this::fillCategoryDesc);
        fillImages(resultPage.getRecords());

        return PageResult.of(resultPage);
    }

    @Override
    public PageResult<PostListVO> getHotPostPage(Long current, Long size) {
        Page<PostListVO> page = new Page<>(current, size);
        IPage<PostListVO> resultPage = postMapper.selectHotPostPage(page);

        resultPage.getRecords().forEach(this::fillCategoryDesc);
        fillImages(resultPage.getRecords());

        return PageResult.of(resultPage);
    }

    @Override
    public PostVO getPostDetail(Long id) {
        // 查询帖子详情（关联用户信息）
        PostVO vo = postMapper.selectPostDetailById(id);
        if (vo == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 填充分类中文名
        vo.setCategoryDesc(PostCategory.getDescByCode(vo.getCategory()));

        // 浏览量 +1（每用户每帖子仅计一次）
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null) {
            LambdaQueryWrapper<PostView> viewWrapper = new LambdaQueryWrapper<>();
            viewWrapper.eq(PostView::getUserId, currentUserId).eq(PostView::getPostId, id);
            if (postViewMapper.selectCount(viewWrapper) == 0) {
                PostView pv = new PostView();
                pv.setUserId(currentUserId);
                pv.setPostId(id);
                pv.setCreateTime(java.time.LocalDateTime.now());
                postViewMapper.insert(pv);
                postMapper.incrementViewCount(id);
            }
        } else {
            postMapper.incrementViewCount(id);
        }

        // 加载帖子图片
        List<PostImage> postImages = postImageMapper.selectByPostId(id);
        if (!postImages.isEmpty()) {
            vo.setImages(postImages.stream().map(PostImage::getUrl).collect(java.util.stream.Collectors.toList()));
        }

        log.info("获取帖子详情: id={}, 浏览量+1", id);
        return vo;
    }

    @Override
    @Transactional
    public Long createPost(PostDTO dto, Long userId) {
        // 校验分类有效性
        if (PostCategory.fromCode(dto.getCategory()) == null) {
            throw new BusinessException("帖子分类无效，支持 1-闲聊 2-史料 3-委托 4-交换");
        }

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategory(dto.getCategory());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);

        postMapper.insert(post);

        // 保存帖子图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 0;
            for (String url : dto.getImages()) {
                PostImage img = new PostImage();
                img.setPostId(post.getId());
                img.setUrl(url);
                img.setSortOrder(order++);
                postImageMapper.insert(img);
            }
        }

        log.info("发布帖子成功: id={}, userId={}", post.getId(), userId);
        return post.getId();
    }

    @Override
    public void updatePost(Long id, PostDTO dto, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 仅作者本人可编辑
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑他人的帖子");
        }

        // 校验分类有效性
        if (PostCategory.fromCode(dto.getCategory()) == null) {
            throw new BusinessException("帖子分类无效，支持 1-闲聊 2-史料 3-委托 4-交换");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategory(dto.getCategory());

        postMapper.updateById(post);

        // 更新图片：先删旧图，再插入新图
        postImageMapper.deleteByPostId(id);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 0;
            for (String url : dto.getImages()) {
                PostImage img = new PostImage();
                img.setPostId(id);
                img.setUrl(url);
                img.setSortOrder(order++);
                postImageMapper.insert(img);
            }
        }

        log.info("编辑帖子成功: id={}, userId={}", id, userId);
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 仅作者本人可删除
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人的帖子");
        }

        postMapper.deleteById(id);
        log.info("删除帖子成功: id={}, userId={}", id, userId);
    }

    @Override
    public void toggleTop(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 切换置顶状态
        post.setIsTop(post.getIsTop() == 1 ? 0 : 1);
        postMapper.updateById(post);
        log.info("帖子置顶状态切换: id={}, isTop={}", id, post.getIsTop());
    }

    @Override
    public void adminDeletePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        postMapper.deleteById(id);
        log.info("管理员删除帖子: id={}, operator={}", id, UserContext.getCurrentUsername());
    }

    @Override
    public void adminUpdatePost(Long id, PostDTO dto) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        if (PostCategory.fromCode(dto.getCategory()) == null) {
            throw new BusinessException("帖子分类无效，支持 1-闲聊 2-史料 3-委托 4-交换");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategory(dto.getCategory());
        postMapper.updateById(post);

        // 更新图片
        postImageMapper.deleteByPostId(id);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 0;
            for (String url : dto.getImages()) {
                PostImage img = new PostImage();
                img.setPostId(id);
                img.setUrl(url);
                img.setSortOrder(order++);
                postImageMapper.insert(img);
            }
        }

        log.info("管理员编辑帖子: id={}, operator={}", id, UserContext.getCurrentUsername());
    }

    @Override
    public PageResult<PostListVO> getPostPageForAdmin(Long current, Long size, Integer category, String keyword) {
        // 管理员查询与普通查询逻辑一致
        return getPostPage(current, size, category, keyword);
    }

    /**
     * 填充帖子列表项的分类中文名
     *
     * @param vo 帖子列表项 VO
     */
    private void fillCategoryDesc(PostListVO vo) {
        if (vo.getCategory() != null) {
            vo.setCategoryDesc(PostCategory.getDescByCode(vo.getCategory()));
        }
    }

    @Override
    public PageResult<PostListVO> getUserPosts(Long userId, Long current, Long size) {
        Page<PostListVO> page = new Page<>(current, size);
        IPage<PostListVO> resultPage = postMapper.selectPostPageByUserId(page, userId);
        resultPage.getRecords().forEach(this::fillCategoryDesc);
        fillImages(resultPage.getRecords());
        return PageResult.of(resultPage);
    }

    /**
     * 批量填充帖子列表的图片URL
     */
    private void fillImages(List<PostListVO> vos) {
        if (vos == null || vos.isEmpty()) return;
        List<Long> postIds = vos.stream().map(PostListVO::getId).collect(Collectors.toList());
        List<PostImage> images = postImageMapper.selectByPostIds(postIds);
        if (images.isEmpty()) return;

        Map<Long, List<String>> imageMap = images.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId,
                        Collectors.mapping(PostImage::getUrl, Collectors.toList())));
        vos.forEach(vo -> {
            List<String> urls = imageMap.get(vo.getId());
            vo.setImages(urls != null ? urls : Collections.emptyList());
        });
    }
}
