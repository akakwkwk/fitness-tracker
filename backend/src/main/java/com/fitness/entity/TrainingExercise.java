package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("training_exercise")
public class TrainingExercise implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long trainingRecordId;
    private Long exerciseLibraryId;
    private String exerciseName;
    private Integer sortOrder;
    private String note;
    private Integer isSuperset;
    private Integer supersetGroup;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<TrainingSet> sets;
}
