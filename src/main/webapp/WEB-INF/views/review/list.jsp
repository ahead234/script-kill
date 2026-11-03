<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>我的评价</h1><p>评价提交后由门店审核，只有已发布内容参与公开评分。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<c:choose>
    <c:when test="${empty reviews}"><div class="empty-state"><strong>还没有评价</strong>已完成的预约可以提交一次评价。</div></c:when>
    <c:otherwise>
        <c:forEach items="${reviews}" var="item">
            <article class="card review-card">
                <div class="panel-header">
                    <div><strong><c:out value="${item.scriptTitle}"/></strong><div class="muted small"><c:out value="${item.bookingCode}"/></div></div>
                    <div class="actions"><span class="rating">${item.rating} / 5</span><span class="badge badge-${item.status}"><c:out value="${item.status}"/></span></div>
                </div>
                <p class="review-content"><c:out value="${item.content}"/></p>
                <c:if test="${not empty item.moderationReason}"><p class="muted small">审核说明：<c:out value="${item.moderationReason}"/></p></c:if>
                <c:if test="${item.status == 'PENDING'}"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/reviews/create?bookingId=${item.bookingId}">修改待审核评价</a></c:if>
            </article>
        </c:forEach>
    </c:otherwise>
</c:choose>
<%@ include file="../common/footer.jspf" %>
