package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.User;
import com.example.health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public CommonResult<User> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            return CommonResult.failed("用户名或密码不能为空");
        }
        return CommonResult.success(userService.register(username, password));
    }

    @PostMapping("/login")
    public CommonResult<String> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        try {
            String token = userService.login(username, password);
            return CommonResult.success(token);
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}
