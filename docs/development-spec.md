# 开发设计说明

## 1. 架构

请求链路为：浏览器 → `SecurityFilter` → Servlet Controller → Service → DAO → MySQL → JSP。

- `controller`：读取参数、调用服务、设置请求属性、转发或重定向。
- `service`：服务端校验、数据归属、状态机、事务和操作日志。
- `dao`：仅使用 JDBC、`PreparedStatement` 和结果集映射。
- `model`：供服务与 JSP 使用的 JavaBean。
- `filter`：UTF-8、认证、角色、CSRF、CSP 与防点击劫持响应头。
- `WEB-INF/views`：JSTL/EL 页面，浏览器不能直接访问，不含 SQL。

## 2. 模块划分

| 模块 | Controller | Service | DAO |
|---|---|---|---|
| 认证 | `AuthServlet` | `AuthService` | `UserDao`、`OperationLogDao` |
| 玩家目录 | `CatalogServlet` | 查询为主 | `ScriptBookDao`、`SessionDao`、`ReviewDao` |
| 剧本 | `AdminScriptServlet` | `ScriptService` | `ScriptBookDao` |
| 场次 | `AdminSessionServlet` | `SessionService` | `SessionDao`、`BookingDao` |
| 预约 | `BookingServlet`、`AdminBookingServlet` | `BookingService` | `BookingDao`、`SessionDao` |
| 评价 | `ReviewServlet`、`AdminReviewServlet` | `ReviewService` | `ReviewDao`、`BookingDao` |
| 后台 | `AdminServlet` | `AdminService` | 用户、预约、场次、评价、日志 DAO |

## 3. 状态机

### 场次

- 表单允许：`DRAFT`、`OPEN`、`CLOSED`、`IN_PROGRESS`、`COMPLETED`。
- `CANCELLED` 只能由 `/admin/sessions/cancel` 完成。
- 取消操作锁定场次，在同一事务中取消关联的 `CONFIRMED`/`CHECKED_IN` 预约、清零有效容量并记录日志。

### 预约

- 创建：无记录 → `CONFIRMED`。
- 玩家取消：`CONFIRMED` → `CANCELLED_BY_PLAYER`，仅限本人且场次未开始。
- 到店：`CONFIRMED` → `CHECKED_IN`。
- 未到店：`CONFIRMED` → `NO_SHOW`，仅限场次开始后。
- 完成：`CHECKED_IN` → `COMPLETED`，仅限场次结束后。
- 门店取消场次：有效预约 → `CANCELLED_BY_STORE`。

### 评价

- 已完成预约首次提交 → `PENDING`。
- 玩家仅能修改本人 `PENDING` 评价。
- 管理审核 → `PUBLISHED` 或 `HIDDEN`，必须填写原因与审核人。
- 公开平均分仅统计 `PUBLISHED`。

## 4. 并发容量设计

`BookingService.create` 的事务顺序：

1. 关闭自动提交。
2. 使用 `SELECT ... FOR UPDATE` 锁定 `play_session`。
3. 重新检查 `OPEN`、开始时间、剧本人数上限、剩余容量。
4. 在同一连接中检查玩家是否已有有效预约。
5. 插入 `booking`。
6. 条件更新 `booked_count`，保证其位于 0 与 `capacity` 之间。
7. 写操作日志并提交；异常统一回滚。

取消和完成等会使预约离开有效集合的操作，也在锁定预约和场次后释放 `booked_count`。`sql/check_booking_capacity.sql` 用于对账。

## 5. 拼场规则

V1.0 允许拼场：单个玩家订单可从 1 人起约，不强制单个订单达到剧本最少人数；场次容量必须在剧本人数范围内，全部有效订单人数之和不得超过容量。是否达到开本最少人数由门店在场次开始前运营确认，V1.0 不提供自动成团/解散任务。

## 6. 安全

- 密码：jBCrypt，数据库只保存哈希。
- SQL：全部参数使用 `PreparedStatement`。
- XSS：JSP 用户内容使用 `<c:out>`；评价内容不以原始 HTML 输出。
- CSRF：Session 内 256 位随机 Token，所有 POST 恒定时间比较。
- 会话：登录后 `changeSessionId()`，退出 `invalidate()`，Cookie 为 HttpOnly。
- 授权：过滤器按路由检查角色，服务层再检查本人归属和业务状态。
- 响应：CSP、`X-Content-Type-Options`、`X-Frame-Options`、`Referrer-Policy`。
- 日志：敏感键名自动脱敏，不记录密码和 Token。

## 7. 时间与错误处理

应用与 JDBC URL 默认采用 `Asia/Shanghai`。生产部署应统一 JVM、Tomcat 与 MySQL 时区。业务错误显示可理解提示，通用错误页不输出堆栈、SQL 或服务器路径。

## 8. JDK 8 适配

- 编译级别：`pom.xml` 使用自定义属性 `java.version=1.8` 配置 `maven-compiler-plugin` 的 `source/target`，不使用 JDK 9+ 才支持的 `maven.compiler.release`。
- 不使用 Java 9+ API：`Set.of(...)` 由 `com.example.scriptbooking.util.Sets.of(...)` 替代；`String.isBlank()` 由 `trim().isEmpty()` 替代。
- 依赖兼容 JDK 8：MySQL Connector/J `8.0.33`、JUnit Jupiter `5.10.3`、Servlet API `4.0.1`、JSP API `2.3.3`、JSTL `1.2`、jBCrypt `0.4`。
- 运行环境：Tomcat 9 在 JDK 8 上提供 Servlet 4.0 与 JSP 2.3；更高版本 JDK 也能构建，产物仍为 Java 8 字节码（major version 52）。
- 若本机 Maven 全局 `conf/settings.xml` 注入了 `maven.compiler.source/target`，本项目使用独立属性名，不受其覆盖。
