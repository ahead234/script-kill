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
