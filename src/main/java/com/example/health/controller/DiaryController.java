package com.example.health.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.health.common.CommonResult;
import com.example.health.entity.Diary;
import com.example.health.entity.User;
import com.example.health.service.DiaryService;
import com.example.health.service.UserService;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;
    
    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @GetMapping("/list")
    public CommonResult<Page<Diary>> list(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        Page<Diary> diaryPage = new Page<>(page, size);
        QueryWrapper<Diary> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", getCurrentUserId());
        wrapper.orderByDesc("create_time");
        return CommonResult.success(diaryService.page(diaryPage, wrapper));
    }

    @PostMapping("/add")
    public CommonResult<Map<String, Object>> add(@RequestBody Diary diary) {
        Long userId = getCurrentUserId();
        diary.setUserId(userId);
        diaryService.save(diary);
        
        // Update mood score
        User user = userService.getById(userId);
        if (user.getMoodScore() == null) {
            user.setMoodScore(100);
        }
        
        int oldScore = user.getMoodScore();
        int change = 0;
        
        String mood = diary.getMood();
        if ("开心".equals(mood)) {
            change = 10;
        } else if ("平静".equals(mood)) {
            change = 0;
        } else {
            // Anxious, Sad, Angry, Fear, etc.
            change = -10;
        }
        
        int newScore = oldScore + change;
        // Clamp score
        if (newScore > 100) newScore = 100;
        if (newScore < 0) newScore = 0;
        
        user.setMoodScore(newScore);
        userService.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "日记已保存");
        result.put("score", newScore);
        
        // Alert logic: If score < 60 AND score decreased
        if (newScore < 60 && change < 0) {
            String advice = "";
            if (newScore < 40) {
                 advice = "您的心情指数处于低位，建议您尝试进行冥想或与信任的朋友倾诉。我们的心理咨询师也随时准备为您提供帮助。";
            } else {
                 advice = "最近是不是有些疲惫？给自己放个小假，做点喜欢的事情吧。";
            }
            result.put("alert", "low_score");
            result.put("alertMessage", "检测到您的心情指数较低（" + newScore + "分）。" + advice);
        }
        
        return CommonResult.success(result);
    }

    @PostMapping("/update")
    public CommonResult<String> update(@RequestBody Diary diary) {
        Diary old = diaryService.getById(diary.getId());
        if (old == null) return CommonResult.failed("日记不存在");
        if (!old.getUserId().equals(getCurrentUserId())) return CommonResult.failed("无权修改");
        
        diaryService.updateById(diary);
        return CommonResult.success("日记已更新");
    }

    @PostMapping("/delete/{id}")
    public CommonResult<String> delete(@PathVariable Long id) {
        Diary old = diaryService.getById(id);
        if (old == null) return CommonResult.failed("日记不存在");
        if (!old.getUserId().equals(getCurrentUserId())) return CommonResult.failed("无权删除");
        
        diaryService.removeById(id);
        return CommonResult.success("日记已删除");
    }
}
