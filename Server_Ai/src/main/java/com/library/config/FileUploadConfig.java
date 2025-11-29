package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 文件上传根目录
     */
    private String basePath;

    /**
     * 头像上传目录
     */
    private String profilePicturePath;

    /**
     * 图书封面上传目录
     */
    private String bookCoverPath;

    /**
     * 访问URL前缀
     */
    private String urlPrefix;
}
