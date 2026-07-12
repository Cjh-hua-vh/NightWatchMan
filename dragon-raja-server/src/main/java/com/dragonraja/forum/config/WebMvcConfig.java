package com.dragonraja.forum.config;

import com.dragonraja.forum.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册 JWT 认证过滤器 + 静态资源映射
 *
 * @author Kou
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 上传文件存储路径（从 application.yml 注入） */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 配置静态资源映射，将 /uploads/** 映射到外部上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    /**
     * 注册 JWT 认证过滤器，拦截所有请求
     *
     * @param jwtAuthenticationFilter JWT 过滤器
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        // 拦截所有请求
        registration.addUrlPatterns("/*");
        registration.setName("jwtAuthenticationFilter");
        // 过滤器执行顺序，越小越先执行
        registration.setOrder(1);
        return registration;
    }
}
