package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.Message;
import java.util.List;

public interface MessageService extends IService<Message> {
    void sendMessage(Long senderId, Long receiverId, String content);
    void withdrawMessage(Long messageId, Long userId);
    void deleteMessage(Long messageId, Long userId);
    void deleteConversation(Long userId, Long otherId); // New method
    List<Message> getMessages(Long userId, Long otherId);
}
