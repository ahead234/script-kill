SET NAMES utf8mb4;

USE script_booking;

-- 可重复导入：先清空演示数据，避免主键/唯一键冲突。表结构由 schema.sql 负责。
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_operation_log;
TRUNCATE TABLE review;
TRUNCATE TABLE booking;
TRUNCATE TABLE play_session;
TRUNCATE TABLE room;
TRUNCATE TABLE script_book;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_role (id, role_code, role_name) VALUES
(1, 'ADMIN', '系统管理员'),
(2, 'MANAGER', '店长'),
(3, 'STAFF', '店员'),
(4, 'PLAYER', '玩家'),
(5, 'VIEWER', '只读用户');

-- 以下演示账号密码均为 123456，仅用于本地教学演示，部署前必须修改。
INSERT INTO sys_user (id, username, password_hash, display_name, phone, status) VALUES
(1, 'admin',   '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '演示管理员', '13800001001', 'ACTIVE'),
(2, 'manager', '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '演示店长',   '13800001002', 'ACTIVE'),
(3, 'staff',   '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '演示店员',   '13800001003', 'ACTIVE'),
(4, 'player',  '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '林小舟',     '13800001004', 'ACTIVE'),
(5, 'viewer',  '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '演示访客',   NULL, 'ACTIVE'),
(6, 'disabled','$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '禁用用户',   NULL, 'DISABLED'),
(7, 'player2', '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '陈默',       '13800001007', 'ACTIVE'),
(8, 'player3', '$2a$10$qylMW0x/8iKSswbnXeLQYu8JEAyozCjBGWJAbx9Z6ccDWYbS.8ze2', '苏晓',       '13800001008', 'ACTIVE');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5),(6,4),(7,4),(8,4);

INSERT INTO script_book
(id, script_code, title, genre, min_players, max_players, duration_minutes, difficulty, tags, description, cover_path, status) VALUES
(1, 'SB-001', '雾港来信', '情感', 5, 6, 240, 'MEDIUM', '民国,还原,沉浸', '一封迟到多年的信，将六位旧友重新带回雾港。', '/assets/img/cover-1.jpg', 'ACTIVE'),
(2, 'SB-002', '第七码头', '推理', 4, 6, 210, 'HARD', '硬核,本格,密室', '封闭码头中的离奇失踪案，线索与时间证词互相矛盾。', '/assets/img/cover-2.jpg', 'ACTIVE'),
(3, 'SB-003', '春日邮局', '欢乐', 4, 7, 180, 'EASY', '欢乐,机制,新手', '经营一间会寄送回忆的邮局，适合新手玩家。', '/assets/img/cover-3.png', 'ACTIVE'),
(4, 'SB-004', '旧城夜雨', '悬疑', 5, 7, 270, 'MEDIUM', '悬疑,阵营,城市', '旧城改造前夜，各方秘密随着大雨浮出水面。', '/assets/img/cover-4.png', 'INACTIVE'),
(5, 'SB-005', '长夜将尽', '恐怖', 5, 7, 200, 'HARD', '恐怖,搜证,心理', '山区旅馆的最后一夜，每位住客都藏着不敢说出口的事。', '/assets/img/cover-5.png', 'ACTIVE'),
(6, 'SB-006', '鲸落之城', '情感', 6, 8, 260, 'MEDIUM', '情感,沉浸,抉择', '一座靠捕鲸兴起的城市，在禁捕令下达后走向各自的结局。', '/assets/img/cover-1.svg', 'ACTIVE'),
(7, 'SB-007', '午夜牌局', '机制', 4, 6, 170, 'EASY', '机制,博弈,欢乐', '每局结束都会有人离开，活到最后的赢家只有一个。', '/assets/img/cover-3.svg', 'ACTIVE'),
(8, 'SB-008', '雾中列车', '推理', 5, 6, 230, 'HARD', '本格,密室,列车', '列车穿过浓雾时，第一节车厢的乘客消失了。', '/assets/img/cover-2.svg', 'ACTIVE'),
(9, 'SB-009', '旧胶片',   '悬疑', 4, 7, 190, 'MEDIUM', '悬疑,还原,年代', '一卷未冲洗的胶片，记录着一场没人愿意提起的意外。', '/assets/img/cover-4.svg', 'ACTIVE'),
(10,'SB-010', '山雨欲来', '阵营', 6, 8, 280, 'HARD', '阵营,对抗,策略', '两方势力在暴雨前夜谈判，每个人都有自己的价码。', '/assets/img/cover-2.svg', 'INACTIVE'),
(11,'SB-011', '糖霜小镇', '欢乐', 5, 8, 150, 'EASY', '新手,欢乐,派对', '小镇甜品大赛开赛，配方失窃只是这场闹剧的开始。', '/assets/img/cover-3.svg', 'ACTIVE');

INSERT INTO room (id, room_code, room_name, capacity, location, status, note) VALUES
(1, 'R01', '沉浸一号房', 6, '一层东侧', 'ACTIVE', '带投影与环绕音响，适合沉浸本'),
(2, 'R02', '推理二号房', 6, '一层西侧', 'ACTIVE', '长桌布局，便于集中讨论'),
(3, 'R03', '欢乐三号房', 7, '二层中庭', 'ACTIVE', NULL),
(4, 'R04', '恐怖四号房', 7, '二层北侧', 'ACTIVE', '可调暗光，配独立音效通道'),
(5, 'R05', '机制五号房', 6, '三层南侧', 'ACTIVE', '投影设备较旧，建议提前调试');

INSERT INTO play_session
(id, session_code, script_id, start_time, end_time, capacity, booked_count, room_name, dm_name, unit_price, status, remark) VALUES
(1, 'SS-20261101-01', 1, '2026-11-01 14:00:00', '2026-11-01 18:00:00', 6, 4, '沉浸一号房', '阿远', 128.00, 'OPEN', '请提前十五分钟到店'),
(2, 'SS-20261101-02', 2, '2026-11-01 19:00:00', '2026-11-01 22:30:00', 6, 3, '推理二号房', '小满', 138.00, 'OPEN', NULL),
(3, 'SS-20261102-01', 3, '2026-11-02 13:30:00', '2026-11-02 16:30:00', 7, 2, '欢乐三号房', '南星', 98.00, 'OPEN', NULL),
(4, 'SS-20261020-01', 1, '2026-10-20 14:00:00', '2026-10-20 18:00:00', 6, 0, '沉浸一号房', '阿远', 128.00, 'COMPLETED', NULL),
(5, 'SS-20261103-01', 5, '2026-11-03 19:00:00', '2026-11-03 22:20:00', 7, 0, '恐怖四号房', '夜阑', 148.00, 'OPEN', '恐怖本，建议有经验玩家'),
(6, 'SS-20261104-01', 8, '2026-11-04 14:00:00', '2026-11-04 17:50:00', 6, 0, '推理二号房', '小满', 158.00, 'OPEN', NULL),
(7, 'SS-20261105-01', 7, '2026-11-05 20:00:00', '2026-11-05 22:50:00', 6, 0, '机制五号房', '南星', 108.00, 'OPEN', NULL);

INSERT INTO booking
(id, booking_code, user_id, session_id, party_size, contact_name, contact_phone, status, player_note, created_at) VALUES
(1, 'BK202610290001', 4, 1, 2, '林小舟', '13800001004', 'CONFIRMED', '两位均为新手', '2026-10-29 10:15:00'),
(2, 'BK202610260001', 4, 4, 2, '林小舟', '13800001004', 'COMPLETED', NULL, '2026-10-26 16:40:00'),
(3, 'BK202610300001', 7, 2, 3, '陈默',   '13800001007', 'CONFIRMED', '希望安排硬核 DM', '2026-10-30 19:05:00'),
(4, 'BK202610310001', 8, 3, 2, '苏晓',   '13800001008', 'CONFIRMED', NULL, '2026-10-31 09:20:00'),
(5, 'BK202610270001', 7, 4, 2, '陈默',   '13800001007', 'COMPLETED', NULL, '2026-10-27 20:10:00'),
(6, 'BK202610310002', 8, 1, 2, '苏晓',   '13800001008', 'CHECKED_IN', NULL, '2026-10-31 13:45:00'),
(7, 'BK202610280001', 8, 4, 1, '苏晓',   '13800001008', 'COMPLETED', NULL, '2026-10-28 21:30:00');

INSERT INTO review
(id, booking_id, user_id, rating, story_rating, dm_rating, atmosphere_rating, content, status, moderation_reason, moderated_by) VALUES
(1, 2, 4, 5, 5, 5, 4, '故事完整，DM 引导自然，整体体验很好。', 'PUBLISHED', '内容正常', 2),
(2, 5, 7, 4, 4, 5, 4, '机制设计有趣，后半段节奏稍慢，整体仍推荐。', 'PENDING', NULL, NULL),
(3, 7, 8, 5, 5, 4, 5, '氛围很好，DM 复盘清晰，但内容涉及结局。', 'HIDDEN', '包含关键结局剧透', 2);

INSERT INTO sys_operation_log
(operator_id, action, entity_type, entity_id, detail, ip_address) VALUES
(2, 'REVIEW_PUBLISH', 'REVIEW', 1, '发布演示评价', '127.0.0.1');
