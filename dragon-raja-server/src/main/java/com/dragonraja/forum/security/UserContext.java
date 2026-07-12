package com.dragonraja.forum.security;

/**
 * 线程上下文 - 当前登录用户
 * 使用 ThreadLocal 存储从 JWT Token 中解析出的用户信息
 * 由 JwtAuthenticationFilter 写入，由 Controller/Service 读取
 * 请求结束后由 JwtAuthenticationFilter 清理
 *
 * @author Kou
 */
public class UserContext {

    /** ThreadLocal 存储 CurrentUser 对象 */
    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     */
    public static void set(Long userId, String username, String role) {
        CONTEXT.set(new CurrentUser(userId, username, role));
    }

    /**
     * 获取当前用户完整信息
     *
     * @return CurrentUser，未登录时返回 null
     */
    public static CurrentUser get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        CurrentUser user = CONTEXT.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录时返回 null
     */
    public static String getCurrentUsername() {
        CurrentUser user = CONTEXT.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     *
     * @return 角色，未登录时返回 null
     */
    public static String getCurrentRole() {
        CurrentUser user = CONTEXT.get();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return true 是管理员，false 不是
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否为审核员（可审核注册申请）
     *
     * @return true 可以审核，false 不可以
     */
    public static boolean canAudit() {
        String role = getCurrentRole();
        return "ADMIN".equals(role) || "AUDITOR".equals(role);
    }

    /**
     * 判断当前是否已登录
     *
     * @return true 已登录，false 未登录
     */
    public static boolean isLoggedIn() {
        return CONTEXT.get() != null;
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏
     * 必须在请求结束后调用
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 当前用户信息内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CurrentUser {
        /** 用户ID */
        private Long userId;
        /** 用户名 */
        private String username;
        /** 角色（USER/ADMIN） */
        private String role;
    }
}
