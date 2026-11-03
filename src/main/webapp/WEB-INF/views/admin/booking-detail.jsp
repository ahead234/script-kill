<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/admin/bookings">预约管理</a> / <c:out value="${booking.bookingCode}"/></div>
<div class="page-header"><div><h1>预约详情</h1><p><c:out value="${booking.scriptTitle}"/></p></div><span class="badge badge-${booking.status}">${booking.status}</span></div>
<%@ include file="../common/messages.jspf" %>
<section class="panel">
    <dl class="details-grid">
        <div class="detail-item"><dt>玩家</dt><dd><c:out value="${booking.userDisplayName}"/>（<c:out value="${booking.username}"/>）</dd></div>
        <div class="detail-item"><dt>联系人</dt><dd><c:out value="${booking.contactName}"/> · <c:out value="${booking.contactPhone}"/></dd></div>
        <div class="detail-item"><dt>预约编号</dt><dd><c:out value="${booking.bookingCode}"/></dd></div>
        <div class="detail-item"><dt>人数 / 金额</dt><dd>${booking.partySize} 人 · ¥${booking.totalPrice}</dd></div>
        <div class="detail-item"><dt>场次时间</dt><dd><c:out value="${fn:replace(booking.startTime,'T',' ')}"/> 至 <c:out value="${fn:replace(booking.endTime,'T',' ')}"/></dd></div>
        <div class="detail-item"><dt>房间 / DM</dt><dd><c:out value="${booking.roomName}"/> / <c:out value="${booking.dmName}"/></dd></div>
    </dl>
    <c:if test="${not empty booking.playerNote}"><p class="muted">玩家备注：<c:out value="${booking.playerNote}"/></p></c:if>
</section>
<section class="panel">
    <div class="panel-header"><h2>状态处理</h2><span class="muted small">所有操作均写入日志</span></div>
    <div class="actions">
        <c:if test="${booking.status == 'CONFIRMED'}">
            <form method="post" action="${pageContext.request.contextPath}/admin/bookings/status"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${booking.id}"><input type="hidden" name="status" value="CHECKED_IN"><button class="button button-primary" type="submit">登记到店</button></form>
            <form method="post" action="${pageContext.request.contextPath}/admin/bookings/status" data-confirm="确认将该预约标记为未到店吗？"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${booking.id}"><input type="hidden" name="status" value="NO_SHOW"><button class="button button-danger" type="submit">标记未到店</button></form>
        </c:if>
        <c:if test="${booking.status == 'CHECKED_IN'}"><form method="post" action="${pageContext.request.contextPath}/admin/bookings/status"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${booking.id}"><input type="hidden" name="status" value="COMPLETED"><button class="button button-primary" type="submit">标记完成</button></form></c:if>
        <c:if test="${booking.status != 'CONFIRMED' && booking.status != 'CHECKED_IN'}"><span class="muted">当前状态无需现场处理。</span></c:if>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/admin/bookings">返回列表</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
