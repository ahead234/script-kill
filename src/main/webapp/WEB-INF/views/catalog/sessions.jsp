<%@ include file="../common/header.jspf" %>
<section class="section-block">
    <div class="page-header">
        <div>
            <h1>下一场故事</h1>
            <p>近期开放的场次，按开始时间排序。</p>
        </div>
    </div>
    <%@ include file="../common/messages.jspf" %>
    <c:choose>
        <c:when test="${empty sessions}">
            <div class="empty-state"><strong>近期还没有开放场次</strong>可以前往「发现剧本」查看全部剧本。</div>
        </c:when>
        <c:otherwise>
            <div class="session-list">
                <c:forEach items="${sessions}" var="item">
                    <div class="session-row">
                        <div>
                            <div class="session-time"><c:out value="${fn:replace(item.startTime, 'T', ' ')}"/></div>
                            <span class="muted small"><c:out value="${item.scriptTitle}"/> · <c:out value="${item.sessionCode}"/></span>
                        </div>
                        <div>
                            <strong><c:out value="${item.roomName}"/></strong><br>
                            <span class="muted small">DM：<c:out value="${item.dmName}"/></span>
                        </div>
                        <div>
                            <span class="price">¥${item.unitPrice}</span><br>
                            <span class="muted small">剩余 ${item.remainingCapacity} / ${item.capacity} 位</span>
                        </div>
                        <a class="button button-primary" href="${ctx}/sessions/detail?id=${item.id}">查看并预约</a>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="../common/footer.jspf" %>
