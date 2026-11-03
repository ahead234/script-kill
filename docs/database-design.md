# 数据库设计

数据库使用 MySQL 8、InnoDB、`utf8mb4`，主键使用 `BIGINT AUTO_INCREMENT`，业务编号另设唯一约束。

## 表与关系

| 表 | 用途 | 关键约束 |
|---|---|---|
| `sys_user` | 账号 | `username` 唯一，状态 `ACTIVE/DISABLED` |
| `sys_role` | 角色字典 | `role_code` 唯一 |
| `sys_user_role` | 用户角色多对多 | `(user_id, role_id)` 联合主键 |
| `script_book` | 剧本 | `script_code` 唯一，人数与时长 CHECK |
| `play_session` | 场次 | `session_code` 唯一，时间/容量/价格 CHECK，关联剧本 |
| `booking` | 预约 | `booking_code` 唯一，人数 CHECK，关联用户和场次 |
| `review` | 评价 | `booking_id` 唯一，评分 CHECK，关联预约、玩家和审核人 |
| `sys_operation_log` | 操作日志 | 关联可空操作人，按时间与对象建立索引 |

关系：用户—角色为多对多；剧本—场次、用户—预约、场次—预约均为一对多；预约—评价为一对零或一。

## 条件唯一预约

MySQL 普通三列唯一索引不能表达“只限制有效状态”。`booking.active_user_session` 是存储生成列：

```sql
CASE WHEN status IN ('CONFIRMED','CHECKED_IN')
     THEN CONCAT(user_id, ':', session_id)
     ELSE NULL END
```

该列唯一，因此同一玩家和场次最多存在一个有效预约；已取消、完成、未到店记录生成 `NULL`，不阻止之后重新预约。服务层仍在场次行锁内主动检查，以提供清晰错误信息。

## 容量缓存

`play_session.booked_count` 只汇总状态为 `CONFIRMED`、`CHECKED_IN` 的 `party_size`。所有增减与预约状态修改在同一事务中完成。

执行 `sql/check_booking_capacity.sql`：结果为空表示缓存与事实记录一致。脚本附带修复 SQL，但默认注释；只能在停止并发写入、完成备份后人工启用。

## 索引

- 剧本：`(status, genre, title)`。
- 场次：`(start_time, status, script_id)`、房间与时间组合索引。
- 预约：用户时间、场次状态、状态时间索引。
- 评价：审核状态时间、用户时间索引。
- 日志：创建时间、对象类型与对象 ID 索引。

## 删除策略

V1.0 不提供剧本、场次、用户物理删除路由。剧本和用户通过状态停用，场次通过状态取消，从而保留预约、评价和审计历史。外键不配置级联删除，避免误删核心业务数据。

## 初始化顺序

1. `sql/schema.sql`
2. `sql/seed.sql`
3. `sql/check_booking_capacity.sql`

`seed.sql` 使用固定 ID，仅适合新建的教学数据库。
