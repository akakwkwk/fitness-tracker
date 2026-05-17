package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.BusinessException;
import com.fitness.dto.DietRecordDTO;
import com.fitness.entity.DietRecord;
import com.fitness.entity.Food;
import com.fitness.entity.User;
import com.fitness.mapper.DietRecordMapper;
import com.fitness.mapper.FoodMapper;
import com.fitness.service.DietService;
import com.fitness.service.UserService;
import com.fitness.utils.SecurityUtils;
import com.fitness.vo.NutritionStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietService {

    private final FoodMapper foodMapper;
    private final UserService userService;

    @Override
    public DietRecord addDietRecord(Long userId, DietRecordDTO dto) {
        Food food = foodMapper.selectById(dto.getFoodId());
        if (food == null) {
            throw new BusinessException("食物不存在");
        }

        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setRecordDate(dto.getRecordDate());
        record.setMealType(dto.getMealType());
        record.setFoodId(dto.getFoodId());
        record.setFoodName(food.getName());
        record.setAmount(dto.getAmount());
        record.setNote(dto.getNote());

        // 计算营养值 (按比例)
        BigDecimal ratio = dto.getAmount().divide(food.getServingSize(), 6, RoundingMode.HALF_UP);
        record.setCalories(food.getCalories().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
        record.setProtein(food.getProtein().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
        record.setCarbs(food.getCarbs().multiply(ratio).setScale(1, RoundingMode.HALF_UP));
        record.setFat(food.getFat().multiply(ratio).setScale(1, RoundingMode.HALF_UP));

        save(record);
        return record;
    }

    @Override
    public void deleteDietRecord(Long userId, Long recordId) {
        DietRecord record = getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("饮食记录不存在");
        }
        removeById(recordId);
    }

    @Override
    public List<DietRecord> getDailyDiet(Long userId, LocalDate date) {
        return list(new LambdaQueryWrapper<DietRecord>()
                .eq(DietRecord::getUserId, userId)
                .eq(DietRecord::getRecordDate, date)
                .eq(DietRecord::getDeleted, 0)
                .orderByAsc(DietRecord::getMealType)
                .orderByAsc(DietRecord::getCreateTime));
    }

    @Override
    public Map<String, Object> getDailyNutritionSummary(Long userId, LocalDate date) {
        List<DietRecord> records = getDailyDiet(userId, date);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (DietRecord r : records) {
            totalCalories = totalCalories.add(r.getCalories() != null ? r.getCalories() : BigDecimal.ZERO);
            totalProtein = totalProtein.add(r.getProtein() != null ? r.getProtein() : BigDecimal.ZERO);
            totalCarbs = totalCarbs.add(r.getCarbs() != null ? r.getCarbs() : BigDecimal.ZERO);
            totalFat = totalFat.add(r.getFat() != null ? r.getFat() : BigDecimal.ZERO);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCalories", totalCalories);
        summary.put("totalProtein", totalProtein);
        summary.put("totalCarbs", totalCarbs);
        summary.put("totalFat", totalFat);
        return summary;
    }

    @Override
    public NutritionStatsVO getNutritionStats(Long userId) {
        LocalDate today = LocalDate.now();
        User user = userService.getById(userId);

        Map<String, Object> todaySummary = getDailyNutritionSummary(userId, today);

        // 本周营养趋势
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            Map<String, Object> daySummary = getDailyNutritionSummary(userId, date);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("calories", daySummary.get("totalCalories"));
            dayData.put("protein", daySummary.get("totalProtein"));
            dayData.put("carbs", daySummary.get("totalCarbs"));
            dayData.put("fat", daySummary.get("totalFat"));
            weeklyTrend.add(dayData);
        }

        // 宏量营养素比例
        BigDecimal todayProtein = (BigDecimal) todaySummary.get("totalProtein");
        BigDecimal todayCarbs = (BigDecimal) todaySummary.get("totalCarbs");
        BigDecimal todayFat = (BigDecimal) todaySummary.get("totalFat");

        Map<String, BigDecimal> macroRatio = new HashMap<>();
        macroRatio.put("protein", todayProtein);
        macroRatio.put("carbs", todayCarbs);
        macroRatio.put("fat", todayFat);

        return NutritionStatsVO.builder()
                .todayCalories((BigDecimal) todaySummary.get("totalCalories"))
                .todayProtein(todayProtein)
                .todayCarbs(todayCarbs)
                .todayFat(todayFat)
                .calorieTarget(user != null ? BigDecimal.valueOf(user.getDailyCalorieTarget()) : BigDecimal.valueOf(2000))
                .proteinTarget(user != null ? BigDecimal.valueOf(user.getDailyProteinTarget()) : BigDecimal.valueOf(150))
                .carbTarget(user != null ? BigDecimal.valueOf(user.getDailyCarbTarget()) : BigDecimal.valueOf(250))
                .fatTarget(user != null ? BigDecimal.valueOf(user.getDailyFatTarget()) : BigDecimal.valueOf(65))
                .weeklyNutritionTrend(weeklyTrend)
                .macroRatio(macroRatio)
                .build();
    }

    @Override
    public Page<Food> searchFood(String keyword, String category, int page, int size) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<Food>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Food::getName, keyword)
                        .or()
                        .like(Food::getBrand, keyword))
                .eq(StringUtils.hasText(category), Food::getCategory, category)
                .eq(Food::getStatus, 1)
                .orderByDesc(Food::getId);
        return foodMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Food addCustomFood(Long userId, Food food) {
        food.setIsUserCreated(1);
        food.setUserId(userId);
        food.setStatus(1);

        // 归一化为每100g的营养值
        BigDecimal ss = food.getServingSize() != null ? food.getServingSize() : BigDecimal.valueOf(100);
        if (ss.compareTo(BigDecimal.ZERO) > 0 && ss.compareTo(BigDecimal.valueOf(100)) != 0) {
            BigDecimal factor = BigDecimal.valueOf(100).divide(ss, 6, RoundingMode.HALF_UP);
            if (food.getCalories() != null) food.setCalories(food.getCalories().multiply(factor).setScale(1, RoundingMode.HALF_UP));
            if (food.getProtein() != null) food.setProtein(food.getProtein().multiply(factor).setScale(1, RoundingMode.HALF_UP));
            if (food.getCarbs() != null) food.setCarbs(food.getCarbs().multiply(factor).setScale(1, RoundingMode.HALF_UP));
            if (food.getFat() != null) food.setFat(food.getFat().multiply(factor).setScale(1, RoundingMode.HALF_UP));
            food.setServingSize(BigDecimal.valueOf(100));
        }

        foodMapper.insert(food);
        return food;
    }

    @Override
    public List<Map<String, Object>> searchOnline(String keyword) {
        // 先从本地数据库模糊搜索
        List<Food> localResults = foodMapper.selectList(
                new LambdaQueryWrapper<Food>()
                        .like(Food::getName, keyword)
                        .eq(Food::getStatus, 1)
                        .last("LIMIT 10"));

        List<Map<String, Object>> results = new ArrayList<>();
        for (Food f : localResults) {
            results.add(foodToMap(f));
        }

        // 补充内置营养数据库（覆盖更多常见食物）
        List<Map<String, Object>> builtin = getBuiltinFoods().stream()
                .filter(f -> ((String) f.get("name")).contains(keyword) || keyword.contains((String) f.get("name")))
                .collect(Collectors.toList());

        // 去重合并
        Set<String> existingNames = results.stream().map(r -> (String) r.get("name")).collect(Collectors.toSet());
        for (Map<String, Object> item : builtin) {
            if (!existingNames.contains(item.get("name"))) {
                results.add(item);
            }
        }

        return results;
    }

    private Map<String, Object> foodToMap(Food f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.getId());
        m.put("name", f.getName());
        m.put("category", f.getCategory());
        m.put("calories", f.getCalories());
        m.put("protein", f.getProtein());
        m.put("carbs", f.getCarbs());
        m.put("fat", f.getFat());
        m.put("servingSize", f.getServingSize());
        m.put("servingUnit", f.getServingUnit() != null ? f.getServingUnit() : "g");
        return m;
    }

    /**
     * 内置营养数据库 - 覆盖常见中餐、外卖、零食等
     * 数据来源：中国食物成分表、薄荷健康、MyFitnessPal
     */
    private List<Map<String, Object>> getBuiltinFoods() {
        List<Map<String, Object>> list = new ArrayList<>();
        // 格式: name, category, calories, protein, carbs, fat (per 100g)
        addFood(list, "白米饭", "主食", 116, 2.6, 25.9, 0.3);
        addFood(list, "糙米饭", "主食", 111, 2.5, 23.0, 0.9);
        addFood(list, "面条(煮熟)", "主食", 110, 4.0, 22.0, 0.5);
        addFood(list, "馒头", "主食", 221, 7.0, 44.2, 1.1);
        addFood(list, "包子(猪肉)", "主食", 220, 8.5, 30.0, 7.0);
        addFood(list, "饺子(猪肉)", "主食", 190, 8.0, 25.0, 6.5);
        addFood(list, "油条", "主食", 386, 6.9, 51.0, 17.6);
        addFood(list, "烧饼", "主食", 290, 8.0, 48.0, 6.0);
        addFood(list, "全麦面包", "主食", 246, 12.0, 41.0, 3.5);
        addFood(list, "燕麦片", "主食", 367, 13.5, 67.0, 6.5);
        addFood(list, "红薯", "主食", 86, 1.6, 20.1, 0.1);
        addFood(list, "土豆", "主食", 77, 2.0, 17.5, 0.1);
        addFood(list, "玉米", "主食", 112, 4.0, 22.8, 1.2);
        addFood(list, "紫薯", "主食", 82, 1.6, 18.0, 0.2);
        addFood(list, "意大利面(熟)", "主食", 131, 5.0, 25.0, 1.1);
        addFood(list, "米粉(熟)", "主食", 109, 2.0, 25.0, 0.2);
        addFood(list, "年糕", "主食", 154, 3.3, 34.7, 0.2);

        addFood(list, "鸡胸肉", "肉类", 165, 31.0, 0, 3.6);
        addFood(list, "鸡腿肉", "肉类", 177, 26.0, 0, 7.2);
        addFood(list, "鸡翅", "肉类", 222, 18.0, 0, 15.5);
        addFood(list, "牛里脊", "肉类", 140, 26.0, 0, 4.0);
        addFood(list, "牛腱子", "肉类", 106, 20.0, 0, 2.0);
        addFood(list, "肥牛卷", "肉类", 250, 15.0, 0, 21.0);
        addFood(list, "猪里脊", "肉类", 143, 21.0, 0, 6.0);
        addFood(list, "猪五花", "肉类", 395, 14.0, 0, 37.0);
        addFood(list, "猪排骨", "肉类", 278, 18.0, 0, 22.0);
        addFood(list, "三文鱼", "肉类", 208, 20.0, 0, 13.0);
        addFood(list, "虾仁", "肉类", 85, 18.0, 0.8, 1.0);
        addFood(list, "金枪鱼(罐头)", "肉类", 116, 26.0, 0, 1.0);
        addFood(list, "带鱼", "肉类", 127, 17.7, 0, 4.9);
        addFood(list, "鲈鱼", "肉类", 105, 18.6, 0, 3.4);
        addFood(list, "牛肉干", "肉类", 550, 45.0, 20.0, 30.0);

        addFood(list, "鸡蛋(全蛋)", "蛋奶", 144, 13.0, 1.1, 10.0);
        addFood(list, "鸡蛋白", "蛋奶", 47, 11.0, 0.7, 0.2);
        addFood(list, "脱脂牛奶", "蛋奶", 34, 3.4, 5.0, 0.1);
        addFood(list, "全脂牛奶", "蛋奶", 61, 3.2, 4.8, 3.3);
        addFood(list, "希腊酸奶", "蛋奶", 59, 10.0, 3.6, 0.7);
        addFood(list, "奶酪", "蛋奶", 328, 25.7, 3.5, 23.5);
        addFood(list, "豆浆(无糖)", "蛋奶", 31, 2.9, 1.2, 1.6);

        addFood(list, "西兰花", "蔬菜", 34, 2.8, 6.6, 0.4);
        addFood(list, "菠菜", "蔬菜", 23, 2.9, 3.6, 0.4);
        addFood(list, "番茄", "蔬菜", 18, 0.9, 3.9, 0.2);
        addFood(list, "黄瓜", "蔬菜", 16, 0.7, 3.6, 0.1);
        addFood(list, "胡萝卜", "蔬菜", 41, 0.9, 9.6, 0.2);
        addFood(list, "生菜", "蔬菜", 15, 1.4, 2.9, 0.2);
        addFood(list, "白菜", "蔬菜", 17, 1.5, 3.2, 0.1);
        addFood(list, "青椒", "蔬菜", 22, 0.9, 5.4, 0.2);
        addFood(list, "茄子", "蔬菜", 25, 1.0, 5.7, 0.2);
        addFood(list, "豆角", "蔬菜", 31, 1.8, 6.7, 0.2);
        addFood(list, "蘑菇", "蔬菜", 22, 3.1, 3.3, 0.3);
        addFood(list, "木耳", "蔬菜", 21, 1.5, 5.3, 0.2);
        addFood(list, "海带", "蔬菜", 13, 1.2, 2.1, 0.1);
        addFood(list, "芹菜", "蔬菜", 14, 0.8, 3.0, 0.1);
        addFood(list, "韭菜", "蔬菜", 26, 2.4, 4.6, 0.4);
        addFood(list, "洋葱", "蔬菜", 40, 1.1, 9.3, 0.1);
        addFood(list, "冬瓜", "蔬菜", 12, 0.4, 2.6, 0.2);
        addFood(list, "南瓜", "蔬菜", 26, 0.7, 6.5, 0.1);

        addFood(list, "苹果", "水果", 52, 0.3, 13.8, 0.2);
        addFood(list, "香蕉", "水果", 89, 1.1, 22.8, 0.3);
        addFood(list, "橙子", "水果", 47, 0.9, 11.8, 0.1);
        addFood(list, "葡萄", "水果", 69, 0.7, 18.1, 0.2);
        addFood(list, "西瓜", "水果", 30, 0.6, 7.6, 0.1);
        addFood(list, "草莓", "水果", 32, 0.7, 7.7, 0.3);
        addFood(list, "蓝莓", "水果", 57, 0.7, 14.5, 0.3);
        addFood(list, "芒果", "水果", 60, 0.8, 15.0, 0.4);
        addFood(list, "猕猴桃", "水果", 61, 1.1, 14.7, 0.5);
        addFood(list, "柚子", "水果", 42, 0.8, 10.7, 0.1);
        addFood(list, "樱桃", "水果", 50, 1.0, 12.2, 0.3);
        addFood(list, "荔枝", "水果", 71, 0.8, 16.6, 0.4);
        addFood(list, "榴莲", "水果", 147, 1.5, 27.1, 5.3);
        addFood(list, "牛油果", "水果", 160, 2.0, 8.5, 15.0);

        addFood(list, "杏仁", "坚果", 579, 21.0, 22.0, 50.0);
        addFood(list, "核桃", "坚果", 654, 15.0, 14.0, 65.0);
        addFood(list, "花生", "坚果", 567, 26.0, 16.0, 49.0);
        addFood(list, "腰果", "坚果", 553, 18.0, 30.0, 44.0);
        addFood(list, "开心果", "坚果", 560, 20.0, 27.0, 45.0);
        addFood(list, "瓜子", "坚果", 575, 24.0, 13.0, 49.0);

        // 常见中式菜品
        addFood(list, "宫保鸡丁", "菜品", 180, 15.0, 10.0, 9.0);
        addFood(list, "鱼香肉丝", "菜品", 160, 10.0, 12.0, 8.0);
        addFood(list, "红烧肉", "菜品", 350, 12.0, 8.0, 32.0);
        addFood(list, "糖醋排骨", "菜品", 280, 15.0, 18.0, 16.0);
        addFood(list, "麻婆豆腐", "菜品", 130, 8.0, 6.0, 8.5);
        addFood(list, "清蒸鱼", "菜品", 100, 18.0, 2.0, 2.0);
        addFood(list, "炒青菜", "菜品", 50, 2.0, 4.0, 3.0);
        addFood(list, "番茄炒蛋", "菜品", 110, 7.0, 6.0, 7.0);
        addFood(list, "回锅肉", "菜品", 250, 12.0, 8.0, 20.0);
        addFood(list, "水煮鱼", "菜品", 180, 16.0, 5.0, 10.0);
        addFood(list, "酸菜鱼", "菜品", 120, 14.0, 3.0, 5.5);
        addFood(list, "蛋炒饭", "菜品", 180, 6.0, 24.0, 7.0);
        addFood(list, "扬州炒饭", "菜品", 190, 8.0, 22.0, 8.0);
        addFood(list, "麻辣烫", "菜品", 80, 5.0, 8.0, 3.0);
        addFood(list, "火锅底料(牛油)", "调味品", 800, 2.0, 5.0, 85.0);

        // 常见饮品
        addFood(list, "可乐", "饮品", 42, 0, 10.6, 0);
        addFood(list, "橙汁", "饮品", 45, 0.7, 10.4, 0.2);
        addFood(list, "奶茶(珍珠)", "饮品", 70, 1.0, 12.0, 2.0);
        addFood(list, "美式咖啡", "饮品", 2, 0.1, 0, 0);
        addFood(list, "拿铁", "饮品", 60, 3.0, 5.0, 3.0);
        addFood(list, "啤酒", "饮品", 43, 0.5, 3.6, 0);
        addFood(list, "红酒", "饮品", 85, 0.1, 2.6, 0);

        // 常见零食
        addFood(list, "薯片", "零食", 536, 7.0, 53.0, 35.0);
        addFood(list, "巧克力", "零食", 546, 5.0, 60.0, 31.0);
        addFood(list, "饼干", "零食", 480, 7.0, 65.0, 22.0);
        addFood(list, "蛋糕", "零食", 348, 5.0, 50.0, 15.0);
        addFood(list, "冰淇淋", "零食", 207, 3.5, 24.0, 11.0);

        // 调味品
        addFood(list, "橄榄油", "调味品", 884, 0, 0, 100.0);
        addFood(list, "花生酱", "调味品", 588, 25.0, 20.0, 50.0);
        addFood(list, "蜂蜜", "调味品", 304, 0.3, 82.0, 0);
        addFood(list, "酱油", "调味品", 53, 5.6, 5.0, 0.1);
        addFood(list, "老干妈", "调味品", 590, 8.0, 12.0, 56.0);

        // 补剂
        addFood(list, "乳清蛋白粉", "补剂", 370, 80.0, 8.0, 2.0);
        addFood(list, "肌酸", "补剂", 0, 0, 0, 0);

        return list;
    }

    private void addFood(List<Map<String, Object>> list, String name, String category,
                          double calories, double protein, double carbs, double fat) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("category", category);
        m.put("calories", BigDecimal.valueOf(calories));
        m.put("protein", BigDecimal.valueOf(protein));
        m.put("carbs", BigDecimal.valueOf(carbs));
        m.put("fat", BigDecimal.valueOf(fat));
        m.put("servingSize", BigDecimal.valueOf(100));
        m.put("servingUnit", "g");
        list.add(m);
    }

    private static class StringUtils {
        static boolean hasText(String str) {
            return str != null && !str.trim().isEmpty();
        }
    }
}
