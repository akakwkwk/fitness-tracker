package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("food")
public class Food implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String brand;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private BigDecimal fiber;
    private BigDecimal sugar;
    private BigDecimal sodium;
    private BigDecimal servingSize;
    private String servingUnit;
    private String barcode;
    private String imageUrl;
    private Integer isUserCreated;
    private Long userId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
