<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/scripts">剧本列表</a> / <c:out value="${book.title}"/></div>
<section class="panel hero-detail">
    <img class="hero-cover-img" src="${ctx}${empty book.coverPath ? '/assets/img/cover-1.svg' : book.coverPath}" alt="<c:out value='${book.title}'/> 封面">
    <div>
        <span class="badge"><c:out value="${book.genre}"/></span>
        <h1><c:out value="${book.title}"/></h1>
        <div class="muted">剧本编码：<c:out value="${book.scriptCode}"/></div>
        <div class="tag-list">
            <c:forEach items="${fn:split(book.tags, ',')}" var="tag"><span class="badge"><c:out value="${tag}"/></span></c:forEach>
        </div>
        <p class="review-content"><c:out value="${book.description}"/></p>
        <dl class="details-grid">
            <div class="detail-item"><dt>建议人数</dt><dd>${book.minPlayers}–${book.maxPlayers} 人</dd></div>
            <div class="detail-item"><dt>预计时长</dt><dd>${book.durationMinutes} 分钟</dd></div>
            <div class="detail-item"><dt>难度</dt><dd><c:out value="${book.difficulty}"/></dd></div>
            <div class="detail-item"><dt>公开评分</dt><dd>${book.reviewCount > 0 ? book.averageRating : '暂无评分'} <span class="muted small">(${book.reviewCount} 条)</span></dd></div>
        </dl>
    </div>
</section>
<section class="panel">
    <div class="panel-header"><h2>可预约场次</h2><span class="muted small">剩余容量实时更新</span></div>
    <c:choose>
        <c:when test="${empty sessions}"><div class="empty-state"><strong>暂时没有开放场次</strong>请稍后再来查看排期。</div></c:when>
        <c:otherwise>
            <div class="session-list">
                <c:forEach items="${sessions}" var="item">
                    <div class="session-row">
                        <div><div class="session-time"><c:out value="${fn:replace(item.startTime, 'T', ' ')}"/></div><span class="muted small"><c:out value="${item.sessionCode}"/></span></div>
                        <div><strong><c:out value="${item.roomName}"/></strong><br><span class="muted small">DM：<c:out value="${item.dmName}"/></span></div>
                        <div><span class="price">¥${item.unitPrice}</span><br><span class="muted small">剩余 ${item.remainingCapacity} / ${item.capacity} 位</span></div>
                        <a class="button button-primary" href="${pageContext.request.contextPath}/sessions/detail?id=${item.id}">查看并预约</a>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<section class="panel">
    <div class="panel-header"><h2>玩家评价</h2><span class="muted small">仅展示审核发布内容</span></div>
    <c:choose>
        <c:when test="${empty reviews}"><div class="empty-state"><strong>暂无公开评价</strong>完成体验后，欢迎分享感受。</div></c:when>
        <c:otherwise>
            <c:forEach items="${reviews}" var="review">
                <article class="review-card card">
                    <div class="panel-header"><strong><c:out value="${review.userDisplayName}"/></strong><span class="rating">${review.rating} / 5</span></div>
                    <div class="review-content"><c:out value="${review.content}"/></div>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="../common/footer.jspf" %>
