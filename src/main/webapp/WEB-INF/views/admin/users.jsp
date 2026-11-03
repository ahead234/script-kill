<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>用户管理</h1><p>管理员可查看角色并启用或禁用账号。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/users">
    <div class="field field-grow"><label for="keyword">用户</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="账号或显示名称"></div>
    <div class="field"><label for="status">状态</label><select class="select" id="status" name="status"><option value="">全部</option><option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option><option value="DISABLED" ${param.status == 'DISABLED' ? 'selected' : ''}>DISABLED</option></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>用户</th><th>联系电话</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
    <tbody><c:forEach items="${result.items}" var="item"><tr>
        <td><span class="cell-title"><c:out value="${item.displayName}"/></span><span class="cell-subtitle"><c:out value="${item.username}"/></span></td>
        <td><c:out value="${empty item.phone ? '-' : item.phone}"/></td><td><c:out value="${item.roleText}"/></td><td><span class="badge badge-${item.status}">${item.status}</span></td>
        <td><c:choose><c:when test="${item.id == sessionScope.currentUser.id}"><span class="muted small">当前账号</span></c:when><c:otherwise><form method="post" action="${pageContext.request.contextPath}/admin/users/status" data-confirm="确认变更该用户状态吗？"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="${item.status == 'ACTIVE' ? 'DISABLED' : 'ACTIVE'}"><button class="button button-small ${item.status == 'ACTIVE' ? 'button-danger' : 'button-secondary'}" type="submit">${item.status == 'ACTIVE' ? '禁用' : '启用'}</button></form></c:otherwise></c:choose></td>
    </tr></c:forEach><c:if test="${empty result.items}"><tr><td colspan="5"><div class="empty-state"><strong>没有用户记录</strong>调整筛选条件后重试。</div></td></tr></c:if></tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
