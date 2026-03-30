package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private DeepSeekService deepSeekService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

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

    @PostMapping("/assessment-report")
    public CommonResult<String> generateAssessmentReport(@RequestBody Map<String, Object> params) {
        String testName = (String) params.get("testName");
        Integer score = (Integer) params.get("score");
        String answersJson = (String) params.get("answersJson");

        if (testName == null || score == null || answersJson == null) {
            return CommonResult.failed("参数不完整");
        }

        // 仅在后端返回完整字符串时使用。如果改为 SSE (Server-Sent Events) 需要修改返回类型为 SseEmitter
        String report = deepSeekService.generateAssessmentReport(testName, score, answersJson);
        return CommonResult.success(report);
    }

    @PostMapping("/assessment-report-stream")
    public SseEmitter generateAssessmentReportStream(@RequestBody Map<String, Object> params) {
        String testName = (String) params.get("testName");
        Integer score = (Integer) params.get("score");
        String answersJson = (String) params.get("answersJson");

        // 设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(300000L);

        if (testName == null || score == null || answersJson == null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("参数不完整"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        executor.execute(() -> {
            try {
                deepSeekService.generateAssessmentReportStream(testName, score, answersJson, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
