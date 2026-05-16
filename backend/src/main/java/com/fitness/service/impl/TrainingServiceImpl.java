package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.BusinessException;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.TrainingExercise;
import com.fitness.entity.TrainingRecord;
import com.fitness.entity.TrainingSet;
import com.fitness.mapper.TrainingExerciseMapper;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.mapper.TrainingSetMapper;
import com.fitness.service.TrainingService;
import com.fitness.vo.TrainingStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord> implements TrainingService {

    private final TrainingExerciseMapper exerciseMapper;
    private final TrainingSetMapper setMapper;

    @Override
    @Transactional
    public TrainingRecord createTraining(Long userId, TrainingRecordDTO dto) {
        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setTrainingDate(dto.getTrainingDate());
        record.setStartTime(dto.getStartTime());
        record.setEndTime(dto.getEndTime());
        record.setDuration(dto.getDuration());
        record.setTrainingType(dto.getTrainingType());
        record.setName(dto.getName());
        record.setNote(dto.getNote());
        record.setFeelingScore(dto.getFeelingScore());
        record.setCaloriesBurned(dto.getCaloriesBurned());
        record.setStatus(1);

        // 计算总容量和总组数
        BigDecimal totalVolume = BigDecimal.ZERO;
        int totalSets = 0;

        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingRecordDTO.TrainingExerciseDTO exDto = dto.getExercises().get(i);
                totalSets += exDto.getSets() != null ? exDto.getSets().size() : 0;
                if (exDto.getSets() != null) {
                    for (TrainingRecordDTO.TrainingSetDTO setDto : exDto.getSets()) {
                        if (setDto.getWeight() != null && setDto.getReps() != null) {
                            totalVolume = totalVolume.add(setDto.getWeight().multiply(BigDecimal.valueOf(setDto.getReps())));
                        }
                    }
                }
            }
        }

        record.setTotalVolume(totalVolume);
        record.setTotalSets(totalSets);
        save(record);

        // 保存动作和组数
        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingRecordDTO.TrainingExerciseDTO exDto = dto.getExercises().get(i);
                TrainingExercise exercise = new TrainingExercise();
                exercise.setTrainingRecordId(record.getId());
                exercise.setExerciseLibraryId(exDto.getExerciseLibraryId());
                exercise.setExerciseName(exDto.getExerciseName());
                exercise.setSortOrder(exDto.getSortOrder() != null ? exDto.getSortOrder() : i);
                exercise.setNote(exDto.getNote());
                exercise.setIsSuperset(exDto.getIsSuperset());
                exercise.setSupersetGroup(exDto.getSupersetGroup());
                exerciseMapper.insert(exercise);

                if (exDto.getSets() != null) {
                    for (TrainingRecordDTO.TrainingSetDTO setDto : exDto.getSets()) {
                        TrainingSet set = new TrainingSet();
                        set.setTrainingExerciseId(exercise.getId());
                        set.setSetNumber(setDto.getSetNumber());
                        set.setSetType(setDto.getSetType() != null ? setDto.getSetType() : "normal");
                        set.setReps(setDto.getReps());
                        set.setWeight(setDto.getWeight());
                        set.setDuration(setDto.getDuration());
                        set.setRestTime(setDto.getRestTime());
                        set.setIsCompleted(setDto.getIsCompleted() != null ? setDto.getIsCompleted() : 1);
                        setMapper.insert(set);
                    }
                }
            }
        }

        return record;
    }

    @Override
    @Transactional
    public TrainingRecord updateTraining(Long userId, Long trainingId, TrainingRecordDTO dto) {
        TrainingRecord record = getById(trainingId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("训练记录不存在");
        }

        record.setTrainingDate(dto.getTrainingDate());
        record.setStartTime(dto.getStartTime());
        record.setEndTime(dto.getEndTime());
        record.setDuration(dto.getDuration());
        record.setTrainingType(dto.getTrainingType());
        record.setName(dto.getName());
        record.setNote(dto.getNote());
        record.setFeelingScore(dto.getFeelingScore());
        record.setCaloriesBurned(dto.getCaloriesBurned());

        // 删除旧的动作和组数
        List<TrainingExercise> oldExercises = exerciseMapper.selectList(
                new LambdaQueryWrapper<TrainingExercise>().eq(TrainingExercise::getTrainingRecordId, trainingId));
        for (TrainingExercise ex : oldExercises) {
            setMapper.delete(new LambdaQueryWrapper<TrainingSet>().eq(TrainingSet::getTrainingExerciseId, ex.getId()));
        }
        exerciseMapper.delete(new LambdaQueryWrapper<TrainingExercise>().eq(TrainingExercise::getTrainingRecordId, trainingId));

        // 保存新的动作和组数
        BigDecimal totalVolume = BigDecimal.ZERO;
        int totalSets = 0;

        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingRecordDTO.TrainingExerciseDTO exDto = dto.getExercises().get(i);
                TrainingExercise exercise = new TrainingExercise();
                exercise.setTrainingRecordId(trainingId);
                exercise.setExerciseLibraryId(exDto.getExerciseLibraryId());
                exercise.setExerciseName(exDto.getExerciseName());
                exercise.setSortOrder(exDto.getSortOrder() != null ? exDto.getSortOrder() : i);
                exercise.setNote(exDto.getNote());
                exercise.setIsSuperset(exDto.getIsSuperset());
                exercise.setSupersetGroup(exDto.getSupersetGroup());
                exerciseMapper.insert(exercise);

                if (exDto.getSets() != null) {
                    totalSets += exDto.getSets().size();
                    for (TrainingRecordDTO.TrainingSetDTO setDto : exDto.getSets()) {
                        TrainingSet set = new TrainingSet();
                        set.setTrainingExerciseId(exercise.getId());
                        set.setSetNumber(setDto.getSetNumber());
                        set.setSetType(setDto.getSetType() != null ? setDto.getSetType() : "normal");
                        set.setReps(setDto.getReps());
                        set.setWeight(setDto.getWeight());
                        set.setDuration(setDto.getDuration());
                        set.setRestTime(setDto.getRestTime());
                        set.setIsCompleted(setDto.getIsCompleted() != null ? setDto.getIsCompleted() : 1);
                        setMapper.insert(set);

                        if (setDto.getWeight() != null && setDto.getReps() != null) {
                            totalVolume = totalVolume.add(setDto.getWeight().multiply(BigDecimal.valueOf(setDto.getReps())));
                        }
                    }
                }
            }
        }

        record.setTotalVolume(totalVolume);
        record.setTotalSets(totalSets);
        updateById(record);

        return record;
    }

    @Override
    @Transactional
    public void deleteTraining(Long userId, Long trainingId) {
        TrainingRecord record = getById(trainingId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("训练记录不存在");
        }
        removeById(trainingId);
    }

    @Override
    public TrainingRecord getTrainingDetail(Long userId, Long trainingId) {
        TrainingRecord record = getById(trainingId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("训练记录不存在");
        }

        // 加载动作和组数
        List<TrainingExercise> exercises = exerciseMapper.selectList(
                new LambdaQueryWrapper<TrainingExercise>()
                        .eq(TrainingExercise::getTrainingRecordId, trainingId)
                        .orderByAsc(TrainingExercise::getSortOrder));

        for (TrainingExercise exercise : exercises) {
            List<TrainingSet> sets = setMapper.selectList(
                    new LambdaQueryWrapper<TrainingSet>()
                            .eq(TrainingSet::getTrainingExerciseId, exercise.getId())
                            .orderByAsc(TrainingSet::getSetNumber));
            exercise.setSets(sets);
        }

        record.setExercises(exercises);
        return record;
    }

    @Override
    public Page<TrainingRecord> getTrainingList(Long userId, LocalDate startDate, LocalDate endDate, String trainingType, int page, int size) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1)
                .ge(startDate != null, TrainingRecord::getTrainingDate, startDate)
                .le(endDate != null, TrainingRecord::getTrainingDate, endDate)
                .eq(trainingType != null, TrainingRecord::getTrainingType, trainingType)
                .orderByDesc(TrainingRecord::getTrainingDate)
                .orderByDesc(TrainingRecord::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public TrainingStatsVO getTrainingStats(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = now.withDayOfMonth(1);

        List<TrainingRecord> allRecords = list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1));

        int totalTrainings = allRecords.size();
        int totalDuration = allRecords.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        BigDecimal totalVolume = allRecords.stream()
                .map(r -> r.getTotalVolume() != null ? r.getTotalVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int thisWeekTrainings = (int) allRecords.stream()
                .filter(r -> r.getTrainingDate() != null && !r.getTrainingDate().isBefore(weekStart))
                .count();

        int thisMonthTrainings = (int) allRecords.stream()
                .filter(r -> r.getTrainingDate() != null && !r.getTrainingDate().isBefore(monthStart))
                .count();

        // 训练类型分布
        Map<String, Long> typeMap = allRecords.stream()
                .filter(r -> r.getTrainingType() != null)
                .collect(Collectors.groupingBy(TrainingRecord::getTrainingType, Collectors.counting()));
        List<Map<String, Object>> typeDistribution = typeMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());

        return TrainingStatsVO.builder()
                .totalTrainings(totalTrainings)
                .totalDuration(totalDuration)
                .totalVolume(totalVolume)
                .thisWeekTrainings(thisWeekTrainings)
                .thisMonthTrainings(thisMonthTrainings)
                .trainingTypeDistribution(typeDistribution)
                .build();
    }

    @Override
    public List<Map<String, Object>> getTrainingCalendar(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<TrainingRecord> records = list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1)
                .ge(TrainingRecord::getTrainingDate, start)
                .le(TrainingRecord::getTrainingDate, end));

        Map<LocalDate, Long> dateCount = records.stream()
                .collect(Collectors.groupingBy(TrainingRecord::getTrainingDate, Collectors.counting()));

        return dateCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", e.getKey().toString());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }

    @Override
    public int getStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;

        List<TrainingRecord> records = list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1)
                .le(TrainingRecord::getTrainingDate, today)
                .orderByDesc(TrainingRecord::getTrainingDate));

        Set<LocalDate> trainingDates = records.stream()
                .map(TrainingRecord::getTrainingDate)
                .collect(Collectors.toSet());

        LocalDate checkDate = today;
        while (trainingDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }
}
