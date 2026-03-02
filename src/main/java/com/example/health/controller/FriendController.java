package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.Friendship;
import com.example.health.entity.User;
import com.example.health.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendshipService friendshipService;

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @PostMapping("/add/{friendId}")
    public CommonResult<String> addFriend(@PathVariable Long friendId, @RequestBody(required = false) Map<String, String> params) {
        try {
            String message = params != null ? params.get("message") : null;
            friendshipService.addFriend(getCurrentUserId(), friendId, message);
            return CommonResult.success("申请已发送");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/accept/{requesterId}")
    public CommonResult<String> acceptFriend(@PathVariable Long requesterId) {
        try {
            friendshipService.acceptFriend(getCurrentUserId(), requesterId);
            return CommonResult.success("已添加好友");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }
    
    @PostMapping("/reject/{requesterId}")
    public CommonResult<String> rejectFriend(@PathVariable Long requesterId, @RequestBody(required = false) Map<String, String> params) {
        try {
            String reason = params != null ? params.get("reason") : null;
            friendshipService.rejectFriend(getCurrentUserId(), requesterId, reason);
            return CommonResult.success("已拒绝申请");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/delete/{friendId}")
    public CommonResult<String> deleteFriend(@PathVariable Long friendId) {
        friendshipService.deleteFriend(getCurrentUserId(), friendId);
        return CommonResult.success("删除成功");
    }

    @GetMapping("/list")
    public CommonResult<List<Friendship>> getFriends() {
        return CommonResult.success(friendshipService.getFriends(getCurrentUserId()));
    }

    @GetMapping("/requests")
    public CommonResult<List<Friendship>> getPendingRequests() {
        return CommonResult.success(friendshipService.getPendingRequests(getCurrentUserId()));
    }
}
