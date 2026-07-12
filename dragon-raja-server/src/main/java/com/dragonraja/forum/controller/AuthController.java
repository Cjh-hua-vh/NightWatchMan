package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.Result;
import com.dragonraja.forum.dto.LoginDTO;
import com.dragonraja.forum.dto.RegisterDTO;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.UserService;
import com.dragonraja.forum.vo.LoginVO;
import com.dragonraja.forum.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册、登录、获取当前用户信息等接口
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     * 注册成功后状态为待审核，需管理员审核通过后方可登录
     *
     * @param dto 注册参数（用户名、密码、昵称等）
     * @return 注册成功提示
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        String message = userService.register(dto);
        return Result.success(message);
    }

    /**
     * 用户登录
     * 校验成功后返回 JWT Token 和基本用户信息
     *
     * @param dto 登录参数（用户名、密码）
     * @return 登录返回 VO（含 Token）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success("登录成功", vo);
    }

    /**
     * 获取当前登录用户信息
     * 从 JWT Token 中解析当前用户ID，查询并返回用户信息
     *
     * @return 当前用户信息 VO
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        UserVO vo = userService.getCurrentUserInfo(userId);
        return Result.success(vo);
    }

    /**
     * 用户登出
     * 将当前用户的在线状态设为离线（0），前端应清除本地存储的 Token
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        userService.logout(userId);
        return Result.success("登出成功");
    }
}
