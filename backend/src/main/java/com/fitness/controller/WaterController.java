package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.entity.WaterRecord;
import com.fitness.service.WaterRecordService;
import com.fitness.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "饮水记录")
@RestController
@RequestMapping("/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterRecordService waterRecordService;

    @Operation(summary = "添加饮水记录")
    @PostMapping
    public Result<WaterRecord> addWaterRecord(@RequestParam Integer amount) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(waterRecordService.addWaterRecord(userId, amount));
    }

    @Operation(summary = "获取今日饮水量")
    @GetMapping("/today")
    public Result<Integer> getTodayWaterIntake() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(waterRecordService.getTodayWaterIntake(userId));
    }

    @Operation(summary = "获取每日饮水记录")
    @GetMapping("/daily")
    public Result<List<WaterRecord>> getDailyWaterRecords(@RequestParam(required = false) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (date == null) date = LocalDate.now();
        return Result.success(waterRecordService.getDailyWaterRecords(userId, date));
    }
}
