package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.UserUpdateDTO;
import com.fitness.service.UserService;
import com.fitness.utils.SecurityUtils;
import com.fitness.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/me")
    public Result<UserVO> updateUser(@RequestBody UserUpdateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.updateUser(userId, dto));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
}
