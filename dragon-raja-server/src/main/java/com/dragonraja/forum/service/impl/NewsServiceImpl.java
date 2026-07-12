package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.NewsDTO;
import com.dragonraja.forum.entity.News;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.NewsMapper;
import com.dragonraja.forum.service.NewsService;
import com.dragonraja.forum.vo.NewsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    private final NewsMapper newsMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<NewsVO> getNewsPage(Long current, Long size) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(News::getIsTop).orderByDesc(News::getCreateTime);
        Page<News> page = newsMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public NewsVO getNewsDetail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) throw new BusinessException(404, "新闻不存在");
        return toVO(news);
    }

    @Override
    public Long createNews(NewsDTO dto) {
        News news = new News();
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setCover(dto.getCover());
        news.setSummary(dto.getSummary());
        news.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        news.setAuthor("诺玛");
        news.setCreateTime(LocalDateTime.now());
        news.setUpdateTime(LocalDateTime.now());
        newsMapper.insert(news);
        return news.getId();
    }

    @Override
    public void updateNews(Long id, NewsDTO dto) {
        News news = newsMapper.selectById(id);
        if (news == null) throw new BusinessException(404, "新闻不存在");
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setCover(dto.getCover());
        news.setSummary(dto.getSummary());
        news.setIsTop(dto.getIsTop());
        newsMapper.updateById(news);
    }

    @Override
    public void deleteNews(Long id) {
        if (newsMapper.selectById(id) == null) throw new BusinessException(404, "新闻不存在");
        newsMapper.deleteById(id);
    }

    private NewsVO toVO(News news) {
        NewsVO vo = new NewsVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setContent(news.getContent());
        vo.setCover(news.getCover());
        vo.setSummary(news.getSummary());
        vo.setAuthor(news.getAuthor());
        vo.setIsTop(news.getIsTop());
        vo.setCreateTime(news.getCreateTime() != null ? news.getCreateTime().format(FMT) : null);
        vo.setUpdateTime(news.getUpdateTime() != null ? news.getUpdateTime().format(FMT) : null);
        return vo;
    }
}
