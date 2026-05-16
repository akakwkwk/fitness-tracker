package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WaterRecordDTO {
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
    @NotNull(message = "饮水量不能为空")
    private Integer amount;
}
