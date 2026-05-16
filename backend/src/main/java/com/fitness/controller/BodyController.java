package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.BodyDataDTO;
import com.fitness.entity.BodyData;
import com.fitness.service.BodyService;
import com.fitness.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "身体数据管理")
@RestController
@RequestMapping("/body")
@RequiredArgsConstructor
public class BodyController {

    private final BodyService bodyService;

    @Operation(summary = "添加身体数据")
    @PostMapping
    public Result<BodyData> addBodyData(@Valid @RequestBody BodyDataDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(bodyService.addBodyData(userId, dto));
    }

    @Operation(summary = "更新身体数据")
    @PutMapping("/{id}")
    public Result<BodyData> updateBodyData(@PathVariable Long id, @Valid @RequestBody BodyDataDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(bodyService.updateBodyData(userId, id, dto));
    }

    @Operation(summary = "删除身体数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBodyData(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        bodyService.deleteBodyData(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取身体数据列表")
    @GetMapping
    public Result<List<BodyData>> getBodyDataList(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(bodyService.getBodyDataList(userId, startDate, endDate));
    }

    @Operation(summary = "获取最新身体数据")
    @GetMapping("/latest")
    public Result<BodyData> getLatestBodyData() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(bodyService.getLatestBodyData(userId));
    }
}
