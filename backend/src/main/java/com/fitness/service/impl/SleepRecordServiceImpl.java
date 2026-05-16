package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.dto.SleepRecordDTO;
import com.fitness.entity.SleepRecord;
import com.fitness.mapper.SleepRecordMapper;
import com.fitness.service.SleepRecordService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class SleepRecordServiceImpl extends ServiceImpl<SleepRecordMapper, SleepRecord> implements SleepRecordService {

    @Override
    public SleepRecord addSleepRecord(Long userId, SleepRecordDTO dto) {
        SleepRecord record = new SleepRecord();
        record.setUserId(userId);
        record.setRecordDate(dto.getRecordDate());
        record.setSleepTime(dto.getSleepTime());
        record.setWakeTime(dto.getWakeTime());
        record.setDuration((int) Duration.between(dto.getSleepTime(), dto.getWakeTime()).toMinutes());
        record.setQuality(dto.getQuality());
        record.setNote(dto.getNote());
        save(record);
        return record;
    }

    @Override
    public List<SleepRecord> getSleepRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        return list(new LambdaQueryWrapper<SleepRecord>()
                .eq(SleepRecord::getUserId, userId)
                .ge(startDate != null, SleepRecord::getRecordDate, startDate)
                .le(endDate != null, SleepRecord::getRecordDate, endDate)
                .orderByDesc(SleepRecord::getRecordDate));
    }

    @Override
    public SleepRecord getLatestSleepRecord(Long userId) {
        return getOne(new LambdaQueryWrapper<SleepRecord>()
                .eq(SleepRecord::getUserId, userId)
                .orderByDesc(SleepRecord::getRecordDate)
                .last("LIMIT 1"));
    }
}
