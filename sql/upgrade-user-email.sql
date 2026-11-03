-- 注册功能所需的邮箱字段（老库升级脚本，可重复执行；重复执行报 Duplicate column name 时忽略即可）
USE script_booking;

ALTER TABLE sys_user
  ADD COLUMN email VARCHAR(120) NULL AFTER username;

ALTER TABLE sys_user
  ADD CONSTRAINT uk_sys_user_email UNIQUE (email);
