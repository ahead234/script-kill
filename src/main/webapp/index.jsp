<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 入口页通过重定向进入剧本列表，保证请求经过 Struts 的 Action 映射。 --%>
<c:redirect url="/scripts"/>
