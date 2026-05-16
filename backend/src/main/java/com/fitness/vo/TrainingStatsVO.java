package com.fitness.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TrainingStatsVO {
    private Integer totalTrainings;
    private Integer totalDuration;
    private BigDecimal totalVolume;
    private Integer thisWeekTrainings;
    private Integer thisMonthTrainings;
    private List<Map<String, Object>> weeklyTrainingCount;
    private List<Map<String, Object>> muscleGroupDistribution;
    private List<Map<String, Object>> trainingTypeDistribution;
}
