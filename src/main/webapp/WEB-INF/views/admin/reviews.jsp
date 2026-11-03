<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>评价审核</h1><p>审核结果会影响剧本公开评分与玩家可见状态。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/reviews">
    <div class="field"><label for="status">审核状态</label><select class="select" id="status" name="status"><option value="">全部</option><c:forEach items="${['PENDING','PUBLISHED','HIDDEN']}" var="state"><option value="${state}" ${param.status == state ? 'selected' : ''}>${state}</option></c:forEach></select></div>
    <button class="button button-primary" type="submit">筛选</button>
</form>
<c:choose><c:when test="${empty result.items}"><div class="empty-state"><strong>没有评价记录</strong>当前筛选条件下暂无内容。</div></c:when><c:otherwise>
<c:forEach items="${result.items}" var="item">
    <article class="card review-card">
        <div class="panel-header"><div><strong><c:out value="${item.scriptTitle}"/></strong><div class="muted small"><c:out value="${item.userDisplayName}"/> · <c:out value="${item.bookingCode}"/></div></div><div class="actions"><span class="rating">${item.rating} / 5</span><span class="badge badge-${item.status}">${item.status}</span></div></div>
        <p class="review-content"><c:out value="${item.content}"/></p>
        <div class="muted small">剧情 ${empty item.storyRating ? '-' : item.storyRating} · DM ${empty item.dmRating ? '-' : item.dmRating} · 氛围 ${empty item.atmosphereRating ? '-' : item.atmosphereRating}</div>
        <c:if test="${not empty item.moderationReason}"><p class="muted small">上次审核：<c:out value="${item.moderationReason}"/> · <c:out value="${item.moderatorName}"/></p></c:if>
        <form class="filter-bar" method="post" action="${pageContext.request.contextPath}/admin/reviews/moderate" data-reason-form>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}">
            <div class="field field-grow"><label for="reason-${item.id}">审核原因</label><input class="input" id="reason-${item.id}" name="reason" maxlength="500" required placeholder="说明发布或隐藏原因"></div>
            <div class="field"><label for="result-${item.id}">审核结果</label><select class="select" id="result-${item.id}" name="status" required><option value="PUBLISHED">发布</option><option value="HIDDEN">隐藏</option></select></div>
            <button class="button button-primary" type="submit">保存审核</button>
        </form>
    </article>
</c:forEach></c:otherwise></c:choose>
<%@ include file="../common/pagination.jspf" %>
<%@ include file="../common/footer.jspf" %>
