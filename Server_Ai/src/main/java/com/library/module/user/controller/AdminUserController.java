package com.library.module.user.controller;

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
 * 管理员用户控制器
 */
@Tag(name = "管理员用户管理", description = "管理员对用户的管理操作")
@RestController
@RequestMapping("/admin/users")
@Validated
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表", description = "获取系统用户列表，支持分页和搜索")
    @GetMapping
    @SaCheckRole("admin")
    public Result<PageResult<UserVO>> getUserList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long size) {

        PageResult<UserVO> pageResult = userService.getUserList(keyword, role, status, page, size);
        return Result.success(pageResult);
    }

    @Operation(summary = "新增用户", description = "管理员新增用户")
    @PostMapping
    @SaCheckRole("admin")
    public Result<Void> addUser(@RequestBody @Validated UserRegisterDTO registerDTO) {
        userService.addUser(registerDTO);
        return Result.success("添加成功", null);
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
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
}