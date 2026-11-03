SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS script_booking
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE script_booking;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS play_session;
DROP TABLE IF EXISTS script_book;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(120) NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(60) NOT NULL,
  phone VARCHAR(30) NULL,
  status ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_user_username UNIQUE (username),
  CONSTRAINT uk_sys_user_email UNIQUE (email),
  -- 注册手机号唯一：并发注册时由数据库兜底；NULL 允许重复（演示账号可不填）
  CONSTRAINT uk_sys_user_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code ENUM('ADMIN','MANAGER','STAFF','PLAYER','VIEWER') NOT NULL,
  role_name VARCHAR(40) NOT NULL,
  CONSTRAINT uk_sys_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE script_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  script_code VARCHAR(30) NOT NULL,
  title VARCHAR(120) NOT NULL,
  genre VARCHAR(50) NOT NULL,
  min_players INT NOT NULL,
  max_players INT NOT NULL,
  duration_minutes INT NOT NULL,
  difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
  tags VARCHAR(255) NULL,
  description TEXT NULL,
  cover_path VARCHAR(255) NULL,
  status ENUM('PENDING','ACTIVE','INACTIVE') NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_script_code UNIQUE (script_code),
  CONSTRAINT ck_script_players CHECK (min_players > 0 AND max_players >= min_players),
  CONSTRAINT ck_script_duration CHECK (duration_minutes > 0),
  INDEX idx_script_search (status, genre, title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE play_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_code VARCHAR(30) NOT NULL,
  script_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  capacity INT NOT NULL,
  booked_count INT NOT NULL DEFAULT 0,
  room_name VARCHAR(60) NULL,
  dm_name VARCHAR(60) NULL,
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status ENUM('DRAFT','OPEN','CLOSED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  remark VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_session_code UNIQUE (session_code),
  CONSTRAINT fk_session_script FOREIGN KEY (script_id) REFERENCES script_book(id),
  CONSTRAINT ck_session_capacity CHECK (capacity > 0 AND booked_count >= 0 AND booked_count <= capacity),
  CONSTRAINT ck_session_time CHECK (end_time > start_time),
  CONSTRAINT ck_session_price CHECK (unit_price >= 0),
  INDEX idx_session_filter (start_time, status, script_id),
  INDEX idx_session_room_time (room_name, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE booking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_code VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  party_size INT NOT NULL,
  contact_name VARCHAR(60) NOT NULL,
  contact_phone VARCHAR(30) NOT NULL,
  status ENUM('CONFIRMED','CANCELLED_BY_PLAYER','CANCELLED_BY_STORE','CHECKED_IN','COMPLETED','NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
  player_note VARCHAR(500) NULL,
  active_user_session VARCHAR(100)
    GENERATED ALWAYS AS (
      CASE WHEN status IN ('CONFIRMED','CHECKED_IN') THEN CONCAT(user_id, ':', session_id) ELSE NULL END
    ) STORED,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_booking_code UNIQUE (booking_code),
  CONSTRAINT uk_booking_active UNIQUE (active_user_session),
  CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_booking_session FOREIGN KEY (session_id) REFERENCES play_session(id),
  CONSTRAINT ck_booking_party_size CHECK (party_size > 0),
  INDEX idx_booking_user (user_id, created_at),
  INDEX idx_booking_session_status (session_id, status),
  INDEX idx_booking_filter (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  story_rating TINYINT NULL,
  dm_rating TINYINT NULL,
  atmosphere_rating TINYINT NULL,
  content VARCHAR(2000) NOT NULL,
  status ENUM('PENDING','PUBLISHED','HIDDEN') NOT NULL DEFAULT 'PENDING',
  moderation_reason VARCHAR(500) NULL,
  moderated_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_review_booking UNIQUE (booking_id),
  CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_review_moderator FOREIGN KEY (moderated_by) REFERENCES sys_user(id),
  CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_story CHECK (story_rating IS NULL OR story_rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_dm CHECK (dm_rating IS NULL OR dm_rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_atmosphere CHECK (atmosphere_rating IS NULL OR atmosphere_rating BETWEEN 1 AND 5),
  INDEX idx_review_status (status, created_at),
  INDEX idx_review_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NULL,
  action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT NULL,
  detail VARCHAR(1000) NULL,
  ip_address VARCHAR(45) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id),
  INDEX idx_log_created (created_at),
  INDEX idx_log_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
