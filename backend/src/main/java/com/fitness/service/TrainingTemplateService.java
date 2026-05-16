package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.TrainingTemplateDTO;
import com.fitness.entity.TrainingTemplate;

import java.util.List;

public interface TrainingTemplateService extends IService<TrainingTemplate> {
    TrainingTemplate createTemplate(Long userId, TrainingTemplateDTO dto);
    TrainingTemplate updateTemplate(Long userId, Long templateId, TrainingTemplateDTO dto);
    void deleteTemplate(Long userId, Long templateId);
    List<TrainingTemplate> getUserTemplates(Long userId);
    TrainingTemplate getTemplateDetail(Long userId, Long templateId);
}
