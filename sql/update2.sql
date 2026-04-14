USE health_db;

-- 补充 friendship 表缺失字段
ALTER TABLE `friendship` 
ADD COLUMN `message` VARCHAR(255) DEFAULT '' COMMENT '申请留言';
