<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>剧本管理</h1><p>维护剧本资料、适玩人数与启用状态。</p></div><a class="button button-primary" href="${pageContext.request.contextPath}/admin/scripts/form">新增剧本</a></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/scripts">
    <div class="field field-grow"><label for="keyword">关键词</label><input class="input" id="keyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="名称或编码"></div>
    <div class="field"><label for="genre">题材</label><input class="input" id="genre" name="genre" value="<c:out value='${param.genre}'/>" placeholder="全部"></div>
    <div class="field"><label for="status">状态</label><select class="select" id="status" name="status"><option value="">全部</option><option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>待审核</option><option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>已上架</option><option value="INACTIVE" ${param.status == 'INACTIVE' ? 'selected' : ''}>已下架</option></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<div class="table-wrap"><table class="data-table">
    <thead><tr><th>封面</th><th>编码 / 名称</th><th>题材</th><th>人数</th><th>时长 / 难度</th><th>状态</th><th>操作</th></tr></thead>
    <tbody>
    <c:forEach items="${result.items}" var="item"><tr>
        <td><img class="table-cover" src="${pageContext.request.contextPath}${empty item.coverPath ? '/assets/img/cover-1.svg' : item.coverPath}" alt=""></td>
        <td><span class="cell-title"><c:out value="${item.title}"/></span><span class="cell-subtitle"><c:out value="${item.scriptCode}"/></span></td>
        <td><c:out value="${item.genre}"/></td><td>${item.minPlayers}–${item.maxPlayers} 人</td><td>${item.durationMinutes} 分钟<span class="cell-subtitle"><c:out value="${item.difficulty}"/></span></td>
        <td><span class="badge badge-${item.status}"><c:out value="${item.status == 'PENDING' ? '待审核' : (item.status == 'ACTIVE' ? '已上架' : '已下架')}"/></span></td>
        <td><div class="actions"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/admin/scripts/form?id=${item.id}">编辑</a>
            <c:choose>
                <c:when test="${item.status == 'PENDING'}">
                    <c:if test="${sessionScope.currentUser.hasRole('ADMIN')}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/scripts/status" data-confirm="确认审核通过并上架该剧本吗？"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="ACTIVE"><button class="button button-small button-primary" type="submit">通过</button></form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/scripts/status" data-confirm="确认驳回该剧本吗？驳回后前台不可见。"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="INACTIVE"><button class="button button-small button-danger" type="submit">驳回</button></form>
                    </c:if>
                    <c:if test="${not sessionScope.currentUser.hasRole('ADMIN')}"><span class="muted small">等待管理员审核</span></c:if>
                </c:when>
                <c:otherwise>
                    <c:if test="${sessionScope.currentUser.hasRole('ADMIN')}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/scripts/status" data-confirm="确认变更该剧本状态吗？"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><input type="hidden" name="status" value="${item.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}"><button class="button button-small ${item.status == 'ACTIVE' ? 'button-danger' : 'button-secondary'}" type="submit">${item.status == 'ACTIVE' ? '停用' : '启用'}</button></form>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div></td>
    </tr></c:forEach>
    <c:if test="${empty result.items}"><tr><td colspan="7"><div class="empty-state"><strong>没有剧本记录</strong>调整筛选条件或新增剧本。</div></td></tr></c:if>
    </tbody>
</table></div>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
