package com.library.module.file.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.library.common.result.Result;
import com.library.config.FileUploadConfig;
import com.library.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传Controller
 */
@Slf4j
@Tag(name = "文件上传", description = "文件上传管理")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadConfig fileUploadConfig;
    private final UserService userService;

    @Operation(summary = "上传头像", description = "上传用户头像图片")
    @PostMapping("/avatar")
    @SaCheckLogin
    public Result<Map<String, String>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(fileUploadConfig.getProfilePicturePath());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(fileUploadConfig.getProfilePicturePath(), filename);
            Files.write(filePath, file.getBytes());

            // 构建访问URL
            String fileUrl = fileUploadConfig.getUrlPrefix() + "/Profile_picture/" + filename;

            // 更新用户头像
            userService.updateAvatar(fileUrl);

            log.info("头像上传成功: {}", fileUrl);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);

            return Result.success("上传成功", result);

        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传图书封面", description = "上传图书封面图片")
    @PostMapping("/book-cover")
    @SaCheckLogin
    public Result<Map<String, String>> uploadBookCover(
            @Parameter(description = "封面文件") @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 验证文件大小（20MB）
        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("图片大小不能超过20MB");
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(fileUploadConfig.getBookCoverPath());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(fileUploadConfig.getBookCoverPath(), filename);
            Files.write(filePath, file.getBytes());

            // 构建URL
            String fileUrl = fileUploadConfig.getUrlPrefix() + "/Books_img/" + filename;

            log.info("图书封面上传成功: {}", fileUrl);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);

            return Result.success("上传成功", result);

        } catch (IOException e) {
            log.error("图书封面上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
