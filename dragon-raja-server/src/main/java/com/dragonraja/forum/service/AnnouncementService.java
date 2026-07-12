package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.AnnouncementDTO;
import com.dragonraja.forum.entity.Announcement;
import com.dragonraja.forum.vo.AnnouncementVO;

/**
 * 公告服务接口
 * 继承 MyBatis-Plus IService，提供通用 CRUD + 自定义业务方法
 *
 * @author Kou
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页公告列表
     */
    PageResult<AnnouncementVO> getAnnouncementPage(Long current, Long size);

    /**
     * 获取最新置顶公告（首页展示用）
     *
     * @return 最新公告 VO，无公告返回 null
     */
    AnnouncementVO getLatestAnnouncement();

    /**
     * 发布公告
     *
     * @param dto    公告参数
     * @param userId 发布者用户ID（管理员）
     * @return 新公告ID
     */
    Long createAnnouncement(AnnouncementDTO dto, Long userId);

    /**
     * 编辑公告
     *
     * @param id     公告ID
     * @param dto    公告参数
     */
    void updateAnnouncement(Long id, AnnouncementDTO dto);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void deleteAnnouncement(Long id);
}
