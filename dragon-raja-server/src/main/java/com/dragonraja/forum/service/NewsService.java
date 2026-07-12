package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.NewsDTO;
import com.dragonraja.forum.entity.News;
import com.dragonraja.forum.vo.NewsVO;

public interface NewsService extends IService<News> {
    PageResult<NewsVO> getNewsPage(Long current, Long size);
    NewsVO getNewsDetail(Long id);
    Long createNews(NewsDTO dto);
    void updateNews(Long id, NewsDTO dto);
    void deleteNews(Long id);
}
