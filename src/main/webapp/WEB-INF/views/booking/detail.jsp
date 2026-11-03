<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/my/bookings">我的预约</a> / <c:out value="${booking.bookingCode}"/></div>
<div class="page-header"><div><h1>预约详情</h1><p><c:out value="${booking.scriptTitle}"/></p></div><span class="badge badge-${booking.status}"><c:out value="${booking.status}"/></span></div>
<%@ include file="../common/messages.jspf" %>
<section class="panel">
    <dl class="details-grid">
        <div class="detail-item"><dt>预约编号</dt><dd><c:out value="${booking.bookingCode}"/></dd></div>
        <div class="detail-item"><dt>预约人数</dt><dd>${booking.partySize} 人</dd></div>
        <div class="detail-item"><dt>开始时间</dt><dd><c:out value="${fn:replace(booking.startTime,'T',' ')}"/></dd></div>
        <div class="detail-item"><dt>房间 / DM</dt><dd><c:out value="${booking.roomName}"/> / <c:out value="${booking.dmName}"/></dd></div>
        <div class="detail-item"><dt>联系人</dt><dd><c:out value="${booking.contactName}"/> · <c:out value="${booking.contactPhone}"/></dd></div>
        <div class="detail-item"><dt>预计金额</dt><dd>¥${booking.totalPrice}</dd></div>
    </dl>
    <c:if test="${not empty booking.playerNote}"><p class="muted">备注：<c:out value="${booking.playerNote}"/></p></c:if>
    <div class="actions">
        <c:if test="${booking.status == 'CONFIRMED'}">
            <form method="post" action="${pageContext.request.contextPath}/bookings/cancel" data-confirm="确认取消该预约吗？">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${booking.id}">
                <button class="button button-danger" type="submit">取消预约</button>
            </form>
        </c:if>
        <c:if test="${booking.status == 'COMPLETED' && empty booking.reviewId}"><a class="button button-primary" href="${pageContext.request.contextPath}/reviews/create?bookingId=${booking.id}">提交评价</a></c:if>
        <c:if test="${not empty booking.reviewId}"><a class="button button-secondary" href="${pageContext.request.contextPath}/my/reviews">查看评价</a></c:if>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/my/bookings">返回列表</a>
    </div>
</section>
<%@ include file="../common/footer.jspf" %>
