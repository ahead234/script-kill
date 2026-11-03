<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/admin/sessions">场次管理</a> / ${playSession.id == 0 ? '新增' : '编辑'}</div>
<div class="page-header"><div><h1>${playSession.id == 0 ? '新增场次' : '编辑场次'}</h1><p>房间冲突、时间关系和容量下限由服务端校验。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="panel form-grid" method="post" action="${pageContext.request.contextPath}/admin/sessions/save">
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${playSession.id == 0 ? '' : playSession.id}">
    <div class="field"><label for="sessionCode">场次编号</label><input class="input" id="sessionCode" name="sessionCode" maxlength="30" value="<c:out value='${playSession.sessionCode}'/>" placeholder="留空自动生成"></div>
    <div class="field"><label class="required" for="scriptId">剧本</label><select class="select" id="scriptId" name="scriptId" required><option value="">请选择</option><c:forEach items="${scripts}" var="script"><option value="${script.id}" ${playSession.scriptId == script.id ? 'selected' : ''}><c:out value="${script.title}"/>（${script.minPlayers}–${script.maxPlayers} 人）</option></c:forEach></select></div>
    <div class="field"><label class="required" for="startTime">开始时间</label><input class="input" id="startTime" name="startTime" type="datetime-local" required value="<c:out value='${playSession.startTime}'/>"></div>
    <div class="field"><label class="required" for="endTime">结束时间</label><input class="input" id="endTime" name="endTime" type="datetime-local" required value="<c:out value='${playSession.endTime}'/>"></div>
    <div class="field"><label class="required" for="capacity">容量</label><input class="input" id="capacity" name="capacity" type="number" min="1" required value="${playSession.capacity}"><span class="help">不得低于当前有效预约人数 ${playSession.bookedCount}。</span></div>
    <div class="field"><label class="required" for="unitPrice">单价</label><input class="input" id="unitPrice" name="unitPrice" type="number" min="0" step="0.01" required value="${playSession.unitPrice}"></div>
    <div class="field"><label for="roomName">房间</label><input class="input" id="roomName" name="roomName" maxlength="60" value="<c:out value='${playSession.roomName}'/>"></div>
    <div class="field"><label for="dmName">DM</label><input class="input" id="dmName" name="dmName" maxlength="60" value="<c:out value='${playSession.dmName}'/>"></div>
    <div class="field"><label class="required" for="status">状态</label><select class="select" id="status" name="status" required><c:forEach items="${['DRAFT','OPEN','CLOSED','IN_PROGRESS','COMPLETED']}" var="state"><option value="${state}" ${playSession.status == state ? 'selected' : ''}>${state}</option></c:forEach></select><span class="help">取消场次请使用列表中的“取消场次”操作。</span></div>
    <div class="field full"><label for="remark">备注</label><textarea class="textarea" id="remark" name="remark" maxlength="500"><c:out value="${playSession.remark}"/></textarea></div>
    <div class="form-actions"><button class="button button-primary" type="submit">保存场次</button><a class="button button-secondary" href="${pageContext.request.contextPath}/admin/sessions">返回列表</a></div>
</form>
<%@ include file="../common/footer.jspf" %>
