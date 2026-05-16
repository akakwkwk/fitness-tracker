package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("training_record")
public class TrainingRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate trainingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String trainingType;
    private String name;
    private String note;
    private Integer feelingScore;
    private BigDecimal totalVolume;
    private Integer totalSets;
    private Integer caloriesBurned;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<TrainingExercise> exercises;
}
