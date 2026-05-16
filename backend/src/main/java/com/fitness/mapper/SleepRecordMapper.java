package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.SleepRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SleepRecordMapper extends BaseMapper<SleepRecord> {
}
