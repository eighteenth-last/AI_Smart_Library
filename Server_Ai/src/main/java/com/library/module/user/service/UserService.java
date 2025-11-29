package com.library.module.user.service;

import com.library.common.result.PageResult;
import com.library.module.user.dto.UserLoginDTO;
import com.library.module.user.dto.UserRegisterDTO;
import com.library.module.user.dto.UserUpdateDTO;
import com.library.module.user.vo.LoginVO;
import com.library.module.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    LoginVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO loginDTO);

    /**
     * 用户退出
     */
    void logout();

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();

    /**
     * 修改个人信息
     */
    void updateProfile(UserUpdateDTO updateDTO);

    /**
     * 更新用户头像
     */
    void updateAvatar(String avatarUrl);

    /**
     * 管理员：获取用户列表
     */
    PageResult<UserVO> getUserList(String keyword, String role, Integer status, Long page, Long size);

    /**
     * 管理员：新增用户
     */
    void addUser(UserRegisterDTO registerDTO);

    /**
     * 管理员：编辑用户
     */
    void updateUser(Long userId, UserUpdateDTO updateDTO);

    /**
     * 管理员：删除用户
     */
    void deleteUser(Long userId);
}