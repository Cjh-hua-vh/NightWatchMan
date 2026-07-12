package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.AnnouncementDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.AnnouncementService;
import com.dragonraja.forum.vo.AnnouncementVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 * 提供公告列表查询、最新公告、发布/编辑/删除公告等接口
 * 发布/编辑/删除操作仅管理员可用
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询公告列表
     * 公开接口，无需登录
     *
     * @param current 当前页（默认1）
     * @param size    每页大小（默认10）
     * @return 分页公告列表
     */
    @GetMapping
    public Result<PageResult<AnnouncementVO>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<AnnouncementVO> pageResult = announcementService.getAnnouncementPage(current, size);
        return Result.success(pageResult);
    }

    /**
     * 获取最新置顶公告
     * 用于首页横幅展示，公开接口
     *
     * @return 最新公告 VO，无公告时 data 为 null
     */
    @GetMapping("/latest")
    public Result<AnnouncementVO> getLatestAnnouncement() {
        AnnouncementVO vo = announcementService.getLatestAnnouncement();
        return Result.success(vo);
    }

    /**
     * 发布公告
     * 仅管理员可操作
     *
     * @param dto 公告参数（标题、内容、是否置顶）
     * @return 新公告ID
     */
    @PostMapping
    public Result<Long> createAnnouncement(@Valid @RequestBody AnnouncementDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("仅管理员可发布公告");
        }
        Long announcementId = announcementService.createAnnouncement(dto, userId);
        return Result.success("发布成功", announcementId);
    }

    /**
     * 编辑公告
     * 仅管理员可操作
     *
     * @param id  公告ID
     * @param dto 公告参数
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO dto) {
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("仅管理员可编辑公告");
        }
        announcementService.updateAnnouncement(id, dto);
        return Result.success("编辑成功");
    }

    /**
     * 删除公告
     * 仅管理员可操作
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw BusinessException.forbidden("仅管理员可删除公告");
        }
        announcementService.deleteAnnouncement(id);
        return Result.success("删除成功");
    }
}
