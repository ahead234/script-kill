# Servlet 路由

所有写操作均为 POST，并要求 Session 中的 CSRF Token。`SecurityFilter` 在服务端检查登录和角色。

## 公共与玩家端

| 方法 | 路由 | 参数 | 角色 | 结果 |
|---|---|---|---|---|
| GET | `/login` | 无 | 公共 | 登录页 |
| POST | `/login` | `username,password,csrfToken` | 公共 | 登录并轮换 Session ID |
| POST | `/logout` | `csrfToken` | 已登录 | 注销并销毁 Session |
| GET | `/scripts` | `keyword,genre,page` | 公共 | 启用剧本分页列表 |
| GET | `/scripts/detail` | `id` | 公共 | 剧本、开放场次、公开评价 |
| GET | `/sessions` | 无 | 公共 | 「下一场故事」近期开放场次（按开始时间升序，最多 24 条） |
| GET | `/sessions/detail` | `id` | 公共 | 场次详情与剩余容量 |
| GET | `/bookings/create` | `sessionId` | PLAYER | 预约表单 |
| POST | `/bookings/create` | `sessionId,partySize,contactName,contactPhone,playerNote,csrfToken` | PLAYER | 事务创建预约 |
| GET | `/my/bookings` | 无 | PLAYER | 本人预约列表 |
| GET | `/bookings/detail` | `id` | PLAYER 且本人 | 预约详情 |
| POST | `/bookings/cancel` | `id,csrfToken` | PLAYER 且本人 | 场次未开始时取消并释放容量 |
| GET | `/my/reviews` | 无 | PLAYER | 本人评价列表 |
| GET | `/reviews/create` | `bookingId` | PLAYER 且本人 | 已完成预约评价表单 |
| POST | `/reviews/create` | `id?,bookingId,rating,storyRating?,dmRating?,atmosphereRating?,content,csrfToken` | PLAYER 且本人 | 新建或修改待审核评价 |

## 管理端

| 方法 | 路由 | 参数 | 角色 | 结果 |
|---|---|---|---|---|
| GET | `/admin/dashboard` | 无 | ADMIN/MANAGER/STAFF/VIEWER | 数据报表页（指标卡 + ECharts 图表） |
| GET | `/admin/api/stats` | 无 | ADMIN/MANAGER/STAFF/VIEWER | 报表数据 JSON（`summary`、`trend`、`bookingStatus`、`scriptDemand`、`reviewStatus`、`sessionStatus`），仅渲染图表用 |
| GET | `/admin/scripts` | `keyword,genre,status,page` | ADMIN/MANAGER | 剧本列表 |
| GET | `/admin/scripts/form` | `id?` | ADMIN/MANAGER | 新增/编辑表单 |
| POST | `/admin/scripts/save` | 剧本字段、`csrfToken` | ADMIN/MANAGER | 新增或修改 |
| POST | `/admin/scripts/status` | `id,status,csrfToken` | ADMIN/MANAGER | 启用/停用 |
| GET | `/admin/sessions` | `keyword,date,status,page` | ADMIN/MANAGER | 场次列表 |
| GET | `/admin/sessions/form` | `id?` | ADMIN/MANAGER | 排期表单 |
| POST | `/admin/sessions/save` | 场次字段、`csrfToken` | ADMIN/MANAGER | 新增或修改 |
| POST | `/admin/sessions/cancel` | `id,csrfToken` | ADMIN/MANAGER | 事务取消场次和有效预约 |
| GET | `/admin/bookings` | `keyword,status,page` | ADMIN/MANAGER/STAFF | 预约列表 |
| GET | `/admin/bookings/detail` | `id` | ADMIN/MANAGER/STAFF | 预约详情 |
| POST | `/admin/bookings/status` | `id,status,csrfToken` | ADMIN/MANAGER/STAFF | 到店/完成/未到店状态机 |
| GET | `/admin/reviews` | `status,page` | ADMIN/MANAGER | 评价审核列表 |
| POST | `/admin/reviews/moderate` | `id,status,reason,csrfToken` | ADMIN/MANAGER | 发布或隐藏 |
| GET | `/admin/users` | `keyword,status,page` | ADMIN | 用户列表 |
| POST | `/admin/users/status` | `id,status,csrfToken` | ADMIN | 启用/禁用，禁止自禁用 |
| GET | `/admin/logs` | `keyword,page` | ADMIN/MANAGER | 操作日志 |

未知路由返回 404，无角色权限返回 403，业务参数错误返回表单提示或 400。所有重定向均使用应用上下文路径。
