package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.entity.WaterRecord;

import java.time.LocalDate;
import java.util.List;

public interface WaterRecordService extends IService<WaterRecord> {
    WaterRecord addWaterRecord(Long userId, Integer amount);
    int getTodayWaterIntake(Long userId);
    List<WaterRecord> getDailyWaterRecords(Long userId, LocalDate date);
}
