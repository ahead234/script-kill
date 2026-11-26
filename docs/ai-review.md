# 实验四 AI 辅助记录：房间管理（Room）

- AI 工具：WorkBuddy 内置 AI 编程助手，模型 DeepSeek-V4.1-Flash
- 技术基线：JavaEE 8（`javax.*`）+ Struts 2.5.26 + Tomcat 9.0.122 + JDK 1.8 + MySQL
- 初稿留档：`docs/ai-draft/RoomDao.draft.java`、`docs/ai-draft/rooms.draft.jsp`（不参与编译与部署，仅作审查证据）
- 最终实现：`model/Room.java`、`dao/RoomDao.java`、`service/RoomService.java`、`action/AdminRoomAction.java`、`views/admin/rooms.jsp`、`views/admin/room-form.jsp`

## 一、prompt 摘要

| 轮次 | prompt 摘要 | AI 输出 |
| --- | --- | --- |
| 1 | 用 Java 写一个房间管理的 DAO，支持分页查询和新增房间 | `RoomDao.draft.java`：单例 + `Statement` 拼串 + 手写连接 |
| 2 | 写一个房间列表的 JSP 页面，带分页和状态标记 | `rooms.draft.jsp`：scriptlet 取数 + `jakarta.tags.core` |
| 3 | 房间改名后，历史场次里的旧房间名怎么处理 | 建议 `UPDATE play_session SET room_name=? WHERE room_name=?` 级联更新 |

第 1、2 轮 prompt 未声明技术基线，AI 按当前主流 Jakarta EE / Spring Boot 3 习惯输出，因此暴露出下方冲突。

## 二、AI 建议 vs JavaEE8 基线冲突

| # | AI 输出位置 | 冲突点 | 处置 | 验证方式 |
| --- | --- | --- | --- | --- |
| 1 | `RoomDao.draft.java:6` `import jakarta.servlet.http.HttpServletRequest` | Tomcat 9 只提供 `javax.servlet.*`，`jakarta.*` 属 Jakarta EE 9+，编译即失败 | 拒绝，删除该 import；取客户端 IP 改由 `WebUtil.clientIp(javax.servlet.http.HttpServletRequest)` 提供 | `mvn -q clean package` 通过 |
| 2 | `rooms.draft.jsp:4` `uri="jakarta.tags.core"` | JSTL 3.0 的 URI；Tomcat 9 配 JSTL 1.2 只认 `http://java.sun.com/jsp/jstl/core`，页面报“无法解析绝对 URI” | 拒绝，改回 `javax` 版 URI，与 `common/header.jspf` 保持一致 | 页面正常渲染，房间列表输出 5 行 |
| 3 | `rooms.draft.jsp:7-10` 在 JSP 里 `RoomDao.getInstance()` 取数 | 绕过 Action/Service 分层，视图层直接持有 request 参数；与实验三确定的分层基线冲突 | 拒绝，页面只消费 `${result.items}`，取数由 `AdminRoomAction.list()` 完成 | 列表页与分页链接可用 |
| 4 | 第 3 轮对话：级联 UPDATE 场次表房间名 | 跨表改写历史排期且无事务、无审计；房间名一旦被误改，历史订单指向错误房间 | 拒绝，房间编号与名称创建后只读，仅允许改容量/位置/备注/状态 | 编辑页编号、名称输入框为 `readonly` |

## 三、人工修改记录（AI 代码不可直接使用）

| # | 位置 | AI 原始写法 | 人工修改 | 理由 |
| --- | --- | --- | --- | --- |
| 1 | `RoomDao.draft.java:33` | `"... LIKE '%" + keyword + "%'"` 字符串拼接 | `PreparedStatement` + `CONCAT('%',?,'%')` | SQL 注入；且拼串无法复用执行计划 |
| 2 | `RoomDao.draft.java:14-16` | 硬编码连接串、账号、明文密码 | 改调 `Database.getConnection()` | 密码不得进代码库；连接参数统一由 `db.properties` / 环境变量提供 |
| 3 | `RoomDao.draft.java:43-45,58-59` | 手工 `close()`，`catch (Exception) { printStackTrace() }` | try-with-resources + `DbErrors.fail(...)` | 异常路径会泄漏连接；吞异常会让列表静默变空 |
| 4 | `RoomDao.draft.java:39` | `rs.getString("roomName")` | `rs.getString("room_name")` | 列名与 DDL 不一致，运行期报 1054 Unknown column |
| 5 | `RoomDao.draft.java:12-24` | 懒汉式单例 `getInstance()` | 无状态 DAO，每次 `new` | 单例非线程安全且无共享状态，没有必要 |

## 四、运行验证

| 用例 | 结果 |
| --- | --- |
| 新增房间 `R06 / 迷雾六号房 / 5 人` | 302 → `/admin/rooms/form?id=6`，数据库可见 |
| 容量填 40 | 表单回显并提示“房间容量必须在2到30之间” |
| 新增重名房间 | 提示“房间名称已存在” |
| `R03`（有 3 场未结束排期）转停用 | 提示“还有 3 场未结束的排期，处理完才能转为停用” |
| `R06` 转检修后新建场次选该房间 | 提示“房间「迷雾六号房」当前为检修状态，不能安排场次” |

---

# 实验五 AI 辅助记录：预约改期（Reschedule）

- 初稿留档：`docs/ai-draft/BookingReschedule.draft.java`（原样保留，不参与编译）
- 最终实现：`service/BookingService#reschedule`、`dao/BookingDao#reschedule`、`action/BookingAction#reschedule`、`views/booking/reschedule.jsp`
- 业务规则：仅本人、仅已确认、开演前 2 小时截止、每笔限一次、同剧本、目标场次余位足够

## 一、prompt 摘要

| 轮次 | prompt 摘要 | AI 输出 |
| --- | --- | --- |
| 1 | 给预约加一个改期功能，把 booking 的 session_id 换成新场次 | 直接 `UPDATE booking SET session_id=?`，返回 `Map`，异常只 `printStackTrace()` |
| 2 | 改期时原场次和新场次的剩余名额要怎么处理 | 建议"改期不影响名额，容量是按场次单独算的"——错误，会导致原场次名额被永久占用 |
| 3 | 改期要限制哪些条件 | 只给出"新场次要有余位"，漏掉开演时间、改期次数、同剧本、归属校验 |

## 二、三层审查记录（审查位置 / AI 原始输出 / 发现 / 处置 / 依据）

| # | 审查层 | 审查位置 | AI 原始输出 | 审查发现 | 处置 | 依据 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 安全性 | `BookingReschedule.draft.java:29-31` | `"UPDATE booking SET session_id=" + targetSessionId + " WHERE id=" + bookingId` | 两个参数都来自请求，字符串拼接即 SQL 注入；`targetSessionId=1 OR 1=1` 可改全表 | **拒绝**，改 `PreparedStatement` 占位符，参数由 `setLong` 绑定 | OWASP A03:2021 注入；`targetSessionId` 完全由客户端控制 |
| 2 | 正确性 | `draft.java:34-36` | 只 UPDATE 预约表，"名额不用动" | 原场次 `booked_count` 不减、新场次不加，余位与实际人数永久偏离；无事务，两步之间失败会留下半改状态 | **修改**，`BookingService` 内开事务：原场次 −N、新场次 +N、改预约、写审计日志，四步同提交 | 场次余位是预约的派生数据，必须与预约同源；`changeBookedCount` 已带 `BETWEEN 0 AND capacity` 兜底 |
| 3 | 安全性 | `draft.java:53-60` | `canReschedule(bookingId, userId)`，且判断写在 JSP 的 `<c:if>` 里 | 方法签名不接收当前登录用户，Action 也没传；任何人改 URL 里的 `id` 就能改他人预约（水平越权/IDOR）；前端判断可被绕过 | **拒绝**，`reschedule(bookingId, userId, targetSessionId, ip)` 强制传当前用户，Service 内 `snapshot.getUserId() != userId` 直接抛 `BusinessException` | 实验三已确立"授权在服务端"；前端只做展示，不承担校验 |
| 4 | 正确性 | 第 2 轮对话建议 | "改期不影响名额" | 与规则 2 同源，且会掩盖超卖：目标场次可能被改入超过容量的人数 | **拒绝**，改期前校验 `target.getRemainingCapacity() >= partySize`，不足时提示"仅剩 N 位" | `play_session.booked_count` 与 `capacity` 的约束在库侧也有 `BETWEEN 0 AND capacity` |
| 5 | 可维护性 | `draft.java:20-22,45-46` | 手动 `conn.close()`、`e.printStackTrace()`、返回 `Map<String,Object>` | 异常路径泄漏连接；堆栈只进 stderr，页面无提示；`Map` 丢失类型，调用方靠字符串 key 猜 | **修改**，try-with-resources、统一 `BusinessException`、方法返回 `void` 由 Action 组装提示 | 与实验三 `DbErrors` / `BusinessException` 的既有约定一致 |

## 三、规则与验证结果

| 规则 | 实现位置 | 验证结果 |
| --- | --- | --- |
| 仅本人可改期 | `BookingService#reschedule` 第一段 | 他人 `id` 提交被拒（越权） |
| 仅 `CONFIRMED` 可改期 | 同上 + `UPDATE ... WHERE status='CONFIRMED'` | 已到店/已取消预约无改期入口，直接 POST 也被拒 |
| 开演前 2 小时截止 | `isReschedulable()` + Service 二次判定 | 18:20 场次（距开演 1h42m）改期被拦，提示"开演前 2 小时内不可改期" |
| 每笔限改期一次 | `reschedule_count` + 条件 UPDATE | 二次改期被拒；详情页入口消失 |
| 同剧本才能互换 | `source.getScriptId() != target.getScriptId()` | 候选列表只列同剧本场次；跨剧本 POST 被拒 |
| 目标余位足够 | `getRemainingCapacity() < partySize` | 剩 1 位的场次灰显，直接 POST 提示"仅剩 1 位，少于本单人数" |
| 两侧名额同步 | 事务内两次 `changeBookedCount` | 改期后原场次 2→0、目标场次 0→2，数据库核对一致 |
