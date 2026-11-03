-- =====================================================================
-- 老库升级脚本：注册手机号唯一约束（可重复执行）
-- 注册页使用 11 位手机号作为唯一的联系方式字段，字段本身已在 sys_user.phone，
-- 这里只补一个唯一约束，防止并发注册时写入重复手机号。
-- 若库中已存在重复手机号，ALTER 会失败：请先清理重复数据再执行。
-- =====================================================================

USE script_booking;

SET @has_phone_index := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'uk_sys_user_phone'
);
SET @add_phone_index := IF(@has_phone_index = 0,
    'ALTER TABLE sys_user ADD CONSTRAINT uk_sys_user_phone UNIQUE (phone)',
    'SELECT ''uk_sys_user_phone 已存在，跳过'' AS info');
PREPARE stmt_phone FROM @add_phone_index;
EXECUTE stmt_phone;
DEALLOCATE PREPARE stmt_phone;

SELECT '注册手机号唯一约束已就绪' AS result;
