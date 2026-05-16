package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.TrainingRecord;
import com.fitness.vo.TrainingStatsVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TrainingService extends IService<TrainingRecord> {
    TrainingRecord createTraining(Long userId, TrainingRecordDTO dto);
    TrainingRecord updateTraining(Long userId, Long trainingId, TrainingRecordDTO dto);
    void deleteTraining(Long userId, Long trainingId);
    TrainingRecord getTrainingDetail(Long userId, Long trainingId);
    Page<TrainingRecord> getTrainingList(Long userId, LocalDate startDate, LocalDate endDate, String trainingType, int page, int size);
    TrainingStatsVO getTrainingStats(Long userId);
    List<Map<String, Object>> getTrainingCalendar(Long userId, int year, int month);
    int getStreakDays(Long userId);
}
