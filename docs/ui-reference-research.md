# UI 参考调研

调研日期：2026-10-29。以下页面均在开发期间通过 HTTPS 实际访问并读取页面内容；未虚构截图。首次访问 Bootstrap 与 Tabler 站点根入口发生超时，随后访问其具体文档页面成功。

## 1. Apache Tomcat 9 Documentation

- URL：https://tomcat.apache.org/tomcat-9.0-doc/
- 访问结果：成功，页面标题为 `Apache Tomcat 9 (9.0.123) - Documentation Index`。
- 观察：左侧/分组式目录清晰划分 User Guide、Reference、Development；主内容以标题、简短说明和深层链接为主；视觉克制，信息层级依赖字体、留白和链接而非装饰。
- 借鉴：后台侧栏按业务域分组；页面标题和说明先于操作区；导航名称保持短且明确。
- 技术核验：页面明确说明 Tomcat 9 实现 Servlet 4.0 与 JSP 2.3，与项目选型一致。

## 2. Bootstrap 5.3 Cards

- URL：https://getbootstrap.com/docs/5.3/components/card/
- 访问结果：成功，页面标题为 `Cards · Bootstrap v5.3`。
- 观察：卡片由 header、body、footer 组成；通过 Grid 与 spacing utilities 保持一致间距；状态不能只依赖颜色，应同时显示文字；移动端使用响应式列数。
- 借鉴：剧本卡片使用固定的内容层级和底部动作区；后台面板统一边框、圆角和间距；状态徽标同时包含明确状态文本；列表在窄屏降为单列。
- 取舍：项目未依赖 CDN 或 Bootstrap 运行时，而是使用本地 CSS，避免网络依赖并配合严格 CSP。

## 3. Tabler UI Layout

- URL：https://docs.tabler.io/ui/layout
- 访问结果：成功，页面标题为 `Layout | Tabler UI Documentation`，读取版本为 1.7.1。
- 观察：文档强调一致的 navigation、content arrangement 和 page headers；组件目录包含 Breadcrumb、Card、Empty state、Pagination、Status、Table、表单 Validation 等后台常用模式；页面头支持标题、副标题、面包屑和动作按钮。
- 借鉴：管理页统一采用顶栏 + 深色侧栏 + 主内容；页面头左侧标题/说明、右侧主操作；筛选区、表格、空状态、分页采用固定顺序；工作台使用少量统计卡片。
- 取舍：没有复制 Tabler 组件或品牌样式，仅借鉴信息架构和一致性原则。

## 落地结果

- 配色使用浅暖灰、白色、深灰正文和低饱和墨绿强调色。
- 阴影轻量，圆角统一为 8–12px，无霓虹、玻璃拟态或大面积紫蓝渐变。
- 玩家端使用封面色块卡片，突出名称、人数、时长、评分与场次入口。
- 管理端使用侧栏、页头、筛选、表格、状态徽标和服务端分页。
- 响应式断点覆盖平板和手机；用户内容由 JSTL 安全输出。

本次调研未生成或保存参考站点截图；最终项目截图必须在本系统实际部署并连接数据库后采集。
