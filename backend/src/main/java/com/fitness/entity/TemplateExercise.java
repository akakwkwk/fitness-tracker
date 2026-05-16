package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("template_exercise")
public class TemplateExercise implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private Long exerciseLibraryId;
    private String exerciseName;
    private Integer targetSets;
    private String targetReps;
    private BigDecimal targetWeight;
    private Integer restTime;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
