
# 部署文档

本项目为「剧本杀预约与场次管理系统」，基于 Java 8 + Struts 2 + JSP + MySQL 8 构建，打包为 WAR 部署到 Apache Tomcat 9。本文档覆盖从环境准备到部署验证的完整流程。

## 1. 环境要求

| 组件 | 版本要求 | 说明 |
|---|---|---|
| JDK | **8**（必需） | `pom.xml` 编译级别为 1.8，产物为 Java 8 字节码（major version 52）。更高版本 JDK 也能构建，但运行环境见下 |
| Maven | 3.8+ | 使用 compiler 3.11.0 / surefire 3.2.5 / war 3.4.0 插件 |
| MySQL | **8.0+** | 数据库使用 `utf8mb4` 与 `utf8mb4_0900_ai_ci` 排序规则，该排序规则仅 MySQL 8.0 支持 |
| Tomcat | **9.x**（必需） | 项目使用 `javax.servlet.*`（Servlet 4.0 / JSP 2.3），**不兼容 Tomcat 10+**（Tomcat 10 改用 `jakarta.*`，部署会直接报 `ClassNotFoundException`） |

> 注意：不要在 Tomcat 10 及以上版本部署本应用；如需更换容器，必须选择提供 `javax.servlet` API 的版本。

## 2. 获取代码与项目结构
```
script_kil/
├── pom.xml                     # Maven 构建配置（war 包，finalName=script-booking-system）
├── sql/                        # 数据库脚本（见第 3 节）
├── docs/                       # 设计与测试文档
└── src/main/
    ├── java/com/example/scriptbooking/   # Action / Service / DAO / Filter / Util
    ├── resources/
    │   ├── struts.xml          # Struts 配置（devMode=false，上传上限 2MB）
    │   └── db.properties       # 数据库连接配置（可被环境变量覆盖）
    └── webapp/
        ├── assets/img/         # 内置封面与轮播图（随 WAR 打包，永久保留）
        └── WEB-INF/web.xml     # SecurityFilter + Struts2 过滤器、错误页、会话配置
```

## 3. 数据库初始化

### 3.1 全新安装

按顺序执行以下脚本（`sql/` 目录）：

| 顺序 | 脚本 | 作用 |
|---|---|---|
| 1 | `schema.sql` | 创建数据库 `script_booking`（utf8mb4）及全部表。**会先 DROP 已有表**，请勿对已有数据的库直接执行 |
| 2 | `seed.sql` | 导入演示数据：5 个角色、8 个演示账号、11 个剧本与示例场次。**可重复导入**（先清空再插入） |

Windows PowerShell 下推荐使用 `source` 方式导入（避免 `<` 重定向的编码问题）：

```powershell
mysql -uroot -p --default-character-set=utf8mb4 -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/schema.sql"
mysql -uroot -p --default-character-set=utf8mb4 -e "source D:/assignments/JuniorYearSemester1/javaEE/script_kill/script_kil/sql/seed.sql"
```

导入完成后验证：

```
mysql -uroot -p -e "USE script_booking; SELECT id, username, status FROM sys_user ORDER BY id; SELECT COUNT(*) FROM script_book;"
```

### 3.2 已有数据库的升级

对已经导入过旧版本数据的库，按需执行增量脚本：

| 脚本 | 用途 |
| --- | --- |
| `upgrade-user-email.sql` | 用户邮箱字段相关升级 |
| `upgrade-register-phone.sql` | 注册手机号字段相关升级 |
| `upgrade-cover-pending.sql` | 封面审核状态相关升级 |
| `upgrade-script-covers.sql` | 将 1~5、12 号剧本封面切换到 `assets/img` 下的永久图片（幂等，可重复执行） |
| `reset_passwords.sql` | 将全部演示账号密码重置为 `123456`（仅本地演示用） |

### 3.3 演示账号

`seed.sql` 导入后，以下账号密码均为 **`123456`**（BCrypt 存储），仅用于本地教学演示：

| 账号 | 角色 | 用途 |
| --- | --- | --- |
| `admin` | 系统管理员 | 后台全部功能 |
| `manager` | 店长 | 剧本/场次/预约/评价管理 |
| `staff` | 店员 | 店员操作 |
| `player`  | 玩家 | 前台浏览、预约、评价 |
| `viewer` | 只读用户 | 只读验证 |
| `disabled` | 玩家（已禁用） | 验证禁用账号无法登录 |

> 
> 生产环境部署后**必须**修改或删除全部演示账号，见第 9 节。

## 4. 应用配置

### 4.1 数据库连接

默认配置在 `src/main/resources/db.properties`，随 WAR 打包进 `WEB-INF/classes`：

```
db.url=jdbc:mysql://localhost:3306/script_booking?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
db.username=root
db.password=z123456
db.driver=com.mysql.cj.jdbc.Driver
```

> 
> 仓库中的密码为本地演示值，发布前请替换为目标环境的真实凭据。

### 4.2 覆盖配置（推荐生产使用）

应用读取配置的优先级为：**环境变量 > 外部配置文件 > db.properties > 代码内置默认值**。

**方式一：环境变量**（推荐，适合 Tomcat 服务方式启动）：

| 环境变量 | 对应属性 | 默认值 |
| --- | --- | --- |
| `DB_URL` | `db.url` | `jdbc:mysql://localhost:3306/script_booking?...`（Asia/Shanghai、UTF-8） |
| `DB_USERNAME` | `db.username` | `root` |
| `DB_PASSWORD` | `db.password` | 空 |
| `DB_DRIVER` | `db.driver` | `com.mysql.cj.jdbc.Driver` |

Windows 设置示例（启动 Tomcat 前执行）：

```
$env:DB_URL = "jdbc:mysql://localhost:3306/script_booking?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的真实密码"
```

**方式二：外部配置文件**：通过 JVM 系统属性指定完整路径，适合不想改环境变量的场景：

```
-Dscriptbooking.config=D:/config/scriptbooking.properties
```

外部文件格式与 `db.properties` 相同（`db.url` / `db.username` / `db.password` / `db.driver`）。

### 4.3 其他运行时参数

| 配置项 | 位置 | 当前值 | 说明 |
| --- | --- | --- | --- |
| `struts.devMode` | `struts.xml` | `false` | 生产必须保持关闭 |
| `struts.fileUpload.maximumSize` | `struts.xml` | `2097152` | 封面上传上限 2MB，支持 jpg/jpeg/png/webp/gif/svg |
| 会话超时 | `web.xml` | 30 分钟 | Cookie 为 HttpOnly，tracking-mode 为 COOKIE |
| 时区 | JDBC URL | `Asia/Shanghai` | 生产环境应统一 JVM、Tomcat、MySQL 三者时区 |

## 5. 构建打包

在项目根目录执行：

```
mvn clean package
```

- 产物：`target/script-booking-system.war`（由 `pom.xml` 的 `finalName` 决定）。
- 建议打包前先跑测试：

```
mvn clean test
```

- 内存不足时（构建机内存较小），先设置：

```
$env:MAVEN_OPTS = "-Xmx512m"
```

> 
> 本项目 JDK 8 适配说明：编译使用独立属性 `java.version=1.8`，不受本机 Maven `settings.xml` 中全局 `maven.compiler.source/target` 影响。

## 6. 部署到 Tomcat 9

### 6.1 部署 WAR

```
# 1. 停止 Tomcat
# 2. 清理旧部署（假设 Tomcat 位于 C:\apache-tomcat-9.0.x）
Remove-Item -Recurse -Force C:\apache-tomcat-9.0.x\webapps\script-booking-system -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force C:\apache-tomcat-9.0.x\work\Catalina\localhost\script-booking-system -ErrorAction SilentlyContinue

# 3. 拷贝 WAR
Copy-Item target\script-booking-system.war C:\apache-tomcat-9.0.x\webapps\

# 4. 启动 Tomcat
C:\apache-tomcat-9.0.x\bin\startup.bat
```

启动后 Tomcat 会将 WAR 解压到 `webapps/script-booking-system/`，访问上下文为 `/script-booking-system`。

> 
> **重要**：第 2 步的清理不可省略。`work` 下的 JSP 编译缓存不清理时，热更新页面可能出现旧内容；`uploads/covers/` 目录位于旧部署目录内，删除该目录会同时清掉后台上传的封面（见第 8 节）。如需保留上传封面，请先备份 `webapps/script-booking-system/uploads/` 再删除，部署后还原。

### 6.2 IDE 部署（IntelliJ IDEA）

使用 IDEA 的 Tomcat 运行配置（exploded 部署）时，注意：

- Application context 建议设置为 `/script-booking-system`；
- IDEA 托管的 Tomcat 实例位于独立目录（如 `D:\IDEA_Data\system\tomcat\<id>`），上传文件保存在该实例的部署目录下，不在 Tomcat 安装目录。

## 7. 部署后验证

按以下清单逐项确认：

1. **首页跳转**：浏览器访问 `[http://localhost:8080/script-booking-system/](http://localhost:8080/script-booking-system/)`，应自动重定向到 `/scripts` 剧本列表。
2. **静态资源**：检查封面与轮播图可匿名访问，返回 200：

```
(Invoke-WebRequest "[http://localhost:8080/script-booking-system/assets/img/cover-1.jpg](http://localhost:8080/script-booking-system/assets/img/cover-1.jpg)").StatusCode   # 期望 200
(Invoke-WebRequest "[http://localhost:8080/script-booking-system/assets/img/banner-1.jpg](http://localhost:8080/script-booking-system/assets/img/banner-1.jpg)").StatusCode  # 期望 200
```

3. **登录**：使用 `admin / 123456` 登录，进入后台首页；ECharts 统计图表应正常渲染（图表库为本地的 `assets/vendor/echarts.min.js`，无需外网）。
4. **角色验证**：分别用 `manager`、`player` 登录，确认菜单与权限符合角色；`disabled` 账号应无法登录。
5. **数据库连通**：前台剧本列表能显示 `seed.sql` 中的 11 个剧本，封面图片正常显示。
6. **错误页**：访问不存在的路径（如 `/script-booking-system/xxx`）应显示自定义 404 页，而非堆栈信息。
7. **容量对账（可选）**：执行 `sql/check_booking_capacity.sql`，确认 `booked_count` 位于 0 与 `capacity` 之间。

## 8. 图片资源与持久化说明

| 路径 | 存放位置 | 是否随 WAR 打包 | 重新部署后 | 访问控制 |
| --- | --- | --- | --- | --- |
| `/assets/img/...` | `src/main/webapp/assets/img/` | **是** | **永久保留** | 匿名可访问 |
| `/uploads/covers/...` | Tomcat 部署目录 | 否（运行时上传生成） | **会丢失** | 仅登录用户可访问 |

- 后台「剧本管理 → 编辑剧本」上传的封面写入部署目录的 `uploads/covers/`（文件名 `script-` + 随机 12 字符 + 扩展名），数据库 `cover_path` 记录为 `/uploads/covers/xxx.jpg`。
- 因此**重新部署前必须备份 `uploads/` 目录**（见 6.1）；或如本项目做法，将正式封面放入 `webapp/assets/img/` 并通过 `sql/upgrade-script-covers.sql` 把数据库指向永久路径。

## 9. 安全检查清单

- 修改或删除全部演示账号（`admin`、`manager`、`staff`、`player*`、`viewer`、`disabled`）。
- 替换 `db.properties` 中的演示密码，或通过环境变量注入真实凭据；不要将真实密码提交到代码仓库。
- 数据库账号遵循最小权限：应用账号仅授予 `script_booking` 库的 SELECT/INSERT/UPDATE/DELETE，不使用 root。
- 确认 `struts.devMode=false`（默认已关闭）。
- 统一 JVM、Tomcat、MySQL 时区为 `Asia/Shanghai`。
- HTTPS：生产环境建议在 Tomcat 配置 TLS 或置于反向代理之后。
- MySQL 8 使用 `caching_sha2_password` 时，JDBC URL 需保留 `allowPublicKeyRetrieval=true`（默认配置已包含）。

## 10. 常见问题排查

**Q1：启动后报数据库连接失败**

- 确认 MySQL 已启动、库名为 `script_booking`、账号密码正确；
- 查看异常提示中的 `DbErrors.describe` 输出，区分「未知库」「拒绝访问」「连接被拒」等场景；
- 若用了环境变量/外部配置，确认优先级与取值（环境变量 > 外部文件 > db.properties）。

**Q2：Tomcat 10 部署后大量 `ClassNotFoundException: javax.servlet.*`**

- 本项目仅兼容 Tomcat 9.x（`javax.servlet`），请换用 Tomcat 9。

**Q3：页面中文乱码**

- 确认导入 SQL 时使用了 `--default-character-set=utf8mb4`；
- 确认数据库与表为 utf8mb4；JDBC URL 保留 `useUnicode=true&characterEncoding=UTF-8`；
- `web.xml` 的 `jsp-config` 已统一 JSP 编码为 UTF-8，勿改动。

**Q4：JSP 改动后页面仍是旧内容**

- 删除 `work/Catalina/localhost/<context>/` 缓存目录后重启 Tomcat（见 6.1）。

**Q5：静态资源 / 封面 404**

- `assets/` 与 `uploads/` 路径由 `web.xml` 中 Struts 过滤器的 `excludePattern` 放行，勿改；
- `/uploads/` 需登录才能访问（`SecurityFilter` 控制），匿名访问返回 403 属预期行为；
- 检查 `cover_path` 数据库值与实际文件是否一致（可执行 `sql/upgrade-script-covers.sql` 修复指向）。

**Q6：上传封面失败**

- 单文件上限 2MB（`struts.fileUpload.maximumSize`），仅支持 jpg/jpeg/png/webp/gif/svg。

**Q7：构建时 `illegal character: '\ufeff'`**

- 源文件带 UTF-8 BOM 所致，去除文件头 BOM 即可（历史问题出现在 `RegisterAction.java`，已修复；新增文件注意保存为无 BOM 的 UTF-8）。

## 11. 相关文档

- [README.md](../README.md)：项目简介与功能说明
- [development-spec.md](development-spec.md)：架构、状态机、并发容量设计
- [database-design.md](database-design.md)：数据库表结构
- [routes.md](routes.md)：路由清单
- [test-report.md](test-report.md)：测试报告
- [known-limitations.md](known-limitations.md)：已知限制

```

```
