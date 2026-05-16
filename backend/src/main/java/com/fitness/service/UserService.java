package com.fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.UserUpdateDTO;
import com.fitness.entity.User;
import com.fitness.vo.LoginVO;
import com.fitness.vo.UserVO;

public interface UserService extends IService<User> {
    LoginVO login(LoginDTO dto, String ip);
    LoginVO register(RegisterDTO dto);
    void logout(String token);
    LoginVO refreshToken(String refreshToken);
    UserVO getCurrentUser(Long userId);
    UserVO updateUser(Long userId, UserUpdateDTO dto);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
