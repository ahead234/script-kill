<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/sessions/detail?id=${playSession.id}">场次详情</a> / 提交预约</div>
<div class="page-header"><div><h1>确认预约信息</h1><p>提交前请核对场次与联系人信息。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<c:choose>
    <c:when test="${empty playSession}"><div class="empty-state"><strong>场次参数无效</strong>请返回剧本列表重新选择场次。</div></c:when>
    <c:otherwise>
        <div class="panel">
            <div class="panel-header"><h2><c:out value="${playSession.scriptTitle}"/></h2><span class="badge badge-${playSession.status}"><c:out value="${playSession.status}"/></span></div>
            <dl class="details-grid">
                <div class="detail-item"><dt>开始时间</dt><dd><c:out value="${fn:replace(playSession.startTime,'T',' ')}"/></dd></div>
                <div class="detail-item"><dt>场地 / DM</dt><dd><c:out value="${playSession.roomName}"/> / <c:out value="${playSession.dmName}"/></dd></div>
                <div class="detail-item"><dt>剩余容量</dt><dd>${playSession.remainingCapacity} 位</dd></div>
                <div class="detail-item"><dt>单价</dt><dd>¥${playSession.unitPrice} / 人</dd></div>
            </dl>
        </div>
        <form class="panel form-grid" method="post" action="${pageContext.request.contextPath}/bookings/create">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="sessionId" value="${playSession.id}">
            <div class="field">
                <label class="required" for="partySize">预约人数</label>
                <input class="input" id="partySize" name="partySize" type="number" min="1" max="${playSession.remainingCapacity}" required value="<c:out value='${empty param.partySize ? 1 : param.partySize}'/>">
                <span class="help">V1.0 支持拼场，可从 1 人起约；最终人数不超过剧本与场次容量。</span>
            </div>
            <div class="field">
                <label class="required" for="contactName">联系人</label>
                <input class="input" id="contactName" name="contactName" maxlength="60" required value="<c:out value='${empty param.contactName ? sessionScope.currentUser.displayName : param.contactName}'/>">
            </div>
            <div class="field">
                <label class="required" for="contactPhone">联系电话</label>
                <input class="input" id="contactPhone" name="contactPhone" maxlength="30" required value="<c:out value='${empty param.contactPhone ? sessionScope.currentUser.phone : param.contactPhone}'/>">
            </div>
            <div class="field full">
                <label for="playerNote">备注</label>
                <textarea class="textarea" id="playerNote" name="playerNote" maxlength="500" placeholder="新手、同行关系或其他需要门店注意的信息"><c:out value="${param.playerNote}"/></textarea>
            </div>
            <div class="form-actions">
                <button class="button button-primary" type="submit">提交并确认预约</button>
                <a class="button button-secondary" href="${pageContext.request.contextPath}/sessions/detail?id=${playSession.id}">返回</a>
            </div>
        </form>
    </c:otherwise>
</c:choose>
<%@ include file="../common/footer.jspf" %>
