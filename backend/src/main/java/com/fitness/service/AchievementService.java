package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.entity.Achievement;
import com.fitness.entity.UserAchievement;

import java.util.List;
import java.util.Map;

public interface AchievementService extends IService<Achievement> {
    List<Map<String, Object>> getUserAchievements(Long userId);
    List<Achievement> checkAndUnlockAchievements(Long userId);
    int getUserAchievementPoints(Long userId);
}
