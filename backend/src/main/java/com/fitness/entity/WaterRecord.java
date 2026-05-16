package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("water_record")
public class WaterRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private Integer amount;
    private LocalDateTime recordTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
