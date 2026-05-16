package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.service.DashboardService;
import com.fitness.utils.SecurityUtils;
import com.fitness.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(dashboardService.getDashboard(userId));
    }
}
