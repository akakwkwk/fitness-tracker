package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TrainingTemplateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String name;
    private String description;
    private String trainingType;
    private Integer estimatedDuration;
    private List<TemplateExerciseDTO> exercises;

    @Data
    public static class TemplateExerciseDTO {
        private Long exerciseLibraryId;
        private String exerciseName;
        private Integer targetSets;
        private String targetReps;
        private BigDecimal targetWeight;
        private Integer restTime;
        private Integer sortOrder;
    }
}
