package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_set")
public class TrainingSet implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long trainingExerciseId;
    private Integer setNumber;
    private String setType;
    private Integer reps;
    private BigDecimal weight;
    private Integer duration;
    private Integer restTime;
    private Integer isCompleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
