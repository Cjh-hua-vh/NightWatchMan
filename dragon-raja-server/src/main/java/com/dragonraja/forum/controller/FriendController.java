package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.FriendApplyDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.FriendService;
import com.dragonraja.forum.vo.FriendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/apply")
    public Result<Void> applyFriend(@Valid @RequestBody FriendApplyDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        friendService.applyFriend(userId, dto.getFriendId());
        return Result.success("好友申请已发送");
    }

    @PutMapping("/accept/{friendId}")
    public Result<Void> acceptFriend(@PathVariable Long friendId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        friendService.acceptFriend(userId, friendId);
        return Result.success("已接受好友申请");
    }

    @PutMapping("/reject/{friendId}")
    public Result<Void> rejectFriend(@PathVariable Long friendId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        friendService.rejectFriend(userId, friendId);
        return Result.success("已拒绝好友申请");
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> removeFriend(@PathVariable Long friendId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        friendService.removeFriend(userId, friendId);
        return Result.success("已删除好友");
    }

    @GetMapping("/list")
    public Result<List<FriendVO>> getFriendList() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(friendService.getFriendList(userId));
    }

    @GetMapping("/requests")
    public Result<List<FriendVO>> getPendingRequests() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(friendService.getPendingRequests(userId));
    }

    @GetMapping("/sent")
    public Result<List<FriendVO>> getSentRequests() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return Result.success(friendService.getSentRequests(userId));
    }

    @DeleteMapping("/sent/{friendId}")
    public Result<Void> cancelRequest(@PathVariable Long friendId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        friendService.cancelRequest(userId, friendId);
        return Result.success("好友申请已撤销");
    }

    @GetMapping("/check/{targetId}")
    public Result<java.util.Map<String, Boolean>> checkFriend(@PathVariable Long targetId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        boolean isFriend = friendService.isFriend(userId, targetId);
        return Result.success(java.util.Map.of("isFriend", isFriend));
    }
}
