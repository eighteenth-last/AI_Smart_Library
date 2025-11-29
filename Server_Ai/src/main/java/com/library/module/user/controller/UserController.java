package com.library.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.user.dto.UserRegisterDTO;
import com.library.module.user.dto.UserUpdateDTO;
import com.library.module.user.service.UserService;
import com.library.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户个人信息管理")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/profile")
    @SaCheckLogin
    public Result<UserVO> getProfile() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    @Operation(summary = "修改个人信息", description = "修改当前用户的个人信息")
    @PutMapping("/profile")
    @SaCheckLogin
    public Result<UserVO> updateProfile(@RequestBody @Validated UserUpdateDTO updateDTO) {
        userService.updateProfile(updateDTO);
        UserVO updatedUser = userService.getCurrentUser();
        return Result.success("修改成功", updatedUser);
    }

    @Operation(summary = "用户列表", description = "管理员获取用户列表，支持分页和搜索")
    @GetMapping
    @SaCheckRole("admin")
    public Result<PageResult<UserVO>> getUserList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选：reader/admin/author") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选：1正常/0禁用") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long size) {
        PageResult<UserVO> result = userService.getUserList(keyword, role, status, page, size);
        return Result.success(result);
    }

    @Operation(summary = "新增用户", description = "管理员新增用户")
    @PostMapping
    @SaCheckRole("admin")
    public Result<Long> addUser(@RequestBody @Validated UserRegisterDTO registerDTO) {
        userService.addUser(registerDTO);
        return Result.success(registerDTO.getUserId() != null ? "用户ID" : "操作失败", registerDTO.getUserId());
    }

    @Operation(summary = "编辑用户", description = "管理员编辑用户信息")
    @PutMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody @Validated UserUpdateDTO updateDTO) {
        userService.updateUser(id, updateDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除用户", description = "管理员删除用户")
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
}