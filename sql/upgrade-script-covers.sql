-- =====================================================================
-- 老库升级脚本：把已上传到 uploads/covers 的剧本封面改为随 WAR 打包的
-- assets/img 静态图片，避免重新部署 Tomcat 后图片丢失（可重复执行）。
--
-- 背景：
--   /uploads/covers/* 位于 Tomcat 部署目录，重新部署 WAR 会被覆盖丢失，
--   且 SecurityFilter 要求登录后才能访问，前台未登录用户会看到裂图。
--   /assets/img/* 随 WAR 打包，重新部署不会丢失，且可公开访问。
--
-- 执行方式（Windows PowerShell）：
--   mysql -uroot -p --default-character-set=utf8mb4 -e "source D:/.../sql/upgrade-script-covers.sql"
-- =====================================================================

USE script_booking;

-- 1) 种子剧本 1、2 改用 assets/img 下的实拍封面
UPDATE script_book
SET cover_path = '/assets/img/cover-1.jpg'
WHERE id = 1;

UPDATE script_book
SET cover_path = '/assets/img/cover-2.jpg'
WHERE id = 2;

-- 2) 店长后台上传的「局中局」封面改用 assets/img 下的正式海报
UPDATE script_book
SET cover_path = '/assets/img/cover-12.jpg'
WHERE id = 12;

-- 3) 3~5 号剧本使用新放入 assets/img 的 PNG 封面
UPDATE script_book
SET cover_path = '/assets/img/cover-3.png'
WHERE id = 3;

UPDATE script_book
SET cover_path = '/assets/img/cover-4.png'
WHERE id = 4;

UPDATE script_book
SET cover_path = '/assets/img/cover-5.png'
WHERE id = 5;

-- 4) 其它仍指向 uploads/covers 的封面：如果对应文件已经复制到
--    assets/img，请按上面的写法逐条替换；下面仅处理空封面，避免误配。
UPDATE script_book
SET cover_path = '/assets/img/cover-1.jpg'
WHERE cover_path IS NULL OR TRIM(cover_path) = '';

SELECT id, title, cover_path
FROM script_book
ORDER BY id;
