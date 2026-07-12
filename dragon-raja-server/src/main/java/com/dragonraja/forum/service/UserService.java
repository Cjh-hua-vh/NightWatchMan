package com.dragonraja.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.AuditUserDTO;
import com.dragonraja.forum.dto.LoginDTO;
import com.dragonraja.forum.dto.RegisterDTO;
import com.dragonraja.forum.dto.UpdateProfileDTO;
import com.dragonraja.forum.entity.User;
import com.dragonraja.forum.vo.LoginVO;
import com.dragonraja.forum.vo.UserVO;

/**
 * 用户服务接口
 * 继承 MyBatis-Plus IService，提供通用 CRUD + 自定义业务方法
 *
 * @author Kou
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 检查用户名是否已存在，BCrypt 加密密码，设置状态为待审核
     *
     * @param dto 注册请求参数
     * @return 注册成功提示消息
     */
    String register(RegisterDTO dto);

    /**
     * 用户登录
     * 校验用户名密码，校验状态，生成 JWT Token，更新在线状态
     *
     * @param dto 登录请求参数
     * @return 登录返回 VO（含 Token）
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息 VO
     */
    UserVO getCurrentUserInfo(Long userId);

    /**
     * 获取个人资料
     *
     * @param userId 用户ID
     * @return 用户信息 VO
     */
    UserVO getProfile(Long userId);

    /**
     * 更新个人资料
     *
     * @param userId 用户ID
     * @param dto    更新参数
     */
    void updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取在线用户列表（分页）
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页用户列表
     */
    PageResult<UserVO> getOnlineUsers(Long current, Long size);

    /**
     * 分页查询所有用户（管理员用）
     *
     * @param current 当前页
     * @param size    每页大小
     * @param keyword 用户名/昵称关键词（可选）
     * @return 分页用户列表
     */
    PageResult<UserVO> getUserPage(Long current, Long size, String keyword);

    /**
     * 审核用户
     *
     * @param userId 用户ID
     * @param dto    审核参数
     */
    void auditUser(Long userId, AuditUserDTO dto);

    /**
     * 分页查询待审核用户（status=0）
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页待审核用户列表
     */
    PageResult<UserVO> getPendingUserPage(Long current, Long size);

    /**
     * 封禁/解封用户
     *
     * @param userId 用户ID
     */
    void toggleBan(Long userId);

    /**
     * 用户登出
     * 将用户在线状态设为离线（0）
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 心跳：更新用户最后活跃时间，同时标为在线
     */
    void heartbeat(Long userId);

    /**
     * 定时任务：清理超时用户（超过3分钟无心跳标离线）
     */
    void cleanupOfflineUsers();

    /**
     * 设置用户在线状态
     *
     * @param userId        用户ID
     * @param onlineStatus  在线状态（0-离线 1-在线）
     */
    void setOnlineStatus(Long userId, int onlineStatus);

    /**
     * 管理员删除用户
     * 物理删除用户记录（同时删除该用户的帖子、评论）
     *
     * @param userId 待删除用户ID
     */
    void deleteUser(Long userId);

    /**
     * 用户注销自己的账号
     * 物理删除用户记录及其帖子、评论
     *
     * @param userId 当前用户ID
     */
    void deleteOwnAccount(Long userId);

    /**
     * 管理员更新用户信息
     * 可修改昵称、血统等级、派系、个性签名、头像
     *
     * @param userId 目标用户ID
     * @param dto    更新参数
     */
    void adminUpdateUser(Long userId, UpdateProfileDTO dto);

    /**
     * 根据昵称或用户名搜索用户
     *
     * @param keyword 搜索关键词
     * @return 用户信息
     */
    UserVO searchByKeyword(String keyword);
}
