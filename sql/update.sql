USE health_db;

-- 补充 user 表缺失字段
ALTER TABLE `user` 
ADD COLUMN `bio` VARCHAR(255) DEFAULT '' COMMENT '个人简介',
ADD COLUMN `mbti` VARCHAR(10) DEFAULT '' COMMENT 'MBTI性格',
ADD COLUMN `mood_score` INT DEFAULT 100 COMMENT '心情分';

-- 补充 post 表缺失字段
ALTER TABLE `post`
ADD COLUMN `title` VARCHAR(255) DEFAULT '' COMMENT '标题',
ADD COLUMN `pseudonym` VARCHAR(100) DEFAULT '' COMMENT '匿名代号';

-- 补充 comment 表缺失字段
ALTER TABLE `comment`
ADD COLUMN `pseudonym` VARCHAR(100) DEFAULT '' COMMENT '匿名代号';

-- 补充 message 表缺失字段
ALTER TABLE `message`
ADD COLUMN `sender_delete` TINYINT(1) DEFAULT 0 COMMENT '发送者删除撤回消息',
ADD COLUMN `deleted_by_sender` TINYINT(1) DEFAULT 0 COMMENT '发送者删除',
ADD COLUMN `deleted_by_receiver` TINYINT(1) DEFAULT 0 COMMENT '接收者删除';

-- 新增 diary 日记表
CREATE TABLE IF NOT EXISTS `diary` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `mood` VARCHAR(50),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日记表';

-- 新增 assessment_record 评测记录表
CREATE TABLE IF NOT EXISTS `assessment_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `score` INT NOT NULL,
    `level` VARCHAR(50),
    `report_content` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评测记录表';

-- 新增 resource_favorite 资源收藏表
CREATE TABLE IF NOT EXISTS `resource_favorite` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源收藏表';
