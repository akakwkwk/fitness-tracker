package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.SleepRecordDTO;
import com.fitness.entity.SleepRecord;

import java.time.LocalDate;
import java.util.List;

public interface SleepRecordService extends IService<SleepRecord> {
    SleepRecord addSleepRecord(Long userId, SleepRecordDTO dto);
    List<SleepRecord> getSleepRecords(Long userId, LocalDate startDate, LocalDate endDate);
    SleepRecord getLatestSleepRecord(Long userId);
}
