package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.User;

public interface UserService extends IService<User> {
    User register(String username, String password);
    String login(String username, String password);
    void updatePasswordWithOld(Long userId, String oldPassword, String newPassword);
    void updateAvatar(Long userId, String avatar);
    void updateUsername(Long userId, String newUsername);
    void updateBio(Long userId, String bio);
    void updateMbti(Long userId, String mbti);
    void deleteAccount(Long userId);
    User getByUsername(String username);
}
