package com.fitness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrainingRecordDTO {
    @NotNull(message = "训练日期不能为空")
    private LocalDate trainingDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private Integer duration;
    private String trainingType;
    private String name;
    private String note;
    private Integer feelingScore;
    private Integer caloriesBurned;
    private List<TrainingExerciseDTO> exercises;

    @Data
    public static class TrainingExerciseDTO {
        private Long exerciseLibraryId;
        private String exerciseName;
        private Integer sortOrder;
        private String note;
        private Integer isSuperset;
        private Integer supersetGroup;
        private List<TrainingSetDTO> sets;
    }

    @Data
    public static class TrainingSetDTO {
        private Integer setNumber;
        private String setType;
        private Integer reps;
        private BigDecimal weight;
        private Integer duration;
        private Integer restTime;
        private Integer isCompleted;
    }
}
