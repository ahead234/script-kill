<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/my/bookings">我的预约</a> / <c:out value="${booking.bookingCode}"/> / 申请改期</div>
<div class="page-header">
    <div><h1>申请改期</h1><p>只能换到同一剧本的其他场次；开演前 2 小时截止，每笔预约限改期一次。</p></div>
    <a class="button button-secondary" href="${pageContext.request.contextPath}/bookings/detail?id=${booking.id}">返回详情</a>
</div>
<%@ include file="../common/messages.jspf" %>
<section class="panel">
    <div class="panel-header"><h2><c:out value="${booking.scriptTitle}"/></h2><span class="badge badge-${booking.status}"><c:out value="${booking.status}"/></span></div>
    <dl class="details-grid">
        <div class="detail-item"><dt>当前场次</dt><dd><c:out value="${fn:replace(booking.startTime,'T',' ')}"/></dd></div>
        <div class="detail-item"><dt>房间 / DM</dt><dd><c:out value="${booking.roomName}"/> / <c:out value="${booking.dmName}"/></dd></div>
        <div class="detail-item"><dt>预约人数</dt><dd>${booking.partySize} 人</dd></div>
        <div class="detail-item"><dt>剩余改期次数</dt><dd>${1 - booking.rescheduleCount} 次</dd></div>
    </dl>
</section>
<c:choose>
    <c:when test="${empty candidates}">
        <div class="empty-state"><strong>暂无可改期的同剧本场次</strong>该剧本目前没有其他开放且有余位的场次，可稍后再来或联系门店。</div>
    </c:when>
    <c:otherwise>
        <form method="post" action="${pageContext.request.contextPath}/bookings/reschedule" data-confirm="确认改到所选场次吗？每笔预约只能改期一次。">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="id" value="${booking.id}">
            <div class="table-wrap">
                <table class="data-table">
                    <thead><tr><th>选择</th><th>场次时间</th><th>房间 / DM</th><th>剩余容量</th><th>单价</th></tr></thead>
                    <tbody>
                    <c:forEach items="${candidates}" var="item">
                        <tr>
                            <td>
                                <c:choose>
                                    <c:when test="${item.remainingCapacity < booking.partySize}">
                                        <input type="radio" name="targetSessionId" value="${item.id}" disabled>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="radio" name="targetSessionId" value="${item.id}" required>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${fn:replace(item.startTime,'T',' ')}"/><span class="cell-subtitle"><c:out value="${item.sessionCode}"/></span></td>
                            <td><c:out value="${item.roomName}"/><span class="cell-subtitle">DM <c:out value="${item.dmName}"/></span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.remainingCapacity < booking.partySize}"><span class="badge badge-CANCELLED_BY_STORE">仅剩 ${item.remainingCapacity} 位</span></c:when>
                                    <c:otherwise>${item.remainingCapacity} 位</c:otherwise>
                                </c:choose>
                            </td>
                            <td>¥${item.unitPrice} / 人</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <p class="muted">灰显场次的剩余容量小于本单人数，无法改入。改期成功后，原场次名额立即释放。</p>
            <div class="form-actions">
                <button class="button button-primary" type="submit">确认改期</button>
                <a class="button button-secondary" href="${pageContext.request.contextPath}/bookings/detail?id=${booking.id}">返回详情</a>
            </div>
        </form>
    </c:otherwise>
</c:choose>
<%@ include file="../common/footer.jspf" %>
