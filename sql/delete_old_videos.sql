USE health_db;

-- 专门删除你点名的这三个旧视频（保留你自己加的新视频）
DELETE FROM `resource` 
WHERE `type` = 4 
AND `title` IN ('Elephants Dream', 'Big Buck Bunny', 'For Bigger Blazes');
