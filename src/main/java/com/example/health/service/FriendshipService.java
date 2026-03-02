package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.Friendship;
import com.example.health.entity.User;
import java.util.List;

public interface FriendshipService extends IService<Friendship> {
    void addFriend(Long userId, Long friendId, String message);
    void acceptFriend(Long userId, Long requesterId);
    void rejectFriend(Long userId, Long requesterId, String reason);
    void deleteFriend(Long userId, Long friendId);
    List<Friendship> getFriends(Long userId);
    List<Friendship> getPendingRequests(Long userId);
}
