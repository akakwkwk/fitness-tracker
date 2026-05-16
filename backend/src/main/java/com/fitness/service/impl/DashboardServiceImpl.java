package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.*;
import com.fitness.service.*;
import com.fitness.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TrainingService trainingService;
    private final DietService dietService;
    private final UserService userService;
    private final WaterRecordService waterRecordService;

    @Override
    public DashboardVO getDashboard(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 今日训练
        List<TrainingRecord> todayTrainings = trainingService.list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getTrainingDate, today)
                .eq(TrainingRecord::getStatus, 1)
                .eq(TrainingRecord::getDeleted, 0));

        int todayTrainingCount = todayTrainings.size();
        int todayCaloriesBurned = todayTrainings.stream()
                .mapToInt(r -> r.getCaloriesBurned() != null ? r.getCaloriesBurned() : 0)
                .sum();

        // 今日营养
        Map<String, Object> todayNutrition = dietService.getDailyNutritionSummary(userId, today);

        // 连续打卡天数
        int streakDays = trainingService.getStreakDays(userId);

        // 本周训练统计
        List<TrainingRecord> weekTrainings = trainingService.list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .ge(TrainingRecord::getTrainingDate, weekStart)
                .eq(TrainingRecord::getStatus, 1)
                .eq(TrainingRecord::getDeleted, 0));

        int weeklyTrainingCount = weekTrainings.size();
        int weeklyTrainingDuration = weekTrainings.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();

        // 最近3次训练
        List<TrainingRecord> recentTrainings = trainingService.list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1)
                .eq(TrainingRecord::getDeleted, 0)
                .orderByDesc(TrainingRecord::getTrainingDate)
                .last("LIMIT 3"));

        List<Map<String, Object>> recentTrainingList = recentTrainings.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("trainingDate", r.getTrainingDate());
            m.put("duration", r.getDuration());
            m.put("trainingType", r.getTrainingType());
            return m;
        }).collect(Collectors.toList());

        // 本周营养趋势
        List<Map<String, Object>> weeklyNutritionTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            Map<String, Object> daySummary = dietService.getDailyNutritionSummary(userId, date);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("calories", daySummary.get("totalCalories"));
            weeklyNutritionTrend.add(dayData);
        }

        // 今日饮水
        int todayWaterIntake = waterRecordService.getTodayWaterIntake(userId);

        User user = userService.getById(userId);

        return DashboardVO.builder()
                .todayTrainingCount(todayTrainingCount)
                .todayCaloriesBurned(todayCaloriesBurned)
                .todayCaloriesIntake((BigDecimal) todayNutrition.get("totalCalories"))
                .todayProtein((BigDecimal) todayNutrition.get("totalProtein"))
                .todayCarbs((BigDecimal) todayNutrition.get("totalCarbs"))
                .todayFat((BigDecimal) todayNutrition.get("totalFat"))
                .todayWaterIntake(todayWaterIntake)
                .streakDays(streakDays)
                .weeklyTrainingCount(weeklyTrainingCount)
                .weeklyTrainingDuration(weeklyTrainingDuration)
                .recentTrainings(recentTrainingList)
                .weeklyNutritionTrend(weeklyNutritionTrend)
                .weeklyTrainingTarget(user != null && user.getWeeklyTrainingTarget() != null ? user.getWeeklyTrainingTarget() : 5)
                .build();
    }
}
