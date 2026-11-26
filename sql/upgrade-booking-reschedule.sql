-- 实验五：预约改期闭环
-- 在已有数据库上增量执行，可重复运行；全新安装请直接用 schema.sql。
USE script_booking;

DROP PROCEDURE IF EXISTS add_booking_reschedule_columns;

DELIMITER $$
CREATE PROCEDURE add_booking_reschedule_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking'
                     AND COLUMN_NAME = 'reschedule_count') THEN
        ALTER TABLE booking ADD COLUMN reschedule_count INT NOT NULL DEFAULT 0 AFTER party_size;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking'
                     AND COLUMN_NAME = 'original_session_id') THEN
        ALTER TABLE booking ADD COLUMN original_session_id BIGINT NULL AFTER session_id;
    END IF;
END$$
DELIMITER ;

CALL add_booking_reschedule_columns();
DROP PROCEDURE add_booking_reschedule_columns;

-- 首次改期时记录原场次，用于追溯；不建外键，避免场次下线后历史预约无法保留。
SELECT COUNT(*) AS booking_rows,
       SUM(reschedule_count) AS rescheduled_total
FROM booking;
