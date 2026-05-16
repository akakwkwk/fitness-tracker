package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.Achievement;
import com.fitness.service.AchievementService;
import com.fitness.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "成就系统")
@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "获取用户成就列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getUserAchievements() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(achievementService.getUserAchievements(userId));
    }

    @Operation(summary = "检查并解锁成就")
    @PostMapping("/check")
    public Result<List<Achievement>> checkAchievements() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(achievementService.checkAndUnlockAchievements(userId));
    }

    @Operation(summary = "获取用户成就积分")
    @GetMapping("/points")
    public Result<Integer> getAchievementPoints() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(achievementService.getUserAchievementPoints(userId));
    }
}
