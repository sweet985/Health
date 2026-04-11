package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.AssessmentRecord;
import com.example.health.entity.User;
import com.example.health.service.AssessmentRecordService;
import com.example.health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment/record")
public class AssessmentRecordController {

    @Autowired
    private AssessmentRecordService assessmentRecordService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.getByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/save")
    public CommonResult<AssessmentRecord> saveRecord(@RequestBody AssessmentRecord record) {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        AssessmentRecord saved = assessmentRecordService.saveRecord(
                user.getId(), 
                record.getScore(), 
                record.getLevel(), 
                record.getReportContent()
        );
        return CommonResult.success(saved);
    }

    @GetMapping("/my")
    public CommonResult<List<AssessmentRecord>> getMyRecords() {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        return CommonResult.success(assessmentRecordService.getUserRecords(user.getId()));
    }

    @DeleteMapping("/{id}")
    public CommonResult<?> deleteRecord(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            return CommonResult.failed("未登录");
        }
        AssessmentRecord record = assessmentRecordService.getById(id);
        if (record == null) {
            return CommonResult.failed("记录不存在");
        }
        if (!record.getUserId().equals(user.getId())) {
            return CommonResult.failed("无权删除");
        }
        assessmentRecordService.removeById(id);
        return CommonResult.success(null);
    }
}
