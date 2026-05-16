package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.SleepRecordDTO;
import com.fitness.entity.SleepRecord;
import com.fitness.service.SleepRecordService;
import com.fitness.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "睡眠记录")
@RestController
@RequestMapping("/sleep")
@RequiredArgsConstructor
public class SleepController {

    private final SleepRecordService sleepRecordService;

    @Operation(summary = "添加睡眠记录")
    @PostMapping
    public Result<SleepRecord> addSleepRecord(@Valid @RequestBody SleepRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(sleepRecordService.addSleepRecord(userId, dto));
    }

    @Operation(summary = "获取睡眠记录列表")
    @GetMapping
    public Result<List<SleepRecord>> getSleepRecords(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(sleepRecordService.getSleepRecords(userId, startDate, endDate));
    }

    @Operation(summary = "获取最新睡眠记录")
    @GetMapping("/latest")
    public Result<SleepRecord> getLatestSleepRecord() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(sleepRecordService.getLatestSleepRecord(userId));
    }
}
