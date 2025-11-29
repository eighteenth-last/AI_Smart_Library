package com.library.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录返回VO
 */
@Schema(description = "登录返回信息")
public class LoginVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "reader001")
    private String username;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "角色", example = "reader")
    private String role;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号", example = "18800000001")
    private String phone;

    @Schema(description = "头像地址", example = "/uploads/Profile_picture/avatar.jpg")
    private String profilePicture;

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "过期时间（秒）", example = "2592000")
    private Long expiresIn;

    public LoginVO() {}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}