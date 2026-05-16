package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    @Select("SELECT training_date, COUNT(*) as count FROM training_record WHERE user_id = #{userId} AND training_date BETWEEN #{start} AND #{end} AND deleted = 0 GROUP BY training_date")
    List<Map<String, Object>> getTrainingDays(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("SELECT training_type, COUNT(*) as count FROM training_record WHERE user_id = #{userId} AND deleted = 0 GROUP BY training_type")
    List<Map<String, Object>> getTrainingTypeDistribution(@Param("userId") Long userId);
}
