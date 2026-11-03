<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>场次管理</h1><p>安排排期、容量、房间和预约开放状态。</p></div><a class="button button-primary" href="${pageContext.request.contextPath}/admin/sessions/form">新增场次</a></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/sessions">
    <div class="field field-grow"><label for="keyword">关键词</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="剧本或场次编号"></div>
    <div class="field"><label for="date">日期</label><input class="input" id="date" name="date" type="date" value="<c:out value='${param.date}'/>"></div>
    <div class="field"><label for="status">状态</label><select class="select" id="status" name="status"><option value="">全部</option><c:forEach items="${['DRAFT','OPEN','CLOSED','IN_PROGRESS','COMPLETED','CANCELLED']}" var="state"><option value="${state}" ${param.status == state ? 'selected' : ''}>${state}</option></c:forEach></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>场次 / 剧本</th><th>时间</th><th>房间 / DM</th><th>容量</th><th>状态</th><th>操作</th></tr></thead>
    <tbody><c:forEach items="${result.items}" var="item"><tr>
        <td><span class="cell-title"><c:out value="${item.scriptTitle}"/></span><span class="cell-subtitle"><c:out value="${item.sessionCode}"/></span></td>
        <td><c:out value="${fn:replace(item.startTime,'T',' ')}"/><span class="cell-subtitle">至 <c:out value="${fn:replace(item.endTime,'T',' ')}"/></span></td>
        <td><c:out value="${item.roomName}"/><span class="cell-subtitle">DM：<c:out value="${item.dmName}"/></span></td>
        <td>${item.bookedCount} / ${item.capacity}<span class="cell-subtitle">剩余 ${item.remainingCapacity}</span></td>
        <td><span class="badge badge-${item.status}">${item.status}</span></td>
        <td><div class="actions"><c:if test="${item.status != 'CANCELLED' && item.status != 'COMPLETED'}"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/admin/sessions/form?id=${item.id}">编辑</a>
            <form method="post" action="${pageContext.request.contextPath}/admin/sessions/cancel" data-confirm="确认取消场次吗？所有有效预约将同步取消。"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><button class="button button-small button-danger" type="submit">取消场次</button></form></c:if>
        </div></td>
    </tr></c:forEach><c:if test="${empty result.items}"><tr><td colspan="6"><div class="empty-state"><strong>没有场次记录</strong>调整条件或创建新排期。</div></td></tr></c:if></tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
