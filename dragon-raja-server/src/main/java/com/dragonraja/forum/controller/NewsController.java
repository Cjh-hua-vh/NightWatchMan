package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.service.NewsService;
import com.dragonraja.forum.vo.NewsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public Result<PageResult<NewsVO>> getNewsList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(newsService.getNewsPage(current, size));
    }

    @GetMapping("/{id}")
    public Result<NewsVO> getNewsDetail(@PathVariable Long id) {
        return Result.success(newsService.getNewsDetail(id));
    }
}
