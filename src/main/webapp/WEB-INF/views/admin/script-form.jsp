<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/admin/scripts">剧本管理</a> / ${book.id == 0 ? '新增' : '编辑'}</div>
<div class="page-header"><div><h1>${book.id == 0 ? '新增剧本' : '编辑剧本'}</h1><p>人数范围、时长与编码均由服务端再次校验。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="panel form-grid" method="post" action="${pageContext.request.contextPath}/admin/scripts/save" enctype="multipart/form-data">
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${book.id == 0 ? '' : book.id}">
    <div class="field"><label class="required" for="scriptCode">剧本编码</label><input class="input" id="scriptCode" name="scriptCode" maxlength="30" required value="<c:out value='${book.scriptCode}'/>"></div>
    <div class="field"><label class="required" for="title">剧本名称</label><input class="input" id="title" name="title" maxlength="120" required value="<c:out value='${book.title}'/>"></div>
    <div class="field"><label class="required" for="genre">题材</label><input class="input" id="genre" name="genre" maxlength="50" required value="<c:out value='${book.genre}'/>"></div>
    <div class="field"><label for="tags">标签</label><input class="input" id="tags" name="tags" maxlength="255" placeholder="使用英文逗号分隔" value="<c:out value='${book.tags}'/>"></div>
    <div class="field"><label class="required" for="minPlayers">最少人数</label><input class="input" id="minPlayers" name="minPlayers" type="number" min="1" required value="${book.minPlayers == 0 ? '' : book.minPlayers}"></div>
    <div class="field"><label class="required" for="maxPlayers">最多人数</label><input class="input" id="maxPlayers" name="maxPlayers" type="number" min="1" required value="${book.maxPlayers == 0 ? '' : book.maxPlayers}"></div>
    <div class="field"><label class="required" for="durationMinutes">预计时长（分钟）</label><input class="input" id="durationMinutes" name="durationMinutes" type="number" min="1" required value="${book.durationMinutes == 0 ? '' : book.durationMinutes}"></div>
    <div class="field"><label class="required" for="difficulty">难度</label><select class="select" id="difficulty" name="difficulty" required><option value="EASY" ${book.difficulty == 'EASY' ? 'selected' : ''}>简单</option><option value="MEDIUM" ${book.difficulty == 'MEDIUM' ? 'selected' : ''}>中等</option><option value="HARD" ${book.difficulty == 'HARD' ? 'selected' : ''}>困难</option></select></div>
    <c:choose>
        <c:when test="${sessionScope.currentUser.hasRole('ADMIN')}">
            <div class="field"><label class="required" for="status">状态</label><select class="select" id="status" name="status" required><option value="PENDING" ${book.status == 'PENDING' ? 'selected' : ''}>待审核</option><option value="ACTIVE" ${book.status == 'ACTIVE' ? 'selected' : ''}>已上架</option><option value="INACTIVE" ${book.status == 'INACTIVE' ? 'selected' : ''}>已下架</option></select></div>
        </c:when>
        <c:otherwise>
            <div class="field"><label>状态</label><span class="muted">${book.id == 0 ? '提交后待管理员审核，通过后前台可见' : (book.status == 'PENDING' ? '待管理员审核' : (book.status == 'ACTIVE' ? '已上架（状态由管理员管理）' : '已下架（状态由管理员管理）'))}</span></div>
        </c:otherwise>
    </c:choose>
    <div class="field"><label for="coverFile">上传封面图片</label><input class="input" id="coverFile" name="coverFile" type="file" accept=".jpg,.jpeg,.png,.webp,.gif,.svg"></div>
    <c:if test="${not empty book.coverPath}">
        <div class="field"><label>当前封面</label><img class="cover-preview" src="${pageContext.request.contextPath}<c:out value='${book.coverPath}'/>" alt="当前封面"></div>
    </c:if>
    <div class="field"><label for="coverPath">或填写封面路径</label><input class="input" id="coverPath" name="coverPath" maxlength="255" value="<c:out value='${book.coverPath}'/>" placeholder="可选，如 /assets/img/cover-1.jpg；上传后将优先使用上传文件"></div>
    <div class="field full"><label for="description">简介</label><textarea class="textarea" id="description" name="description" maxlength="5000"><c:out value="${book.description}"/></textarea></div>
    <div class="form-actions"><button class="button button-primary" type="submit">保存剧本</button><a class="button button-secondary" href="${pageContext.request.contextPath}/admin/scripts">返回列表</a></div>
</form>
<%@ include file="../common/footer.jspf" %>
