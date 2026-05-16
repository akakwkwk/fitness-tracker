package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.BusinessException;
import com.fitness.dto.BodyDataDTO;
import com.fitness.entity.BodyData;
import com.fitness.entity.User;
import com.fitness.mapper.BodyDataMapper;
import com.fitness.service.BodyService;
import com.fitness.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyServiceImpl extends ServiceImpl<BodyDataMapper, BodyData> implements BodyService {

    private final UserService userService;

    @Override
    public BodyData addBodyData(Long userId, BodyDataDTO dto) {
        BodyData data = new BodyData();
        BeanUtils.copyProperties(dto, data);
        data.setUserId(userId);

        // 自动计算BMI
        if (data.getWeight() != null) {
            User user = userService.getById(userId);
            if (user != null && user.getHeight() != null && user.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightInMeters = user.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal bmi = data.getWeight().divide(heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
                data.setBmi(bmi);
            }
        }

        save(data);
        return data;
    }

    @Override
    public BodyData updateBodyData(Long userId, Long dataId, BodyDataDTO dto) {
        BodyData data = getById(dataId);
        if (data == null || !data.getUserId().equals(userId)) {
            throw new BusinessException("身体数据不存在");
        }

        BeanUtils.copyProperties(dto, data);

        // 重新计算BMI
        if (data.getWeight() != null) {
            User user = userService.getById(userId);
            if (user != null && user.getHeight() != null && user.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightInMeters = user.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal bmi = data.getWeight().divide(heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
                data.setBmi(bmi);
            }
        }

        updateById(data);
        return data;
    }

    @Override
    public void deleteBodyData(Long userId, Long dataId) {
        BodyData data = getById(dataId);
        if (data == null || !data.getUserId().equals(userId)) {
            throw new BusinessException("身体数据不存在");
        }
        removeById(dataId);
    }

    @Override
    public List<BodyData> getBodyDataList(Long userId, LocalDate startDate, LocalDate endDate) {
        return list(new LambdaQueryWrapper<BodyData>()
                .eq(BodyData::getUserId, userId)
                .ge(startDate != null, BodyData::getRecordDate, startDate)
                .le(endDate != null, BodyData::getRecordDate, endDate)
                .eq(BodyData::getDeleted, 0)
                .orderByDesc(BodyData::getRecordDate));
    }

    @Override
    public BodyData getLatestBodyData(Long userId) {
        return getOne(new LambdaQueryWrapper<BodyData>()
                .eq(BodyData::getUserId, userId)
                .eq(BodyData::getDeleted, 0)
                .orderByDesc(BodyData::getRecordDate)
                .last("LIMIT 1"));
    }
}
