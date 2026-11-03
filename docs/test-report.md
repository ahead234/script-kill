# 测试报告

测试日期：2026-10-29  
环境：Windows、Maven 3.9.14；构建使用 JDK 1.8.0_504（Temurin，`D:\appdata\jdk`），另验证 JDK 24.0.2 亦可构建；检测到 MySQL CLI 9.0.1。

## 已执行

| 项目 | 命令/方法 | 结果 | 说明 |
|---|---|---|---|
| Java 编译与 WAR 打包（JDK 8） | `mvn clean test package`，`JAVA_HOME=D:\appdata\jdk`（1.8.0_504） | 通过 | 日志为 `javac [debug target 1.8]`，生成 WAR；`javap` 显示 `major version: 52` |
| JDK 24 交叉构建 | `mvn clean package -DskipTests`，`JAVA_HOME=D:\appdata\java\jdk`（24.0.2） | 通过 | 仍按 `target 1.8` 编译，仅提示建议使用 `--release 8` |
| JUnit 5 | `mvn test` | 通过 | 3 个测试全部通过 |
| 正常人数、电话、时间范围 | `ValidatorsTest` | 通过 | 合法输入被接受 |
| 非正人数、非法电话、评分、时间 | `ValidatorsTest` | 通过 | 均抛出业务异常 |
| 演示密码哈希 | `ValidatorsTest` | 通过 | `password` 可匹配种子 BCrypt，错误密码不匹配 |
| IDE 诊断 | Java 与 Web 目录诊断 | 通过 | 未发现 error/warning |
| MySQL 客户端检测 | `mysql --version` | 通过 | 客户端 9.0.1 可用 |
| MySQL 连接 | `mysql -uroot --connect-timeout=3 ...` | 未通过 | root 账号要求密码，环境未提供凭据；未绕过认证 |

测试过程中首次运行演示密码测试时发现原种子哈希与文档密码不匹配。已重新生成 BCrypt 哈希、同步更新 `seed.sql`，随后复测通过。该失败及修复如实保留在报告中。

## 代码级核验

- 预约创建在事务中对场次执行 `SELECT ... FOR UPDATE`，锁内复查状态、时间、重复预约和剩余容量。
- 场次取消、玩家取消、完成/未到店等容量变化与日志在同一事务中提交或回滚。
- DAO 使用 `PreparedStatement`；JSP 不含 SQL。
- 所有状态变更路由为 POST，过滤器统一校验 CSRF。
- 玩家详情控制器检查预约所有者，评价服务再次检查预约所有者和完成状态。
- 评价公开查询仅选择 `PUBLISHED`。
- 页面用户内容使用 `<c:out>` 输出。

## 未执行

以下项目需要可用的 MySQL 凭据、Tomcat 9 和实际运行环境，本次未将其标记为通过：

- `schema.sql`、`seed.sql` 在实际 MySQL 8 实例上的完整导入。
- `check_booking_capacity.sql` 的数据库对账结果。
- 多连接同时预约最后名额的并发集成测试。
- 登录、禁用用户、越权、CSRF、完整业务状态机的浏览器端到端测试。
- Tomcat 9 实际部署与 JSP 运行时编译。
- 不同屏幕宽度下的实际浏览器冒烟。
- 页面截图。

## 建议验收步骤

1. 按 `README.md` 初始化测试库并配置独立测试账号。
2. 执行 `sql/check_booking_capacity.sql`，确认返回空集。
3. 用两个 PLAYER 会话同时预约仅剩 1 位的同一场次，确认只有一个成功且容量不为负。
4. 按需求清单逐项执行登录、权限、剧本、场次、预约、评价与日志测试。
5. 在 Tomcat 9 实际运行后截图，并将通过、失败、未执行结果继续补充到本报告。
