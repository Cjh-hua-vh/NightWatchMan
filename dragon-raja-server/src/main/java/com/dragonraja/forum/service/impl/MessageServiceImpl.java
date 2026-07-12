package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.MessageDTO;
import com.dragonraja.forum.entity.Message;
import com.dragonraja.forum.entity.MessageImage;
import com.dragonraja.forum.entity.User;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.MessageImageMapper;
import com.dragonraja.forum.mapper.MessageMapper;
import com.dragonraja.forum.mapper.UserMapper;
import com.dragonraja.forum.service.MessageService;
import com.dragonraja.forum.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final MessageImageMapper messageImageMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<MessageVO> getUserUnreadMessages(Long userId, Long current, Long size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .ne(Message::getType, "PRIVATE")
         .eq(Message::getIsRead, 0)
         .orderByDesc(Message::getCreateTime);
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PageResult<MessageVO> getNotifications(Long userId, Long current, Long size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .in(Message::getType, "SYSTEM", "REPLY", "PRIVATE", "EMAIL")
         .eq(Message::getIsRead, 0)
         .orderByDesc(Message::getCreateTime);
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PageResult<MessageVO> getUserMessages(Long userId, Long current, Long size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .ne(Message::getType, "PRIVATE")
         .orderByDesc(Message::getCreateTime);
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public PageResult<MessageVO> getSentMessages(Long userId, Long current, Long size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getSenderId, userId)
         .ne(Message::getType, "PRIVATE")
         .orderByDesc(Message::getCreateTime);
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .eq(Message::getIsRead, 0);
        return messageMapper.selectCount(w);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        LambdaUpdateWrapper<Message> w = new LambdaUpdateWrapper<>();
        w.eq(Message::getId, messageId).eq(Message::getReceiverId, userId).set(Message::getIsRead, 1);
        messageMapper.update(null, w);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Message> w = new LambdaUpdateWrapper<>();
        w.eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0).set(Message::getIsRead, 1);
        messageMapper.update(null, w);
    }

    @Override
    public void markNotificationsRead(Long userId) {
        LambdaUpdateWrapper<Message> w = new LambdaUpdateWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .eq(Message::getIsRead, 0)
         .set(Message::getIsRead, 1);
        messageMapper.update(null, w);
    }

    @Override
    public void clearAllMessages(Long userId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId).ne(Message::getType, "PRIVATE");
        int count = messageMapper.delete(w);
        log.info("用户 {} 清空了 {} 条消息通知（不含私信）", userId, count);
    }

    @Override
    @Transactional
    public Long sendMessage(Long senderId, MessageDTO dto) {
        User sender = userMapper.selectById(senderId);
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(dto.getReceiverId());
        msg.setTitle(dto.getTitle());
        msg.setContent(dto.getContent());
        msg.setSender(sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "诺玛");
        msg.setType("EMAIL");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        saveMessageImages(msg.getId(), dto.getImages());
        return msg.getId();
    }

    @Override
    public List<MessageVO> getSessionList(Long userId) {
        List<Message> messages = messageMapper.selectSessionList(userId);
        return messages.stream().map(msg -> {
            MessageVO vo = toVO(msg);
            Long targetId = (msg.getSenderId() != null && msg.getSenderId().equals(userId)) ? msg.getReceiverId() : msg.getSenderId();
            if (targetId != null) {
                vo.setUnreadCount(messageMapper.selectUnreadCountByTarget(userId, targetId));
                User targetUser = userMapper.selectById(targetId);
                if (targetUser != null) {
                    vo.setAvatar(targetUser.getAvatar());
                }
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MessageVO> getChatHistory(Long userId, Long targetId) {
        List<Message> messages = messageMapper.selectChatHistory(userId, targetId);
        markSessionRead(userId, targetId);
        return messages.stream().map(this::toVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public Long sendPrivateMessage(Long senderId, Long receiverId, String content) {
        User sender = userMapper.selectById(senderId);
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setTitle("");
        msg.setContent(content);
        msg.setSender(sender != null ? sender.getNickname() != null ? sender.getNickname() : sender.getUsername() : "用户");
        msg.setType("PRIVATE");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg.getId();
    }

    @Override
    public void markSessionRead(Long userId, Long targetId) {
        LambdaUpdateWrapper<Message> w = new LambdaUpdateWrapper<>();
        w.eq(Message::getReceiverId, userId)
         .eq(Message::getSenderId, targetId)
         .eq(Message::getIsRead, 0)
         .eq(Message::getType, "PRIVATE")
         .set(Message::getIsRead, 1);
        messageMapper.update(null, w);
    }

    @Override
    public void deleteSession(Long userId, Long targetId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getType, "PRIVATE")
         .and(wrapper -> wrapper
            .and(w1 -> w1.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetId))
            .or(w2 -> w2.eq(Message::getSenderId, targetId).eq(Message::getReceiverId, userId)));
        messageMapper.delete(w);
        log.info("用户 {} 删除了与用户 {} 的私信会话", userId, targetId);
    }

    @Override
    @Transactional
    public void broadcastMessage(String title, String content) {
        // 给所有正常状态的用户发送消息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, 1);
        java.util.List<User> users = userMapper.selectList(userWrapper);
        for (User u : users) {
            Message msg = new Message();
            msg.setReceiverId(u.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setSender("诺玛");
            msg.setSenderId(1L);
            msg.setType("SYSTEM");
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        }
        log.info("诺玛广播消息: {} (发送给 {} 人)", title, users.size());
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg == null || !msg.getReceiverId().equals(userId)) {
            throw new BusinessException(404, "消息不存在");
        }
        messageMapper.deleteById(messageId);
    }

    @Override
    public void batchDeleteMessages(List<Long> ids, Long userId) {
        // 只删除属于当前用户的消息
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.in(Message::getId, ids).eq(Message::getReceiverId, userId);
        messageMapper.delete(w);
        if (ids.size() == 1) log.info("用户 {} 删除了 1 条消息", userId);
        else log.info("用户 {} 批量删除了 {} 条消息", userId, ids.size());
    }

    @Override
    public void sendReplyNotification(Long receiverId, String senderName, Long senderId, String title, String content, Long postId, Long commentId) {
        Message msg = new Message();
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setSender(senderName);
        msg.setSenderId(senderId);
        msg.setType("REPLY");
        msg.setPostId(postId);
        msg.setCommentId(commentId);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
    }

    private void saveMessageImages(Long messageId, java.util.List<String> images) {
        if (images == null || images.isEmpty()) return;
        int order = 0;
        for (String url : images) {
            MessageImage img = new MessageImage();
            img.setMessageId(messageId);
            img.setUrl(url);
            img.setSortOrder(order++);
            messageImageMapper.insert(img);
        }
    }

    private MessageVO toVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setTitle(msg.getTitle());
        vo.setContent(msg.getContent());
        vo.setSender(msg.getSender());
        vo.setType(msg.getType());
        vo.setPostId(msg.getPostId());
        vo.setCommentId(msg.getCommentId());
        vo.setIsRead(msg.getIsRead());
        vo.setCreateTime(msg.getCreateTime() != null ? msg.getCreateTime().format(FMT) : null);
        if (msg.getSenderId() != null) {
            User sender = userMapper.selectById(msg.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
                vo.setAvatar(sender.getAvatar());
            }
        }
        if (msg.getReceiverId() != null) {
            User receiver = userMapper.selectById(msg.getReceiverId());
            if (receiver != null) {
                vo.setReceiverName(receiver.getNickname() != null ? receiver.getNickname() : receiver.getUsername());
            }
        }
        java.util.List<MessageImage> imgs = messageImageMapper.selectByMessageId(msg.getId());
        if (!imgs.isEmpty()) {
            vo.setImages(imgs.stream().map(MessageImage::getUrl).collect(java.util.stream.Collectors.toList()));
        }
        return vo;
    }
}
