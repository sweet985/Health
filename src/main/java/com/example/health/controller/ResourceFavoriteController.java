package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.Resource;
import com.example.health.entity.User;
import com.example.health.service.ResourceFavoriteService;
import com.example.health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/favorite")
public class ResourceFavoriteController {

    @Autowired
    private ResourceFavoriteService resourceFavoriteService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }

    @PostMapping("/toggle/{resourceId}")
    public CommonResult<Boolean> toggleFavorite(@PathVariable Long resourceId) {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        boolean isFavorited = resourceFavoriteService.toggleFavorite(user.getId(), resourceId);
        return CommonResult.success(isFavorited);
    }

    @GetMapping("/ids")
    public CommonResult<List<Long>> getUserFavoriteIds() {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        return CommonResult.success(resourceFavoriteService.getUserFavoriteIds(user.getId()));
    }

    @GetMapping("/list")
    public CommonResult<List<Resource>> getUserFavoriteResources() {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        return CommonResult.success(resourceFavoriteService.getUserFavoriteResources(user.getId()));
    }
}