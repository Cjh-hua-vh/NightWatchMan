package com.dragonraja.forum.service;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.vo.FriendVO;
import java.util.List;

public interface FriendService {
    void applyFriend(Long userId, Long friendId);
    void acceptFriend(Long userId, Long friendId);
    void rejectFriend(Long userId, Long friendId);
    void removeFriend(Long userId, Long friendId);
    List<FriendVO> getFriendList(Long userId);
    List<FriendVO> getPendingRequests(Long userId);
    List<FriendVO> getSentRequests(Long userId);
    void cancelRequest(Long userId, Long friendId);
    boolean isFriend(Long userId, Long targetId);
}
