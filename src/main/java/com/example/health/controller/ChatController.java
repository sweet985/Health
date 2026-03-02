package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.Message;
import com.example.health.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private MessageService messageService;

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @PostMapping("/send")
    public CommonResult<String> sendMessage(@RequestBody Map<String, Object> params) {
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = (String) params.get("content");
        messageService.sendMessage(getCurrentUserId(), receiverId, content);
        return CommonResult.success("发送成功");
    }

    @GetMapping("/history")
    public CommonResult<List<Message>> getHistory(@RequestParam Long otherId) {
        return CommonResult.success(messageService.getMessages(getCurrentUserId(), otherId));
    }
    
    @PostMapping("/withdraw/{id}")
    public CommonResult<String> withdrawMessage(@PathVariable Long id) {
        try {
            messageService.withdrawMessage(id, getCurrentUserId());
            return CommonResult.success("撤回成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }
    
    @PostMapping("/delete/{id}")
    public CommonResult<String> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id, getCurrentUserId());
            return CommonResult.success("删除成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/delete_conversation/{otherId}")
    public CommonResult<String> deleteConversation(@PathVariable Long otherId) {
        messageService.deleteConversation(getCurrentUserId(), otherId);
        return CommonResult.success("删除成功");
    }
}
