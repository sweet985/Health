package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Friendship;
import com.example.health.entity.User;
import com.example.health.mapper.FriendshipMapper;
import com.example.health.mapper.UserMapper;
import com.example.health.service.FriendshipService;
import com.example.health.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship> implements FriendshipService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public void addFriend(Long userId, Long friendId, String messageContent) {
        if (userId.equals(friendId)) throw new RuntimeException("不能添加自己为好友");
        
        // Check if exists
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("friend_id", friendId);
        if (count(wrapper) > 0) throw new RuntimeException("已发送申请或已是好友");

        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(0); // Pending
        friendship.setMessage(messageContent);
        save(friendship);
    }

    @Override
    @Transactional
    public void acceptFriend(Long userId, Long requesterId) {
        System.out.println("Accepting friend request: Me=" + userId + ", Requester=" + requesterId);
        
        // Find the request: user_id=requester, friend_id=me
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", requesterId).eq("friend_id", userId);
        
        List<Friendship> requests = list(wrapper);
        Friendship request = null;
        
        for (Friendship f : requests) {
            if (f.getStatus() == 0) {
                request = f;
                break;
            }
        }
        
        if (request == null && !requests.isEmpty()) {
             request = requests.get(0);
        }

        if (request == null) throw new RuntimeException("申请不存在");
        
        // 1. Update the requester's record (Requester -> Me) to status 1
        request.setStatus(1);
        updateById(request);
        
        // 2. FORCE Create or Update my record (Me -> Requester) to status 1
        QueryWrapper<Friendship> reverseWrapper = new QueryWrapper<>();
        reverseWrapper.eq("user_id", userId).eq("friend_id", requesterId);
        Friendship reverse = getOne(reverseWrapper);
        
        if (reverse == null) {
            reverse = new Friendship();
            reverse.setUserId(userId);
            reverse.setFriendId(requesterId);
            reverse.setStatus(1);
            save(reverse);
        } else {
            reverse.setStatus(1);
            updateById(reverse);
        }
        System.out.println("Updated/Created reverse record");
        
        // 3. Send system message to both users to initialize chat
        messageService.sendMessage(userId, requesterId, "我们已经是好友了，开始聊天吧！");
    }
    
    @Override
    @Transactional
    public void rejectFriend(Long userId, Long requesterId, String reason) {
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", requesterId).eq("friend_id", userId).eq("status", 0);
        Friendship request = getOne(wrapper);
        if (request == null) throw new RuntimeException("申请不存在");
        
        removeById(request.getId());
        messageService.sendMessage(userId, requesterId, "拒绝了好友申请: " + (reason != null ? reason : "无理由"));
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("friend_id", friendId)
               .or()
               .eq("user_id", friendId).eq("friend_id", userId);
        remove(wrapper);
    }

    @Override
    public List<Friendship> getFriends(Long userId) {
        System.out.println("Getting friends for user: " + userId);
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("status", 1);
        List<Friendship> list = list(wrapper);
        
        // Filter out friendships where user does not exist (e.g. deleted user)
        // And populate friendInfo
        return list.stream().filter(f -> {
            User friend = userMapper.selectById(f.getFriendId());
            if (friend != null) {
                friend.setPassword(null);
                f.setFriendInfo(friend);
                return true;
            }
            return false; // Remove if friend user not found
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Friendship> getPendingRequests(Long userId) {
        QueryWrapper<Friendship> wrapper = new QueryWrapper<>();
        wrapper.eq("friend_id", userId).eq("status", 0);
        List<Friendship> list = list(wrapper);
        
        for (Friendship f : list) {
            User requester = userMapper.selectById(f.getUserId());
            if (requester != null) {
                requester.setPassword(null);
                f.setFriendInfo(requester);
            }
        }
        return list;
    }
}
