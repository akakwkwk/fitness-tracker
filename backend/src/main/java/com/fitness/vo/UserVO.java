package com.fitness.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
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
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
