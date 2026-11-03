<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/my/reviews">我的评价</a> / ${review.id == 0 ? '提交' : '修改'}</div>
<div class="page-header"><div><h1>${review.id == 0 ? '分享本次体验' : '修改待审核评价'}</h1><p><c:out value="${booking.scriptTitle}"/> · <c:out value="${booking.bookingCode}"/></p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="panel form-grid" method="post" action="${pageContext.request.contextPath}/reviews/create">
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
    <input type="hidden" name="id" value="${review.id == 0 ? '' : review.id}">
    <input type="hidden" name="bookingId" value="${booking.id}">
    <div class="field">
        <label class="required" for="rating">总评分</label>
        <select class="select" id="rating" name="rating" required>
            <option value="">请选择</option>
            <c:forEach begin="1" end="5" var="score"><option value="${score}" ${review.rating == score ? 'selected' : ''}>${score} 分</option></c:forEach>
        </select>
    </div>
    <div class="field">
        <label for="storyRating">剧情评分</label>
        <select class="select" id="storyRating" name="storyRating"><option value="">不评分</option><c:forEach begin="1" end="5" var="score"><option value="${score}" ${review.storyRating == score ? 'selected' : ''}>${score} 分</option></c:forEach></select>
    </div>
    <div class="field">
        <label for="dmRating">DM 评分</label>
        <select class="select" id="dmRating" name="dmRating"><option value="">不评分</option><c:forEach begin="1" end="5" var="score"><option value="${score}" ${review.dmRating == score ? 'selected' : ''}>${score} 分</option></c:forEach></select>
    </div>
    <div class="field">
        <label for="atmosphereRating">氛围评分</label>
        <select class="select" id="atmosphereRating" name="atmosphereRating"><option value="">不评分</option><c:forEach begin="1" end="5" var="score"><option value="${score}" ${review.atmosphereRating == score ? 'selected' : ''}>${score} 分</option></c:forEach></select>
    </div>
    <div class="field full">
        <label class="required" for="content">评价内容</label>
        <textarea class="textarea" id="content" name="content" maxlength="2000" required placeholder="请真实描述剧情、DM 引导和门店体验"><c:out value="${review.content}"/></textarea>
        <span class="help">评价内容会经过审核并安全转义后公开展示。</span>
    </div>
    <div class="form-actions"><button class="button button-primary" type="submit">提交审核</button><a class="button button-secondary" href="${pageContext.request.contextPath}/my/reviews">取消</a></div>
</form>
<%@ include file="../common/footer.jspf" %>
