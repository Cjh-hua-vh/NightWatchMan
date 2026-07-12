package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dragonraja.forum.entity.Friend;
import com.dragonraja.forum.entity.User;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.FriendMapper;
import com.dragonraja.forum.mapper.UserMapper;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.FriendService;
import com.dragonraja.forum.vo.FriendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void applyFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new BusinessException("不能添加自己为好友");
        if (userMapper.selectById(friendId) == null) throw new BusinessException(404, "用户不存在");
        // 检查是否已发送过申请
        LambdaQueryWrapper<Friend> check = new LambdaQueryWrapper<>();
        check.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        if (friendMapper.selectCount(check) > 0) throw new BusinessException("已发送过好友申请");

        Friend f = new Friend();
        f.setUserId(userId); f.setFriendId(friendId); f.setStatus(0);
        f.setCreateTime(LocalDateTime.now());
        f.setUpdateTime(LocalDateTime.now());
        friendMapper.insert(f);
        log.info("好友申请: userId={} -> friendId={}", userId, friendId);

        // 管理员添加好友 → 自动双向通过
        if (UserContext.isAdmin()) {
            f.setStatus(1);
            friendMapper.updateById(f);
            // 反向关系
            LambdaQueryWrapper<Friend> revCheck = new LambdaQueryWrapper<>();
            revCheck.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId);
            if (friendMapper.selectCount(revCheck) == 0) {
                Friend rev = new Friend();
                rev.setUserId(friendId); rev.setFriendId(userId); rev.setStatus(1);
                rev.setCreateTime(LocalDateTime.now()); rev.setUpdateTime(LocalDateTime.now());
                friendMapper.insert(rev);
            }
            log.info("管理员添加好友(双向): admin={} -> user={}", userId, friendId);
        }
    }

    @Override
    @Transactional
    public void acceptFriend(Long userId, Long friendId) {
        Friend f = findPending(userId, friendId);
        f.setStatus(1);
        friendMapper.updateById(f);
        // 反向也建立好友关系（检查是否已有反向记录）
        LambdaQueryWrapper<Friend> reverseCheck = new LambdaQueryWrapper<>();
        reverseCheck.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend existingReverse = friendMapper.selectOne(reverseCheck);
        if (existingReverse != null) {
            existingReverse.setStatus(1);
            existingReverse.setUpdateTime(LocalDateTime.now());
            friendMapper.updateById(existingReverse);
        } else {
            Friend reverse = new Friend();
            reverse.setUserId(userId); reverse.setFriendId(friendId); reverse.setStatus(1);
            reverse.setCreateTime(LocalDateTime.now());
            reverse.setUpdateTime(LocalDateTime.now());
            friendMapper.insert(reverse);
        }
    }

    @Override
    @Transactional
    public void rejectFriend(Long userId, Long friendId) {
        Friend f = findPending(userId, friendId);
        f.setStatus(2);
        friendMapper.updateById(f);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> w1 = new LambdaQueryWrapper<>();
        w1.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId).eq(Friend::getStatus, 1);
        friendMapper.delete(w1);
        LambdaQueryWrapper<Friend> w2 = new LambdaQueryWrapper<>();
        w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId).eq(Friend::getStatus, 1);
        friendMapper.delete(w2);
    }

    @Override
    public List<FriendVO> getFriendList(Long userId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, userId).eq(Friend::getStatus, 1);
        List<Friend> list = friendMapper.selectList(w);
        return toVOList(list, true);
    }

    @Override
    public List<FriendVO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getFriendId, userId).eq(Friend::getStatus, 0);
        List<Friend> list = friendMapper.selectList(w);
        return toVOList(list, false);
    }

    @Override
    public void cancelRequest(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId).eq(Friend::getStatus, 0);
        if (friendMapper.delete(w) == 0) {
            throw new BusinessException(404, "未找到该好友申请");
        }
    }

    @Override
    public List<FriendVO> getSentRequests(Long userId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, userId).eq(Friend::getStatus, 0);
        List<Friend> list = friendMapper.selectList(w);
        return toVOList(list, true);
    }

    private Friend findPending(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId).eq(Friend::getStatus, 0);
        Friend f = friendMapper.selectOne(w);
        if (f == null) throw new BusinessException(404, "未找到该好友申请");
        return f;
    }

    private List<FriendVO> toVOList(List<Friend> list, boolean isFriend) {
        List<FriendVO> result = new ArrayList<>();
        for (Friend f : list) {
            Long targetId = isFriend ? f.getFriendId() : f.getUserId();
            User u = userMapper.selectById(targetId);
            if (u == null) continue;
            FriendVO vo = new FriendVO();
            vo.setId(f.getId());
            vo.setFriendId(targetId);
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setBloodlineGrade(u.getBloodlineGrade());
            vo.setFaction(u.getFaction());
            vo.setSignature(u.getSignature());
            vo.setYanling(u.getYanling());
            vo.setOnlineStatus(u.getOnlineStatus());
            vo.setStatus(f.getStatus());
            vo.setCreateTime(f.getCreateTime() != null ? f.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean isFriend(Long userId, Long targetId) {
        LambdaQueryWrapper<Friend> w = new LambdaQueryWrapper<>();
        w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, targetId).eq(Friend::getStatus, 1);
        return friendMapper.selectCount(w) > 0;
    }
}
