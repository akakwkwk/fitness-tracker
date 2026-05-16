package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {

    @Select("SELECT SUM(calories) as totalCalories, SUM(protein) as totalProtein, SUM(carbs) as totalCarbs, SUM(fat) as totalFat FROM diet_record WHERE user_id = #{userId} AND record_date = #{date} AND deleted = 0")
    Map<String, Object> getDailyNutritionSummary(@Param("userId") Long userId, @Param("date") LocalDate date);
}
