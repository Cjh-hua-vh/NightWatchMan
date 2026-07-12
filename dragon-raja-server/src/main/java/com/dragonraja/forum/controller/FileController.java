package com.dragonraja.forum.controller;

import com.dragonraja.forum.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 * 提供头像等文件上传接口，文件存储在服务器本地 static/uploads 目录
 *
 * @author Kou
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    /** 上传文件存储根目录 */
    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    /** 头像子目录 */
    private static final String AVATAR_SUB_DIR = "avatars";
    /** 帖子图片子目录 */
    private static final String POSTS_SUB_DIR = "posts";
    private static final String MESSAGES_SUB_DIR = "messages";

    /** 允许的图片类型 */
    private static final String[] ALLOWED_TYPES = { "image/jpeg", "image/png", "image/gif", "image/webp" };

    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, AVATAR_SUB_DIR));
            Files.createDirectories(Paths.get(uploadDir, POSTS_SUB_DIR));
            Files.createDirectories(Paths.get(uploadDir, MESSAGES_SUB_DIR));
            log.info("上传目录已初始化: {}", Paths.get(uploadDir).toAbsolutePath());
        } catch (IOException e) {
            log.error("无法创建上传目录: {}", e.getMessage());
        }
    }

    /**
     * 上传头像
     * 接收 multipart/form-data 格式的图片文件
     *
     * @param file 上传的图片文件
     * @return 可访问的头像 URL
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 校验文件是否为空
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            return Result.error(400, "仅支持 JPG/PNG/GIF/WebP 格式的图片");
        }

        // 生成唯一文件名（UUID + 原始扩展名）
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            // 确保目录存在（使用绝对路径，避开相对路径解析问题）
            Path avatarDir = Paths.get(uploadDir, AVATAR_SUB_DIR).toAbsolutePath();
            Files.createDirectories(avatarDir);

            // 保存文件 — 用 Files.copy 显式写入，更可靠
            Path filePath = avatarDir.resolve(filename).toAbsolutePath();
            try (var in = file.getInputStream()) {
                Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // 返回可访问的 URL（Spring Boot 默认 /static/ 映射到 classpath:static/）
            String avatarUrl = "/uploads/avatars/" + filename;
            log.info("头像上传成功: {}, 大小: {} bytes, 写入路径: {}", avatarUrl, file.getSize(), filePath);

            return Result.success("上传成功", avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /** 上传帖子图片 */
    @PostMapping("/upload/post-image")
    public Result<String> uploadPostImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "请选择要上传的文件");
        String contentType = file.getContentType();
        boolean allowed = false;
        for (String t : ALLOWED_TYPES) { if (t.equals(contentType)) { allowed = true; break; } }
        if (!allowed) return Result.error(400, "仅支持 JPG/PNG/GIF/WebP 格式");

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf("."));
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadDir, POSTS_SUB_DIR).toAbsolutePath();
            Files.createDirectories(dir);
            Path filePath = dir.resolve(filename).toAbsolutePath();
            try (var in = file.getInputStream()) {
                Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            String url = "/uploads/posts/" + filename;
            log.info("帖子图片上传: {}, 写入路径: {}", url, filePath);
            return Result.success("上传成功", url);
        } catch (IOException e) {
            log.error("帖子图片上传失败: {}", e.getMessage(), e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /** 上传消息图片 */
    @PostMapping("/upload/message-image")
    public Result<String> uploadMessageImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "请选择要上传的文件");
        String contentType = file.getContentType();
        boolean allowed = false;
        for (String t : ALLOWED_TYPES) { if (t.equals(contentType)) { allowed = true; break; } }
        if (!allowed) return Result.error(400, "仅支持 JPG/PNG/GIF/WebP 格式");

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf("."));
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadDir, MESSAGES_SUB_DIR).toAbsolutePath();
            Files.createDirectories(dir);
            Path filePath = dir.resolve(filename).toAbsolutePath();
            try (var in = file.getInputStream()) {
                Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            String url = "/uploads/messages/" + filename;
            log.info("消息图片上传: {}, 写入路径: {}", url, filePath);
            return Result.success("上传成功", url);
        } catch (IOException e) {
            log.error("消息图片上传失败: {}", e.getMessage(), e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
