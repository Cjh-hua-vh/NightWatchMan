package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.MessageDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.MessageService;
import com.dragonraja.forum.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<MessageVO>> getMessages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(messageService.getUserUnreadMessages(userId, current, size));
    }

    /** 获取全部消息（包含已读），供邮件页面使用 */
    @GetMapping("/all")
    public Result<PageResult<MessageVO>> getAllMessages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(messageService.getUserMessages(userId, current, size));
    }

    /** 获取已发送邮件 */
    @GetMapping("/sent")
    public Result<PageResult<MessageVO>> getSentMessages(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(messageService.getSentMessages(userId, current, size));
    }

    @GetMapping("/unread")
    public Result<Map<String, Long>> getUnreadCount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return Result.success(Map.of("count", 0L));
        return Result.success(Map.of("count", messageService.getUnreadCount(userId)));
    }

    /** 通知弹窗列表（包含所有类型，不排除 PRIVATE） */
    @GetMapping("/notifications")
    public Result<PageResult<MessageVO>> getNotifications(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "5") Long size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(messageService.getNotifications(userId, current, size));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.markAsRead(id, userId);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.markAllAsRead(userId);
        return Result.success("全部标为已读");
    }

    /** 标记通知消息已读（不含私信） */
    @PutMapping("/read-notifications")
    public Result<Void> markNotificationsRead() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.markNotificationsRead(userId);
        return Result.success("ok");
    }

    /** 清空当前用户所有消息通知 */
    @DeleteMapping("/clear-all")
    public Result<Void> clearAllMessages() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.clearAllMessages(userId);
        return Result.success("已清空所有消息");
    }

    /**
     * 用户向好友发送消息
     */
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody MessageDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        if (dto.getReceiverId() == null) throw new BusinessException("请指定接收者");
        messageService.sendMessage(userId, dto);
        return Result.success("发送成功");
    }

    /**
     * 用户删除自己的邮件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.deleteMessage(id, userId);
        return Result.success("已删除");
    }

    /**
     * 批量删除邮件
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteMessages(@RequestBody List<Long> ids) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        if (ids == null || ids.isEmpty()) throw new BusinessException("请选择要删除的消息");
        messageService.batchDeleteMessages(ids, userId);
        return Result.success("已批量删除");
    }

    /**
     * 获取私信会话列表
     */
    @GetMapping("/chat/sessions")
    public Result<List<MessageVO>> getSessionList() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(messageService.getSessionList(userId));
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/chat/history")
    public Result<List<MessageVO>> getChatHistory(@RequestParam Long targetId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        if (targetId == null) throw new BusinessException("请指定聊天对象");
        return Result.success(messageService.getChatHistory(userId, targetId));
    }

    /**
     * 发送私信
     */
    @PostMapping("/chat/send")
    public Result<Long> sendPrivateMessage(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        
        Object receiverIdObj = body.get("receiverId");
        if (receiverIdObj == null) throw new BusinessException("请指定接收者");
        
        Long receiverId;
        if (receiverIdObj instanceof Number) {
            receiverId = ((Number) receiverIdObj).longValue();
        } else {
            try {
                receiverId = Long.parseLong(String.valueOf(receiverIdObj));
            } catch (NumberFormatException e) {
                throw new BusinessException("接收者ID格式不正确");
            }
        }
        
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) throw new BusinessException("消息内容不能为空");
        if (receiverId.equals(userId)) throw new BusinessException("不能给自己发私信");
        Long msgId = messageService.sendPrivateMessage(userId, receiverId, content.trim());
        return Result.success("发送成功", msgId);
    }

    /**
     * 删除私信会话
     */
    @DeleteMapping("/chat/session/{targetId}")
    public Result<Void> deleteSession(@PathVariable Long targetId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        messageService.deleteSession(userId, targetId);
        return Result.success("会话已删除");
    }
}
