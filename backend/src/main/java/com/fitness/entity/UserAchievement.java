package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long achievementId;
    private LocalDateTime unlockTime;
    private Integer progress;
    private Integer isNotified;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
