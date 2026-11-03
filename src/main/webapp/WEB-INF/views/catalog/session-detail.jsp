<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/scripts/detail?id=${playSession.scriptId}"><c:out value="${playSession.scriptTitle}"/></a> / 场次详情</div>
<div class="page-header">
    <div><h1><c:out value="${playSession.scriptTitle}"/></h1><p>场次编号：<c:out value="${playSession.sessionCode}"/></p></div>
    <span class="badge badge-${playSession.status}"><c:out value="${playSession.status}"/></span>
</div>
<%@ include file="../common/messages.jspf" %>
<section class="panel">
    <dl class="details-grid">
        <div class="detail-item"><dt>开始时间</dt><dd><c:out value="${fn:replace(playSession.startTime, 'T', ' ')}"/></dd></div>
        <div class="detail-item"><dt>结束时间</dt><dd><c:out value="${fn:replace(playSession.endTime, 'T', ' ')}"/></dd></div>
        <div class="detail-item"><dt>房间</dt><dd><c:out value="${empty playSession.roomName ? '待安排' : playSession.roomName}"/></dd></div>
        <div class="detail-item"><dt>DM</dt><dd><c:out value="${empty playSession.dmName ? '待安排' : playSession.dmName}"/></dd></div>
        <div class="detail-item"><dt>容量</dt><dd>剩余 ${playSession.remainingCapacity} / ${playSession.capacity} 位</dd></div>
        <div class="detail-item"><dt>单价</dt><dd class="price">¥${playSession.unitPrice} / 人</dd></div>
    </dl>
    <c:if test="${not empty playSession.remark}"><p class="muted"><c:out value="${playSession.remark}"/></p></c:if>
    <div class="actions">
        <c:choose>
            <c:when test="${playSession.bookable && sessionScope.currentUser.hasRole('PLAYER')}">
                <a class="button button-primary" href="${pageContext.request.contextPath}/bookings/create?sessionId=${playSession.id}">立即预约</a>
            </c:when>
            <c:when test="${playSession.bookable && empty sessionScope.currentUser}">
                <a class="button button-primary" href="${pageContext.request.contextPath}/login">登录后预约</a>
            </c:when>
            <c:otherwise><button class="button button-secondary" type="button" disabled>当前不可预约</button></c:otherwise>
        </c:choose>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/scripts/detail?id=${playSession.scriptId}">返回剧本详情</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
