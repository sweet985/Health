package com.example.health.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DeepSeekService {
    String chat(String message, String assistantId);
    String generateAssessmentReport(String testName, int score, String answersJson);
    void generateAssessmentReportStream(String testName, int score, String answersJson, SseEmitter emitter);
}
