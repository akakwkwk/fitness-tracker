package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.BodyDataDTO;
import com.fitness.entity.BodyData;

import java.time.LocalDate;
import java.util.List;

public interface BodyService extends IService<BodyData> {
    BodyData addBodyData(Long userId, BodyDataDTO dto);
    BodyData updateBodyData(Long userId, Long dataId, BodyDataDTO dto);
    void deleteBodyData(Long userId, Long dataId);
    List<BodyData> getBodyDataList(Long userId, LocalDate startDate, LocalDate endDate);
    BodyData getLatestBodyData(Long userId);
}
