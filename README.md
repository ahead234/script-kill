# 剧本杀预约与场次管理系统

基于 Java 8、Struts 2.5、JSP 2.3、JSTL、JDBC 和 MySQL 8 的 Java Web MVC 教学项目。请求统一由 Struts Action 处理，视图跳转、鉴权与异常处理均通过 `struts.xml` 配置完成，以 WAR 形式部署到 Tomcat 9。

## 已实现功能

- 登录、退出、BCrypt 密码校验、Session ID 轮换。
- `ADMIN`、`MANAGER`、`STAFF`、`PLAYER`、`VIEWER` 服务端权限控制。
- 剧本分页、筛选、新增、编辑；店长新增剧本进入待审核（PENDING），管理员审核通过（ACTIVE）后前台才可见。
- 剧本封面上传（`/uploads/covers/`）与内置静态封面（`/assets/img/`）。
- 场次排期、房间冲突、容量限制、开放和事务化取消。
- 玩家预约、本人查询和取消；工作人员到店、完成、未到店登记。
- `SELECT ... FOR UPDATE` 行锁下的并发容量控制及 `booked_count` 对账 SQL。
- 已完成预约评价、待审核修改、发布/隐藏与公开评分统计。
- 用户启停、管理工作台、关键操作日志。
- 前台首页：轮播、筛选、自适应卡片栅格、剧本详情与预约。
- 管理后台：左侧固定导航 + 右侧主区域，报表页使用 ECharts 呈现四类图表。
- CSRF、PreparedStatement、安全输出、CSP 与统一错误页。
- Struts 2.5：Action 统一处理请求、`struts.xml` 配置命名空间与结果跳转、拦截器完成登录/角色/CSRF 校验、全局异常映射渲染统一错误页。

## 环境要求

- JDK 8（推荐；已实测 JDK 8 与 JDK 24 均可构建，产物字节码均为 Java 8）
- Maven 3.8+
- MySQL 8.0+
- Apache Tomcat 9.x（建议运行在 JDK 8 上）

不能部署到 Tomcat 10+，因为 Tomcat 10 使用 `jakarta.servlet.*`，与本项目的 `javax.servlet.*` 不兼容。

## 配置数据库连接

先编辑 `src/main/resources/db.properties`，填入本机真实密码：

```properties
db.url=jdbc:mysql://localhost:3306/script_booking?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
db.username=root
db.password=你的密码
db.driver=com.mysql.cj.jdbc.Driver
```

不要提交真实生产密码。也可使用环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`DB_DRIVER` 覆盖文件配置（需重启 Tomcat），或通过 JVM 参数 `-Dscriptbooking.config=D:\secure\db.properties` 指定外部文件。

## 初始化数据库

PowerShell 不支持 `<` 输入重定向，请使用 `source` 方式（路径替换为实际路径）：

```powershell
mysql -uroot -p -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/schema.sql"
mysql -uroot -p -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/seed.sql"
mysql -uroot -p -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/check_booking_capacity.sql"
```

也可以在 CMD（命令提示符）中使用重定向：

```bat
mysql -uroot -p < sql\schema.sql
mysql -uroot -p script_booking < sql\seed.sql
mysql -uroot -p script_booking < sql\check_booking_capacity.sql
```

`schema.sql` 会创建并使用 `script_booking` 数据库。执行 `check_booking_capacity.sql` 时结果应为空。

`seed.sql` 可重复执行：脚本开头会先清空演示数据再插入，因此在已有数据的库上直接重跑不会报主键冲突。但它要求表已存在——如果从未建库，请先执行 `schema.sql`。

## 剧本封面与图片字段

图片字段位于 `script_book` 表的 `cover_path` 列（`VARCHAR(255)`，建表脚本 `schema.sql` 中 `description` 之后），存储站点内相对路径，浏览器通过 `<img src>` 直接访问：

- `/assets/img/cover-1.svg` ~ `cover-4.svg`：随 WAR 打包的内置占位封面。
- `/assets/img/cover-1.jpg`、`cover-2.jpg`、`cover-12.jpg`：已放入 `src/main/webapp/assets/img/` 的实拍/正式封面，随 WAR 打包，重新部署不会丢失，前台无需登录即可访问。
- `/assets/img/cover-3.png`、`cover-4.png`、`cover-5.png`：3~5 号剧本的新封面，同样随 WAR 打包，重新部署不会丢失。
- `/uploads/covers/xxx.jpg`：后台"剧本管理 → 编辑剧本"上传的封面（支持 jpg/jpeg/png/webp/gif/svg，≤2MB），保存在部署目录的 `uploads/covers/`，重新部署会丢失且仅登录用户可访问。

已有数据库可执行 `sql/upgrade-script-covers.sql`，把 1~5、12 号剧本的封面切换到 `assets/img` 下的永久图片。

老库若没有 `cover_path` 列或旧的 `status` 枚举，执行迁移脚本：

```powershell
mysql -uroot -p --default-character-set=utf8mb4 -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/upgrade-cover-pending.sql"
```

该脚本会补齐 `cover_path` 列、把 `status` 扩展为 `PENDING/ACTIVE/INACTIVE`（默认 `PENDING`），并把存量剧本置为 `ACTIVE`。若提示 `Duplicate column name` 说明列已存在，跳过该条即可。

### 剧本审核流程

1. 店长（MANAGER）在「剧本管理 → 新增剧本」提交剧本，状态自动为 **待审核**，前台不可见。
2. 管理员（ADMIN）在「剧本管理」列表对待审核剧本点击 **通过**（上架）或 **驳回**（下架）；上下架与状态变更也仅限管理员。
3. 状态为 **已上架（ACTIVE）** 的剧本才会出现在前台「发现剧本」与「下一场故事」。
4. 店长编辑剧本不会改变审核/上下架状态；店长可通过"编辑剧本"页面的上传控件更换封面图片。

注意：上传的封面保存在部署目录（如 `webapps/script-booking-system/uploads/covers/`），重新部署 WAR 会覆盖该目录，请提前备份 `uploads/covers/` 并在部署后拷回；随包的内置封面（`src/main/webapp/assets/img/`）不受影响。若希望某张封面永久保留，请把文件放到 `src/main/webapp/assets/img/`，并把 `script_book.cover_path` 改为对应的 `/assets/img/xxx` 路径。

## 构建与部署

建议将 `JAVA_HOME` 指向 JDK 8 后构建：

```powershell
# Windows PowerShell：指向本机 JDK 8 安装目录
$env:JAVA_HOME = 'D:\appdata\jdk'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

mvn clean test
mvn clean package
```

若构建时报 `Could not reserve enough space for object heap`（常见于同时运行 Tomcat、可用内存不足），先停止 Tomcat，或限制构建堆：

```powershell
$env:MAVEN_OPTS = '-Xmx512m'
mvn clean package
```

## 页面布局约定

本项目包含两套独立页面，视觉与布局各自成体系。

### 普通用户前台（上下流式）

- 外层为流式布局（`.app-shell` 使用 `display: block`），内容自上而下排列。
- 顶部导航使用 flex：左侧为文字标识「雾灯剧场」，中间为「发现剧本 / 下一场故事」，右侧为登录或账号入口。
- 导航下方为轮播组件（3 张图片自动轮播，原生 JS 实现，鼠标悬停暂停，可点击圆点切换）。
- 轮播下方依次为标题文案、筛选区（剧本名称、题材、查找）与剧本卡片区域。
- 卡片区使用网格布局固定为一行四列，列宽和 18px 间距全程统一；最后一行不足四张时卡片保持原有宽度、剩余位置留空，不拉伸卡片、不自动铺满容器。单张卡片自上而下为：封面图片、剧本名称、人数、题材、难易程度、一句话介绍；左下角为评分，右下角为「查看场次」按钮，点击进入剧本详情页并可预约。

### 管理员后台（左右分栏）

- 管理端不再显示前台顶部导航：左侧为整屏固定的深绿导航栏（品牌、按角色渲染的菜单、底部账号信息与「退出登录」），右侧为内容区（`.admin-page .admin-main` 预留侧栏宽度）。
- 侧栏菜单含用户管理、剧本管理、场次排期、预约管理、评价管理、操作日志与数据报表，按当前角色显示，当前项左侧高亮并在移动端折叠为横向导航。
- 内容区顶部为标题条：左侧标题与说明，右侧为报表操作区（开始日期、结束日期与刷新按钮）；下方卡片使用 Grid 自适应排列。
- 报表页依次为 4 张指标卡、「预约统计」（状态分布环图 + 预约趋势柱图）与「运营明细」（热门剧本 + 审核 / 排期状态对比）。
- 日期区间通过 `/admin/api/stats?from=YYYY-MM-DD&to=YYYY-MM-DD` 只作用于预约趋势，未选择时默认近 7 日；指标卡展示当前快照。页面通过外部 `admin-charts.js` 请求数据，不写内联脚本，保持 CSP `script-src 'self'`。

### 前端资源与图表约束

- 图表使用 ECharts 5.5.1，文件位于 `assets/vendor/echarts.min.js`，本地引入不依赖外网；报表数据来自 `/admin/api/stats`。
- 未引入 Element Plus / Ant Design Vue：项目 CSP 为 `script-src 'self'` 且没有前端构建流程，Vue 运行时模板编译需要 `unsafe-eval`、组件库大量依赖内联样式，会破坏现有安全策略。交互改用原生 JS 实现，样式也更贴合"克制、无多余装饰"的要求。
- 图表样式约定：直角柱体、细环、细网格线，不使用渐变、圆角大色块、阴影特效等多余装饰，优先保证数据对比清晰。

构建产物为 `target/script-booking-system.war`。重新部署时必须清掉旧解压目录与 JSP 编译缓存，否则会出现"页面是新的但样式/图片 404"的现象：

```powershell
# 1) 停止 Tomcat
# 2) 删除旧部署与缓存（路径按实际安装位置调整）
Remove-Item -Recurse -Force <tomcat>\webapps\script-booking-system
Remove-Item -Recurse -Force <tomcat>\work\Catalina\localhost\script-booking-system
# 3) 拷入新的 target\script-booking-system.war，再启动 Tomcat
# 4) 验证静态资源可访问（应返回 CSS 文本而不是 404）：
#    http://localhost:8080/script-booking-system/assets/css/front.css
```

启动后访问：

```text
http://localhost:8080/script-booking-system/
```

若在 IDE 中部署 exploded artifact，请把 Application Context 设置为 `/script-booking-system`。

### JDK 8 适配说明

- `pom.xml` 使用自定义属性 `java.version`（值为 `1.8`）配置 `maven-compiler-plugin` 的 `source/target`，不使用 `maven.compiler.release`，保证 JDK 8 可以编译。
- 代码不使用 Java 9+ API：`Set.of(...)` 已替换为 `com.example.scriptbooking.util.Sets.of(...)`，`String.isBlank()` 已替换为 `trim().isEmpty()`。
- MySQL Connector/J 固定为 `8.0.33`，兼容 JDK 8 运行时。
- 若本机 Maven 全局 `conf/settings.xml` 注入了 `maven.compiler.source/target`，本项目使用独立的 `java.version` 属性，不受其覆盖。
- 构建日志显示 `javac [debug target 1.8]`；可用 `javap -verbose` 确认 `major version: 52`。

### 常见启动问题

| 页面提示 | 原因 | 处理 |
|---|---|---|
| 数据库连接失败：账号或密码错误（Access denied） | `db.password` 与实际不符 | 填写正确密码，或设置环境变量 `DB_PASSWORD` 后重启 Tomcat |
| 数据库连接失败：数据库不存在 | 未执行 `sql/schema.sql` | 按上文导入建库脚本 |
| 查询或保存失败：数据表不存在 | 未建表或未导入演示数据 | 执行 `sql/schema.sql` 与 `sql/seed.sql` |
| 无法连接 MySQL 服务 | 服务未启动或端口不一致 | 启动 MySQL 服务，确认端口为 3306 |
| 认证需要公钥检索 | 账号使用 `caching_sha2_password` 且配置不完整 | 确认 URL 含 `allowPublicKeyRetrieval=true` |

页面只显示错误类别与处理建议；SQL、堆栈和服务器路径只写入 Tomcat 日志（`logs/catalina.out`、`logs/localhost.<日期>.log`）。

### 中文乱码排查

| 现象 | 原因 | 处理 |
|---|---|---|
| 页面固定文案乱码（如“欢迎回来”变成问号或方块） | JSP 源文件被 Tomcat 按 ISO-8859-1 读取 | 已在 `web.xml` 的 `jsp-config` 中统一 `page-encoding=UTF-8`；重启 Tomcat 并删除 `work/Catalina/localhost/script-booking-system` 让其重新编译 JSP |
| 只有数据库内容乱码（剧本名、用户名等） | 导入 SQL 时客户端字符集不是 UTF-8（Windows 控制台常见） | 用 `mysql -uroot -p --default-character-set=utf8mb4 -e "source <路径>/sql/seed.sql"` 重新导入 |
| Tomcat 控制台日志乱码 | 控制台编码与日志编码不一致 | 将 Tomcat `conf/logging.properties` 中 `java.util.logging.ConsoleHandler.encoding` 改为 `GBK` |
| 提交表单后中文变问号 | 请求体编码未生效 | `SecurityFilter` 已对所有请求设置 `request.setCharacterEncoding("UTF-8")`，确认过滤器未被绕过 |

编码要点：`web.xml` 中 `jsp-config` 统一 JSP 源文件编码与默认响应编码；`SecurityFilter` 统一请求与响应编码；`db.properties` 的 `db.url` 含 `useUnicode=true&characterEncoding=UTF-8`；建库脚本使用 `utf8mb4` 并在开头执行 `SET NAMES utf8mb4`。

修改 `src/main/resources/db.properties` 后需要重新 `mvn package` 并重新部署；也可直接编辑 Tomcat 已解压目录 `webapps/script-booking-system/WEB-INF/classes/db.properties`，然后重启 Tomcat。

## 演示账号

种子数据中的联系人均为虚构信息。所有演示账号初始密码均为 `123456`，仅限本地教学使用。

若已经导入过旧版种子数据（旧密码为 `password`），无需重建库，单独执行 `sql/reset_passwords.sql` 即可把全部账号密码重置为 `123456`。

| 账号 | 角色 | 入口 |
|---|---|---|
| `admin` | ADMIN | 全部管理功能 |
| `manager` | MANAGER | 剧本、场次、预约、评价、日志 |
| `staff` | STAFF | 预约接待与状态登记 |
| `player` | PLAYER | 浏览、预约、取消、评价 |
| `viewer` | VIEWER | 管理工作台只读概览 |
| `player2` | PLAYER | 第二个玩家，用于演示多用户预约与评价 |
| `player3` | PLAYER | 第三个玩家，用于演示到店与评价审核 |
| `disabled` | PLAYER / DISABLED | 用于验证禁用用户不能登录 |

首次部署后应立即修改或删除演示账号。

## 工程结构

```text
src/main/java/com/example/scriptbooking/
├── action/       Struts Action：请求参数绑定、调用服务层、返回 result
├── interceptor/  Struts 拦截器：登录、角色与 CSRF 校验
├── service/      校验、状态机与事务
├── dao/          PreparedStatement 与结果映射
├── model/        JavaBean 业务模型
├── filter/       请求编码、安全响应头、上传目录访问控制
├── util/         JDBC、分页、参数校验、编号生成、JDK 8 兼容工具
└── exception/    业务异常
src/main/resources/struts.xml   Struts 命名空间、Action、结果与拦截器栈
src/main/webapp/WEB-INF/views/  JSP 视图
sql/                           建表、演示数据、容量对账
docs/                          设计、路由、调研、测试和限制
```

## Struts 2.5 迁移说明

- 所有原 Servlet 已替换为 `action` 包下的 Action，请求路径保持不变：命名空间对应 URL 目录，Action 名称对应最后一段。例如 `/admin/scripts/form` 由 `namespace="/admin/scripts"` 的 `form` 处理，`/scripts/detail` 由 `namespace="/scripts"` 的 `detail` 处理。
- 表单参数通过 Action 属性与 Setter 绑定（含文件上传的 `coverFile` / `coverFileFileName`），不再在 Action 中逐个 `getParameter`。
- 页面跳转全部写在 `struts.xml` 的 `<result>` 中：JSP 转发使用默认 `dispatcher`，重定向使用 `redirect` 结果与 `redirectUrl` 属性。
- 登录、角色与 CSRF 校验由 `AuthInterceptor` 完成，拦截器栈按角色划分（`publicStack`、`playerStack`、`staffStack`、`managerStack`、`adminStack`、`managementStack`）。
- 未捕获异常由 `<global-exception-mappings>` 映射到统一错误页，`BusinessException` 仍在表单内就地提示，保持原有交互。


## 关键业务约定

- V1.0 支持拼场，单次预约可从 1 人起约；预约人数不得超过剧本上限和场次剩余容量。
- 场次容量必须处于剧本最少/最多人数范围内。
- `booked_count` 只统计 `CONFIRMED`、`CHECKED_IN`，取消、完成或未到店时在同一事务中释放。
- `GET` 只读取数据；取消、审核、启停和状态流转均使用带 CSRF Token 的 `POST`。
- 玩家只能操作本人预约与评价；管理权限由过滤器和服务层规则共同保障。

更多信息见 `docs/development-spec.md`、`docs/database-design.md`、`docs/routes.md`、`docs/test-report.md`。
