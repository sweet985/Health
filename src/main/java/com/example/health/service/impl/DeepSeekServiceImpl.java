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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    @Override
    public String generateAssessmentReport(String testName, int score, String answersJson) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1500);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位权威的临床心理学专家。用户刚刚完成了一份名为《" + testName + "》的心理测评。请根据用户的总得分和具体每道题的答题详情，出具一份正式、专业、结构化的【心理测评临床报告】。\n\n" +
                "报告必须使用Markdown格式，排版要求像一份正式的医疗或心理诊断表单，语言要求客观、严谨、专业，避免过度口语化。\n\n" +
                "请严格按照以下结构输出报告：\n" +
                "### 📋 一、 测评概况\n" +
                "（简述得分情况、反映出的整体心理状态等级，如正常、轻度、中度、重度，并给出专业界定。）\n\n" +
                "### 🔍 二、 核心维度剖析\n" +
                "（这是报告的核心。结合用户得分较高的具体题目，使用心理学专业术语进行深入分析。指出主要的压力源、认知偏差或突出的症状表现，并解释其背后的心理机制。）\n\n" +
                "### ⚠️ 三、 风险评估\n" +
                "（评估当前的心理状态对日常社会功能、躯体健康可能造成的影响，预警潜在风险。）\n\n" +
                "### 💊 四、 临床干预与指导建议\n" +
                "（提供3-5条结构化的干预方案，如：1. 认知干预... 2. 行为调整... 3. 专业支持...。建议要具体、可操作。）");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "测评名称：" + testName + "\n总得分：" + score + "\n用户的具体答题详情（JSON格式，包含题目、选项和得分）：\n" + answersJson);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(90000)
                    .execute();

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    return messageObj.getStr("content");
                }
            } else {
                return "抱歉，AI深度分析报告生成失败，请稍后再试。（API Error: " + response.getStatus() + "）";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，连接AI服务出现了问题，无法生成分析报告，请稍后再试。";
        }
        
        return "抱歉，AI未能生成报告。";
    }

    @Override
    public void generateAssessmentReportStream(String testName, int score, String answersJson, SseEmitter emitter) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1500); // 允许较长的报告
        requestBody.put("stream", true); // 开启流式输出
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位权威的临床心理学专家。用户刚刚完成了一份名为《" + testName + "》的心理测评。请根据用户的总得分和具体每道题的答题详情，出具一份正式、专业、结构化的【心理测评临床报告】。\n\n" +
                "报告必须使用Markdown格式，排版要求像一份正式的医疗或心理诊断表单，语言要求客观、严谨、专业，避免过度口语化。\n\n" +
                "请严格按照以下结构输出报告：\n" +
                "### 📋 一、 测评概况\n" +
                "（简述得分情况、反映出的整体心理状态等级，如正常、轻度、中度、重度，并给出专业界定。）\n\n" +
                "### 🔍 二、 核心维度剖析\n" +
                "（这是报告的核心。结合用户得分较高的具体题目，使用心理学专业术语进行深入分析。指出主要的压力源、认知偏差或突出的症状表现，并解释其背后的心理机制。）\n\n" +
                "### ⚠️ 三、 风险评估\n" +
                "（评估当前的心理状态对日常社会功能、躯体健康可能造成的影响，预警潜在风险。）\n\n" +
                "### 💊 四、 临床干预与指导建议\n" +
                "（提供3-5条结构化的干预方案，如：1. 认知干预... 2. 行为调整... 3. 专业支持...。建议要具体、可操作。）");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "测评名称：" + testName + "\n总得分：" + score + "\n用户的具体答题详情（JSON格式，包含题目、选项和得分）：\n" + answersJson);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(90000)
                    .executeAsync(); // 异步执行以获取流

            if (response.isOk()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.bodyStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                            String data = line.substring(6);
                            JSONObject jsonResponse = JSONUtil.parseObj(data);
                            JSONArray choices = jsonResponse.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject delta = choice.getJSONObject("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = delta.getStr("content");
                                    // 发送片段到客户端
                                    emitter.send(SseEmitter.event().data(content));
                                }
                            }
                        }
                    }
                    emitter.complete();
                }
            } else {
                emitter.send(SseEmitter.event().name("error").data("API Error: " + response.getStatus()));
                emitter.complete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                emitter.send(SseEmitter.event().name("error").data("连接AI服务出现异常"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }
}
