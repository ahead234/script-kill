<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>预约管理</h1><p>查询玩家预约并完成到店、完成或未到店登记。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/bookings">
    <div class="field field-grow"><label for="keyword">关键词</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="预约编号、剧本、玩家"></div>
    <div class="field"><label for="status">状态</label><select class="select" id="status" name="status"><option value="">全部</option><c:forEach items="${['CONFIRMED','CANCELLED_BY_PLAYER','CANCELLED_BY_STORE','CHECKED_IN','COMPLETED','NO_SHOW']}" var="state"><option value="${state}" ${param.status == state ? 'selected' : ''}>${state}</option></c:forEach></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>预约 / 玩家</th><th>剧本 / 场次</th><th>时间</th><th>人数</th><th>状态</th><th>操作</th></tr></thead>
    <tbody><c:forEach items="${result.items}" var="item"><tr>
        <td><span class="cell-title"><c:out value="${item.bookingCode}"/></span><span class="cell-subtitle"><c:out value="${item.userDisplayName}"/>（<c:out value="${item.username}"/>）</span></td>
        <td><c:out value="${item.scriptTitle}"/><span class="cell-subtitle"><c:out value="${item.sessionCode}"/></span></td>
        <td><c:out value="${fn:replace(item.startTime,'T',' ')}"/></td><td>${item.partySize} 人</td>
        <td><span class="badge badge-${item.status}">${item.status}</span></td>
        <td><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/admin/bookings/detail?id=${item.id}">查看处理</a></td>
    </tr></c:forEach><c:if test="${empty result.items}"><tr><td colspan="6"><div class="empty-state"><strong>没有预约记录</strong>调整筛选条件后重试。</div></td></tr></c:if></tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
