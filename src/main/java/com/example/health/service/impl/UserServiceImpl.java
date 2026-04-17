package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.User;
import com.example.health.mapper.UserMapper;
import com.example.health.service.PostService;
import com.example.health.service.UserService;
import com.example.health.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostService postService;

    @Override
    public User register(String username, String password) {
        // Check if user exists (including deleted ones)
        User existingUser = userMapper.selectByUsernameIncludingDeleted(username);
        
        if (existingUser != null) {
            if (existingUser.getDeleted() == 0) {
                throw new RuntimeException("用户名已存在");
            } else {
                // Recover deleted user
                String encodedPassword = passwordEncoder.encode(password);
                int rows = userMapper.restoreUser(existingUser.getId(), encodedPassword);
                if (rows > 0) {
                    existingUser.setDeleted(0);
                    existingUser.setPassword(encodedPassword);
                    return existingUser;
                } else {
                    throw new RuntimeException("注册失败，请稍后重试");
                }
            }
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        save(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        User user = getByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public void updatePasswordWithOld(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        User user = getById(userId);
        user.setAvatar(avatar);
        updateById(user);
    }
    
    @Override
    public void updateUsername(Long userId, String newUsername) {
        // Check if username exists (including deleted users)
        User existingUser = userMapper.selectByUsernameIncludingDeleted(newUsername);
        
        if (existingUser != null) {
            // If the existing user is NOT the current user, then it's a duplicate
            if (!existingUser.getId().equals(userId)) {
                throw new RuntimeException("用户名已存在");
            }
            // If it IS the current user, we just proceed (or do nothing if name is same)
        }
        
        User user = getById(userId);
        user.setUsername(newUsername);
        updateById(user);
    }

    @Override
    public void updateBio(Long userId, String bio) {
        User user = getById(userId);
        user.setBio(bio);
        updateById(user);
    }

    @Override
    public void updateMbti(Long userId, String mbti) {
        User user = getById(userId);
        user.setMbti(mbti);
        updateById(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        // 1. Delete all posts published by this user (including comments and likes on them)
        postService.deletePostsByUserId(userId);
        
        // 2. Delete the user account (logical delete)
        removeById(userId);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new QueryWrapper<User>().eq("username", username));
    }
}
