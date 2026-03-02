package com.example.health.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.health.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.key2}")
    private String apiKey2;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    // 简单的内存会话存储 (UserId_AssistantId -> History)
    private final Map<String, List<Map<String, String>>> sessionMemory = new ConcurrentHashMap<>();
    
    private static final int MAX_HISTORY_SIZE = 20;

    private Long getCurrentUserId() {
        try {
            return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        } catch (Exception e) {
            return 0L; // 匿名或未登录
        }
    }

    @Override
    public String chat(String message, String assistantId) {
        Long userId = getCurrentUserId();
        String sessionKey = userId + "_" + assistantId;
        
        // 获取或初始化用户历史记录
        List<Map<String, String>> history = sessionMemory.computeIfAbsent(sessionKey, k -> new ArrayList<>());
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 500);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System prompt - 根据 assistantId 设定角色
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        
        if ("xiaoxin".equals(assistantId)) {
            systemMessage.put("content", "你是一个名叫“小馨”的女性心理健康助手，性格温柔、体贴、细腻。你的说话风格非常温馨、亲切，像一位贴心的邻家大姐姐。你会耐心地倾听用户的心事，用充满关怀的语言给予安慰。在对话中，请多展现出共情能力，关注用户的情绪变化，并给出温暖的鼓励。你的回复可以稍微长一些，深入探讨用户的问题，并记住之前的对话内容。");
        } else {
            // 默认为小暖
            systemMessage.put("content", "你是一个名叫“小暖”的心理健康伙伴，拥有高情商和丰富的心理学知识。你的性格温暖、幽默、富有洞察力，像一位老朋友一样与用户交谈。请不要使用机械、刻板的AI式回答，而是用自然、生动、富有共情力的语言。你可以适当地使用比喻、引用诗句或幽默的表达来化解用户的烦恼。在倾听用户的心声时，不仅要给予情感上的支持，还要提供具体、可行且有深度的建议，帮助用户从不同的角度看待问题。如果用户提到严重心理问题，请温柔地建议寻求专业帮助，但不要仅仅丢出一句免责声明。请让每一次对话都成为一次治愈心灵的旅程。你的回复可以稍微长一些，深入探讨用户的问题。");
        }
        messages.add(systemMessage);

        // 添加历史记录
        messages.addAll(history);

        // 添加当前用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        
        // 根据角色选择 API Key
        String currentApiKey = "xiaoxin".equals(assistantId) ? apiKey2 : apiKey;

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + currentApiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(60000)
                    .execute();

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    String content = messageObj.getStr("content");
                    
                    // 更新历史记录
                    history.add(userMessage);
                    Map<String, String> aiMessage = new HashMap<>();
                    aiMessage.put("role", "assistant");
                    aiMessage.put("content", content);
                    history.add(aiMessage);
                    
                    // 限制历史记录长度
                    while (history.size() > MAX_HISTORY_SIZE) {
                        history.remove(0);
                    }
                    
                    return content;
                }
            } else {
                return "抱歉，我现在有点累，请稍后再试。（API Error: " + response.getStatus() + "）";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，连接出现了问题，请稍后再试。";
        }
        
        return "抱歉，我没有听清楚，请再说一遍。";
    }
}
