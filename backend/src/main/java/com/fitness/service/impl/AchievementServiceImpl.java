package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.entity.*;
import com.fitness.mapper.AchievementMapper;
import com.fitness.mapper.UserAchievementMapper;
import com.fitness.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    private final UserAchievementMapper userAchievementMapper;
    private final TrainingService trainingService;
    private final DietService dietService;

    @Override
    public List<Map<String, Object>> getUserAchievements(Long userId) {
        List<Achievement> allAchievements = list();
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));

        Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        return allAchievements.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("description", a.getDescription());
            map.put("icon", a.getIcon());
            map.put("category", a.getCategory());
            map.put("points", a.getPoints());
            map.put("rarity", a.getRarity());

            UserAchievement ua = userAchievementMap.get(a.getId());
            map.put("unlocked", ua != null);
            map.put("unlockTime", ua != null ? ua.getUnlockTime() : null);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Achievement> checkAndUnlockAchievements(Long userId) {
        List<Achievement> allAchievements = list();
        List<UserAchievement> existing = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));
        Set<Long> unlockedIds = existing.stream().map(UserAchievement::getAchievementId).collect(Collectors.toSet());

        List<Achievement> newlyUnlocked = new ArrayList<>();

        // 获取用户统计数据
        long trainingCount = trainingService.count(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getStatus, 1)
                .eq(TrainingRecord::getDeleted, 0));
        int streakDays = trainingService.getStreakDays(userId);

        for (Achievement a : allAchievements) {
            if (unlockedIds.contains(a.getId())) continue;

            boolean unlocked = false;
            switch (a.getConditionType()) {
                case "training_count":
                    unlocked = trainingCount >= a.getConditionValue();
                    break;
                case "streak_days":
                    unlocked = streakDays >= a.getConditionValue();
                    break;
                default:
                    break;
            }

            if (unlocked) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(a.getId());
                ua.setUnlockTime(LocalDateTime.now());
                ua.setProgress(a.getConditionValue());
                ua.setIsNotified(0);
                userAchievementMapper.insert(ua);
                newlyUnlocked.add(a);
            }
        }

        return newlyUnlocked;
    }

    @Override
    public int getUserAchievementPoints(Long userId) {
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));

        if (userAchievements.isEmpty()) return 0;

        Set<Long> achievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        List<Achievement> achievements = listByIds(achievementIds);
        return achievements.stream().mapToInt(Achievement::getPoints).sum();
    }
}
