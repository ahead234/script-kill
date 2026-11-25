-- 实验四增量：房间实体（个人项目 MVP 的第二个核心实体）
-- 说明：现有 play_session.room_name 是自由文本，缺少容量、位置与可用状态；
-- 本脚本新增 room 表，并把已有场次里出现过的房间去重迁移为房间档案。
-- 可重复执行。
SET NAMES utf8mb4;
USE script_booking;

CREATE TABLE IF NOT EXISTS room (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_code VARCHAR(20) NOT NULL,
  room_name VARCHAR(60) NOT NULL,
  capacity INT NOT NULL,
  location VARCHAR(80) NULL,
  status ENUM('ACTIVE','MAINTENANCE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_room_code UNIQUE (room_code),
  CONSTRAINT uk_room_name UNIQUE (room_name),
  -- 剧本杀房间至少容纳 2 人，单房间不超过 30 人
  CONSTRAINT ck_room_capacity CHECK (capacity BETWEEN 2 AND 30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO room (room_code, room_name, capacity, location, status) VALUES
  ('R01', '沉浸一号房', 6, '一层东侧', 'ACTIVE'),
  ('R02', '推理二号房', 6, '一层西侧', 'ACTIVE'),
  ('R03', '欢乐三号房', 7, '二层中庭', 'ACTIVE'),
  ('R04', '恐怖四号房', 7, '二层北侧', 'ACTIVE'),
  ('R05', '机制五号房', 6, '三层南侧', 'ACTIVE')
AS new
ON DUPLICATE KEY UPDATE capacity = new.capacity;
