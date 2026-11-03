-- =====================================================================
-- 老库升级脚本：剧本封面图片 + 审核状态（可重复执行）
-- 适用：已经按旧版 schema.sql 建库、不想全量重建数据的库。
-- 全新安装请只执行 schema.sql（已包含以下全部改动）+ seed.sql。
-- 本脚本全程幂等：字段已存在会自动跳过，重复执行不会报错。
-- =====================================================================

USE script_booking;

-- 1) 封面图片字段（script_book 表，description 之后）
--    存储站点内相对路径，例如 /uploads/covers/script-xxx.jpg
--    或 /assets/img/cover-1.svg，浏览器通过 <img src> 直接访问。
SET @has_cover := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'script_book'
      AND COLUMN_NAME = 'cover_path'
);
SET @add_cover := IF(@has_cover = 0,
    'ALTER TABLE script_book ADD COLUMN cover_path VARCHAR(255) NULL AFTER description',
    'SELECT ''cover_path 列已存在，跳过'' AS info');
PREPARE stmt_cover FROM @add_cover;
EXECUTE stmt_cover;
DEALLOCATE PREPARE stmt_cover;

-- 2) 剧本审核状态
--    PENDING  待审核：店长提交后、管理员审核前，前台不可见
--    ACTIVE   已上架：审核通过（或管理员直接创建），前台"发现剧本"可见
--    INACTIVE 已下架：审核驳回或管理员手动下架，前台不可见
--    旧数据全部置为 ACTIVE，保证原有剧本继续在前台展示。
ALTER TABLE script_book
  MODIFY COLUMN status ENUM('PENDING','ACTIVE','INACTIVE') NOT NULL DEFAULT 'PENDING';
UPDATE script_book SET status = 'ACTIVE' WHERE status <> 'PENDING';

SELECT '剧本封面与审核状态升级完成' AS result;
