USE health_db;

-- 一键清空原来所有旧的治愈图片（如果你不想看到旧图了，执行这句）
DELETE FROM `resource` WHERE `type` = 2;

-- 在这里登记你的新图片（把名字和路径改成你自己的，去掉前面的 -- 即可执行）
-- INSERT INTO `resource` (`type`, `title`, `content`) VALUES 
-- (2, '我的新图片1', '/images/你的图片1.jpg'),
-- (2, '我的新图片2', '/images/你的图片2.png');
