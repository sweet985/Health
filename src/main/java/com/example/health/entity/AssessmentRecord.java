package com.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_record")
public class AssessmentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer score;
    private String level;
    private String reportContent;
    private LocalDateTime createTime;
}
