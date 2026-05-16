package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.DietRecordDTO;
import com.fitness.entity.DietRecord;
import com.fitness.entity.Food;
import com.fitness.service.DietService;
import com.fitness.utils.SecurityUtils;
import com.fitness.vo.NutritionStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "饮食管理")
@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @Operation(summary = "添加饮食记录")
    @PostMapping
    public Result<DietRecord> addDietRecord(@Valid @RequestBody DietRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(dietService.addDietRecord(userId, dto));
    }

    @Operation(summary = "删除饮食记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDietRecord(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        dietService.deleteDietRecord(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取每日饮食记录")
    @GetMapping("/daily")
    public Result<List<DietRecord>> getDailyDiet(@RequestParam(required = false) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (date == null) date = LocalDate.now();
        return Result.success(dietService.getDailyDiet(userId, date));
    }

    @Operation(summary = "获取每日营养汇总")
    @GetMapping("/daily/summary")
    public Result<Map<String, Object>> getDailyNutritionSummary(@RequestParam(required = false) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (date == null) date = LocalDate.now();
        return Result.success(dietService.getDailyNutritionSummary(userId, date));
    }

    @Operation(summary = "获取营养统计")
    @GetMapping("/stats")
    public Result<NutritionStatsVO> getNutritionStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(dietService.getNutritionStats(userId));
    }

    @Operation(summary = "搜索食物")
    @GetMapping("/food/search")
    public Result<Page<Food>> searchFood(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(dietService.searchFood(keyword, category, page, size));
    }

    @Operation(summary = "添加自定义食物")
    @PostMapping("/food")
    public Result<Food> addCustomFood(@RequestBody Food food) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(dietService.addCustomFood(userId, food));
    }

    @Operation(summary = "联网查询食物营养")
    @GetMapping("/food/online")
    public Result<List<Map<String, Object>>> searchOnline(@RequestParam String keyword) {
        return Result.success(dietService.searchOnline(keyword));
    }
}
