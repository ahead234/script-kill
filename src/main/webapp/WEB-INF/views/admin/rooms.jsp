<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>房间管理</h1><p>维护门店房间的容量、位置与可用状态，房间是场次排期的前置条件。</p></div><a class="button button-primary" href="${pageContext.request.contextPath}/admin/rooms/form">新增房间</a></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/rooms">
    <div class="field field-grow"><label for="keyword">关键词</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="房间编号或名称"></div>
    <div class="field"><label for="status">状态</label><select class="select" id="status" name="status"><option value="">全部</option><option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>可用</option><option value="MAINTENANCE" ${param.status == 'MAINTENANCE' ? 'selected' : ''}>检修</option><option value="DISABLED" ${param.status == 'DISABLED' ? 'selected' : ''}>停用</option></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>编号 / 房间</th><th>位置</th><th>容量</th><th>状态</th><th>备注</th><th>操作</th></tr></thead>
    <tbody><c:forEach items="${result.items}" var="item"><tr>
        <td><span class="cell-title"><c:out value="${item.roomName}"/></span><span class="cell-subtitle"><c:out value="${item.roomCode}"/></span></td>
        <td><c:out value="${empty item.location ? '—' : item.location}"/></td>
        <td>${item.capacity} 人</td>
        <td><span class="badge badge-${item.status}"><c:out value="${item.statusLabel}"/></span></td>
        <td><c:out value="${empty item.note ? '—' : item.note}"/></td>
        <td><div class="actions"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/admin/rooms/form?id=${item.id}">编辑</a>
            <c:if test="${item.status == 'ACTIVE'}">
                <form method="post" action="${pageContext.request.contextPath}/admin/rooms/status" data-confirm="转为检修后，该房间不能再安排新场次，确认继续？">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="MAINTENANCE">
                    <button class="button button-small button-secondary" type="submit">转检修</button>
                </form>
            </c:if>
            <c:if test="${item.status != 'ACTIVE'}">
                <form method="post" action="${pageContext.request.contextPath}/admin/rooms/status">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="ACTIVE">
                    <button class="button button-small button-secondary" type="submit">恢复可用</button>
                </form>
            </c:if>
        </div></td>
    </tr></c:forEach><c:if test="${empty result.items}"><tr><td colspan="6"><div class="empty-state"><strong>没有房间记录</strong>先新增房间，再安排场次。</div></td></tr></c:if></tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
