package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.DietRecordDTO;
import com.fitness.entity.DietRecord;
import com.fitness.entity.Food;
import com.fitness.vo.NutritionStatsVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DietService extends IService<DietRecord> {
    DietRecord addDietRecord(Long userId, DietRecordDTO dto);
    void deleteDietRecord(Long userId, Long recordId);
    List<DietRecord> getDailyDiet(Long userId, LocalDate date);
    Map<String, Object> getDailyNutritionSummary(Long userId, LocalDate date);
    NutritionStatsVO getNutritionStats(Long userId);
    Page<Food> searchFood(String keyword, String category, int page, int size);
    Food addCustomFood(Long userId, Food food);
    List<Map<String, Object>> searchOnline(String keyword);
}
