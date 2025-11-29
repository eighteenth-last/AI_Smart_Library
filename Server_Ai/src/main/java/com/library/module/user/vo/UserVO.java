package com.library.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "reader001")
    private String username;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "角色", example = "reader")
    private String role;

    @Schema(description = "手机号", example = "18800000001")
    private String phone;

    @Schema(description = "邮箱", example = "reader@lib.com")
    private String email;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String profilePicture;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createdAt;

    public UserVO() {}

    // Getter and Setter methods
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}