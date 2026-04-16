USE health_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空用户表及所有相关的用户生成数据
TRUNCATE TABLE `user`;
TRUNCATE TABLE `post`;
TRUNCATE TABLE `post_like`;
TRUNCATE TABLE `comment`;
TRUNCATE TABLE `friendship`;
TRUNCATE TABLE `message`;
TRUNCATE TABLE `diary`;
TRUNCATE TABLE `assessment_record`;
TRUNCATE TABLE `resource_favorite`;

SET FOREIGN_KEY_CHECKS = 1;