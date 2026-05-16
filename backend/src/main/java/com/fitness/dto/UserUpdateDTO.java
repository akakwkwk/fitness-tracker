package com.fitness.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer activityLevel;
    private Integer fitnessGoal;
    private Integer dailyCalorieTarget;
    private Integer dailyProteinTarget;
    private Integer dailyCarbTarget;
    private Integer dailyFatTarget;
    private Integer dailyWaterTarget;
    private Integer weeklyTrainingTarget;
}
