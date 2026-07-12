package com.dragonraja.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 * 允许前端域名跨域访问，允许所有请求头和方法
 *
 * @author Kou
 */
@Configuration
public class CorsConfig {

    /**
     * 注册 CORS 过滤器
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 显式列出允许的来源（包括本地开发与 cpolar 公网穿透域名）
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOriginPattern("https://6175b2a2.r5.cpolar.top");
        config.addAllowedOriginPattern("http://6175b2a2.r5.cpolar.top");
        config.addAllowedOriginPattern("https://6563181d.r5.cpolar.top");
        config.addAllowedOriginPattern("http://6563181d.r5.cpolar.top");
        config.addAllowedOriginPattern("https://*.cpolar.top");
        config.addAllowedOriginPattern("http://*.cpolar.top");
        config.addAllowedOriginPattern("https://*.cpolar.cn");
        config.addAllowedOriginPattern("http://*.cpolar.cn");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 预检请求有效期 3600 秒
        config.setMaxAge(3600L);
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
