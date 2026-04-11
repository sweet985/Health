package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.AssessmentRecord;

import java.util.List;

public interface AssessmentRecordService extends IService<AssessmentRecord> {
    AssessmentRecord saveRecord(Long userId, Integer score, String level, String reportContent);
    List<AssessmentRecord> getUserRecords(Long userId);
}
