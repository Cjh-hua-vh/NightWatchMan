package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.MessageDTO;
import com.dragonraja.forum.entity.Message;
import com.dragonraja.forum.vo.MessageVO;
import java.util.List;

public interface MessageService extends IService<Message> {
    PageResult<MessageVO> getUserMessages(Long userId, Long current, Long size);
    PageResult<MessageVO> getUserUnreadMessages(Long userId, Long current, Long size);

    /** 获取通知列表（不过滤 PRIVATE，供顶部铃铛弹窗使用） */
    PageResult<MessageVO> getNotifications(Long userId, Long current, Long size);
    long getUnreadCount(Long userId);
    void markAsRead(Long messageId, Long userId);
    void markAllAsRead(Long userId);

    /** 标记所有通知消息已读（不含私信） */
    void markNotificationsRead(Long userId);

    /** 清空当前用户所有消息通知（物理删除） */
    void clearAllMessages(Long userId);
    Long sendMessage(Long senderId, MessageDTO dto);
    void broadcastMessage(String title, String content);
    void deleteMessage(Long messageId, Long userId);

    /** 批量删除邮件 */
    void batchDeleteMessages(List<Long> ids, Long userId);

    /** 发送回复/评论通知 */
    void sendReplyNotification(Long receiverId, String senderName, Long senderId, String title, String content, Long postId, Long commentId);

    /** 获取发件箱（已发送邮件） */
    PageResult<MessageVO> getSentMessages(Long userId, Long current, Long size);

    /** 获取私信会话列表 */
    List<MessageVO> getSessionList(Long userId);

    /** 获取聊天记录 */
    List<MessageVO> getChatHistory(Long userId, Long targetId);

    /** 发送私信 */
    Long sendPrivateMessage(Long senderId, Long receiverId, String content);

    /** 标记会话已读 */
    void markSessionRead(Long userId, Long targetId);

    /** 删除私信会话（删除与指定用户的所有私信） */
    void deleteSession(Long userId, Long targetId);
}
