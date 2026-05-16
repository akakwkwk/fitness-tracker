package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.entity.WaterRecord;
import com.fitness.mapper.WaterRecordMapper;
import com.fitness.service.WaterRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaterRecordServiceImpl extends ServiceImpl<WaterRecordMapper, WaterRecord> implements WaterRecordService {

    @Override
    public WaterRecord addWaterRecord(Long userId, Integer amount) {
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setRecordDate(LocalDate.now());
        record.setAmount(amount);
        record.setRecordTime(LocalDateTime.now());
        save(record);
        return record;
    }

    @Override
    public int getTodayWaterIntake(Long userId) {
        List<WaterRecord> records = list(new LambdaQueryWrapper<WaterRecord>()
                .eq(WaterRecord::getUserId, userId)
                .eq(WaterRecord::getRecordDate, LocalDate.now()));
        return records.stream().mapToInt(WaterRecord::getAmount).sum();
    }

    @Override
    public List<WaterRecord> getDailyWaterRecords(Long userId, LocalDate date) {
        return list(new LambdaQueryWrapper<WaterRecord>()
                .eq(WaterRecord::getUserId, userId)
                .eq(WaterRecord::getRecordDate, date)
                .orderByAsc(WaterRecord::getRecordTime));
    }
}
