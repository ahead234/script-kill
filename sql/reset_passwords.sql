-- 将全部演示账号的密码重置为 123456（BCrypt，cost 10）。
-- 适用于已经导入过 sql/seed.sql 的库，无需重建表。
-- 执行方式（PowerShell）：
--   mysql -uroot -p --default-character-set=utf8mb4 -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/reset_passwords.sql"
-- 仅用于本地教学演示，部署前必须修改密码。

SET NAMES utf8mb4;

USE script_booking;

UPDATE sys_user SET password_hash='$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2';

SELECT id, username, display_name, status FROM sys_user ORDER BY id;
