package com.library.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户更新DTO
 */
@Schema(description = "用户更新请求参数")
public class UserUpdateDTO {

    @Size(min = 3, max = 20, message = "用户名长度在 3 到 20 个字符")
    @Schema(description = "用户名", example = "reader001")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度在 6 到 20 个字符")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50字符")
    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18800000001")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "reader@lib.com")
    private String email;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String profilePicture;

    @Schema(description = "状态", example = "1")
    private Integer status;

    public UserUpdateDTO() {}

    // Getter and Setter methods
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
}