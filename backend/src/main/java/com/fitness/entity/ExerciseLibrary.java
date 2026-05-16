package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("exercise_library")
public class ExerciseLibrary implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String nameEn;
    private String category;
    private String muscleGroup;
    private String secondaryMuscle;
    private String equipment;
    private Integer difficulty;
    private String description;
    private String instructions;
    private String tips;
    private String imageUrl;
    private String videoUrl;
    private Integer isCompound;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
