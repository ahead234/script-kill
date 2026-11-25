<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.scriptbooking.dao.SessionDao" %>
<%@ page import="com.example.scriptbooking.dao.SessionQuery" %>
<%@ page import="com.example.scriptbooking.model.PlaySession" %>
<%@ page import="com.example.scriptbooking.util.Page" %>
<%--
    实验三对照页：场次列表的“改造前”写法。
    数据访问、循环、条件判断全部用 Java 脚本片段写在 JSP 里，仅用于与 admin/sessions.jsp（JSTL/EL 版）
    渲染同一份数据做对比，不参与正式导航；访问前需要登录（SecurityFilter 对 /lab3/ 做了登录检查）。
--%>
<%
    Page<PlaySession> pageData = new SessionDao().findPage(SessionQuery.none(), 1, 10);
    List<PlaySession> items = pageData.getItems();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>场次管理（改造前对照页）</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/app.css">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/admin.css">
</head>
<body class="admin-page">
<div class="admin-main" style="margin-left:0">
    <main class="main-content" style="padding-top:24px">
        <div class="page-header">
            <div>
                <h1>场次管理</h1>
                <p>改造前写法：JSP 脚本片段 + &lt;%= %&gt; 直接输出，共 <%= pageData.getTotal() %> 条记录。</p>
            </div>
        </div>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>场次 / 剧本</th>
                    <th>时间</th>
                    <th>房间 / DM</th>
                    <th>容量</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (items.isEmpty()) {
                %>
                <tr>
                    <td colspan="6">没有场次记录</td>
                </tr>
                <%
                    } else {
                        for (PlaySession item : items) {
                %>
                <tr>
                    <td>
                        <span class="cell-title"><%= item.getScriptTitle() %></span>
                        <span class="cell-subtitle"><%= item.getSessionCode() %></span>
                    </td>
                    <td>
                        <%= item.getStartTime().toString().replace('T', ' ') %>
                        <span class="cell-subtitle">至 <%= item.getEndTime().toString().replace('T', ' ') %></span>
                    </td>
                    <td>
                        <%= item.getRoomName() == null ? "待定" : item.getRoomName() %>
                        <span class="cell-subtitle">DM：<%= item.getDmName() == null ? "待定" : item.getDmName() %></span>
                    </td>
                    <td>
                        <%= item.getBookedCount() %> / <%= item.getCapacity() %>
                        <span class="cell-subtitle">剩余 <%= item.getRemainingCapacity() %></span>
                    </td>
                    <td><span class="badge badge-<%= item.getStatus() %>"><%= item.getStatus() %></span></td>
                    <td>
                        <div class="actions">
                            <%
                                if (!"CANCELLED".equals(item.getStatus()) && !"COMPLETED".equals(item.getStatus())) {
                            %>
                            <a class="button button-small button-secondary"
                               href="<%= ctx %>/admin/sessions/form?id=<%= item.getId() %>">编辑</a>
                            <form method="post" action="<%= ctx %>/admin/sessions/cancel">
                                <input type="hidden" name="id" value="<%= item.getId() %>">
                                <button class="button button-small button-danger" type="submit">取消场次</button>
                            </form>
                            <%
                                } else {
                            %>
                            <span class="cell-subtitle">不可编辑</span>
                            <%
                                }
                            %>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
