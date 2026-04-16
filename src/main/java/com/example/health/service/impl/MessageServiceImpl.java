package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Message;
import com.example.health.mapper.MessageMapper;
import com.example.health.service.MessageService;
import com.example.health.websocket.WebSocketServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void sendMessage(Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsWithdrawn(false);
        message.setSenderDelete(false);
        message.setDeletedBySender(false);
        message.setDeletedByReceiver(false);
        save(message);
        
        // Push notification via WebSocket
        WebSocketServer.sendMessageToUser(receiverId, "NEW_MESSAGE");
        WebSocketServer.sendMessageToUser(senderId, "NEW_MESSAGE");
    }

    @Override
    public void withdrawMessage(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null) throw new RuntimeException("消息不存在");
        if (!message.getSenderId().equals(userId)) throw new RuntimeException("只能撤回自己发送的消息");
        
        // Check time (5 minutes)
        if (Duration.between(message.getCreateTime(), LocalDateTime.now()).toMinutes() > 5) {
            throw new RuntimeException("超过5分钟无法撤回");
        }
        
        message.setIsWithdrawn(true);
        updateById(message);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null) return;
        
        // Check permissions: user must be sender or receiver
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
             throw new RuntimeException("无权删除此消息");
        }
        
        // Unified deletion logic: delete for self
        if (message.getSenderId().equals(userId)) {
            message.setDeletedBySender(true);
            // Also set senderDelete for legacy withdrawn prompt support if needed
            if (message.getIsWithdrawn()) {
                message.setSenderDelete(true);
            }
        } else if (message.getReceiverId().equals(userId)) {
            message.setDeletedByReceiver(true);
        }
        
        updateById(message);
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, Long otherId) {
        // Mark all messages between these two users as deleted for the current user
        // 1. Messages I sent: update deletedBySender = true
        // 2. Messages I received: update deletedByReceiver = true
        
        // Batch update for sent messages
        QueryWrapper<Message> sentWrapper = new QueryWrapper<>();
        sentWrapper.eq("sender_id", userId).eq("receiver_id", otherId);
        Message sentUpdate = new Message();
        sentUpdate.setDeletedBySender(true);
        update(sentUpdate, sentWrapper);
        
        // Batch update for received messages
        QueryWrapper<Message> receivedWrapper = new QueryWrapper<>();
        receivedWrapper.eq("sender_id", otherId).eq("receiver_id", userId);
        Message receivedUpdate = new Message();
        receivedUpdate.setDeletedByReceiver(true);
        update(receivedUpdate, receivedWrapper);
    }

    @Override
    public List<Message> getMessages(Long userId, Long otherId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        
        // Condition: (sender=userId AND receiver=otherId) OR (sender=otherId AND receiver=userId)
        // AND handle deletion logic:
        // If I am sender, deletedBySender must be false (or null/0)
        // If I am receiver, deletedByReceiver must be false (or null/0)
        
        wrapper.and(w -> 
            w.and(s -> s.eq("sender_id", userId)
                        .eq("receiver_id", otherId)
                        .and(d -> d.eq("deleted_by_sender", 0).or().isNull("deleted_by_sender")))
             .or(r -> r.eq("sender_id", otherId)
                       .eq("receiver_id", userId)
                       .and(d -> d.eq("deleted_by_receiver", 0).or().isNull("deleted_by_receiver")))
        );
        
        wrapper.orderByAsc("create_time");
        List<Message> list = list(wrapper);
        
        // Legacy filter logic for withdrawn messages deleted by sender
        list.removeIf(m -> m.getIsWithdrawn() && Boolean.TRUE.equals(m.getSenderDelete()) && m.getSenderId().equals(userId));
        
        return list;
    }
}
