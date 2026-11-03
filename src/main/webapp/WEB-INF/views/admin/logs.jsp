<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>操作日志</h1><p>追踪关键状态变更；日志内容不记录密码和会话令牌。</p></div></div>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/logs">
    <div class="field field-grow"><label for="keyword">关键词</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="操作、对象类型或操作人"></div>
    <button class="button button-primary" type="submit">查询</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>时间</th><th>操作人</th><th>动作</th><th>对象</th><th>详情</th><th>IP</th></tr></thead>
    <tbody><c:forEach items="${result.items}" var="item"><tr>
        <td><c:out value="${fn:replace(item.createdAt,'T',' ')}"/></td><td><c:out value="${empty item.operatorName ? '系统' : item.operatorName}"/></td><td><span class="cell-title"><c:out value="${item.action}"/></span></td>
        <td><c:out value="${item.entityType}"/> #<c:out value="${item.entityId}"/></td><td><c:out value="${item.detail}"/></td><td><c:out value="${item.ipAddress}"/></td>
    </tr></c:forEach><c:if test="${empty result.items}"><tr><td colspan="6"><div class="empty-state"><strong>没有日志记录</strong>当前条件下暂无操作日志。</div></td></tr></c:if></tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
