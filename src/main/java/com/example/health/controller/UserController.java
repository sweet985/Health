package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.User;
import com.example.health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @GetMapping("/info")
    public CommonResult<User> getUserInfo() {
        return CommonResult.success(userService.getById(getCurrentUserId()));
    }

    @PostMapping("/update/password")
    public CommonResult<String> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return CommonResult.failed("旧密码或新密码不能为空");
        }
        
        try {
            userService.updatePasswordWithOld(getCurrentUserId(), oldPassword, newPassword);
            return CommonResult.success("修改成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/update/username")
    public CommonResult<String> updateUsername(@RequestBody Map<String, String> params) {
        String newUsername = params.get("username");
        try {
            userService.updateUsername(getCurrentUserId(), newUsername);
            return CommonResult.success("修改成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/update/avatar")
    public CommonResult<String> updateAvatar(@RequestBody Map<String, String> params) {
        String avatar = params.get("avatar");
        userService.updateAvatar(getCurrentUserId(), avatar);
        return CommonResult.success("修改成功");
    }

    @PostMapping("/update/bio")
    public CommonResult<String> updateBio(@RequestBody Map<String, String> params) {
        String bio = params.get("bio");
        if (bio != null && bio.length() > 50) {
            return CommonResult.failed("个人简介最多50个字");
        }
        userService.updateBio(getCurrentUserId(), bio);
        return CommonResult.success("修改成功");
    }

    @PostMapping("/update/mbti")
    public CommonResult<String> updateMbti(@RequestBody Map<String, String> params) {
        String mbti = params.get("mbti");
        userService.updateMbti(getCurrentUserId(), mbti);
        return CommonResult.success("修改成功");
    }

    @PostMapping("/delete")
    public CommonResult<String> deleteAccount() {
        userService.deleteAccount(getCurrentUserId());
        return CommonResult.success("账号已注销");
    }
    
    @GetMapping("/search")
    public CommonResult<User> searchUser(@RequestParam String username) {
        User user = userService.getByUsername(username);
        if (user != null) {
            user.setPassword(null); // Hide password
            return CommonResult.success(user);
        }
        return CommonResult.failed("用户不存在");
    }
}
