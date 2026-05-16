package com.fitness.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class NutritionStatsVO {
    private BigDecimal todayCalories;
    private BigDecimal todayProtein;
    private BigDecimal todayCarbs;
    private BigDecimal todayFat;
    private BigDecimal calorieTarget;
    private BigDecimal proteinTarget;
    private BigDecimal carbTarget;
    private BigDecimal fatTarget;
    private List<Map<String, Object>> weeklyNutritionTrend;
    private Map<String, BigDecimal> macroRatio;
}
