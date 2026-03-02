package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public CommonResult<String> chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        String assistantId = params.getOrDefault("assistantId", "xiaonuan"); // 默认小暖
        
        if (message == null || message.trim().isEmpty()) {
            return CommonResult.failed("消息不能为空");
        }
        String response = deepSeekService.chat(message, assistantId);
        return CommonResult.success(response);
    }
}
