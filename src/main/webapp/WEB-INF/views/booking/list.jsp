<%@ include file="../common/header.jspf" %>
<div class="page-header"><div><h1>我的预约</h1><p>查看场次、跟踪状态或取消尚未开始的预约。</p></div><a class="button button-secondary" href="${pageContext.request.contextPath}/scripts">继续选剧本</a></div>
<%@ include file="../common/messages.jspf" %>
<c:choose>
    <c:when test="${empty bookings}"><div class="empty-state"><strong>还没有预约记录</strong>从剧本列表选择一个开放场次开始体验。</div></c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="data-table">
                <thead><tr><th>预约 / 剧本</th><th>场次时间</th><th>人数</th><th>状态</th><th>操作</th></tr></thead>
                <tbody>
                <c:forEach items="${bookings}" var="item">
                    <tr>
                        <td><a class="cell-title" href="${pageContext.request.contextPath}/bookings/detail?id=${item.id}"><c:out value="${item.scriptTitle}"/></a><span class="cell-subtitle"><c:out value="${item.bookingCode}"/></span></td>
                        <td><c:out value="${fn:replace(item.startTime,'T',' ')}"/><span class="cell-subtitle"><c:out value="${item.roomName}"/></span></td>
                        <td>${item.partySize} 人</td>
                        <td><span class="badge badge-${item.status}"><c:out value="${item.status}"/></span></td>
                        <td><div class="actions"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/bookings/detail?id=${item.id}">详情</a>
                            <c:if test="${item.status == 'CONFIRMED'}"><form method="post" action="${pageContext.request.contextPath}/bookings/cancel" data-confirm="确认取消该预约吗？名额将立即释放。"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${item.id}"><button class="button button-small button-danger" type="submit">取消</button></form></c:if>
                        </div></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
<%@ include file="../common/footer.jspf" %>
