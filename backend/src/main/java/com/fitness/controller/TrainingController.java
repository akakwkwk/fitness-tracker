package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.TrainingRecordDTO;
import com.fitness.entity.ExerciseLibrary;
import com.fitness.entity.TrainingRecord;
import com.fitness.entity.TrainingTemplate;
import com.fitness.mapper.ExerciseLibraryMapper;
import com.fitness.service.TrainingService;
import com.fitness.service.TrainingTemplateService;
import com.fitness.utils.SecurityUtils;
import com.fitness.vo.TrainingStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "训练管理")
@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTemplateService templateService;
    private final ExerciseLibraryMapper exerciseLibraryMapper;

    @Operation(summary = "创建训练记录")
    @PostMapping
    public Result<TrainingRecord> createTraining(@Valid @RequestBody TrainingRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.createTraining(userId, dto));
    }

    @Operation(summary = "更新训练记录")
    @PutMapping("/{id}")
    public Result<TrainingRecord> updateTraining(@PathVariable Long id, @Valid @RequestBody TrainingRecordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.updateTraining(userId, id, dto));
    }

    @Operation(summary = "删除训练记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTraining(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        trainingService.deleteTraining(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取训练详情")
    @GetMapping("/{id}")
    public Result<TrainingRecord> getTrainingDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.getTrainingDetail(userId, id));
    }

    @Operation(summary = "获取训练列表")
    @GetMapping
    public Result<Page<TrainingRecord>> getTrainingList(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String trainingType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.getTrainingList(userId, startDate, endDate, trainingType, page, size));
    }

    @Operation(summary = "获取训练统计")
    @GetMapping("/stats")
    public Result<TrainingStatsVO> getTrainingStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.getTrainingStats(userId));
    }

    @Operation(summary = "获取训练日历")
    @GetMapping("/calendar")
    public Result<List<Map<String, Object>>> getTrainingCalendar(
            @RequestParam int year, @RequestParam int month) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.getTrainingCalendar(userId, year, month));
    }

    @Operation(summary = "获取连续打卡天数")
    @GetMapping("/streak")
    public Result<Integer> getStreakDays() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(trainingService.getStreakDays(userId));
    }

    // 动作库
    @Operation(summary = "获取动作库列表")
    @GetMapping("/exercises")
    public Result<List<ExerciseLibrary>> getExercises(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String muscleGroup) {
        return Result.success(exerciseLibraryMapper.selectList(null));
    }

    @Operation(summary = "添加自定义动作")
    @PostMapping("/exercises")
    public Result<ExerciseLibrary> addExercise(@RequestBody ExerciseLibrary exercise) {
        exercise.setStatus(1);
        exerciseLibraryMapper.insert(exercise);
        return Result.success(exercise);
    }

    @Operation(summary = "获取动作库分类")
    @GetMapping("/exercises/categories")
    public Result<List<String>> getExerciseCategories() {
        List<ExerciseLibrary> exercises = exerciseLibraryMapper.selectList(null);
        return Result.success(exercises.stream()
                .map(ExerciseLibrary::getCategory)
                .distinct()
                .sorted()
                .toList());
    }

    // 训练模板
    @Operation(summary = "获取训练模板列表")
    @GetMapping("/templates")
    public Result<List<TrainingTemplate>> getTemplates() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.getUserTemplates(userId));
    }

    @Operation(summary = "获取训练模板详情")
    @GetMapping("/templates/{id}")
    public Result<TrainingTemplate> getTemplateDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.getTemplateDetail(userId, id));
    }

    @Operation(summary = "创建训练模板")
    @PostMapping("/templates")
    public Result<TrainingTemplate> createTemplate(@Valid @RequestBody com.fitness.dto.TrainingTemplateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.createTemplate(userId, dto));
    }

    @Operation(summary = "更新训练模板")
    @PutMapping("/templates/{id}")
    public Result<TrainingTemplate> updateTemplate(@PathVariable Long id, @Valid @RequestBody com.fitness.dto.TrainingTemplateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(templateService.updateTemplate(userId, id, dto));
    }

    @Operation(summary = "删除训练模板")
    @DeleteMapping("/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        templateService.deleteTemplate(userId, id);
        return Result.success();
    }
}
