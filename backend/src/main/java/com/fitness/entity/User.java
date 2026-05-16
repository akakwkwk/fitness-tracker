package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
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
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
