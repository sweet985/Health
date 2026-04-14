USE health_db;

-- 插入你刚才添加的三个视频到 resource 表
INSERT INTO `resource` (`type`, `title`, `content`) VALUES 
(4, '可爱小狗', '/videos/可爱小狗.mp4'),
(4, '水上小城', '/videos/水上小城.mp4'),
(4, '翠鸟', '/videos/翠鸟.mp4');
