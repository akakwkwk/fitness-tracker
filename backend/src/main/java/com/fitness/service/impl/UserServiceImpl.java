package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.BusinessException;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.UserUpdateDTO;
import com.fitness.entity.User;
import com.fitness.mapper.UserMapper;
import com.fitness.service.UserService;
import com.fitness.utils.JwtUtils;
import com.fitness.vo.LoginVO;
import com.fitness.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        user.setFitnessGoal(2); // 默认减脂
        user.setActivityLevel(2); // 默认轻度活动
        user.setDailyCalorieTarget(2000);
        user.setDailyProteinTarget(150);
        user.setDailyCarbTarget(250);
        user.setDailyFatTarget(65);
        user.setDailyWaterTarget(2000);
        save(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 将token加入黑名单
        redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 24, TimeUnit.HOURS);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtils.isTokenValid(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException(401, "refresh token无效");
        }

        Long userId = jwtUtils.extractUserId(refreshToken);
        String username = jwtUtils.extractUsername(refreshToken);

        String newToken = jwtUtils.generateToken(userId, username);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);

        User user = getById(userId);
        return LoginVO.builder()
                .userId(userId)
                .username(username)
                .nickname(user != null ? user.getNickname() : username)
                .avatar(user != null ? user.getAvatar() : null)
                .token(newToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserVO updateUser(Long userId, UserUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        if (dto.getHeight() != null) user.setHeight(dto.getHeight());
        if (dto.getWeight() != null) user.setWeight(dto.getWeight());
        if (dto.getActivityLevel() != null) user.setActivityLevel(dto.getActivityLevel());
        if (dto.getFitnessGoal() != null) user.setFitnessGoal(dto.getFitnessGoal());
        if (dto.getDailyCalorieTarget() != null) user.setDailyCalorieTarget(dto.getDailyCalorieTarget());
        if (dto.getDailyProteinTarget() != null) user.setDailyProteinTarget(dto.getDailyProteinTarget());
        if (dto.getDailyCarbTarget() != null) user.setDailyCarbTarget(dto.getDailyCarbTarget());
        if (dto.getDailyFatTarget() != null) user.setDailyFatTarget(dto.getDailyFatTarget());
        if (dto.getDailyWaterTarget() != null) user.setDailyWaterTarget(dto.getDailyWaterTarget());
        if (dto.getWeeklyTrainingTarget() != null) user.setWeeklyTrainingTarget(dto.getWeeklyTrainingTarget());

        updateById(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }
}
