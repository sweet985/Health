package com.example.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type; // 1-TreeHole, 2-Community
    private String title;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private String pseudonym; // For TreeHole
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private String username; // For display
    @TableField(exist = false)
    private String userAvatar; // For display
    @TableField(exist = false)
    private Boolean isLiked; // Current user liked?
}
