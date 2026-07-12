package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.UpdateProfileDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.UserService;
import com.dragonraja.forum.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 提供个人资料管理、在线用户查询等接口
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户个人资料
     *
     * @return 用户信息 VO
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        UserVO vo = userService.getProfile(userId);
        return Result.success(vo);
    }

    /**
     * 更新个人资料
     * 仅更新传入的字段（昵称、头像、签名、血统等级、派系）
     *
     * @param dto 更新参数
     * @return 操作结果
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        userService.updateProfile(userId, dto);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     *
     * @param params 包含 oldPassword 和 newPassword
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new BusinessException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("新密码长度需6-20位");
        }

        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 获取在线用户列表（分页）
     * 公开接口，无需登录
     *
     * @param current 当前页（默认1）
     * @param size    每页大小（默认10）
     * @return 分页在线用户列表
     */
    @GetMapping("/online")
    public Result<PageResult<UserVO>> getOnlineUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<UserVO> pageResult = userService.getOnlineUsers(current, size);
        return Result.success(pageResult);
    }

    /**
     * 搜索用户（添加好友用）
     * 支持按ID或昵称搜索
     */
    @GetMapping("/search")
    public Result<UserVO> searchUser(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) String keyword) {
        UserVO vo;
        if (id != null) {
            vo = userService.getCurrentUserInfo(id);
        } else if (keyword != null && !keyword.isBlank()) {
            vo = userService.searchByKeyword(keyword);
        } else {
            throw new BusinessException("请提供搜索条件");
        }
        return Result.success(vo);
    }

    /** 公开获取指定用户资料（查看他人主页用） */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getCurrentUserInfo(id));
    }

    /** 心跳：维持在线状态 */
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat() {
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            userService.heartbeat(userId);
        }
        return Result.success("ok");
    }
}
