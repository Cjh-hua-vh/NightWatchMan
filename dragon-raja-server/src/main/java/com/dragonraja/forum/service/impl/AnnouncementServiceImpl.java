package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.AnnouncementDTO;
import com.dragonraja.forum.entity.Announcement;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.AnnouncementMapper;
import com.dragonraja.forum.service.AnnouncementService;
import com.dragonraja.forum.vo.AnnouncementVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 公告服务实现类
 *
 * @author Kou
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    /** 日期格式化 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<AnnouncementVO> getAnnouncementPage(Long current, Long size) {
        // 置顶优先，再按创建时间倒序
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getIsTop)
               .orderByDesc(Announcement::getCreateTime);

        Page<Announcement> page = new Page<>(current, size);
        Page<Announcement> resultPage = this.page(page, wrapper);

        // 转换为 VO
        Page<AnnouncementVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public AnnouncementVO getLatestAnnouncement() {
        // 查询最新的一条置顶公告，如果没有则查最新一条公告
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime)
                .last("LIMIT 1");

        Announcement announcement = this.getOne(wrapper);
        return announcement != null ? convertToVO(announcement) : null;
    }

    @Override
    public Long createAnnouncement(AnnouncementDTO dto, Long userId) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 1);
        announcement.setUserId(userId);

        this.save(announcement);
        log.info("发布公告成功: id={}, userId={}", announcement.getId(), userId);
        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementDTO dto) {
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        if (dto.getIsTop() != null) {
            announcement.setIsTop(dto.getIsTop());
        }

        this.updateById(announcement);
        log.info("编辑公告成功: id={}", id);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }

        this.removeById(id);
        log.info("删除公告成功: id={}", id);
    }

    /**
     * 将 Announcement 实体转换为 AnnouncementVO
     *
     * @param announcement 公告实体
     * @return AnnouncementVO
     */
    private AnnouncementVO convertToVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(announcement.getId());
        vo.setTitle(announcement.getTitle());
        vo.setContent(announcement.getContent());
        vo.setIsTop(announcement.getIsTop());
        vo.setCreateTime(announcement.getCreateTime() != null ? announcement.getCreateTime().format(DATE_FORMATTER) : null);
        vo.setUpdateTime(announcement.getUpdateTime() != null ? announcement.getUpdateTime().format(DATE_FORMATTER) : null);
        return vo;
    }
}
