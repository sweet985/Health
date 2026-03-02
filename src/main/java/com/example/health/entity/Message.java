package com.example.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Boolean isWithdrawn;
    private Boolean senderDelete; // If sender deleted the withdrawn message (legacy, keep for compatibility or merge)
    
    // New fields for conversation deletion
    private Boolean deletedBySender;
    private Boolean deletedByReceiver;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
