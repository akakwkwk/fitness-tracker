package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SleepRecordDTO {
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
    @NotNull(message = "入睡时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sleepTime;
    @NotNull(message = "醒来时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime wakeTime;
    private Integer quality;
    private String note;
}
