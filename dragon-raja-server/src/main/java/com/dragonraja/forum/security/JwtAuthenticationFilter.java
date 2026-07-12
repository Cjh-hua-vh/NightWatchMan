package com.dragonraja.forum.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 * 继承 OncePerRequestFilter，每次请求只执行一次
 * 职责：
 * 1. 判断请求路径是否在白名单中，白名单直接放行
 * 2. 从 Authorization 请求头提取 Bearer Token
 * 3. 校验 Token 有效性，解析用户信息
 * 4. 将用户信息存入 UserContext（ThreadLocal）
 * 5. 请求结束后清理 UserContext，防止内存泄漏
 *
 * @author Kou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    /** 路径匹配器（支持 Ant 风格通配符） */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** JWT 白名单路径（无需鉴权即可访问） */
    private static final List<String> WHITELIST = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/posts",
            "/api/posts/hot",
            "/api/posts/*",
            "/api/comments/post/*",
            "/api/announcements",
            "/api/announcements/latest",
            "/api/user/online",
            "/api/user/search",
            "/api/news",
            "/api/news/*",
            "/uploads/*",
            "/uploads/**",
            "/api/blood-test/submit",
            "/api/blood-test/exam",
            "/api/blood-test/skip"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();

            // 白名单路径放行，但仍尝试解析 Token（让已登录用户的 UserContext 可用）
            if (isWhitelisted(path, method)) {
                String token = extractToken(request);
                if (token != null && jwtUtils.isValid(token)) {
                    Claims claims = jwtUtils.parseToken(token);
                    UserContext.set(Long.parseLong(claims.getSubject()),
                            claims.get("username", String.class),
                            claims.get("role", String.class));
                }
                filterChain.doFilter(request, response);
                return;
            }

            // 从请求头获取 Token
            String token = extractToken(request);

            if (token != null && jwtUtils.isValid(token)) {
                // 解析 Token 并存入 ThreadLocal
                Claims claims = jwtUtils.parseToken(token);
                Long userId = Long.parseLong(claims.getSubject());
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);
                UserContext.set(userId, username, role);
            }
            // Token 不存在或无效时不报错，后续 Controller 中需要登录的操作会通过 UserContext 判断

            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后务必清理 ThreadLocal，防止内存泄漏
            UserContext.clear();
        }
    }

    /**
     * 判断请求路径是否在白名单中
     * 注意：POST /api/posts 需要鉴权，但 GET /api/posts 是白名单
     * 因此白名单中的路径需结合 HTTP 方法判断
     *
     * @param path   请求路径
     * @param method HTTP 方法
     * @return true 在白名单中，false 需要鉴权
     */
    private boolean isWhitelisted(String path, String method) {
        // GET 请求才走白名单（POST /api/posts 需鉴权）
        if ("GET".equalsIgnoreCase(method)) {
            for (String pattern : WHITELIST) {
                if (PATH_MATCHER.match(pattern, path)) {
                    return true;
                }
            }
        }
        // POST /api/auth/register 和 POST /api/auth/login 始终白名单
        if ("POST".equalsIgnoreCase(method)) {
            return "/api/auth/register".equals(path) || "/api/auth/login".equals(path);
        }
        return false;
    }

    /**
     * 从 Authorization 请求头提取 Bearer Token
     *
     * @param request HTTP 请求
     * @return Token 字符串，不存在则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
