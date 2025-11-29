package com.library.module.user.controller;

import com.library.common.result.Result;
import com.library.module.user.dto.UserLoginDTO;
import com.library.module.user.dto.UserRegisterDTO;
import com.library.module.user.service.UserService;
import com.library.module.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "用户注册、登录、退出")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册", description = "支持读者和作者角色注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody @Validated UserRegisterDTO registerDTO) {
        LoginVO loginVO = userService.register(registerDTO);
        return Result.success("注册成功", loginVO);
    }

    @Operation(summary = "用户登录", description = "用户登录，返回 Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "用户退出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("退出成功", null);
    }
}