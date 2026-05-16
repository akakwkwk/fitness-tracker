package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.BusinessException;
import com.fitness.dto.TrainingTemplateDTO;
import com.fitness.entity.TemplateExercise;
import com.fitness.entity.TrainingTemplate;
import com.fitness.mapper.TemplateExerciseMapper;
import com.fitness.mapper.TrainingTemplateMapper;
import com.fitness.service.TrainingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTemplateServiceImpl extends ServiceImpl<TrainingTemplateMapper, TrainingTemplate> implements TrainingTemplateService {

    private final TemplateExerciseMapper templateExerciseMapper;

    @Override
    @Transactional
    public TrainingTemplate createTemplate(Long userId, TrainingTemplateDTO dto) {
        TrainingTemplate template = new TrainingTemplate();
        template.setUserId(userId);
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setTrainingType(dto.getTrainingType());
        template.setEstimatedDuration(dto.getEstimatedDuration());
        template.setUseCount(0);
        template.setStatus(1);
        save(template);

        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingTemplateDTO.TemplateExerciseDTO exDto = dto.getExercises().get(i);
                TemplateExercise exercise = new TemplateExercise();
                exercise.setTemplateId(template.getId());
                exercise.setExerciseLibraryId(exDto.getExerciseLibraryId());
                exercise.setExerciseName(exDto.getExerciseName());
                exercise.setTargetSets(exDto.getTargetSets());
                exercise.setTargetReps(exDto.getTargetReps());
                exercise.setTargetWeight(exDto.getTargetWeight());
                exercise.setRestTime(exDto.getRestTime());
                exercise.setSortOrder(exDto.getSortOrder() != null ? exDto.getSortOrder() : i);
                templateExerciseMapper.insert(exercise);
            }
        }

        return template;
    }

    @Override
    @Transactional
    public TrainingTemplate updateTemplate(Long userId, Long templateId, TrainingTemplateDTO dto) {
        TrainingTemplate template = getById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException("模板不存在");
        }

        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setTrainingType(dto.getTrainingType());
        template.setEstimatedDuration(dto.getEstimatedDuration());
        updateById(template);

        // 删除旧的动作
        templateExerciseMapper.delete(new LambdaQueryWrapper<TemplateExercise>()
                .eq(TemplateExercise::getTemplateId, templateId));

        // 保存新的动作
        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingTemplateDTO.TemplateExerciseDTO exDto = dto.getExercises().get(i);
                TemplateExercise exercise = new TemplateExercise();
                exercise.setTemplateId(templateId);
                exercise.setExerciseLibraryId(exDto.getExerciseLibraryId());
                exercise.setExerciseName(exDto.getExerciseName());
                exercise.setTargetSets(exDto.getTargetSets());
                exercise.setTargetReps(exDto.getTargetReps());
                exercise.setTargetWeight(exDto.getTargetWeight());
                exercise.setRestTime(exDto.getRestTime());
                exercise.setSortOrder(exDto.getSortOrder() != null ? exDto.getSortOrder() : i);
                templateExerciseMapper.insert(exercise);
            }
        }

        return template;
    }

    @Override
    public void deleteTemplate(Long userId, Long templateId) {
        TrainingTemplate template = getById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException("模板不存在");
        }
        removeById(templateId);
    }

    @Override
    public List<TrainingTemplate> getUserTemplates(Long userId) {
        return list(new LambdaQueryWrapper<TrainingTemplate>()
                .eq(TrainingTemplate::getUserId, userId)
                .eq(TrainingTemplate::getStatus, 1)
                .orderByDesc(TrainingTemplate::getUseCount));
    }

    @Override
    public TrainingTemplate getTemplateDetail(Long userId, Long templateId) {
        TrainingTemplate template = getById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException("模板不存在");
        }

        List<TemplateExercise> exercises = templateExerciseMapper.selectList(
                new LambdaQueryWrapper<TemplateExercise>()
                        .eq(TemplateExercise::getTemplateId, templateId)
                        .orderByAsc(TemplateExercise::getSortOrder));
        template.setExercises(exercises);
        return template;
    }
}
