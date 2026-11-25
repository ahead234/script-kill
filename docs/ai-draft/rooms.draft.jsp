<%-- AI 初稿（未加技术基线约束时的原始输出，仅作审查留档，不参与部署） --%>
<%-- 原始 prompt：「写一个房间列表的 JSP 页面，带分页和状态标记」 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="com.example.scriptbooking.dao.RoomDao" %>
<%
    RoomDao dao = RoomDao.getInstance();
    request.setAttribute("rooms", dao.findPage(request.getParameter("keyword"), 1, 10));
%>
<table>
    <c:forEach items="${rooms}" var="room">
        <tr>
            <td>${room.roomName}</td>
            <td>${room.capacity}</td>
            <td>
                <c:choose>
                    <c:when test="${room.status eq 'ACTIVE'}">可用</c:when>
                    <c:otherwise>停用</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
