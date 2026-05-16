package com.fitness.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVO {
    private Integer todayTrainingCount;
    private Integer todayCaloriesBurned;
    private BigDecimal todayCaloriesIntake;
    private BigDecimal todayProtein;
    private BigDecimal todayCarbs;
    private BigDecimal todayFat;
    private Integer todayWaterIntake;
    private Integer streakDays;
    private Integer weeklyTrainingCount;
    private Integer weeklyTrainingDuration;
    private List<Map<String, Object>> recentTrainings;
    private List<Map<String, Object>> weeklyNutritionTrend;
    private Integer weeklyTrainingTarget;
}
