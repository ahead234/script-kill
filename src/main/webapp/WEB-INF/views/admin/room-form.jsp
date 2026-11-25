<%@ include file="../common/header.jspf" %>
<div class="breadcrumb"><a href="${pageContext.request.contextPath}/admin/rooms">房间管理</a> / ${room.id == 0 ? '新增' : '编辑'}</div>
<div class="page-header"><div><h1>${room.id == 0 ? '新增房间' : '编辑房间'}</h1><p>房间编号与名称创建后不可修改，场次排期依赖这两个字段。</p></div></div>
<%@ include file="../common/messages.jspf" %>
<form class="panel form-grid" method="post" action="${pageContext.request.contextPath}/admin/rooms/save">
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
    <input type="hidden" name="id" value="${room.id == 0 ? '' : room.id}">
    <div class="field"><label class="required" for="roomCode">房间编号</label>
        <c:choose>
            <c:when test="${room.id == 0}"><input class="input" id="roomCode" name="roomCode" maxlength="20" required placeholder="如 R06" value="<c:out value='${room.roomCode}'/>"></c:when>
            <c:otherwise><input class="input" id="roomCode" value="<c:out value='${room.roomCode}'/>" readonly></c:otherwise>
        </c:choose>
    </div>
    <div class="field"><label class="required" for="roomName">房间名称</label>
        <c:choose>
            <c:when test="${room.id == 0}"><input class="input" id="roomName" name="roomName" maxlength="60" required placeholder="如 迷雾六号房" value="<c:out value='${room.roomName}'/>"></c:when>
            <c:otherwise><input class="input" id="roomName" value="<c:out value='${room.roomName}'/>" readonly></c:otherwise>
        </c:choose>
    </div>
    <div class="field"><label class="required" for="capacity">容量（2-30 人）</label><input class="input" id="capacity" name="capacity" type="number" min="2" max="30" required value="${room.capacity == 0 ? '' : room.capacity}"></div>
    <div class="field"><label class="required" for="status">状态</label><select class="select" id="status" name="status"><option value="ACTIVE" ${room.status == 'ACTIVE' ? 'selected' : ''}>可用</option><option value="MAINTENANCE" ${room.status == 'MAINTENANCE' ? 'selected' : ''}>检修</option><option value="DISABLED" ${room.status == 'DISABLED' ? 'selected' : ''}>停用</option></select></div>
    <div class="field full"><label for="location">位置</label><input class="input" id="location" name="location" maxlength="80" placeholder="如 二层北侧" value="<c:out value='${room.location}'/>"></div>
    <div class="field full"><label for="note">备注</label><textarea class="textarea" id="note" name="note" maxlength="255" rows="3"><c:out value="${room.note}"/></textarea></div>
    <div class="form-actions"><button class="button button-primary" type="submit">保存房间</button><a class="button button-secondary" href="${pageContext.request.contextPath}/admin/rooms">返回列表</a></div>
</form>
<%@ include file="../common/footer.jspf" %>
