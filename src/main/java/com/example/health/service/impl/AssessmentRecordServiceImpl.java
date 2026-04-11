package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.AssessmentRecord;
import com.example.health.mapper.AssessmentRecordMapper;
import com.example.health.service.AssessmentRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssessmentRecordServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentRecordService {

    @Override
    public AssessmentRecord saveRecord(Long userId, Integer score, String level, String reportContent) {
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setScore(score);
        record.setLevel(level);
        record.setReportContent(reportContent);
        record.setCreateTime(LocalDateTime.now());
        this.save(record);
        return record;
    }

    @Override
    public List<AssessmentRecord> getUserRecords(Long userId) {
        QueryWrapper<AssessmentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}
